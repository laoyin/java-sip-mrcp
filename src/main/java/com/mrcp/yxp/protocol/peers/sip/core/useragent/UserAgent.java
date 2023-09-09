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

import java.io.File;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import com.mrcp.yxp.protocol.mrcp.mrcp4j.client.MrcpChannel;
import com.mrcp.yxp.protocol.peers.Config;
import com.mrcp.yxp.protocol.peers.FileLogger;
import com.mrcp.yxp.protocol.peers.Logger;
import com.mrcp.yxp.protocol.peers.XmlConfig;
import com.mrcp.yxp.protocol.peers.media.AbstractSoundManager;
import com.mrcp.yxp.protocol.peers.media.Echo;
import com.mrcp.yxp.protocol.peers.media.MediaManager;
import com.mrcp.yxp.protocol.peers.media.MediaMode;
import com.mrcp.yxp.protocol.peers.sdp.SDPManager;
import com.mrcp.yxp.protocol.peers.sip.Utils;
import com.mrcp.yxp.protocol.peers.sip.core.useragent.handlers.ByeHandler;
import com.mrcp.yxp.protocol.peers.sip.core.useragent.handlers.CancelHandler;
import com.mrcp.yxp.protocol.peers.sip.core.useragent.handlers.InviteHandler;
import com.mrcp.yxp.protocol.peers.sip.core.useragent.handlers.OptionsHandler;
import com.mrcp.yxp.protocol.peers.sip.core.useragent.handlers.RegisterHandler;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipURI;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipUriSyntaxException;
import com.mrcp.yxp.protocol.peers.sip.transaction.Transaction;
import com.mrcp.yxp.protocol.peers.sip.transaction.TransactionManager;
import com.mrcp.yxp.protocol.peers.sip.transactionuser.Dialog;
import com.mrcp.yxp.protocol.peers.sip.transactionuser.DialogManager;
import com.mrcp.yxp.protocol.peers.sip.transport.SipMessage;
import com.mrcp.yxp.protocol.peers.sip.transport.SipRequest;
import com.mrcp.yxp.protocol.peers.sip.transport.SipResponse;
import com.mrcp.yxp.protocol.peers.sip.transport.TransportManager;


public class UserAgent {

    public final static String CONFIG_FILE = "conf" + File.separator + "peers.xml";
    public final static int RTP_DEFAULT_PORT = 8000;

    private String peersHome;
    private Logger logger;
    private Config config;

    private List<String> peers;
    //private List<Dialog> dialogs;

    //TODO factorize echo and captureRtpSender
    private Echo echo;

    private UAC uac;
    private UAS uas;

    private ChallengeManager challengeManager;

    private DialogManager dialogManager;
    private TransactionManager transactionManager;
    private TransportManager transportManager;

    private int cseqCounter;
    private SipListener sipListener;

    private SDPManager sdpManager;
    private AbstractSoundManager soundManager;
    private MediaManager mediaManager;

    private int mrcpStatus;
    private MrcpChannel mrcpChannel;
    private InviteHandler timerInvitehandler;

    public String getGarmmar() {
        return garmmar;
    }

    public void setGarmmar(String garmmar) {
        this.garmmar = garmmar;
    }

    private String garmmar;

    public InviteHandler getTimerInvitehandler() {
        return timerInvitehandler;
    }

    public MrcpChannel getMrcpChannel() {
        return mrcpChannel;
    }

    public void setMrcpChannel(MrcpChannel mrcpChannel) {
        this.mrcpChannel = mrcpChannel;
    }



    public int getMrcpStatus() {
        return mrcpStatus;
    }

    public void setMrcpStatus(int mrcpStatus) {
        this.mrcpStatus = mrcpStatus;
    }



    public UserAgent(SipListener sipListener, String peersHome,
            Logger logger, AbstractSoundManager soundManager)
                    throws SocketException {
        this(sipListener, null, peersHome, logger, soundManager);
    }

    public UserAgent(SipListener sipListener, Config config,
            Logger logger, AbstractSoundManager soundManager)
                    throws SocketException {
        this(sipListener, config, null, logger, soundManager);
    }

    private UserAgent(SipListener sipListener, Config config, String peersHome,
            Logger logger, AbstractSoundManager soundManager)
                    throws SocketException {
        this.sipListener = sipListener;
        if (peersHome == null) {
            peersHome = Utils.DEFAULT_PEERS_HOME;
        }
        this.peersHome = peersHome;
        if (logger == null) {
            logger = new FileLogger(this.peersHome);
        }
        this.logger = logger;
        if (config == null) {
            config = new XmlConfig(this.peersHome + File.separator
                    + CONFIG_FILE, this.logger);
        }
        this.config = config;

        cseqCounter = 1;

        StringBuffer buf = new StringBuffer();
        buf.append("starting user agent [");
        buf.append("myAddress: ");
        buf.append(config.getLocalInetAddress().getHostAddress()).append(", ");
        buf.append("sipPort: ");
        buf.append(config.getSipPort()).append(", ");
        buf.append("userpart: ");
        buf.append(config.getUserPart()).append(", ");
        buf.append("domain: ");
        buf.append(config.getDomain()).append("]");
        logger.info(buf.toString());

        //transaction user

        dialogManager = new DialogManager(logger);

        //transaction

        transactionManager = new TransactionManager(logger);

        //transport

        transportManager = new TransportManager(transactionManager,
                config, logger);

        transactionManager.setTransportManager(transportManager);

        //core

        InviteHandler inviteHandler = new InviteHandler(this,
                dialogManager,
                transactionManager,
                transportManager,
                logger);
        CancelHandler cancelHandler = new CancelHandler(this,
                dialogManager,
                transactionManager,
                transportManager,
                logger);
        ByeHandler byeHandler = new ByeHandler(this,
                dialogManager,
                transactionManager,
                transportManager,
                logger);
        OptionsHandler optionsHandler = new OptionsHandler(this,
                transactionManager,
                transportManager,
                logger);
        RegisterHandler registerHandler = new RegisterHandler(this,
                transactionManager,
                transportManager,
                logger);

        InitialRequestManager initialRequestManager =
            new InitialRequestManager(
                this,
                inviteHandler,
                cancelHandler,
                byeHandler,
                optionsHandler,
                registerHandler,
                dialogManager,
                transactionManager,
                transportManager,
                logger);
        MidDialogRequestManager midDialogRequestManager =
            new MidDialogRequestManager(
                this,
                inviteHandler,
                cancelHandler,
                byeHandler,
                optionsHandler,
                registerHandler,
                dialogManager,
                transactionManager,
                transportManager,
                logger);

        uas = new UAS(this,
                initialRequestManager,
                midDialogRequestManager,
                dialogManager,
                transactionManager,
                transportManager);

        uac = new UAC(this,
                initialRequestManager,
                midDialogRequestManager,
                dialogManager,
                transactionManager,
                transportManager,
                logger);

        challengeManager = new ChallengeManager(config,
                initialRequestManager,
                midDialogRequestManager,
                dialogManager,
                logger);
        registerHandler.setChallengeManager(challengeManager);
        inviteHandler.setChallengeManager(challengeManager);
        byeHandler.setChallengeManager(challengeManager);

        peers = new ArrayList<String>();
        //dialogs = new ArrayList<Dialog>();

        sdpManager = new SDPManager(this, logger);
        inviteHandler.setSdpManager(sdpManager);
        optionsHandler.setSdpManager(sdpManager);
        // soundManager  = new SoundManager(config.isMediaDebug(), logger,
        // this.peersHome);
        this.soundManager = soundManager;
        mediaManager = new MediaManager(this, logger);

        this.timerInvitehandler = inviteHandler;
    }

    // client methods

    public void close() {
        transportManager.closeTransports();
        config.setPublicInetAddress(null);
    }

    public SipRequest register() throws SipUriSyntaxException {
        return uac.register();
    }

    public void unregister() throws SipUriSyntaxException {
        uac.unregister();
    }

    public SipRequest invite(String requestUri, String callId)
            throws SipUriSyntaxException {
        return uac.invite(requestUri, callId);
    }

    public void terminate(SipRequest sipRequest) {
        uac.terminate(sipRequest);
    }

    public void acceptCall(SipRequest sipRequest, Dialog dialog) {
        uas.acceptCall(sipRequest, dialog);
    }

    public void rejectCall(SipRequest sipRequest) {
        uas.rejectCall(sipRequest);
    }


    /**
     * Gives the sipMessage if sipMessage is a SipRequest or
     * the SipRequest corresponding to the SipResponse
     * if sipMessage is a SipResponse
     * @param sipMessage
     * @return null if sipMessage is neither a SipRequest neither a SipResponse
     */
    public SipRequest getSipRequest(SipMessage sipMessage) {
        if (sipMessage instanceof SipRequest) {
            return (SipRequest) sipMessage;
        } else if (sipMessage instanceof SipResponse) {
            SipResponse sipResponse = (SipResponse) sipMessage;
            Transaction transaction = (Transaction)transactionManager
                .getClientTransaction(sipResponse);
            if (transaction == null) {
                transaction = (Transaction)transactionManager
                    .getServerTransaction(sipResponse);
            }
            if (transaction == null) {
                return null;
            }
            return transaction.getRequest();
        } else {
            return null;
        }
    }

//    public List<Dialog> getDialogs() {
//        return dialogs;
//    }

    public List<String> getPeers() {
        return peers;
    }

//    public Dialog getDialog(String peer) {
//        for (Dialog dialog : dialogs) {
//            String remoteUri = dialog.getRemoteUri();
//            if (remoteUri != null) {
//                if (remoteUri.contains(peer)) {
//                    return dialog;
//                }
//            }
//        }
//        return null;
//    }

    public String generateCSeq(String method) {
        StringBuffer buf = new StringBuffer();
        buf.append(cseqCounter++);
        buf.append(' ');
        buf.append(method);
        return buf.toString();
    }

    public boolean isRegistered() {
        return uac.getInitialRequestManager().getRegisterHandler()
            .isRegistered();
    }

    public UAS getUas() {
        return uas;
    }

    public UAC getUac() {
        return uac;
    }

    public DialogManager getDialogManager() {
        return dialogManager;
    }

    public int getSipPort() {
        return transportManager.getSipPort();
    }

    public int getRtpPort() {
        return config.getRtpPort();
    }

    public String getDomain() {
        return config.getDomain();
    }

    public String getUserpart() {
        return config.getUserPart();
    }

    public MediaMode getMediaMode() {
        return config.getMediaMode();
    }

    public boolean isMediaDebug() {
        return config.isMediaDebug();
    }

    public SipURI getOutboundProxy() {
        return config.getOutboundProxy();
    }

    public Echo getEcho() {
        return echo;
    }

    public void setEcho(Echo echo) {
        this.echo = echo;
    }

    public SipListener getSipListener() {
        return sipListener;
    }

    public AbstractSoundManager getSoundManager() {
        return soundManager;
    }

    public MediaManager getMediaManager() {
        return mediaManager;
    }

    public Config getConfig() {
        return config;
    }

    public String getPeersHome() {
        return peersHome;
    }

    public TransportManager getTransportManager() {
        return transportManager;
    }

    public TransactionManager getTransactionManager() {return  transactionManager;}


    public void hangup(SipRequest sipRequest) throws InterruptedException {
        this.terminate(sipRequest);
        this.getTransportManager().closeTransports();
        this.getMediaManager().stopSession();
        this.getMrcpChannel().stopChannel();
        try{
            Thread.sleep(100);
        }catch (Exception e){
            System.out.println(e.getStackTrace());
        }
        this.getTransactionManager().stopTimer();
        this.getTimerInvitehandler().stopTimer();
        this.getConfig().getAsrQueue().put("STOP");
    }
}
