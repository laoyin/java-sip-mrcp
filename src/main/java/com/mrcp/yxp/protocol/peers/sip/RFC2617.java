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

    Copyright 2008, 2009, 2010, 2011 Yohann Martineau
*/

package com.mrcp.yxp.protocol.peers.sip;

public class RFC2617 {

    // SCHEMES

    public static final String SCHEME_DIGEST = "Digest";

    // PARAMETERS

    public static final String PARAM_NONCE    = "nonce";
    public static final String PARAM_OPAQUE   = "opaque";
    public static final String PARAM_REALM    = "realm";
    public static final String PARAM_RESPONSE = "response";
    public static final String PARAM_URI      = "uri";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_QOP      = "qop";
    public static final String PARAM_CNONCE   = "cnonce";
    public static final String PARAM_NC       = "nc";
    public static final String PARAM_ALGORITHM= "algorithm";

    // MISCELLANEOUS

    public static final char PARAM_SEPARATOR       = ',';
    public static final char PARAM_VALUE_SEPARATOR = '=';
    public static final char PARAM_VALUE_DELIMITER = '"';
    public static final char DIGEST_SEPARATOR      = ':';
}
