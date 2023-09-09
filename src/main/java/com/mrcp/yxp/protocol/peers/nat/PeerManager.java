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

package com.mrcp.yxp.protocol.peers.nat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.apache.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PeerManager extends Thread {
    private static org.apache.log4j.Logger log = LogManager.getLogger(PeerManager.class);
    private InetAddress localAddress;
    private int localPort;
    private Document document;

    public PeerManager(InetAddress localAddress, int localPort) {
        this.localAddress = localAddress;
        this.localPort = localPort;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public void run() {
        DatagramSocket datagramSocket;
        try {
            datagramSocket = new DatagramSocket(localPort, localAddress);
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }
//        UDPReceiver udpReceiver = new UDPReceiver(datagramSocket);
//        udpReceiver.start();
        while (true) {
            Element root = document.getDocumentElement();
            NodeList peers = root.getChildNodes();
            for (int i = 0; i < peers.getLength(); ++i) {
                Node node = peers.item(i);
                if (node.getNodeName().equals("peer")) {
                    createConnection(node, datagramSocket);
                }
            }
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private void createConnection(Node peer, DatagramSocket datagramSocket) {
        NodeList childNodes = peer.getChildNodes();
        String ipAddress = null;
        String port = null;
        for (int i = 0; i < childNodes.getLength(); ++i) {
            Node node = childNodes.item(i);
            String nodeName = node.getNodeName();
            if (nodeName.equals("ipaddress")) {
                ipAddress = node.getTextContent();
            } else if (nodeName.equals("port")) {
                port = node.getTextContent();
            }
        }
        if (ipAddress == null || port == null) {
            return;
        }
        int remotePort = Integer.parseInt(port);
        try {
            InetAddress remoteAddress = InetAddress.getByName(ipAddress);
            // DatagramSocket datagramSocket = new DatagramSocket(localPort, localAddress);
            for (int i = 0; i < 5; ++i) {
                String message = "hello world " + System.currentTimeMillis();
                byte[] buf = message.getBytes();
                DatagramPacket datagramPacket =
                    new DatagramPacket(buf, buf.length, remoteAddress, remotePort);
                datagramSocket.send(datagramPacket);
                log.debug("> sent:\n" + message);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }

            //datagramSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
}
