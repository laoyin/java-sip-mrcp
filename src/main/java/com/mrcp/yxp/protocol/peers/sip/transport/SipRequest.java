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
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipURI;

public class SipRequest extends SipMessage {
    protected String method;
    protected SipURI requestUri;
    //protected String requestUri;

    public SipRequest(String method, SipURI requestUri) {
        super();
        this.method = method;
        this.requestUri = requestUri;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(method).append(' ').append(requestUri).append(
                ' ').append(RFC3261.DEFAULT_SIP_VERSION).append(RFC3261.CRLF);
        buf.append(super.toString());
        return buf.toString();
    }

    public String getMethod() {
        return method;
    }

    public SipURI getRequestUri() {
        return requestUri;
    }

}
