package com.mrcp.yxp.protocol.peers.sdp;

import java.util.Hashtable;

public class MrcpClientDestination {
    private String destination;
    private int port;
    private Codec codec;

    public Hashtable getAttributes() {
        return attributes;
    }

    public void setAttributes(Hashtable attributes) {
        this.attributes = attributes;
    }

    private Hashtable attributes;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Codec getCodec() {
        return codec;
    }

    public void setCodec(Codec codec) {
        this.codec = codec;
    }


}
