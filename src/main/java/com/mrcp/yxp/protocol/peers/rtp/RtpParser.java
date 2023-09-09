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

package com.mrcp.yxp.protocol.peers.rtp;

import com.mrcp.yxp.protocol.peers.Logger;

// RFC 3550
public class RtpParser {

    private Logger logger;

    public RtpParser(Logger logger) {
        this.logger = logger;
    }

    public RtpPacket decode(byte[] packet) {
        if (packet.length < 12) {
            logger.error("RTP packet too short");
            return null;
        }
        RtpPacket rtpPacket = new RtpPacket();
        int b = (int)(packet[0] & 0xff);
        rtpPacket.setVersion((b & 0xc0) >> 6);
        rtpPacket.setPadding((b & 0x20) != 0);
        rtpPacket.setExtension((b & 0x10) != 0);
        rtpPacket.setCsrcCount(b & 0x0f);
        b = (int)(packet[1] & 0xff);
        rtpPacket.setMarker((b & 0x80) != 0);
        rtpPacket.setPayloadType(b & 0x7f);
        b = (int)(packet[2] & 0xff);
        rtpPacket.setSequenceNumber(b * 256 + (int)(packet[3] & 0xff));
        b = (int)(packet[4] & 0xff);
        rtpPacket.setTimestamp(b * 256 * 256 * 256
                + (int)(packet[5] & 0xff) * 256 * 256
                + (int)(packet[6] & 0xff) * 256
                + (int)(packet[7] & 0xff));
        b = (int)(packet[8] & 0xff);
        rtpPacket.setSsrc(b * 256 * 256 * 256
                + (int)(packet[9] & 0xff) * 256 * 256
                + (int)(packet[10] & 0xff) * 256
                + (int)(packet[11] & 0xff));
        long[] csrcList = new long[rtpPacket.getCsrcCount()];
        for (int i = 0; i < csrcList.length; ++i)
            csrcList[i] = (int)(packet[12 + i] & 0xff) << 24
                + (int)(packet[12 + i + 1] & 0xff) << 16
                + (int)(packet[12 + i + 2] & 0xff) << 8
                + (int)(packet[12 + i + 3] & 0xff);
        rtpPacket.setCsrcList(csrcList);
        int dataOffset = 12 + csrcList.length * 4;
        int dataLength = packet.length - dataOffset;
        byte[] data = new byte[dataLength];
        System.arraycopy(packet, dataOffset, data, 0, dataLength);
        rtpPacket.setData(data);
        return rtpPacket;
    }

    public byte[] encode(RtpPacket rtpPacket) {
        byte[] data = rtpPacket.getData();
        int packetLength = 12 + rtpPacket.getCsrcCount() * 4 + data.length;
        byte[] packet = new byte[packetLength];
        int b = (rtpPacket.getVersion() << 6)
            + ((rtpPacket.isPadding() ? 1 : 0) << 5)
            + ((rtpPacket.isExtension() ? 1 : 0) << 4)
            + (rtpPacket.getCsrcCount());
        packet[0] = new Integer(b).byteValue();
        b = ((rtpPacket.isMarker() ? 1 : 0) << 7)
            + rtpPacket.getPayloadType();
        packet[1] = new Integer(b).byteValue();
        b = rtpPacket.getSequenceNumber() >> 8;
        packet[2] = new Integer(b).byteValue();
        b = rtpPacket.getSequenceNumber() & 0xff;
        packet[3] = new Integer(b).byteValue();
        b = (int)(rtpPacket.getTimestamp() >> 24);
        packet[4] = new Integer(b).byteValue();
        b = (int)(rtpPacket.getTimestamp() >> 16);
        packet[5] = new Integer(b).byteValue();
        b = (int)(rtpPacket.getTimestamp() >> 8);
        packet[6] = new Integer(b).byteValue();
        b = (int)(rtpPacket.getTimestamp() & 0xff);
        packet[7] = new Integer(b).byteValue();
        b = (int)(rtpPacket.getSsrc() >> 24);
        packet[8] = new Integer(b).byteValue();
        b = (int)(rtpPacket.getSsrc() >> 16);
        packet[9] = new Integer(b).byteValue();
        b = (int)(rtpPacket.getSsrc() >> 8);
        packet[10] = new Integer(b).byteValue();
        b = (int)(rtpPacket.getSsrc() & 0xff);
        packet[11] = new Integer(b).byteValue();
        for (int i = 0; i < rtpPacket.getCsrcCount(); ++i) {
            b = (int)(rtpPacket.getCsrcList()[i] >> 24);
            packet[12 + i * 4] = new Integer(b).byteValue();
            b = (int)(rtpPacket.getCsrcList()[i] >> 16);
            packet[12 + i * 4 + 1] = new Integer(b).byteValue();
            b = (int)(rtpPacket.getCsrcList()[i] >> 8);
            packet[12 + i * 4 + 2] = new Integer(b).byteValue();
            b = (int)(rtpPacket.getCsrcList()[i] & 0xff);
            packet[12 + i * 4 + 3] = new Integer(b).byteValue();
        }
        System.arraycopy(data, 0, packet, 12 + rtpPacket.getCsrcCount() * 4,
                data.length);
        return packet;
    }

}
