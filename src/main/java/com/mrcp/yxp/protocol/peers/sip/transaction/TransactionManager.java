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

package com.mrcp.yxp.protocol.peers.sip.transaction;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;

import com.mrcp.yxp.protocol.peers.Logger;
import com.mrcp.yxp.protocol.peers.sip.RFC3261;
import com.mrcp.yxp.protocol.peers.sip.Utils;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderFieldName;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderFieldValue;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderParamName;
import com.mrcp.yxp.protocol.peers.sip.transport.SipMessage;
import com.mrcp.yxp.protocol.peers.sip.transport.SipRequest;
import com.mrcp.yxp.protocol.peers.sip.transport.SipResponse;
import com.mrcp.yxp.protocol.peers.sip.transport.TransportManager;


public class TransactionManager {

    protected Timer timer;

    // TODO remove client transactions when they reach terminated state
    // TODO check that server transactions are removed in all transitions to terminated
    private Hashtable<String, ClientTransaction> clientTransactions;
    private Hashtable<String, ServerTransaction> serverTransactions;

    private TransportManager transportManager;
    private Logger logger;

    public TransactionManager(Logger logger) {
        this.logger = logger;
        clientTransactions = new Hashtable<String, ClientTransaction>();
        serverTransactions = new Hashtable<String, ServerTransaction>();
        timer = new Timer(TransactionManager.class.getSimpleName()
                + " " + Timer.class.getSimpleName());
    }

    public ClientTransaction createClientTransaction(SipRequest sipRequest,
                                                     InetAddress inetAddress, int port, String transport,
                                                     String pBranchId, ClientTransactionUser clientTransactionUser) {
        String branchId;
        if (pBranchId == null || "".equals(pBranchId.trim())
                || !pBranchId.startsWith(RFC3261.BRANCHID_MAGIC_COOKIE)) {
            branchId = Utils.generateBranchId();
        } else {
            branchId = pBranchId;
        }
        String method = sipRequest.getMethod();
        ClientTransaction clientTransaction;
        if (RFC3261.METHOD_INVITE.equals(method)) {
            clientTransaction = new InviteClientTransaction(branchId,
                    inetAddress, port, transport, sipRequest, clientTransactionUser,
                    timer, transportManager, this, logger);
        } else {
            clientTransaction = new NonInviteClientTransaction(branchId,
                    inetAddress, port, transport, sipRequest, clientTransactionUser,
                    timer, transportManager, this, logger);
        }
        clientTransactions.put(getTransactionId(branchId, method),
                clientTransaction);
        return clientTransaction;
    }

    public ServerTransaction createServerTransaction(SipResponse sipResponse,
                                                     int port, String transport,
                                                     ServerTransactionUser serverTransactionUser,
                                                     SipRequest sipRequest) {
        SipHeaderFieldValue via = Utils.getTopVia(sipResponse);
        String branchId = via.getParam(new SipHeaderParamName(
                RFC3261.PARAM_BRANCH));
        String cseq = sipResponse.getSipHeaders().get(
                new SipHeaderFieldName(RFC3261.HDR_CSEQ)).toString();
        String method = cseq.substring(cseq.lastIndexOf(' ') + 1);
        ServerTransaction serverTransaction;
        // TODO create server transport user and pass it to server transaction
        if (RFC3261.METHOD_INVITE.equals(method)) {
            serverTransaction = new InviteServerTransaction(branchId, port,
                    transport, sipResponse, serverTransactionUser, sipRequest,
                    timer, this, transportManager, logger);
            // serverTransaction = new InviteServerTransaction(branchId);
        } else {
            serverTransaction = new NonInviteServerTransaction(branchId, port,
                    transport, method, serverTransactionUser, sipRequest, timer,
                    transportManager, this, logger);
        }
        serverTransactions.put(getTransactionId(branchId, method),
                serverTransaction);
        return serverTransaction;
    }

    public ClientTransaction getClientTransaction(SipMessage sipMessage) {
        SipHeaderFieldValue via = Utils.getTopVia(sipMessage);
        String branchId = via.getParam(new SipHeaderParamName(
                RFC3261.PARAM_BRANCH));
        String cseq = sipMessage.getSipHeaders().get(
                new SipHeaderFieldName(RFC3261.HDR_CSEQ)).toString();
        String method = cseq.substring(cseq.lastIndexOf(' ') + 1);
        return clientTransactions.get(getTransactionId(branchId, method));
    }

    public List<ClientTransaction> getClientTransactionsFromCallId(String callId,
            String method) {
        ArrayList<ClientTransaction> clientTransactionsFromCallId =
            new ArrayList<ClientTransaction>();
        for (ClientTransaction clientTransaction: clientTransactions.values()) {
            Transaction transaction = (Transaction)clientTransaction;
            SipRequest sipRequest = transaction.getRequest();
            String reqCallId = Utils.getMessageCallId(sipRequest);
            String reqMethod = sipRequest.getMethod();
            if (callId.equals(reqCallId) && method.equals(reqMethod)) {
                clientTransactionsFromCallId.add(clientTransaction);
            }
        }
        return clientTransactionsFromCallId;
    }

    public ServerTransaction getServerTransaction(SipMessage sipMessage) {
        SipHeaderFieldValue via = Utils.getTopVia(sipMessage);
        String branchId = via.getParam(new SipHeaderParamName(
                RFC3261.PARAM_BRANCH));
        String method;
        if (sipMessage instanceof SipRequest) {
            method = ((SipRequest)sipMessage).getMethod();
        } else {
            String cseq = sipMessage.getSipHeaders().get(
                    new SipHeaderFieldName(RFC3261.HDR_CSEQ)).toString();
            method = cseq.substring(cseq.lastIndexOf(' ') + 1);
        }
        if (RFC3261.METHOD_ACK.equals(method)) {
            method = RFC3261.METHOD_INVITE;
//            InviteServerTransaction inviteServerTransaction =
//                (InviteServerTransaction)
//                serverTransactions.get(getTransactionId(branchId, method));
//            if (inviteServerTransaction == null) {
//                Logger.debug("received ACK for unknown transaction" +
//                		" branchId = " + branchId + ", method = " + method);
//            } else {
//                SipResponse sipResponse =
//                    inviteServerTransaction.getLastResponse();
//                if (sipResponse == null) {
//                    Logger.debug("received ACK but no response sent " +
//                    		"branchId = " + branchId + ", method = " + method);
//                } else {
//                    int statusCode = sipResponse.getStatusCode();
//                    if (statusCode >= RFC3261.CODE_MIN_SUCCESS &&
//                            statusCode < RFC3261.CODE_MIN_REDIR) {
//                        // success response => ACK is not in INVITE server
//                        // transaction
//                        return null;
//                    } else {
//                        // error => ACK belongs to INVITE server transaction
//                        return inviteServerTransaction;
//                    }
//                }
//            }
            // TODO if positive response, ACK does not belong to transaction
            // retrieve transaction and take responses from transaction
            // and check if a positive response has been received
            // if it is the case, a new standalone transaction must be created
            // for the ACK
        }
        return serverTransactions.get(getTransactionId(branchId, method));
    }

    public ServerTransaction getServerTransaction(String branchId, String method) {
        return serverTransactions.get(getTransactionId(branchId, method));
    }

    void removeServerTransaction(String branchId, String method) {
        serverTransactions.remove(getTransactionId(branchId, method));
    }

    void removeClientTransaction(String branchId, String method) {
        clientTransactions.remove(getTransactionId(branchId, method));
    }

    private String getTransactionId(String branchId, String method) {
        StringBuffer buf = new StringBuffer();
        buf.append(branchId);
        buf.append(Transaction.ID_SEPARATOR);
        buf.append(method);
        return buf.toString();
    }

    public void setTransportManager(TransportManager transportManager) {
        this.transportManager = transportManager;
    }


    // stop timer
    public void stopTimer(){
        timer.cancel();
    }

}
