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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.mrcp.yxp.protocol.mrcp.mrcp4j.MrcpEventName;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.MrcpRequestState;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.MrcpResourceType;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.message.MrcpEvent;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.message.MrcpResponse;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.server.mina.IoTextLoggingFilter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.mina.common.TransportType;
import org.apache.mina.io.IoAcceptor;
import org.apache.mina.protocol.ProtocolCodecFactory;
import org.apache.mina.registry.Service;
import org.apache.mina.registry.ServiceRegistry;
import org.apache.mina.registry.SimpleServiceRegistry;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.message.request.MrcpRequest;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.server.delegator.RecogOnlyRequestDelegator;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.server.delegator.RecorderRequestDelegator;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.server.delegator.SpeakVerifyRequestDelegator;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.server.delegator.SpeechSynthRequestDelegator;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.server.delegator.VoiceEnrollmentRequestDelegator;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.server.mina.SimpleProtocolProvider;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.server.provider.RecogOnlyRequestHandler;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.server.provider.RecorderRequestHandler;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.server.provider.SpeakVerifyRequestHandler;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.server.provider.SpeechSynthRequestHandler;
import com.mrcp.yxp.protocol.mrcp.mrcp4j.server.provider.VoiceEnrollmentRequestHandler;

/**
 *
 * @author Niels Godfredsen {@literal <}<a href="mailto:ngodfredsen@users.sourceforge.net">ngodfredsen@users.sourceforge.net</a>{@literal >}
 */
public class MrcpServerSocket {

	private static Logger _log = LogManager.getLogger(MrcpServerSocket.class);

    private static ProtocolCodecFactory CODEC_FACTORY = new MrcpCodecFactory();

    private MrcpRequestProcessorImpl _requestProcessorImpl;

    private int _port;

    /**
     * Creates a MRCP server socket, bound to the specified port
     *
     * @param port the port number to bind to
     * @throws IOException if an I/O error occurs when opening the socket.
     */
    public MrcpServerSocket(int port) throws IOException {
        _port = port;

        _requestProcessorImpl = new MrcpRequestProcessorImpl();

        ServiceRegistry registry = new SimpleServiceRegistry();
        addLogger(registry);
        Service service = new Service("MRCPv2", TransportType.SOCKET, port);
        registry.bind(service, new SimpleProtocolProvider(CODEC_FACTORY, new MrcpProtocolHandler(_requestProcessorImpl)));

        if (_log.isDebugEnabled()) {
            _log.debug("MRCPv2 protocol provider listening on port " + port);
        }

    }

    /**
     * TODOC
     * @return Returns the port.
     */
    public int getPort() {
        return _port;
    }

    public void openChannel(String channelID, RecogOnlyRequestHandler requestHandler) {
        validateChannelID(channelID, RecogOnlyRequestHandler.RESOURCE_TYPES);
        openChannel(channelID, new RecogOnlyRequestDelegator(requestHandler));
    }

    public void openChannel(String channelID, VoiceEnrollmentRequestHandler requestHandler) {
        validateChannelID(channelID, VoiceEnrollmentRequestHandler.RESOURCE_TYPES);
        openChannel(channelID, new VoiceEnrollmentRequestDelegator(requestHandler));
    }

    public void openChannel(String channelID, SpeechSynthRequestHandler requestHandler) {
        validateChannelID(channelID, SpeechSynthRequestHandler.RESOURCE_TYPES);
        openChannel(channelID, new SpeechSynthRequestDelegator(requestHandler));
    }

    public void openChannel(String channelID, SpeakVerifyRequestHandler requestHandler) {
        validateChannelID(channelID, SpeakVerifyRequestHandler.RESOURCE_TYPES);
        openChannel(channelID, new SpeakVerifyRequestDelegator(requestHandler));
    }

    public void openChannel(String channelID, RecorderRequestHandler requestHandler) {
        validateChannelID(channelID, RecorderRequestHandler.RESOURCE_TYPES);
        openChannel(channelID, new RecorderRequestDelegator(requestHandler));
    }

    private static void validateChannelID(String channelID, MrcpResourceType[] expected) {
        MrcpResourceType actual = MrcpResourceType.fromChannelID(channelID);
        for (MrcpResourceType type : expected) {
            if (type.equals(actual)) {
                return;
            }
        }

        throw new IllegalArgumentException(
            "Incorrect channel resource type for specified request handler: " + channelID
        );
    }

    private void openChannel(String channelID, MrcpRequestHandler requestHandler) {
        _requestProcessorImpl.registerRequestHandler(channelID, requestHandler);
    }

    public void closeChannel(String channelID) {
        _requestProcessorImpl.unregisterRequestHandler(channelID);
    }

    private static void addLogger(ServiceRegistry registry) {
        IoAcceptor acceptor = registry.getIoAcceptor(TransportType.SOCKET);
        acceptor.getFilterChain().addLast("logger", new IoTextLoggingFilter());
        _log.debug("Logging ON");
    }

    public static void main(String[] args) throws Exception {
        int port = 32416;
        String channelID = "32AECB23433801@speechrecog";

        MrcpServerSocket serverSocket = new MrcpServerSocket(port);
        serverSocket.openChannel(channelID, new BogusRequestHandler());

        if (_log.isDebugEnabled()) {
            _log.debug("MRCP server socket listening on port " + port);
        }
    }

    private static class BogusRequestHandler implements MrcpRequestHandler {

        public MrcpResponse handleRequest(MrcpRequest request, MrcpSession session) {
            MrcpResponse response = session.createResponse(MrcpResponse.STATUS_SUCCESS, MrcpRequestState.IN_PROGRESS);
            new BogusEventThread(session).start();
            return response;
        }

    }

    private static class BogusEventThread extends Thread {

        private MrcpSession _session;

        BogusEventThread(MrcpSession session) {
            _session = session;
        }

        @Override
        public void run() {

            MrcpEvent event = _session.createEvent(MrcpEventName.RECOGNITION_COMPLETE, MrcpRequestState.COMPLETE);

            try {
                _session.postEvent(event);
            } catch (TimeoutException e){
                _log.warn(e, e);
            }
        }

    }

}
