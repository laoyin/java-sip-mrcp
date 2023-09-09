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

    Copyright 2007-2013 Yohann Martineau
*/

package com.mrcp.yxp.protocol.peers.sip.transport;

import java.net.InetAddress;

import com.mrcp.yxp.protocol.peers.sip.RFC3261;

public class SipTransportConnection {

    public static final int EMPTY_PORT = -1;

    private InetAddress localInetAddress;
    private int localPort = EMPTY_PORT;

    private InetAddress remoteInetAddress;
    private int remotePort = EMPTY_PORT;

    private String transport;// UDP, TCP or SCTP

    public SipTransportConnection(InetAddress localInetAddress,
            int localPort, InetAddress remoteInetAddress, int remotePort,
            String transport) {
        this.localInetAddress = localInetAddress;
        this.localPort = localPort;
        this.remoteInetAddress = remoteInetAddress;
        this.remotePort = remotePort;
        this.transport = transport;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != SipTransportConnection.class) {
            return false;
        }
        SipTransportConnection other = (SipTransportConnection)obj;
        if (!transport.equalsIgnoreCase(other.transport)) {
            return false;
        }
        if (RFC3261.TRANSPORT_UDP.equalsIgnoreCase(transport)) {
            return localInetAddress.equals(other.localInetAddress) &&
                localPort == other.localPort;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        appendInetAddress(buf, localInetAddress);
        buf.append(':');
        appendPort(buf, localPort);
        buf.append('/');
        if (!RFC3261.TRANSPORT_UDP.equalsIgnoreCase(transport)) {
            appendInetAddress(buf, remoteInetAddress);
            buf.append(':');
            appendPort(buf, remotePort);
            buf.append('/');
        }
        buf.append(transport.toUpperCase());
        return buf.toString();
    }

    private void appendInetAddress(StringBuffer buf, InetAddress inetAddress) {
        if (inetAddress != null) {
            buf.append(inetAddress.getHostAddress());
        } else {
            buf.append("-");
        }
    }

    private void appendPort(StringBuffer buf, int port) {
        if (port != EMPTY_PORT) {
            buf.append(port);
        } else {
            buf.append("-");
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public InetAddress getLocalInetAddress() {
        return localInetAddress;
    }

    public int getLocalPort() {
        return localPort;
    }

    public InetAddress getRemoteInetAddress() {
        return remoteInetAddress;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getTransport() {
        return transport;
    }


}
