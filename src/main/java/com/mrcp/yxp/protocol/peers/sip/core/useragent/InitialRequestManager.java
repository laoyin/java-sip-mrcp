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

    Copyright 2007, 2008, 2009, 2010, 2011 Yohann Martineau
*/

package com.mrcp.yxp.protocol.peers.sip.core.useragent;

import com.mrcp.yxp.protocol.peers.Logger;
import com.mrcp.yxp.protocol.peers.sip.RFC3261;
import com.mrcp.yxp.protocol.peers.sip.Utils;
import com.mrcp.yxp.protocol.peers.sip.core.useragent.handlers.ByeHandler;
import com.mrcp.yxp.protocol.peers.sip.core.useragent.handlers.CancelHandler;
import com.mrcp.yxp.protocol.peers.sip.core.useragent.handlers.InviteHandler;
import com.mrcp.yxp.protocol.peers.sip.core.useragent.handlers.OptionsHandler;
import com.mrcp.yxp.protocol.peers.sip.core.useragent.handlers.RegisterHandler;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.NameAddress;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderFieldName;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderFieldValue;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderParamName;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaders;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipURI;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipUriSyntaxException;
import com.mrcp.yxp.protocol.peers.sip.transaction.ClientTransaction;
import com.mrcp.yxp.protocol.peers.sip.transaction.ServerTransaction;
import com.mrcp.yxp.protocol.peers.sip.transaction.ServerTransactionUser;
import com.mrcp.yxp.protocol.peers.sip.transaction.TransactionManager;
import com.mrcp.yxp.protocol.peers.sip.transactionuser.DialogManager;
import com.mrcp.yxp.protocol.peers.sip.transport.SipRequest;
import com.mrcp.yxp.protocol.peers.sip.transport.SipResponse;
import com.mrcp.yxp.protocol.peers.sip.transport.TransportManager;

public class InitialRequestManager extends RequestManager
        implements ServerTransactionUser {

    public InitialRequestManager(UserAgent userAgent,
            InviteHandler inviteHandler,
            CancelHandler cancelHandler,
            ByeHandler byeHandler,
            OptionsHandler optionsHandler,
            RegisterHandler registerHandler,
            DialogManager dialogManager,
            TransactionManager transactionManager,
            TransportManager transportManager,
            Logger logger) {
        super(userAgent,
                inviteHandler,
                cancelHandler,
                byeHandler,
                optionsHandler,
                registerHandler,
                dialogManager,
                transactionManager,
                transportManager,
                logger);
        registerHandler.setInitialRequestManager(this);
    }

    /**
     * gives a new request outside of a dialog
     *
     * @param requestUri
     * @param method
     * @return
     * @throws SipUriSyntaxException
     */
    public SipRequest getGenericRequest(String requestUri, String method,
            String profileUri, String callId, String fromTag)
            throws SipUriSyntaxException {
        //8.1.1
        SipRequest request = new SipRequest(method, new SipURI(requestUri));
        SipHeaders headers = request.getSipHeaders();
        //String hostAddress = utils.getMyAddress().getHostAddress();

        //Via

        //TODO no Via should be added directly by UAC, Via is normally added by Transaction layer

//        StringBuffer viaBuf = new StringBuffer();
//        viaBuf.append(RFC3261.DEFAULT_SIP_VERSION);
//        // TODO choose real transport
//        viaBuf.append("/UDP ");
//        viaBuf.append(hostAddress);
//        SipHeaderFieldValue via = new SipHeaderFieldValue(viaBuf.toString());
//        via.addParam(new SipHeaderParamName(RFC3261.PARAM_BRANCHID),
//                utils.generateBranchId());
//        headers.add(new SipHeaderFieldName(RFC3261.HDR_VIA), via);

        Utils.addCommonHeaders(headers);

        //To

        NameAddress to = new NameAddress(requestUri);
        headers.add(new SipHeaderFieldName(RFC3261.HDR_TO),
                new SipHeaderFieldValue(to.toString()));

        //From

        NameAddress fromNA = new NameAddress(profileUri);
        SipHeaderFieldValue from = new SipHeaderFieldValue(fromNA.toString());
        String localFromTag;
        if (fromTag != null) {
            localFromTag = fromTag;
        } else {
            localFromTag = Utils.generateTag();
        }
        from.addParam(new SipHeaderParamName(RFC3261.PARAM_TAG), localFromTag);
        headers.add(new SipHeaderFieldName(RFC3261.HDR_FROM), from);

        //Call-ID

        SipHeaderFieldName callIdName =
            new SipHeaderFieldName(RFC3261.HDR_CALLID);
        String localCallId;
        if (callId != null) {
            localCallId = callId;
        } else {
            localCallId = Utils.generateCallID(
                    userAgent.getConfig().getLocalInetAddress());
        }
        headers.add(callIdName, new SipHeaderFieldValue(localCallId));

        //CSeq

        headers.add(new SipHeaderFieldName(RFC3261.HDR_CSEQ),
                new SipHeaderFieldValue(userAgent.generateCSeq(method)));

        return request;
    }

    public SipRequest createInitialRequest(String requestUri, String method,
            String profileUri) throws SipUriSyntaxException {
        return createInitialRequest(requestUri, method, profileUri, null);
    }

    public SipRequest createInitialRequest(String requestUri, String method,
            String profileUri, String callId) throws SipUriSyntaxException {

        return createInitialRequest(requestUri, method, profileUri, callId,
                null, null);
    }

    public SipRequest createInitialRequest(String requestUri, String method,
            String profileUri, String callId, String fromTag,
            MessageInterceptor messageInterceptor)
                throws SipUriSyntaxException {

        SipRequest sipRequest = getGenericRequest(requestUri, method,
                profileUri, callId, fromTag);

        // TODO add route header for outbound proxy give it to xxxHandler to create
        // clientTransaction
        SipURI outboundProxy = userAgent.getOutboundProxy();
        if (outboundProxy != null) {
            NameAddress outboundProxyNameAddress =
                new NameAddress(outboundProxy.toString());
            sipRequest.getSipHeaders().add(new SipHeaderFieldName(RFC3261.HDR_ROUTE),
                    new SipHeaderFieldValue(outboundProxyNameAddress.toString()), 0);
        }
        ClientTransaction clientTransaction = null;
        if (RFC3261.METHOD_INVITE.equals(method)) {
            clientTransaction = inviteHandler.preProcessInvite(sipRequest);
        } else if (RFC3261.METHOD_REGISTER.equals(method)) {
            clientTransaction = registerHandler.preProcessRegister(sipRequest);
        }
        createInitialRequestEnd(sipRequest, clientTransaction, profileUri,
                messageInterceptor, true);
        return sipRequest;
    }

    private void createInitialRequestEnd(SipRequest sipRequest,
            ClientTransaction clientTransaction, String profileUri,
            MessageInterceptor messageInterceptor, boolean addContact) {
    	if (clientTransaction == null) {
    		logger.error("method not supported");
    		return;
    	}
    	if (addContact) {
    	    addContact(sipRequest, clientTransaction.getContact(), profileUri);
    	}
        if (messageInterceptor != null) {
            messageInterceptor.postProcess(sipRequest);
        }
        // TODO create message receiver on client transport port
        clientTransaction.start();
    }

    public void createCancel(SipRequest inviteRequest,
            MidDialogRequestManager midDialogRequestManager, String profileUri) {
        SipHeaders inviteHeaders = inviteRequest.getSipHeaders();
        SipHeaderFieldValue callId = inviteHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_CALLID));
        SipRequest sipRequest;
        try {
            sipRequest = getGenericRequest(
                    inviteRequest.getRequestUri().toString(), RFC3261.METHOD_CANCEL,
                    profileUri, callId.getValue(), null);
        } catch (SipUriSyntaxException e) {
            logger.error("syntax error", e);
            return;
        }

        ClientTransaction clientTransaction = null;
            clientTransaction = cancelHandler.preProcessCancel(sipRequest,
                    inviteRequest, midDialogRequestManager);
        if (clientTransaction != null) {
            createInitialRequestEnd(sipRequest, clientTransaction, profileUri,
                    null, false);
        }


    }

    public void manageInitialRequest(SipRequest sipRequest) {
        SipHeaders headers = sipRequest.getSipHeaders();

        // TODO authentication

        //method inspection
        SipResponse sipResponse = null;
        if (!UAS.SUPPORTED_METHODS.contains(sipRequest.getMethod())) {
            //TODO generate 405 (using 8.2.6 &) with Allow header
            //(20.5) and send it
            sipResponse = generateResponse(sipRequest, null,
                    RFC3261.CODE_405_METHOD_NOT_ALLOWED,
                    RFC3261.REASON_405_METHOD_NOT_ALLOWED);
            SipHeaders sipHeaders = sipResponse.getSipHeaders();
            sipHeaders.add(new SipHeaderFieldName(RFC3261.HDR_ALLOW),
                    new SipHeaderFieldValue(Utils.generateAllowHeader()));
        }


        SipHeaderFieldValue contentType =
            headers.get(new SipHeaderFieldName(RFC3261.HDR_CONTENT_TYPE));
        if (contentType != null) {
            if (!RFC3261.CONTENT_TYPE_SDP.equals(contentType.getValue())) {
                //TODO generate 415 with a Accept header listing supported content types
                //8.2.3
            }
        }


        //etc.

        if (sipResponse != null) {
            ServerTransaction serverTransaction =
                transactionManager.createServerTransaction(
                    sipResponse, userAgent.getSipPort(), RFC3261.TRANSPORT_UDP,
                    this, sipRequest);
            serverTransaction.start();
            serverTransaction.receivedRequest(sipRequest);
            serverTransaction.sendReponse(sipResponse);
        }

        //TODO create server transaction
        String method = sipRequest.getMethod();
        if (RFC3261.METHOD_INVITE.equals(method)) {
            inviteHandler.handleInitialInvite(sipRequest);
        } else if (RFC3261.METHOD_CANCEL.equals(method)) {
            cancelHandler.handleCancel(sipRequest);
        } else if (RFC3261.METHOD_OPTIONS.equals(method)) {
            optionsHandler.handleOptions(sipRequest);
        }
    }

    public void addContact(SipRequest sipRequest, String contactEnd,
            String profileUri) {
        SipHeaders sipHeaders = sipRequest.getSipHeaders();



        //Contact

        StringBuffer contactBuf = new StringBuffer();
        contactBuf.append(RFC3261.SIP_SCHEME);
        contactBuf.append(RFC3261.SCHEME_SEPARATOR);
        String userPart = Utils.getUserPart(profileUri);
        contactBuf.append(userPart);
        contactBuf.append(RFC3261.AT);
        contactBuf.append(contactEnd);

        NameAddress contactNA = new NameAddress(contactBuf.toString());
        SipHeaderFieldValue contact =
            new SipHeaderFieldValue(contactNA.toString());
        sipHeaders.add(new SipHeaderFieldName(RFC3261.HDR_CONTACT),
                new SipHeaderFieldValue(contact.toString()));
    }

    ///////////////////////////////////////////////////////////
    // ServerTransactionUser methods
    ///////////////////////////////////////////////////////////

    @Override
    public void transactionFailure() {
        // TODO Auto-generated method stub

    }

}
