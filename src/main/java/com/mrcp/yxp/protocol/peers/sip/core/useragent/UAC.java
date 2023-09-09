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

    Copyright 2007-2013 Yohann Martineau
*/

package com.mrcp.yxp.protocol.peers.sip.core.useragent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mrcp.yxp.protocol.peers.Logger;
import com.mrcp.yxp.protocol.peers.sip.RFC3261;
import com.mrcp.yxp.protocol.peers.sip.Utils;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderFieldName;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderFieldValue;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderParamName;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaders;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipUriSyntaxException;
import com.mrcp.yxp.protocol.peers.sip.transaction.ClientTransaction;
import com.mrcp.yxp.protocol.peers.sip.transaction.InviteClientTransaction;
import com.mrcp.yxp.protocol.peers.sip.transaction.TransactionManager;
import com.mrcp.yxp.protocol.peers.sip.transactionuser.Dialog;
import com.mrcp.yxp.protocol.peers.sip.transactionuser.DialogManager;
import com.mrcp.yxp.protocol.peers.sip.transactionuser.DialogState;
import com.mrcp.yxp.protocol.peers.sip.transport.SipMessage;
import com.mrcp.yxp.protocol.peers.sip.transport.SipRequest;
import com.mrcp.yxp.protocol.peers.sip.transport.SipResponse;
import com.mrcp.yxp.protocol.peers.sip.transport.TransportManager;

public class UAC {

    private InitialRequestManager initialRequestManager;
    private MidDialogRequestManager midDialogRequestManager;

    private String registerCallID;
    private String profileUri;

    //FIXME
    private UserAgent userAgent;
    private TransactionManager transactionManager;
    private DialogManager dialogManager;
    private List<String> guiClosedCallIds;
    private Logger logger;

    /**
     * should be instanciated only once, it was a singleton.
     */
    public UAC(UserAgent userAgent,
            InitialRequestManager initialRequestManager,
            MidDialogRequestManager midDialogRequestManager,
            DialogManager dialogManager,
            TransactionManager transactionManager,
            TransportManager transportManager,
            Logger logger) {
        this.userAgent = userAgent;
        this.initialRequestManager = initialRequestManager;
        this.midDialogRequestManager = midDialogRequestManager;
        this.dialogManager = dialogManager;
        this.transactionManager = transactionManager;
        this.logger = logger;
        guiClosedCallIds = Collections.synchronizedList(new ArrayList<String>());
        profileUri = RFC3261.SIP_SCHEME + RFC3261.SCHEME_SEPARATOR
            + userAgent.getUserpart() + RFC3261.AT + userAgent.getDomain();
    }

    /**
     * For the moment we consider that only one profile uri is used at a time.
     * @throws SipUriSyntaxException
     */
    SipRequest register() throws SipUriSyntaxException {
        String domain = userAgent.getDomain();
        String requestUri = RFC3261.SIP_SCHEME + RFC3261.SCHEME_SEPARATOR
            + domain;
        SipListener sipListener = userAgent.getSipListener();
        profileUri = RFC3261.SIP_SCHEME + RFC3261.SCHEME_SEPARATOR
        	+ userAgent.getUserpart() + RFC3261.AT + domain;
        registerCallID = Utils.generateCallID(
                userAgent.getConfig().getLocalInetAddress());
        SipRequest sipRequest = initialRequestManager.createInitialRequest(
                requestUri, RFC3261.METHOD_REGISTER, profileUri,
                registerCallID);
        if (sipListener != null) {
            sipListener.registering(sipRequest);
        }
        return sipRequest;
    }

    void unregister() throws SipUriSyntaxException {
        if (getInitialRequestManager().getRegisterHandler().isRegistered()) {
            String requestUri = RFC3261.SIP_SCHEME + RFC3261.SCHEME_SEPARATOR
                + userAgent.getDomain();
            MessageInterceptor messageInterceptor = new MessageInterceptor() {

                @Override
                public void postProcess(SipMessage sipMessage) {
                    initialRequestManager.registerHandler.unregister();
                    SipHeaders sipHeaders = sipMessage.getSipHeaders();
                    SipHeaderFieldValue contact = sipHeaders.get(
                            new SipHeaderFieldName(RFC3261.HDR_CONTACT));
                    contact.addParam(new SipHeaderParamName(RFC3261.PARAM_EXPIRES),
                            "0");
                }

            };
            // for any reason, asterisk requires a new Call-ID to unregister
            registerCallID = Utils.generateCallID(
                    userAgent.getConfig().getLocalInetAddress());
            initialRequestManager.createInitialRequest(requestUri,
                    RFC3261.METHOD_REGISTER, profileUri, registerCallID, null,
                    messageInterceptor);
        }
    }

    SipRequest invite(String requestUri, String callId)
            throws SipUriSyntaxException {
        return initialRequestManager.createInitialRequest(requestUri,
                RFC3261.METHOD_INVITE, profileUri, callId);

    }

    private SipRequest getInviteWithAuth(String callId) {
        List<ClientTransaction> clientTransactions =
            transactionManager.getClientTransactionsFromCallId(callId,
                    RFC3261.METHOD_INVITE);
        SipRequest sipRequestNoAuth = null;
        for (ClientTransaction clientTransaction: clientTransactions) {
            InviteClientTransaction inviteClientTransaction =
                (InviteClientTransaction)clientTransaction;
            SipRequest sipRequest = inviteClientTransaction.getRequest();
            SipHeaders sipHeaders = sipRequest.getSipHeaders();
            SipHeaderFieldName authorization = new SipHeaderFieldName(
                    RFC3261.HDR_AUTHORIZATION);
            SipHeaderFieldValue value = sipHeaders.get(authorization);
            if (value == null) {
                SipHeaderFieldName proxyAuthorization = new SipHeaderFieldName(
                        RFC3261.HDR_PROXY_AUTHORIZATION);
                value = sipHeaders.get(proxyAuthorization);
            }
            if (value != null) {
                return sipRequest;
            }
            sipRequestNoAuth = sipRequest;
        }
        return sipRequestNoAuth;
    }

    void terminate(SipRequest sipRequest) {
        String callId = Utils.getMessageCallId(sipRequest);
        if (!guiClosedCallIds.contains(callId)) {
            guiClosedCallIds.add(callId);
        }
        Dialog dialog = dialogManager.getDialog(callId);
        SipRequest inviteWithAuth = getInviteWithAuth(callId);
        if (dialog != null) {
            SipRequest originatingRequest;
            if (inviteWithAuth != null) {
                originatingRequest = inviteWithAuth;
            } else {
                originatingRequest = sipRequest;
            }
            ClientTransaction clientTransaction =
                transactionManager.getClientTransaction(originatingRequest);
            if (clientTransaction != null) {
                synchronized (clientTransaction) {
                    DialogState dialogState = dialog.getState();
                    if (dialog.EARLY.equals(dialogState)) {
                        initialRequestManager.createCancel(inviteWithAuth,
                                midDialogRequestManager, profileUri);
                    } else if (dialog.CONFIRMED.equals(dialogState)) {
                        // clientTransaction not yet removed
                        midDialogRequestManager.generateMidDialogRequest(
                                dialog, RFC3261.METHOD_BYE, null);
                        guiClosedCallIds.remove(callId);
                    }
                }
            } else {
                // clientTransaction Terminated and removed
                logger.debug("clientTransaction null");
                midDialogRequestManager.generateMidDialogRequest(
                        dialog, RFC3261.METHOD_BYE, null);
                guiClosedCallIds.remove(callId);
            }
        } else {
            InviteClientTransaction inviteClientTransaction =
                (InviteClientTransaction)transactionManager
                    .getClientTransaction(inviteWithAuth);
            if (inviteClientTransaction == null) {
                logger.error("cannot find invite client transaction" +
                        " for call " + callId);
            } else {
                SipResponse sipResponse =
                    inviteClientTransaction.getLastResponse();
                if (sipResponse != null) {
                    int statusCode = sipResponse.getStatusCode();
                    if (statusCode < RFC3261.CODE_200_OK) {
                        initialRequestManager.createCancel(inviteWithAuth,
                                midDialogRequestManager, profileUri);
                    }
                }
            }
        }
        userAgent.getMediaManager().stopSession();
    }

    public InitialRequestManager getInitialRequestManager() {
        return initialRequestManager;
    }

    public List<String> getGuiClosedCallIds() {
        return guiClosedCallIds;
    }

}
