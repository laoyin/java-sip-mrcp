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

package com.mrcp.yxp.protocol.peers.sip.core.useragent.handlers;

import com.mrcp.yxp.protocol.peers.Logger;
import com.mrcp.yxp.protocol.peers.sdp.SDPManager;
import com.mrcp.yxp.protocol.peers.sip.RFC3261;
import com.mrcp.yxp.protocol.peers.sip.Utils;
import com.mrcp.yxp.protocol.peers.sip.core.useragent.ChallengeManager;
import com.mrcp.yxp.protocol.peers.sip.core.useragent.UserAgent;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderFieldName;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderFieldValue;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderParamName;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaders;
import com.mrcp.yxp.protocol.peers.sip.transaction.TransactionManager;
import com.mrcp.yxp.protocol.peers.sip.transport.SipRequest;
import com.mrcp.yxp.protocol.peers.sip.transport.SipResponse;
import com.mrcp.yxp.protocol.peers.sip.transport.TransportManager;

public abstract class MethodHandler {

    protected UserAgent userAgent;
    protected TransactionManager transactionManager;
    protected TransportManager transportManager;
    protected ChallengeManager challengeManager;
    protected SDPManager sdpManager;
    protected boolean challenged;
    protected Logger logger;

    public MethodHandler(UserAgent userAgent,
            TransactionManager transactionManager,
            TransportManager transportManager, Logger logger) {
        this.userAgent = userAgent;
        this.transactionManager = transactionManager;
        this.transportManager = transportManager;
        this.logger = logger;
        challenged = false;
    }

    protected SipResponse buildGenericResponse(SipRequest sipRequest,
            int statusCode, String reasonPhrase) {
        //8.2.6
        SipResponse sipResponse = new SipResponse(statusCode, reasonPhrase);
        SipHeaders respHeaders = sipResponse.getSipHeaders();
        SipHeaders reqHeaders = sipRequest.getSipHeaders();
        SipHeaderFieldName fromName = new SipHeaderFieldName(RFC3261.HDR_FROM);
        respHeaders.add(fromName, reqHeaders.get(fromName));
        SipHeaderFieldName callIdName = new SipHeaderFieldName(RFC3261.HDR_CALLID);
        respHeaders.add(callIdName, reqHeaders.get(callIdName));
        SipHeaderFieldName cseqName = new SipHeaderFieldName(RFC3261.HDR_CSEQ);
        respHeaders.add(cseqName, reqHeaders.get(cseqName));
        SipHeaderFieldName viaName = new SipHeaderFieldName(RFC3261.HDR_VIA);
        respHeaders.add(viaName, reqHeaders.get(viaName));
        SipHeaderFieldName toName = new SipHeaderFieldName(RFC3261.HDR_TO);
        String to = reqHeaders.get(toName).getValue();
        SipHeaderFieldValue toValue = new SipHeaderFieldValue(to);
        toValue.addParam(new SipHeaderParamName(RFC3261.PARAM_TAG),
                Utils.randomString(10));// TODO 19.3
        respHeaders.add(toName, toValue);
        return sipResponse;
    }

    public void setChallengeManager(ChallengeManager challengeManager) {
        this.challengeManager = challengeManager;
    }

    public void setSdpManager(SDPManager sdpManager) {
        this.sdpManager = sdpManager;
    }

}
