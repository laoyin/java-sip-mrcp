package com.mrcp.yxp.protocol.socketclient;

import com.mrcp.yxp.protocol.mrcp.mrcp4j.MrcpMethodName;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.client.MrcpChannel;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.client.MrcpEventListener;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.client.MrcpInvocationException;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.message.MrcpEvent;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.message.MrcpResponse;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.message.header.IllegalValueException;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.message.request.MrcpRequest;
import com.mrcp.yxp.protocol.peers.sdp.NoCodecException;
import com.mrcp.yxp.protocol.peers.sip.core.useragent.UserAgent;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;

public class CustomMrcpMessageHandler implements MrcpEventListener {
    private static Logger log = LogManager.getLogger(CustomMrcpMessageHandler.class);

    private MrcpChannel recogChannel;
    private int remotePort;
    private ArrayBlockingQueue asrQueue;
    private UserAgent userAgent;

    public CustomMrcpMessageHandler(MrcpChannel recogChannel, int remotePort, ArrayBlockingQueue asrQueue){
        this.recogChannel = recogChannel;
        this.remotePort = remotePort;
        this.asrQueue = asrQueue;
    }


    private boolean sendMrcpMessage(MrcpChannel _recogChannel, MrcpMethodName command, String content){
        MrcpRequest request4 = _recogChannel.createRequest(command);
        if (MrcpMethodName.DEFINE_GRAMMAR.toString().equals(command.toString())){
            request4.setContent("application/srgs+xml", null, content);
        }else if(MrcpMethodName.RECOGNIZE.toString().equals(command.toString())){
            request4.setContent("text/uri-list", null, content);
        }else{
            log.error("no such MrcpMethodName!!!  "+ command.toString());
        }

        try {
            MrcpResponse response2 = _recogChannel.sendRequest(request4);
            if (response2.getStatusCode() == MrcpResponse.STATUS_SUCCESS) {
                log.debug("mrcp status is ok ");
                return true;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MrcpInvocationException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public void initChannelEventStart(MrcpChannel _recogChannel,  UserAgent userAgent) {

        UmsCollection.pubUmsDict(String.valueOf(remotePort), 0);
        this.userAgent = userAgent;

        new Thread() {

            public void run() {
                String cg = userAgent.getGarmmar();
                if (cg != null) {
                    if(sendMrcpMessage(_recogChannel, MrcpMethodName.DEFINE_GRAMMAR, cg)){
                        log.debug("first garmmar is ok, garmmar is ok, garmmar is ok, ");
                    }
                }
                if(sendMrcpMessage(_recogChannel, MrcpMethodName.RECOGNIZE, "builtin:speech/transcribe")){
                    userAgent.setMrcpStatus(1);
                    userAgent.setMrcpChannel(_recogChannel);
                    UmsCollection.pubUmsDict(String.valueOf(remotePort), 1);
                    log.debug("RECOGNIZE is ok, RECOGNIZE is ok, RECOGNIZE is ok, ");
                }

            }
        }.start();

    }


    @Override
    public void eventReceived(MrcpEvent event) {

        if("RECOGNITION-COMPLETE".equals(event.getEventName().toString())) {
            try {
                this.asrQueue.put(event);
            } catch (Exception e) {
                log.error("error", e);
            }

            //  发送garmaer信息
            //log.debug("yxp 再次发起 mrcp request");
            UmsCollection.pubUmsDict(String.valueOf(remotePort), 0);

            new Thread() {
                public void run(){
                    String cg =  userAgent.getGarmmar();
                    if (cg != null) {
                        if(sendMrcpMessage(recogChannel, MrcpMethodName.DEFINE_GRAMMAR, cg)){
                            log.debug("again garmmar is ok, garmmar is ok, garmmar is ok, ");
                        }
                    }
                    log.debug("yxp 再次发起 mrcp request  RECOGNIZE");
                    if(sendMrcpMessage(recogChannel, MrcpMethodName.RECOGNIZE, "builtin:speech/transcribe")){
                        UmsCollection.pubUmsDict(String.valueOf(remotePort), 1);
                        log.debug("RECOGNIZE is ok, RECOGNIZE is ok, RECOGNIZE is ok, ");
                    }
                }

            }.start();


        }else{
            log.debug("orther event");
            log.debug(event.getEventName());
            log.debug(event.getContent());
        }
    }
}
