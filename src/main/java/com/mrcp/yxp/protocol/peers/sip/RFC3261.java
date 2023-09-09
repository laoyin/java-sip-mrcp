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

package com.mrcp.yxp.protocol.peers.sip;

public final class RFC3261 {

    //SYNTAX ENCODING

      //HEADERS

        //Methods

    public static final String METHOD_INVITE   = "INVITE";
    public static final String METHOD_ACK      = "ACK";
    public static final String METHOD_REGISTER = "REGISTER";
    public static final String METHOD_BYE      = "BYE";
    public static final String METHOD_OPTIONS  = "OPTIONS";
    public static final String METHOD_CANCEL   = "CANCEL";

        //Classical form

    public static final String HDR_ALLOW               = "Allow";
    public static final String HDR_AUTHORIZATION       = "Authorization";
    public static final String HDR_CALLID              = "Call-ID";
    public static final String HDR_CONTACT             = "Contact";
    public static final String HDR_CONTENT_ENCODING    = "Content-Encoding";
    public static final String HDR_CONTENT_LENGTH      = "Content-Length";
    public static final String HDR_CONTENT_TYPE        = "Content-Type";
    public static final String HDR_CSEQ                = "CSeq";
    public static final String HDR_EXPIRES             = "Expires";
    public static final String HDR_FROM                = "From";
    public static final String HDR_MAX_FORWARDS        = "Max-Forwards";
    public static final String HDR_RECORD_ROUTE        = "Record-Route";
    public static final String HDR_PROXY_AUTHENTICATE  = "Proxy-Authenticate";
    public static final String HDR_PROXY_AUTHORIZATION = "Proxy-Authorization";
    public static final String HDR_ROUTE               = "Route";
    public static final String HDR_SUBJECT             = "Subject";
    public static final String HDR_SUPPORTED           = "Supported";
    public static final String HDR_TO                  = "To";
    public static final String HDR_VIA                 = "Via";
    public static final String HDR_WWW_AUTHENTICATE    = "WWW-Authenticate";

        //Compact form

    public static final char COMPACT_HDR_CALLID           = 'i';
    public static final char COMPACT_HDR_CONTACT          = 'm';
    public static final char COMPACT_HDR_CONTENT_ENCODING = 'e';
    public static final char COMPACT_HDR_CONTENT_LENGTH   = 'l';
    public static final char COMPACT_HDR_CONTENT_TYPE     = 'c';
    public static final char COMPACT_HDR_FROM             = 'f';
    public static final char COMPACT_HDR_SUBJECT          = 's';
    public static final char COMPACT_HDR_SUPPORTED        = 'k';
    public static final char COMPACT_HDR_TO               = 't';
    public static final char COMPACT_HDR_VIA              = 'v';

        //Parameters

    public static final String PARAM_BRANCH     = "branch";
    public static final String PARAM_EXPIRES    = "expires";
    public static final String PARAM_MADDR      = "maddr";
    public static final String PARAM_RECEIVED   = "received";
    public static final String PARAM_RPORT      = "rport";
    public static final String PARAM_SENTBY     = "sent-by";
    public static final String PARAM_TAG        = "tag";
    public static final String PARAM_TRANSPORT  = "transport";
    public static final String PARAM_TTL        = "ttl";

    public static final String PARAM_SEPARATOR  = ";";
    public static final String PARAM_ASSIGNMENT = "=";

        //Miscellaneous

    public static final char   FIELD_NAME_SEPARATOR = ':';
    public static final String DEFAULT_SIP_VERSION  = "SIP/2.0";
    public static final String CRLF                 = "\r\n";
    public static final String IPV4_TTL             = "1";
    public static final char   AT                   = '@';
    public static final String LOOSE_ROUTING        = "lr";
    public static final char   LEFT_ANGLE_BRACKET   = '<';
    public static final char   RIGHT_ANGLE_BRACKET  = '>';
    public static final String HEADER_SEPARATOR     = ",";

      //STATUS CODES
    public static final int CODE_MIN_PROV                            = 100;
    public static final int CODE_MIN_SUCCESS                         = 200;
    public static final int CODE_MIN_REDIR                           = 300;
    public static final int CODE_MAX                                 = 699;

    public static final int CODE_100_TRYING                          = 100;
    public static final int CODE_180_RINGING                         = 180;
    public static final int CODE_200_OK                              = 200;
    public static final int CODE_401_UNAUTHORIZED                    = 401;
    public static final int CODE_405_METHOD_NOT_ALLOWED              = 405;
    public static final int CODE_407_PROXY_AUTHENTICATION_REQUIRED   = 407;
    public static final int CODE_481_CALL_TRANSACTION_DOES_NOT_EXIST = 481;
    public static final int CODE_486_BUSYHERE                        = 486;
    public static final int CODE_487_REQUEST_TERMINATED              = 487;
    public static final int CODE_500_SERVER_INTERNAL_ERROR           = 500;

      //REASON PHRASES
    public static final String REASON_180_RINGING  = "Ringing";
    public static final String REASON_200_OK       = "OK";
    public static final String REASON_405_METHOD_NOT_ALLOWED =
        "Method Not Allowed";
    public static final String REASON_481_CALL_TRANSACTION_DOES_NOT_EXIST =
        "Call/Transaction Does Not Exist";
    public static final String REASON_486_BUSYHERE = "Busy Here";
    public static final String REASON_487_REQUEST_TERMINATED =
        "Request Terminated";
    public static final String REASON_500_SERVER_INTERNAL_ERROR =
        "Server Internal Error";

    //TRANSPORT

    public static final String TRANSPORT_UDP                = "UDP";
    public static final String TRANSPORT_TCP                = "TCP";
    public static final String TRANSPORT_SCTP               = "SCTP";
    public static final String TRANSPORT_TLS                = "TLS";
    public static final int    TRANSPORT_UDP_USUAL_MAX_SIZE = 1300;
    public static final int    TRANSPORT_UDP_MAX_SIZE       = 65535;
    public static final char   TRANSPORT_VIA_SEP            = '/';
    public static final char   TRANSPORT_VIA_SEP2           = ' ';
    public static final int    TRANSPORT_DEFAULT_PORT       = 5060;
    public static final int    TRANSPORT_TLS_PORT           = 5061;
    public static final char   TRANSPORT_PORT_SEP           = ':';


    //TRANSACTION


    //TRANSACTION USER

    public static final int    DEFAULT_MAXFORWARDS   = 70;
    public static final String BRANCHID_MAGIC_COOKIE = "z9hG4bK";
    public static final String SIP_SCHEME            = "sip";
    public static final char   SCHEME_SEPARATOR      = ':';

    //TIMERS (in milliseconds)

    public static final int TIMER_T1 = 500;
    public static final int TIMER_T2 = 4000;
    public static final int TIMER_T4 = 5000;
    public static final int TIMER_INVITE_CLIENT_TRANSACTION = 32000;


    //TRANSACTION USER


    //CORE

    public static final String CONTENT_TYPE_SDP = "application/sdp";
    public static final int DEFAULT_EXPIRES = 3600;

}
