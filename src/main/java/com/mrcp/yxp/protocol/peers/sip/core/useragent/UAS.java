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

package com.mrcp.yxp.protocol.peers.sip.core.useragent;

import java.net.SocketException;
import java.util.ArrayList;

import com.mrcp.yxp.protocol.peers.sip.RFC3261;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderFieldName;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderFieldValue;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderParamName;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaders;
import com.mrcp.yxp.protocol.peers.sip.transaction.TransactionManager;
import com.mrcp.yxp.protocol.peers.sip.transactionuser.Dialog;
import com.mrcp.yxp.protocol.peers.sip.transactionuser.DialogManager;
import com.mrcp.yxp.protocol.peers.sip.transport.SipMessage;
import com.mrcp.yxp.protocol.peers.sip.transport.SipRequest;
import com.mrcp.yxp.protocol.peers.sip.transport.SipResponse;
import com.mrcp.yxp.protocol.peers.sip.transport.SipServerTransportUser;
import com.mrcp.yxp.protocol.peers.sip.transport.TransportManager;

public class UAS implements SipServerTransportUser {

    public final static ArrayList<String> SUPPORTED_METHODS;

    static {
        SUPPORTED_METHODS = new ArrayList<String>();
        SUPPORTED_METHODS.add(RFC3261.METHOD_INVITE);
        SUPPORTED_METHODS.add(RFC3261.METHOD_ACK);
        SUPPORTED_METHODS.add(RFC3261.METHOD_CANCEL);
        SUPPORTED_METHODS.add(RFC3261.METHOD_OPTIONS);
        SUPPORTED_METHODS.add(RFC3261.METHOD_BYE);
    };

    private InitialRequestManager initialRequestManager;
    private MidDialogRequestManager midDialogRequestManager;

    private DialogManager dialogManager;

    /**
     * should be instanciated only once, it was a singleton.
     */
    public UAS(UserAgent userAgent,
            InitialRequestManager initialRequestManager,
            MidDialogRequestManager midDialogRequestManager,
            DialogManager dialogManager,
            TransactionManager transactionManager,
            TransportManager transportManager) throws SocketException {
        this.initialRequestManager = initialRequestManager;
        this.midDialogRequestManager = midDialogRequestManager;
        this.dialogManager = dialogManager;
        transportManager.setSipServerTransportUser(this);
        transportManager.createServerTransport(
                RFC3261.TRANSPORT_UDP, userAgent.getConfig().getSipPort());
    }

    public void messageReceived(SipMessage sipMessage) {
        if (sipMessage instanceof SipRequest) {
            requestReceived((SipRequest) sipMessage);
        } else if (sipMessage instanceof SipResponse) {
            responseReceived((SipResponse) sipMessage);
        } else {
            throw new RuntimeException("unknown message type");
        }
    }

    private void responseReceived(SipResponse sipResponse) {

    }

    private void requestReceived(SipRequest sipRequest) {
        //TODO 8.2

        //TODO JTA to make request processing atomic

        SipHeaders headers = sipRequest.getSipHeaders();

        //TODO find whether the request is within an existing dialog or not
        SipHeaderFieldValue to =
            headers.get(new SipHeaderFieldName(RFC3261.HDR_TO));
        String toTag = to.getParam(new SipHeaderParamName(RFC3261.PARAM_TAG));
        if (toTag != null) {
            Dialog dialog = dialogManager.getDialog(sipRequest);
            if (dialog != null) {
                //this is a mid-dialog request
                midDialogRequestManager.manageMidDialogRequest(sipRequest, dialog);
                //TODO continue processing
            } else {
                //TODO reject the request with a 481 Call/Transaction Does Not Exist

            }
        } else {

            initialRequestManager.manageInitialRequest(sipRequest);

        }
    }

    void acceptCall(SipRequest sipRequest, Dialog dialog) {
        initialRequestManager.getInviteHandler().acceptCall(sipRequest,
                dialog);
    }

    void rejectCall(SipRequest sipRequest) {
        initialRequestManager.getInviteHandler().rejectCall(sipRequest);
    }

    public InitialRequestManager getInitialRequestManager() {
        return initialRequestManager;
    }

    public MidDialogRequestManager getMidDialogRequestManager() {
        return midDialogRequestManager;
    }

}
