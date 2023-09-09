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

    Copyright 2007, 2008, 2009, 2010 Yohann Martineau
*/

package com.mrcp.yxp.protocol.peers.sip.transport;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.mrcp.yxp.protocol.peers.Config;
import com.mrcp.yxp.protocol.peers.Logger;
import com.mrcp.yxp.protocol.peers.sip.RFC3261;


public class UdpMessageSender extends MessageSender {

    private DatagramSocket datagramSocket;

    public UdpMessageSender(InetAddress inetAddress, int port,
            DatagramSocket datagramSocket, Config config,
            Logger logger) throws SocketException {
        super(datagramSocket.getLocalPort(), inetAddress, port,
                config, RFC3261.TRANSPORT_UDP, logger);
        this.datagramSocket = datagramSocket;
    }

    @Override
    public synchronized void sendMessage(SipMessage sipMessage) throws IOException {
        logger.debug("UdpMessageSender.sendMessage");
        if (sipMessage == null) {
            return;
        }
        byte[] buf = sipMessage.toString().getBytes();
        sendBytes(buf);
        StringBuffer direction = new StringBuffer();
        direction.append("SENT to ").append(inetAddress.getHostAddress());
        direction.append("/").append(port);
        logger.traceNetwork(new String(buf), direction.toString());
    }

    @Override
    public synchronized void sendBytes(byte[] bytes) throws IOException {
        logger.debug("UdpMessageSender.sendBytes");
        final DatagramPacket packet = new DatagramPacket(bytes, bytes.length,
                inetAddress, port);
        logger.debug("UdpMessageSender.sendBytes " + bytes.length
                + " " + inetAddress + ":" + port);
        // AccessController.doPrivileged added for plugin compatibility
        AccessController.doPrivileged(
            new PrivilegedAction<Void>() {

                @Override
                public Void run() {
                    try {
                        logger.debug(datagramSocket.getLocalAddress().toString());
                        datagramSocket.send(packet);
                    } catch (Throwable t) {
                        logger.error("throwable", new Exception(t));
                    }
                    return null;
                }
            }
        );

        logger.debug("UdpMessageSender.sendBytes packet sent");
    }

}
