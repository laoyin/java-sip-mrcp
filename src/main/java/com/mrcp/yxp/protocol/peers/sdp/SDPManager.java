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

    Copyright 2007, 2008, 2009, 2010, 2012 Yohann Martineau
*/

package com.mrcp.yxp.protocol.peers.sdp;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import com.mrcp.yxp.protocol.peers.Config;
import com.mrcp.yxp.protocol.peers.Logger;
import com.mrcp.yxp.protocol.peers.rtp.RFC3551;
import com.mrcp.yxp.protocol.peers.rtp.RFC4733;
import com.mrcp.yxp.protocol.peers.sip.core.useragent.UserAgent;

public class SDPManager {

    private SdpParser sdpParser;
    private UserAgent userAgent;
    private List<Codec> supportedCodecs;
    private Random random;

    private Logger logger;

    public SDPManager(UserAgent userAgent, Logger logger) {
        this.userAgent = userAgent;
        this.logger = logger;
        sdpParser = new SdpParser();
        supportedCodecs = new ArrayList<Codec>();
        random = new Random();
        //TODO retrieve codecs from configuration file
        Codec codec = new Codec();
        codec.setPayloadType(RFC3551.PAYLOAD_TYPE_PCMU);
        codec.setName(RFC3551.PCMU);
        supportedCodecs.add(codec);
        codec = new Codec();
        codec.setPayloadType(RFC3551.PAYLOAD_TYPE_PCMA);
        codec.setName(RFC3551.PCMA);
        supportedCodecs.add(codec);
        codec = new Codec();
        codec.setPayloadType(RFC4733.PAYLOAD_TYPE_TELEPHONE_EVENT);
        codec.setName(RFC4733.TELEPHONE_EVENT);
        //TODO add fmtp:101 0-15 attribute
        supportedCodecs.add(codec);
    }

    public SessionDescription parse(byte[] sdp) {
        try {
            return sdpParser.parse(sdp);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public MediaDestination getMediaDestination(
            SessionDescription sessionDescription) throws NoCodecException {
        InetAddress destAddress = sessionDescription.getIpAddress();
        List<MediaDescription> mediaDescriptions = sessionDescription.getMediaDescriptions();
        for (MediaDescription mediaDescription: mediaDescriptions) {
            if (RFC4566.MEDIA_AUDIO.equals(mediaDescription.getType())) {
                for (Codec offerCodec: mediaDescription.getCodecs()) {
                    if (supportedCodecs.contains(offerCodec)) {
                        String offerCodecName = offerCodec.getName();
                        if (offerCodecName.equalsIgnoreCase(RFC3551.PCMU) ||
                                offerCodecName.equalsIgnoreCase(RFC3551.PCMA)) {
                            int destPort = mediaDescription.getPort();
                            if (mediaDescription.getIpAddress() != null) {
                                destAddress = mediaDescription.getIpAddress();
                            }
                            MediaDestination mediaDestination =
                                new MediaDestination();
                            mediaDestination.setDestination(
                                    destAddress.getHostAddress());
                            mediaDestination.setPort(destPort);
                            mediaDestination.setCodec(offerCodec);
                            return mediaDestination;
                        }
                    }
                }
            }
        }
        throw new NoCodecException();
    }

    // TODO 创建sdp方法
    public SessionDescription createSessionDescription(SessionDescription offer,
            int localRtpPort)
            throws IOException {
        SessionDescription sessionDescription = new SessionDescription();
        sessionDescription.setUsername("user1");
        sessionDescription.setId(random.nextInt(Integer.MAX_VALUE));
        sessionDescription.setVersion(random.nextInt(Integer.MAX_VALUE));
        Config config = userAgent.getConfig();
        InetAddress inetAddress = config.getPublicInetAddress();
        if (inetAddress == null) {
            inetAddress = config.getLocalInetAddress();
        }
        sessionDescription.setIpAddress(inetAddress);
        sessionDescription.setName("-");
        sessionDescription.setAttributes(new Hashtable<String, String>());
        List<Codec> codecs;
        if (offer == null) {
            codecs = supportedCodecs;
        } else {
            codecs = new ArrayList<Codec>();
            for (MediaDescription mediaDescription:
                    offer.getMediaDescriptions()) {
                if (RFC4566.MEDIA_AUDIO.equals(mediaDescription.getType())) {
                    for (Codec codec: mediaDescription.getCodecs()) {
                        if (supportedCodecs.contains(codec)) {
                            codecs.add(codec);
                        }
                    }
                }
            }
        }
        MediaDescription mediaDescription = new MediaDescription();
        Hashtable<String, String> attributes = new Hashtable<String, String>();
        attributes.put(RFC4566.ATTR_SENDRECV, "");
        mediaDescription.setAttributes(attributes);
        mediaDescription.setType(RFC4566.MEDIA_AUDIO);
        mediaDescription.setPort(localRtpPort);
        mediaDescription.setCodecs(codecs);
        List<MediaDescription> mediaDescriptions =
            new ArrayList<MediaDescription>();
        mediaDescriptions.add(mediaDescription);
        sessionDescription.setMediaDescriptions(mediaDescriptions);
        return sessionDescription;
    }


    // yxp get MRCPSDP
    public MrcpClientDestination getMrcpClientDestination(
            SessionDescription sessionDescription) throws NoCodecException {
        InetAddress destAddress = sessionDescription.getIpAddress();
        List<MediaDescription> mediaDescriptions = sessionDescription.getMediaDescriptions();
        for (MediaDescription mediaDescription: mediaDescriptions) {
            if (RFC4566.MEDIA_MRCP.equals(mediaDescription.getType())) {

                int destPort = mediaDescription.getPort();
                if (mediaDescription.getIpAddress() != null) {
                    destAddress = mediaDescription.getIpAddress();
                }
                MrcpClientDestination mediaDestination =
                        new MrcpClientDestination();
                mediaDestination.setDestination(
                        destAddress.getHostAddress());
                mediaDestination.setPort(destPort);
                mediaDestination.setAttributes(mediaDescription.getAttributes());
                return mediaDestination;



            }
        }
        throw new NoCodecException();
    }

}
