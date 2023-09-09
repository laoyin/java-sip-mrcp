/*
 * MRCP4J - Java API implementation of MRCPv2 specification
 *
 * Copyright (C) 2005-2006 SpeechForge - http://www.speechforge.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact: ngodfredsen@users.sourceforge.net
 *
 */
package com.mrcp.yxp.protocol.mrcp.mrcp4j.server;

import com.mrcp.yxp.protocol.mrcp.mrcp4j.MrcpRequestState;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.message.MrcpEvent;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.message.MrcpResponse;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.message.request.MrcpRequest;

import org.apache.mina.protocol.ProtocolHandlerAdapter;
import org.apache.mina.protocol.ProtocolSession;

/**
 *
 * @author Niels Godfredsen {@literal <}<a href="mailto:ngodfredsen@users.sourceforge.net">ngodfredsen@users.sourceforge.net</a>{@literal >}
 */
public class MrcpProtocolHandler extends ProtocolHandlerAdapter {

    private MrcpRequestProcessor _requestProcessor;

    public MrcpProtocolHandler(MrcpRequestProcessor requestProcessor) {
        _requestProcessor = requestProcessor;
    }

    /* (non-Javadoc)
     * @see org.apache.mina.protocol.ProtocolHandler#exceptionCaught(org.apache.mina.protocol.ProtocolSession, java.lang.Throwable)
     */
    @Override
    public void exceptionCaught(ProtocolSession session, Throwable cause) {
        // close connection when unexpected exception is caught.
        session.close();
    }

    /* (non-Javadoc)
     * @see org.apache.mina.protocol.ProtocolHandler#messageReceived(org.apache.mina.protocol.ProtocolSession, java.lang.Object)
     */
    @Override
    public void messageReceived(ProtocolSession session, Object message) {
        MrcpRequest request = (MrcpRequest) message;
        new EventThread(_requestProcessor, session, request).start(); // TODO: move threading down chain
    }

    private static class EventThread extends Thread {

        private MrcpRequestProcessor _requestProcessor;
        private ProtocolSession _session;
        private MrcpRequest _request;

        EventThread(MrcpRequestProcessor requestProcessor, ProtocolSession session, MrcpRequest request) {
            _request = request;
            _requestProcessor = requestProcessor;
            _session = session;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            MrcpResponse response = _requestProcessor.processRequest(_request);
            _session.write(response);

            MrcpRequestState requestState = response.getRequestState();

            while (!requestState.equals(MrcpRequestState.COMPLETE) && _session.isConnected()) {
                MrcpEvent event = _requestProcessor.getNextEvent(_request);
                if (event != null) {
                    _session.write(event);
                    requestState = event.getRequestState();
                } else {
                    break;
                }
            }
        }
    }


}
