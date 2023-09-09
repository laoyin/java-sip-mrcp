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

package com.mrcp.yxp.protocol.peers.sip.transport;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;

import com.mrcp.yxp.protocol.peers.Config;
import com.mrcp.yxp.protocol.peers.Logger;
import com.mrcp.yxp.protocol.peers.sip.transaction.TransactionManager;
import com.mrcp.yxp.protocol.peers.sip.RFC3261;
import com.mrcp.yxp.protocol.peers.sip.Utils;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderFieldValue;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderParamName;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipParserException;
import com.mrcp.yxp.protocol.peers.sip.transaction.ClientTransaction;
import com.mrcp.yxp.protocol.peers.sip.transaction.ServerTransaction;

public abstract class MessageReceiver implements Runnable {

    public static final int BUFFER_SIZE = 2048;//FIXME should correspond to MTU 1024;
    public static final String CHARACTER_ENCODING = "US-ASCII";

    protected int port;
    private boolean isListening;

    //private UAS uas;
    private SipServerTransportUser sipServerTransportUser;
    private TransactionManager transactionManager;
    private TransportManager transportManager;
    private Config config;
    protected Logger logger;

    public MessageReceiver(int port, TransactionManager transactionManager,
            TransportManager transportManager, Config config, Logger logger) {
        super();
        this.port = port;
        this.transactionManager = transactionManager;
        this.transportManager = transportManager;
        this.config = config;
        this.logger = logger;
        isListening = true;
    }

    public void run() {
        while (isListening) {
            try {
                listen();
            } catch (IOException e) {
                logger.error("input/output error", e);
            }
        }
    }

    protected abstract void listen() throws IOException;

    protected boolean isRequest(byte[] message) {
        String beginning = null;
        try {
            beginning = new String(message, 0,
                    RFC3261.DEFAULT_SIP_VERSION.length(), CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException e) {
            logger.error("unsupported encoding", e);
        }
        if (RFC3261.DEFAULT_SIP_VERSION.equals(beginning)) {
            return false;
        }
        return true;
    }

    protected void processMessage(byte[] message, InetAddress sourceIp,
            int sourcePort, String transport) throws IOException {
        ByteArrayInputStream byteArrayInputStream =
            new ByteArrayInputStream(message);
        InputStreamReader inputStreamReader = new InputStreamReader(
                byteArrayInputStream);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String startLine = reader.readLine();
        while ("".equals(startLine)) {
            startLine = reader.readLine();
        }
        if (startLine == null) {
            return;
        }
        if (!startLine.contains(RFC3261.DEFAULT_SIP_VERSION)) {
            // keep-alive, send back to sender
            SipTransportConnection sipTransportConnection =
                new SipTransportConnection(config.getLocalInetAddress(),
                        port, sourceIp, sourcePort, transport);
            MessageSender messageSender = transportManager.getMessageSender(
                    sipTransportConnection);
            if (messageSender != null) {
                messageSender.sendBytes(message);
            }
            return;
        }
        StringBuffer direction = new StringBuffer();
        direction.append("RECEIVED from ").append(sourceIp.getHostAddress());
        direction.append("/").append(sourcePort);
        logger.traceNetwork(new String(message),
                direction.toString());
        SipMessage sipMessage = null;
        try {
            sipMessage = transportManager.sipParser.parse(
                    new ByteArrayInputStream(message));
        } catch (IOException e) {
            logger.error("input/output error", e);
        } catch (SipParserException e) {
            logger.error("SIP parser error", e);
        }
        if (sipMessage == null) {
            return;
        }

        // RFC3261 18.2

        if (sipMessage instanceof SipRequest) {
            SipRequest sipRequest = (SipRequest)sipMessage;


            SipHeaderFieldValue topVia = Utils.getTopVia(sipRequest);
            String sentBy =
                topVia.getParam(new SipHeaderParamName(RFC3261.PARAM_SENTBY));
            if (sentBy != null) {
                int colonPos = sentBy.indexOf(RFC3261.TRANSPORT_PORT_SEP);
                if (colonPos < 0) {
                    colonPos = sentBy.length();
                }
                sentBy = sentBy.substring(0, colonPos);
                if (!InetAddress.getByName(sentBy).equals(sourceIp)) {
                    topVia.addParam(new SipHeaderParamName(
                            RFC3261.PARAM_RECEIVED),
                            sourceIp.getHostAddress());
                }
            }
            //RFC3581
            //TODO check rport configuration
            SipHeaderParamName rportName = new SipHeaderParamName(
                    RFC3261.PARAM_RPORT);
            String rport = topVia.getParam(rportName);
            if (rport != null && "".equals(rport)) {
                topVia.removeParam(rportName);
                topVia.addParam(rportName, String.valueOf(sourcePort));
            }

            ServerTransaction serverTransaction =
                transactionManager.getServerTransaction(sipRequest);
            if (serverTransaction == null) {
                //uas.messageReceived(sipMessage);
                sipServerTransportUser.messageReceived(sipMessage);
            } else {
                serverTransaction.receivedRequest(sipRequest);
            }
        } else {
            SipResponse sipResponse = (SipResponse)sipMessage;
            ClientTransaction clientTransaction =
                transactionManager.getClientTransaction(sipResponse);
            logger.debug("ClientTransaction = " + clientTransaction);
            if (clientTransaction == null) {
                //uas.messageReceived(sipMessage);
                sipServerTransportUser.messageReceived(sipMessage);
            } else {
                clientTransaction.receivedResponse(sipResponse);
            }
        }
    }

    public synchronized void setListening(boolean isListening) {
        this.isListening = isListening;
    }

    public synchronized boolean isListening() {
        return isListening;
    }

    public void setSipServerTransportUser(
            SipServerTransportUser sipServerTransportUser) {
        this.sipServerTransportUser = sipServerTransportUser;
    }

//    public void setUas(UAS uas) {
//        this.uas = uas;
//    }

}
