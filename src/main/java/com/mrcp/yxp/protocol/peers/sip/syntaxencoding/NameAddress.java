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

import com.mrcp.yxp.protocol.peers.sip.RFC3261;

public class NameAddress {

    public static String nameAddressToUri(String nameAddress) {
        int leftPos = nameAddress.indexOf(RFC3261.LEFT_ANGLE_BRACKET);
        int rightPos = nameAddress.indexOf(RFC3261.RIGHT_ANGLE_BRACKET);
        if (leftPos < 0 || rightPos < 0) {
            return nameAddress;
        }
        return nameAddress.substring(leftPos + 1, rightPos);
    }

    protected String addrSpec;
    protected String displayName;

    public NameAddress(String addrSpec) {
        super();
        this.addrSpec = addrSpec;
    }

    public NameAddress(String addrSpec, String displayName) {
        super();
        this.addrSpec = addrSpec;
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (displayName != null) {
            buf.append(displayName);
            buf.append(' ');
        }
        buf.append(RFC3261.LEFT_ANGLE_BRACKET);
        buf.append(addrSpec);
        buf.append(RFC3261.RIGHT_ANGLE_BRACKET);
        return buf.toString();
    }

    public String getAddrSpec() {
        return addrSpec;
    }

}
