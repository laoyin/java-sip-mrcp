/*
    This file is part of Peers, a java SIP softphone.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2012 Yohann Martineau
*/

package com.mrcp.yxp.protocol.peers;

import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.util.concurrent.ArrayBlockingQueue;

import com.mrcp.yxp.protocol.peers.media.MediaMode;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipURI;

public class JavaConfig implements Config {

    private InetAddress localInetAddress;
    private InetAddress publicInetAddress;
    private String userPart;
    private String domain;
    private String password;
    private SipURI outboundProxy;
    private int sipPort;
    private MediaMode mediaMode;
    private boolean mediaDebug;
    private String mediaFile;
    private int rtpPort;
    private String authorizationUsername;

    @Override
    public void save() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public InetAddress getLocalInetAddress() {
        return localInetAddress;
    }

    @Override
    public InetAddress getPublicInetAddress() {
        return publicInetAddress;
    }

    @Override
    public String getUserPart() {
        return userPart;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public SipURI getOutboundProxy() {
        return outboundProxy;
    }

    @Override
    public int getSipPort() {
        return sipPort;
    }

    @Override
    public MediaMode getMediaMode() {
        return mediaMode;
    }

    @Override
    public boolean isMediaDebug() {
        return mediaDebug;
    }

    @Override
    public int getRtpPort() {
        return rtpPort;
    }

    public String getAuthorizationUsername() {
        return authorizationUsername;
    }

    @Override
    public void setLocalInetAddress(InetAddress inetAddress) {
        localInetAddress = inetAddress;
    }

    @Override
    public void setPublicInetAddress(InetAddress inetAddress) {
        publicInetAddress = inetAddress;
    }

    @Override
    public void setUserPart(String userPart) {
        this.userPart = userPart;
    }

    @Override
    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void setOutboundProxy(SipURI outboundProxy) {
        this.outboundProxy = outboundProxy;
    }

    @Override
    public void setSipPort(int sipPort) {
        this.sipPort = sipPort;
    }

    @Override
    public void setMediaMode(MediaMode mediaMode) {
        this.mediaMode = mediaMode;
    }

    @Override
    public void setMediaDebug(boolean mediaDebug) {
        this.mediaDebug = mediaDebug;
    }

    @Override
    public void setRtpPort(int rtpPort) {
        this.rtpPort = rtpPort;
    }

    public void setAuthorizationUsername(String authorizationUsername) {
        this.authorizationUsername = authorizationUsername;
    }

    @Override
    public PipedOutputStream getOutstream() {
        return null;
    }

    @Override
    public ArrayBlockingQueue getAsrQueue() {
        return null;
    }

    @Override
    public String getMediaFile() {
        return mediaFile;
    }

    @Override
    public void setMediaFile(String mediaFile) {
        this.mediaFile = mediaFile;
    }

}
