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

import org.apache.log4j.LogManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPReceiver extends Thread {
    private static org.apache.log4j.Logger log = LogManager.getLogger(UDPReceiver.class);

    private DatagramSocket datagramSocket;

    public UDPReceiver(DatagramSocket datagramSocket) {
        super();
        this.datagramSocket = datagramSocket;
    }

    @Override
    public void run() {
        try {
            while (true) {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                datagramSocket.receive(packet);
                log.debug("< received:\n"
                        + new String(packet.getData()));
            }
        } catch (IOException e) {
            log.error("error", e);
        }
    }
}
