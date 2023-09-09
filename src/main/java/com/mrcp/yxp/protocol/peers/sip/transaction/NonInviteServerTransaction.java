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

package com.mrcp.yxp.protocol.peers.sip.transaction;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.mrcp.yxp.protocol.peers.Logger;
import com.mrcp.yxp.protocol.peers.sip.RFC3261;
import com.mrcp.yxp.protocol.peers.sip.transport.SipRequest;
import com.mrcp.yxp.protocol.peers.sip.transport.SipResponse;
import com.mrcp.yxp.protocol.peers.sip.transport.TransportManager;


public class NonInviteServerTransaction extends NonInviteTransaction
        implements ServerTransaction/*, SipServerTransportUser*/ {

    public final NonInviteServerTransactionState TRYING;
    public final NonInviteServerTransactionState PROCEEDING;
    public final NonInviteServerTransactionState COMPLETED;
    public final NonInviteServerTransactionState TERMINATED;

    protected ServerTransactionUser serverTransactionUser;
    protected Timer timer;
    protected String transport;

    private NonInviteServerTransactionState state;
    //private int port;

    NonInviteServerTransaction(String branchId, int port, String transport,
            String method, ServerTransactionUser serverTransactionUser,
            SipRequest sipRequest, Timer timer, TransportManager transportManager,
            TransactionManager transactionManager, Logger logger) {
        super(branchId, method, timer, transportManager, transactionManager,
                logger);

        TRYING = new NonInviteServerTransactionStateTrying(getId(), this,
                logger);
        state = TRYING;
        PROCEEDING = new NonInviteServerTransactionStateProceeding(getId(),
                this, logger);
        COMPLETED = new NonInviteServerTransactionStateCompleted(getId(), this,
                logger);
        TERMINATED = new NonInviteServerTransactionStateTerminated(getId(),
                this, logger);

        //this.port = port;
        this.transport = transport;
        this.serverTransactionUser = serverTransactionUser;
        request = sipRequest;
//        sipServerTransport = SipTransportFactory.getInstance()
//            .createServerTransport(this, port, transport);
        try {
            transportManager.createServerTransport(transport, port);
        } catch (IOException e) {
            logger.error("input/output error", e);
        }

        //TODO pass request to TU
    }

    public void setState(NonInviteServerTransactionState state) {
        this.state.log(state);
        this.state = state;
    }

    public void receivedRequest(SipRequest sipRequest) {
        state.receivedRequest();
    }

    public void sendReponse(SipResponse sipResponse) {
        responses.add(sipResponse);
        int statusCode = sipResponse.getStatusCode();
        if (statusCode < RFC3261.CODE_200_OK) {
            state.received1xx();
        } else if (statusCode <= RFC3261.CODE_MAX) {
            state.received200To699();
        }
    }

    void sendLastResponse() {
        //sipServerTransport.sendResponse(responses.get(responses.size() - 1));
        int nbOfResponses = responses.size();
        if (nbOfResponses > 0) {
            try {
                transportManager.sendResponse(responses.get(nbOfResponses - 1));
            } catch (IOException e) {
                logger.error("input/output error", e);
            }
        }
    }

    public void start() {
        // TODO Auto-generated method stub

    }

//    public void messageReceived(SipMessage sipMessage) {
//        // TODO Auto-generated method stub
//
//    }

    class TimerJ extends TimerTask {
        @Override
        public void run() {
            state.timerJFires();
        }
    }

}
