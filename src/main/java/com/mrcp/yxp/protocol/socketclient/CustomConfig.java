package com.mrcp.yxp.protocol.socketclient;

import com.mrcp.yxp.protocol.peers.Config;
import com.mrcp.yxp.protocol.peers.media.MediaMode;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipURI;

import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;

public class CustomConfig implements Config {
    private InetAddress publicIpAddress;
    private String mediaFile;
    private String clientIp;
    private PipedOutputStream outStream;
    private ArrayBlockingQueue asrQueue;
    private int sipPort;
    private int rtpPort;

    public CustomConfig(String file, String clientip){
        this.mediaFile = file;
        this.clientIp = clientip;
    }

    public CustomConfig(String clientip, PipedOutputStream outStream, ArrayBlockingQueue asrQueue){
        this.clientIp = clientip;
        this.outStream = outStream;
        this.asrQueue = asrQueue;
    }

    @Override
    public void save() {

    }

    @Override
    public InetAddress getLocalInetAddress() {
        InetAddress inetAddress;
        try {
            // if you have only one active network interface, getLocalHost()
            // should be enough
            //inetAddress = InetAddress.getLocalHost();
            // if you have several network interfaces like I do,
            // select the right one after running ipconfig or ifconfig
            inetAddress = InetAddress.getByName(clientIp);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
        return inetAddress;
    }

    @Override
    public InetAddress getPublicInetAddress() {
        return publicIpAddress;
    }

    @Override
    public String getUserPart() {
        return "alice";
    }

    @Override
    public String getDomain() {
        return this.clientIp;
    }

    @Override
    public String getPassword() {
        return "secret1234";
    }

    @Override
    public SipURI getOutboundProxy() {
        return null;
    }

    @Override
    public int getSipPort() {

        if (sipPort!=0){
            return sipPort;
        }
        return 0;
    }

    @Override // use microphone and speakers to capture and playback sound
    public MediaMode getMediaMode() {
        return MediaMode.pipeline;
    }

    @Override
    public boolean isMediaDebug() {
        return false;
    }


    @Override
    public String getMediaFile() {
        return mediaFile;
    }

    @Override
    public int getRtpPort() {

        if (rtpPort!=0){
            return rtpPort;
        }
        return 0;
    }

    @Override
    public String getAuthorizationUsername() {
        return null;
    }


    @Override
    public void setLocalInetAddress(InetAddress inetAddress) {

    }

    @Override
    public void setPublicInetAddress(InetAddress inetAddress) {
        publicIpAddress = inetAddress;
    }

    @Override
    public void setUserPart(String s) {

    }

    @Override
    public void setDomain(String s) {

    }

    @Override
    public void setPassword(String s) {

    }

    @Override
    public void setOutboundProxy(SipURI sipURI) {

    }

    @Override
    public void setSipPort(int i) {
        sipPort = i;
    }

    @Override
    public void setMediaMode(MediaMode mediaMode) {

    }

    @Override
    public void setMediaDebug(boolean b) {

    }



    @Override
    public void setMediaFile(String s) {

    }

    @Override
    public void setRtpPort(int i) {
        rtpPort = i;
    }

    @Override
    public void setAuthorizationUsername(String s) {

    }


    public PipedOutputStream getOutstream(){
        return outStream;
    }

    public ArrayBlockingQueue getAsrQueue(){
        return asrQueue;
    }

}
