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

package com.mrcp.yxp.protocol.peers.sip.syntaxencoding;

import java.util.HashMap;

import com.mrcp.yxp.protocol.peers.sip.RFC3261;


public class SipHeadersTable {

    private HashMap<Character, String> headers;

    /**
     * should be instanciated only once, it was a singleton.
     */
    public SipHeadersTable() {
        headers = new HashMap<Character, String>();
        //RFC 3261 Section 10
        headers.put(RFC3261.COMPACT_HDR_CALLID,           RFC3261.HDR_CALLID);
        headers.put(RFC3261.COMPACT_HDR_CONTACT,          RFC3261.HDR_CONTACT);
        headers.put(RFC3261.COMPACT_HDR_CONTENT_ENCODING, RFC3261.HDR_CONTENT_ENCODING);
        headers.put(RFC3261.COMPACT_HDR_CONTENT_LENGTH,   RFC3261.HDR_CONTENT_LENGTH);
        headers.put(RFC3261.COMPACT_HDR_CONTENT_TYPE,     RFC3261.HDR_CONTENT_TYPE);
        headers.put(RFC3261.COMPACT_HDR_FROM,             RFC3261.HDR_FROM);
        headers.put(RFC3261.COMPACT_HDR_SUBJECT,          RFC3261.HDR_SUBJECT);
        headers.put(RFC3261.COMPACT_HDR_SUPPORTED,        RFC3261.HDR_SUPPORTED);
        headers.put(RFC3261.COMPACT_HDR_TO,               RFC3261.HDR_TO);
        headers.put(RFC3261.COMPACT_HDR_VIA,              RFC3261.HDR_VIA);
    }

    public String getLongForm(char compactForm) {
        return headers.get(compactForm);
    }

}
