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

import com.mrcp.yxp.protocol.peers.sip.RFC3261;

public class SipResponse extends SipMessage {
    protected int statusCode;
    protected String reasonPhrase;

    public SipResponse(int statusCode, String reasonPhrase) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(RFC3261.DEFAULT_SIP_VERSION).append(' ').append(statusCode
                ).append(' ').append(reasonPhrase).append(RFC3261.CRLF);
        buf.append(super.toString());
        return buf.toString();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

}
