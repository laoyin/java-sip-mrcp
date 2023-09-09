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
import com.mrcp.yxp.protocol.peers.sip.transport.SipMessage;
import com.mrcp.yxp.protocol.peers.sip.transport.SipRequest;
import com.mrcp.yxp.protocol.peers.sip.transport.SipResponse;
import com.mrcp.yxp.protocol.peers.sip.transport.SipServerTransportUser;
import com.mrcp.yxp.protocol.peers.sip.transport.TransportManager;


public class InviteServerTransaction extends InviteTransaction
        implements ServerTransaction, SipServerTransportUser {

    public final InviteServerTransactionState INIT;
    public final InviteServerTransactionState PROCEEDING;
    public final InviteServerTransactionState COMPLETED;
    public final InviteServerTransactionState CONFIRMED;
    public final InviteServerTransactionState TERMINATED;

    protected String transport;
    protected int nbRetrans;
    protected ServerTransactionUser serverTransactionUser;

    private InviteServerTransactionState state;
    //private SipServerTransport sipServerTransport;
    private int port;

    InviteServerTransaction(String branchId, int port, String transport,
            SipResponse sipResponse, ServerTransactionUser serverTransactionUser,
            SipRequest sipRequest, Timer timer, TransactionManager transactionManager,
            TransportManager transportManager, Logger logger) {
        super(branchId, timer, transportManager, transactionManager, logger);

        INIT = new InviteServerTransactionStateInit(getId(), this, logger);
        state = INIT;
        PROCEEDING = new InviteServerTransactionStateProceeding(getId(), this,
                logger);
        COMPLETED = new InviteServerTransactionStateCompleted(getId(), this,
                logger);
        CONFIRMED = new InviteServerTransactionStateConfirmed(getId(), this,
                logger);
        TERMINATED = new InviteServerTransactionStateTerminated(getId(), this,
                logger);

        this.request = sipRequest;
        this.port = port;
        this.transport = transport;
        responses.add(sipResponse);
        nbRetrans = 0;
        this.serverTransactionUser = serverTransactionUser;
        //TODO pass INV to TU, send 100 if TU won't in 200ms
    }

    public void start() {
        state.start();

//        sipServerTransport = SipTransportFactory.getInstance()
//            .createServerTransport(this, port, transport);
        try {
            transportManager.createServerTransport(transport, port);
        } catch (IOException e) {
            logger.error("input/output error", e);
        }
    }

    public void receivedRequest(SipRequest sipRequest) {
        String method = sipRequest.getMethod();
        if (RFC3261.METHOD_INVITE.equals(method)) {
            state.receivedInvite();
        } else {
            // if not INVITE, we consider that a ACK is received
            // in the case the call was not successful
            state.receivedAck();
        }

    }

    public void sendReponse(SipResponse sipResponse) {
        //TODO check that a retransmission response will be considered as
        //equal (for contains) to the first response
        if (!responses.contains(sipResponse)) {
            responses.add(sipResponse);
        }
        int statusCode = sipResponse.getStatusCode();
        if (statusCode == RFC3261.CODE_MIN_PROV) {
            // TODO 100 trying
        } else if (statusCode < RFC3261.CODE_MIN_SUCCESS) {
            state.received101To199();
        } else if (statusCode < RFC3261.CODE_MIN_REDIR) {
            state.received2xx();
        } else if (statusCode <= RFC3261.CODE_MAX) {
            state.received300To699();
        } else {
            logger.error("invalid response code");
        }
    }

    public void setState(InviteServerTransactionState state) {
        this.state.log(state);
        this.state = state;
    }

    public void messageReceived(SipMessage sipMessage) {
        // TODO Auto-generated method stub

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

    public SipResponse getLastResponse() {
        int nbOfResponses = responses.size();
        if (nbOfResponses > 0) {
            return responses.get(nbOfResponses - 1);
        }
        return null;
    }

    // TODO send provional response
    /*
     * maybe the 200 response mechanism could be retrieved for 1xx responses.
     */

// void stopSipServerTransport() {
//        sipServerTransport.stop();
//    }

    class TimerG extends TimerTask {
        @Override
        public void run() {
            state.timerGFires();
        }
    }

    class TimerH extends TimerTask {
        @Override
        public void run() {
            state.timerHFiresOrTransportError();
        }
    }

    class TimerI extends TimerTask {
        @Override
        public void run() {
            state.timerIFires();
        }
    }

}
