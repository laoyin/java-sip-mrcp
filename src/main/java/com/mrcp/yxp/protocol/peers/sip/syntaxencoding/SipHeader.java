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

public class SipHeader {

    private SipHeaderFieldName name;
    private SipHeaderFieldValue value;

    SipHeader(SipHeaderFieldName name, SipHeaderFieldValue value) {
        super();
        this.name = name;
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SipHeader) {
            SipHeader objHdr = (SipHeader) obj;
            return name.equals(objHdr.name);
        }
        return false;
    }

    public SipHeaderFieldName getName() {
        return name;
    }

    public SipHeaderFieldValue getValue() {
        return value;
    }

    public void setValue(SipHeaderFieldValue value) {
        this.value = value;
    }

}
