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
//
//import com.mrcp.client.javademo.peers.sip.RFC3261;
//import com.mrcp.client.javademo.peers.sip.syntaxencoding.SipHeaderFieldName;
//import com.mrcp.client.javademo.peers.sip.syntaxencoding.SipHeaderFieldValue;
//import com.mrcp.client.javademo.peers.sip.syntaxencoding.SipHeaders;
import com.mrcp.yxp.protocol.peers.sip.RFC3261;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderFieldName;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderFieldValue;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaders;

public abstract class SipMessage {

    protected String sipVersion;
    protected SipHeaders sipHeaders;
    protected byte[] body;

    public SipMessage() {
        sipVersion = RFC3261.DEFAULT_SIP_VERSION;
        sipHeaders = new SipHeaders();
    }

    public String getSipVersion() {
        return sipVersion;
    }

    public void setSipHeaders(SipHeaders sipHeaders) {
        this.sipHeaders = sipHeaders;
    }

    public SipHeaders getSipHeaders() {
        return sipHeaders;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        SipHeaderFieldName contentLengthName =
            new SipHeaderFieldName(RFC3261.HDR_CONTENT_LENGTH);
        SipHeaderFieldValue contentLengthValue =
            sipHeaders.get(contentLengthName);
        if (contentLengthValue == null) {
            contentLengthValue = new SipHeaderFieldValue(
                    String.valueOf(body.length));
            sipHeaders.add(contentLengthName, contentLengthValue);
        } else {
            contentLengthValue.setValue(String.valueOf(body.length));
        }
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(sipHeaders.toString());
        buf.append(RFC3261.CRLF);
        if (body != null) {
            buf.append(new String(body));
        }
        return buf.toString();
    }

}
