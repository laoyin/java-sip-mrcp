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

    Copyright 2007, 2008, 2009, 2010, 2012 Yohann Martineau
*/

package com.mrcp.yxp.protocol.peers.sip.core.useragent.handlers;

import java.io.IOException;
import java.util.Random;

import com.mrcp.yxp.protocol.peers.Logger;
import com.mrcp.yxp.protocol.peers.sdp.SessionDescription;
import com.mrcp.yxp.protocol.peers.sip.RFC3261;
import com.mrcp.yxp.protocol.peers.sip.Utils;
import com.mrcp.yxp.protocol.peers.sip.core.useragent.UserAgent;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderFieldName;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderFieldValue;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaders;
import com.mrcp.yxp.protocol.peers.sip.transaction.ServerTransaction;
import com.mrcp.yxp.protocol.peers.sip.transaction.ServerTransactionUser;
import com.mrcp.yxp.protocol.peers.sip.transaction.TransactionManager;
import com.mrcp.yxp.protocol.peers.sip.transport.SipRequest;
import com.mrcp.yxp.protocol.peers.sip.transport.SipResponse;
import com.mrcp.yxp.protocol.peers.sip.transport.TransportManager;

public class OptionsHandler extends MethodHandler
        implements ServerTransactionUser {

    public static final int MAX_PORTS = 65536;

    public OptionsHandler(UserAgent userAgent,
            TransactionManager transactionManager,
            TransportManager transportManager, Logger logger) {
        super(userAgent, transactionManager, transportManager, logger);
    }

    public void handleOptions(SipRequest sipRequest) {
        SipResponse sipResponse = buildGenericResponse(sipRequest,
                RFC3261.CODE_200_OK, RFC3261.REASON_200_OK);
        int localPort = new Random().nextInt(MAX_PORTS);
        try {
            SessionDescription sessionDescription =
                sdpManager.createSessionDescription(null, localPort);
            sipResponse.setBody(sessionDescription.toString().getBytes());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        SipHeaders sipHeaders = sipResponse.getSipHeaders();
        sipHeaders.add(new SipHeaderFieldName(RFC3261.HDR_CONTENT_TYPE),
                new SipHeaderFieldValue(RFC3261.CONTENT_TYPE_SDP));
        sipHeaders.add(new SipHeaderFieldName(RFC3261.HDR_ALLOW),
                new SipHeaderFieldValue(Utils.generateAllowHeader()));
        ServerTransaction serverTransaction =
            transactionManager.createServerTransaction(
                sipResponse, userAgent.getSipPort(), RFC3261.TRANSPORT_UDP,
                this, sipRequest);
        serverTransaction.start();
        serverTransaction.receivedRequest(sipRequest);
        serverTransaction.sendReponse(sipResponse);
    }

    @Override
    public void transactionFailure() {
        // TODO Auto-generated method stub

    }

}
