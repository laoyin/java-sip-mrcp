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
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import com.mrcp.yxp.protocol.peers.Logger;
import com.mrcp.yxp.protocol.peers.sip.RFC3261;
import com.mrcp.yxp.protocol.peers.sip.Utils;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderFieldName;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderFieldValue;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderParamName;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaders;
import com.mrcp.yxp.protocol.peers.sip.transport.MessageSender;
import com.mrcp.yxp.protocol.peers.sip.transport.SipClientTransportUser;
import com.mrcp.yxp.protocol.peers.sip.transport.SipRequest;
import com.mrcp.yxp.protocol.peers.sip.transport.SipResponse;
import com.mrcp.yxp.protocol.peers.sip.transport.TransportManager;


public class InviteClientTransaction extends InviteTransaction
        implements ClientTransaction, SipClientTransportUser {

    public final InviteClientTransactionState INIT;
    public final InviteClientTransactionState CALLING;
    public final InviteClientTransactionState PROCEEDING;
    public final InviteClientTransactionState COMPLETED;
    public final InviteClientTransactionState TERMINATED;

    protected ClientTransactionUser transactionUser;
    protected String transport;

    private InviteClientTransactionState state;
    //private SipClientTransport sipClientTransport;
    private MessageSender messageSender;
    private int nbRetrans;
    private SipRequest ack;
    private int remotePort;
    private InetAddress remoteInetAddress;

    InviteClientTransaction(String branchId, InetAddress inetAddress,
            int port, String transport, SipRequest sipRequest,
            ClientTransactionUser transactionUser, Timer timer,
            TransportManager transportManager,
            TransactionManager transactionManager, Logger logger) {
        super(branchId, timer, transportManager, transactionManager,
                logger);

        this.transport = transport;

        SipHeaderFieldValue via = new SipHeaderFieldValue("");
        via.addParam(new SipHeaderParamName(RFC3261.PARAM_BRANCH), branchId);
        sipRequest.getSipHeaders().add(new SipHeaderFieldName(RFC3261.HDR_VIA), via, 0);

        nbRetrans = 0;

        INIT = new InviteClientTransactionStateInit(getId(), this, logger);
        state = INIT;
        CALLING = new InviteClientTransactionStateCalling(getId(), this,
                logger);
        PROCEEDING = new InviteClientTransactionStateProceeding(getId(), this,
                logger);
        COMPLETED = new InviteClientTransactionStateCompleted(getId(), this,
                logger);
        TERMINATED = new InviteClientTransactionStateTerminated(getId(), this,
                logger);

        //17.1.1.2

        request = sipRequest;
        this.transactionUser = transactionUser;

        remotePort = port;
        remoteInetAddress = inetAddress;

        try {
            messageSender = transportManager.createClientTransport(
                    request, remoteInetAddress, remotePort, transport);
        } catch (IOException e) {
            logger.error("input/output error", e);
            transportError();
        }

    }

    public void setState(InviteClientTransactionState state) {
        this.state.log(state);
        this.state = state;
        if(TERMINATED.equals(state)) {
            //transactionManager.removeClientTransaction(branchId, method);
            transactionManager = null;
        }
    }

    public void start() {
        state.start();
        //send request using transport information and sipRequest
//        try {
//            sipClientTransport = SipTransportFactory.getInstance()
//                .createClientTransport(this, request, remoteInetAddress,
//                        remotePort, transport);
//            sipClientTransport.send(request);
//        } catch (IOException e) {
//            //e.printStackTrace();
//            transportError();
//        }

        try {
            messageSender.sendMessage(request);
        } catch (IOException e) {
            logger.error("input/output error", e);
            transportError();
        }
        logger.debug("InviteClientTransaction.start");

        if (RFC3261.TRANSPORT_UDP.equals(transport)) {
            //start timer A with value T1 for retransmission
            timer.schedule(new TimerA(), RFC3261.TIMER_T1);
        }

        //TODO start timer B with value 64*T1 for transaction timeout
        timer.schedule(new TimerB(), 64 * RFC3261.TIMER_T1);
    }

    public synchronized void receivedResponse(SipResponse sipResponse) {
        responses.add(sipResponse);
        // 17.1.1
        int statusCode = sipResponse.getStatusCode();
        if (statusCode < RFC3261.CODE_MIN_PROV) {
            logger.error("invalid response code");
        } else if (statusCode < RFC3261.CODE_MIN_SUCCESS) {
            state.received1xx();
        } else if (statusCode < RFC3261.CODE_MIN_REDIR) {
            state.received2xx();
        } else if (statusCode <= RFC3261.CODE_MAX) {
            state.received300To699();
        } else {
            logger.error("invalid response code");
        }
    }

    public void transportError() {
        state.transportError();
    }

    void createAndSendAck() {

        //p.126 last paragraph

        //17.1.1.3
        ack = new SipRequest(RFC3261.METHOD_ACK, request.getRequestUri());
        SipHeaderFieldValue topVia = Utils.getTopVia(request);
        SipHeaders ackSipHeaders = ack.getSipHeaders();
        ackSipHeaders.add(new SipHeaderFieldName(RFC3261.HDR_VIA), topVia);
        Utils.copyHeader(request, ack, RFC3261.HDR_CALLID);
        Utils.copyHeader(request, ack, RFC3261.HDR_FROM);
        Utils.copyHeader(getLastResponse(), ack, RFC3261.HDR_TO);
        //TODO what happens if a prov response is received after a 200+ ...
        SipHeaders requestSipHeaders = request.getSipHeaders();
        SipHeaderFieldName cseqName = new SipHeaderFieldName(RFC3261.HDR_CSEQ);
        SipHeaderFieldValue cseq = requestSipHeaders.get(cseqName);
        cseq.setValue(cseq.toString().replace(RFC3261.METHOD_INVITE, RFC3261.METHOD_ACK));
        ackSipHeaders.add(cseqName, cseq);
        Utils.copyHeader(request, ack, RFC3261.HDR_ROUTE);

        sendAck();
    }

    void sendAck() {
        //ack is passed to the transport layer...
        //TODO manage ACK retrans
        //sipClientTransport.send(ack);
        try {
            messageSender.sendMessage(ack);
        } catch (IOException e) {
            logger.error("input/output error", e);
            transportError();
        }
    }

    void sendRetrans() {
        ++nbRetrans;
        //sipClientTransport.send(request);
        try {
            messageSender.sendMessage(request);
        } catch (IOException e) {
            logger.error("input/output error", e);
            transportError();
        }
        timer.schedule(new TimerA(), (long)Math.pow(2, nbRetrans) * RFC3261.TIMER_T1);
    }

    public void requestTransportError(SipRequest sipRequest, Exception e) {
        // TODO Auto-generated method stub

    }

    public void responseTransportError(Exception e) {
        // TODO Auto-generated method stub

    }

    class TimerA extends TimerTask {
        @Override
        public void run() {
            state.timerAFires();
        }
    }

    class TimerB extends TimerTask {
        @Override
        public void run() {
            state.timerBFires();
        }
    }

    class TimerD extends TimerTask {
        @Override
        public void run() {
            state.timerDFires();
        }
    }

    public String getContact() {
        if (messageSender != null) {
            return messageSender.getContact();
        }
        return null;
    }

}
