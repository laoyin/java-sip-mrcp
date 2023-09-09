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

    Copyright 2010 Yohann Martineau
*/

package com.mrcp.yxp.protocol.peers;

import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.util.concurrent.ArrayBlockingQueue;

import com.mrcp.yxp.protocol.peers.media.MediaMode;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipURI;

public interface Config {

    public void save();
    public InetAddress getLocalInetAddress();
    public InetAddress getPublicInetAddress();
    public String getUserPart();
    public String getDomain();
    public String getPassword();
    public SipURI getOutboundProxy();
    public int getSipPort();
    public MediaMode getMediaMode();
    public boolean isMediaDebug();
    public String getMediaFile();
    public int getRtpPort();
    public String getAuthorizationUsername();
    public void setLocalInetAddress(InetAddress inetAddress);
    public void setPublicInetAddress(InetAddress inetAddress);
    public void setUserPart(String userPart);
    public void setDomain(String domain);
    public void setPassword(String password);
    public void setOutboundProxy(SipURI outboundProxy);
    public void setSipPort(int sipPort);
    public void setMediaMode(MediaMode mediaMode);
    public void setMediaDebug(boolean mediaDebug);
    public void setMediaFile(String mediaFile);
    public void setRtpPort(int rtpPort);
    public void setAuthorizationUsername(String authorizationUsername);
    public PipedOutputStream getOutstream();
    public ArrayBlockingQueue getAsrQueue();

}
