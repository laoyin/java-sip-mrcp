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

public class SipHeaderFieldName {

    private final static SipHeadersTable SIP_HEADER_TABLE =
        new SipHeadersTable();

    private String name;

    public SipHeaderFieldName(String name) {
        super();
        if (name.length() == 1) {
            this.name = SIP_HEADER_TABLE.getLongForm(name.charAt(0));
        } else {
            this.name = name;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        String objName = ((SipHeaderFieldName)obj).getName();
        if (name.equalsIgnoreCase(objName)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
