package com.mrcp.yxp.protocol;

import com.mrcp.yxp.protocol.mrcp.mrcp4j.message.MrcpEvent;
import com.mrcp.yxp.protocol.peers.sip.core.useragent.SipListener;
import com.mrcp.yxp.protocol.peers.sip.core.useragent.UserAgent;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipUriSyntaxException;
import com.mrcp.yxp.protocol.peers.sip.transport.SipRequest;
import com.mrcp.yxp.protocol.peers.sip.transport.SipResponse;
import com.mrcp.yxp.protocol.socketclient.CustomConfig;
import com.mrcp.yxp.protocol.utils.XmlParse;
import com.tencent.core.utils.ByteUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PipedOutputStream;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;


public class MrcpJavaClient implements SipListener {
    private static Logger log = LogManager.getLogger(MrcpJavaClient.class);

    public  static  final String audioPathLeft = "/Users/yinxingpan/freeswitch-data/third_server/mrcp-proxy-server/mrcpclientdemo/audio/test_8k_left.pcm";

//    public static final String audioPathRight  = "/Users/yinxingpan/Downloads/user-b8xSiBGOEE-PCMU.pcm";

    public static final String audioPathRight = "/Users/yinxingpan/freeswitch-data/third_server/mrcp-proxy-server/mrcpclientdemo/audio/test_8k_right.pcm";
    public static final String LocalHostIp ="172.20.10.2";  // 本地局域网ip
    public static final String MrcpServerIp ="43.*.*.132";   // mrcp-server 外网ip
    public static final String MrcpServerPort ="5060";  // mrcp-server port
    public static  boolean testRun = true;

    public static final int MRCP_STATUS_INIT = 0;
    public static final int MRCP_STATUS_RUNING = 1;
    public static final int MRCP_STATUS_STOPPED = 2;
    public static final String MSG_QUIT = "STOP";

    private UserAgent userAgent;
    private SipRequest sipRequest;
    private volatile int mrcp_status = MRCP_STATUS_INIT;

    /*输入的音频流*/
    private ArrayBlockingQueue<byte[]> audioBlockingQueue;

    public MrcpJavaClient(ArrayBlockingQueue audioBlockingQueue) throws SocketException {
        this.audioBlockingQueue = audioBlockingQueue;
        this.userAgent = new UserAgent(this,
                new CustomConfig(LocalHostIp, new PipedOutputStream(), new ArrayBlockingQueue(100)),
                null, null);
        this.userAgent.setGarmmar(
                "<grammar xmlns=\"http://www.w3.org/2001/06/grammar\" xml:lang=\"en-US\" version=\"1.0\" mode=\"voice\" root=\"digit\">\n" +
                "  <rule hotword_id=\"test_hotword\" customization_id=\"test_customization_id\"/>\n" +
                "</grammar>");
        log.debug("JavaMrcpMain 初始化完");
    }

    public UserAgent getUserAgent(){
        return this.userAgent;
    }

    public boolean isRecording(){
        return mrcp_status == MRCP_STATUS_RUNING;
    }
    public void stop(){
        mrcp_status = MRCP_STATUS_STOPPED;
    }
    public void start() throws SocketException {
        mrcp_status = MRCP_STATUS_RUNING;
        log.debug("JavaMrcpMain 开始启动!");
        /* 读取数据流，发送 */
        new Thread(){
            public void run() {
                try {
                    sipRequest = userAgent.invite("sip:mrcp@"+MrcpServerIp+":"+MrcpServerPort, null);
                } catch (SipUriSyntaxException e) {
                    e.printStackTrace();
                }


                //FileInputStream fileInputStream = new FileInputStream(audioPathLeft);
                byte buffer[] = null;
                try {
                    while (userAgent.getMrcpStatus()!=1){
                        Thread.sleep(30);
                    }
                    while (isRecording()) {
                        buffer = audioBlockingQueue.poll();
                        // status 为1 时候 mrcp 建立成功，可以写入数据了
                        if(userAgent.getMrcpStatus()==1 && buffer!=null){
                            userAgent.getConfig().getOutstream().write(buffer);
                        }
                    }
                    // 主动断开 hangup()
                    userAgent.hangup(sipRequest);
                } catch (Exception e) {
                    log.error("error", e);
                }

            }
        }.start();

    }

    public static String printAsrResult(Object obj) {
        String res;
        if(obj instanceof MrcpEvent){
            MrcpEvent event  = (MrcpEvent) obj;
            if(event !=null && event.hasContent()){
                String content = event.getContent().replace("\u0000", "");
                res = XmlParse.readAsrData(content);
            }else{
                res = "null";
            }
        }else if(obj instanceof String){
            res = (String) obj;
        }else{
            res = obj.toString();
        }
        return res;
    }



    @Override
    public void registering(SipRequest sipRequest) { }

    @Override
    public void registerSuccessful(SipResponse sipResponse) {

    }
    @Override
    public void registerFailed(SipResponse sipResponse) { }

    @Override
    public void incomingCall(SipRequest sipRequest, SipResponse provResponse) { }

    @Override
    public void remoteHangup(SipRequest sipRequest) { }

    @Override
    public void ringing(SipResponse sipResponse) { }

    @Override
    public void calleePickup(SipResponse sipResponse) { }

    @Override
    public void error(SipResponse sipResponse) { }


    public static void main(String[] args) {
        ArrayBlockingQueue<byte[]> audioBlockingQueue_Left =new ArrayBlockingQueue<>(1024);
        ArrayBlockingQueue<byte[]> audioBlockingQueue_Right =new ArrayBlockingQueue<>(1024);

        try {
            InputStream fileInputStream_Left = new FileInputStream(audioPathLeft);
            InputStream fileInputStream_Right = new FileInputStream(audioPathRight);
            // 独立线程输入pcm音频数据
            Thread thread_Left = new Thread(new Runnable() {
                @Override
                public void run() {
                    //socket建议每次传输20ms数据
                    List<byte[]> speechData = ByteUtils.subToSmallBytes(fileInputStream_Left, 320);
                    for (int i = 0; i < speechData.size(); i++) {
                        //模拟音频间隔
                        try{
                            if (Thread.currentThread().isInterrupted()){
                                return;
                            }
                            Thread.sleep(1);
                            //发送数据
                            audioBlockingQueue_Left.offer(speechData.get(i));
                        }catch (Exception as){
                            log.error(as.getMessage());
                        }

                    }
                }
            });
            thread_Left.start();
            Thread thread_Right = new Thread(new Runnable() {
                @Override
                public void run() {
                    //socket建议每次传输20ms数据
                    List<byte[]> speechData = ByteUtils.subToSmallBytes(fileInputStream_Right, 320);
                    for (int i = 0; i < speechData.size(); i++) {
                        //模拟音频间隔
                        try{
                            if (Thread.currentThread().isInterrupted()){
                                return;
                            }
                            Thread.sleep(5);
                            //发送数据
                            audioBlockingQueue_Right.offer(speechData.get(i));
                        }catch (Exception as){
                            log.error(as.getMessage());
                        }

                    }
                }
            });
            thread_Right.start();

            MrcpJavaClient javaMrcpUtil_Left = new MrcpJavaClient(audioBlockingQueue_Left);
            MrcpJavaClient javaMrcpUtil_Right = new MrcpJavaClient(audioBlockingQueue_Right);

            javaMrcpUtil_Left.start();
            javaMrcpUtil_Right.start();

            /* 左声道接收文本 */
            new Thread(){
                public void run() {
                    while(javaMrcpUtil_Left.isRecording()){
                        try {
                            Object obj  = javaMrcpUtil_Left.getUserAgent().getConfig().getAsrQueue().take();
                            if(MSG_QUIT.equals(obj)){
                                log.debug("asr_Left:" + printAsrResult(obj));
                                break;
                            }
                            log.debug("asr_Left:" + printAsrResult(obj));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                }
            }.start();

            /* 右声道接收文本 */
            new Thread(){
                public void run() {
                    while(javaMrcpUtil_Right.isRecording()){
                        try {
                            Object obj  = javaMrcpUtil_Right.getUserAgent().getConfig().getAsrQueue().take();
                            if(MSG_QUIT.equals(obj)){
                                log.debug("asr_Right:" + printAsrResult(obj));
                                break;
                            }
                            log.debug("asr_Right:" + printAsrResult(obj));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                }
            }.start();

            Thread.sleep(30000);
            log.debug("时间到了,现在停止录音");

            javaMrcpUtil_Left.stop();
            javaMrcpUtil_Right.stop();
        } catch (SocketException | FileNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
