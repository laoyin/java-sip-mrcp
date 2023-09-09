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

package com.mrcp.yxp.protocol.peers.media;

import com.mrcp.yxp.protocol.peers.rtp.RFC4733;
import com.mrcp.yxp.protocol.peers.rtp.RtpPacket;

import java.util.ArrayList;
import java.util.List;

public class DtmfFactory {

    public List<RtpPacket> createDtmfPackets(char digit) {
        List<RtpPacket> packets = new ArrayList<RtpPacket>();
        byte[] data = new byte[4];
        // RFC4733
        if (digit == '*') {
            data[0] = 10;
        } else if (digit == '#') {
            data[0] = 11;
        } else if (digit >= 'A' && digit <= 'D') {
            data[0] = (byte) (digit - 53);
        } else {
            data[0] = (byte) (digit - 48);
        }
        data[1] = 10; // volume 10
        // Set Duration to 160
        // duration 8 bits
        data[2] = 0;
        // duration 8 bits
        data[3] = -96;

        RtpPacket rtpPacket = new RtpPacket();
        rtpPacket.setData(data);
        rtpPacket.setPayloadType(RFC4733.PAYLOAD_TYPE_TELEPHONE_EVENT);
        rtpPacket.setMarker(true);
        packets.add(rtpPacket);

        // two classical packets

        rtpPacket = new RtpPacket();
        // set duration to 320
        data = data.clone();
        data[2] = 1;
        data[3] = 64;
        rtpPacket.setData(data);
        rtpPacket.setIncrementTimeStamp(false);
        rtpPacket.setMarker(false);
        rtpPacket.setPayloadType(RFC4733.PAYLOAD_TYPE_TELEPHONE_EVENT);
        packets.add(rtpPacket);

        rtpPacket = new RtpPacket();
        // set duration to 320
        data = data.clone();
        data[2] = 1;
        data[3] = -32;
        rtpPacket.setData(data);
        rtpPacket.setIncrementTimeStamp(false);
        rtpPacket.setMarker(false);
        rtpPacket.setPayloadType(RFC4733.PAYLOAD_TYPE_TELEPHONE_EVENT);
        packets.add(rtpPacket);

        data = data.clone();
        // create three end event packets
        data[1] = -0x76; // end event flag + volume set to 10
        // set Duration to 640
        data[2] = 2; // duration 8 bits
        data[3] = -128; // duration 8 bits
        for (int r = 0; r < 3; r++) {
            rtpPacket = new RtpPacket();
            rtpPacket.setData(data);
            rtpPacket.setIncrementTimeStamp(false);
            rtpPacket.setMarker(false);
            rtpPacket.setPayloadType(RFC4733.PAYLOAD_TYPE_TELEPHONE_EVENT);
            packets.add(rtpPacket);
        }

        return packets;
    }

}
