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

package com.mrcp.yxp.protocol.peers.sip.transactionuser;

import java.util.Collection;
import java.util.Hashtable;

import com.mrcp.yxp.protocol.peers.Logger;
import com.mrcp.yxp.protocol.peers.sip.RFC3261;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderFieldName;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderFieldValue;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaderParamName;
import com.mrcp.yxp.protocol.peers.sip.syntaxencoding.SipHeaders;
import com.mrcp.yxp.protocol.peers.sip.transport.SipMessage;
import com.mrcp.yxp.protocol.peers.sip.transport.SipResponse;


public class DialogManager {

    private Hashtable<String, Dialog> dialogs;
    private Logger logger;

    public DialogManager(Logger logger) {
        this.logger = logger;
        dialogs = new Hashtable<String, Dialog>();
    }

    /**
     * @param sipResponse sip response must contain a To tag, a
     *        From tag and a Call-ID
     * @return the new Dialog created
     */
    public synchronized Dialog createDialog(SipResponse sipResponse) {
        SipHeaders sipHeaders = sipResponse.getSipHeaders();
        String callID = sipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_CALLID)).toString();
        SipHeaderFieldValue from = sipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_FROM));
        SipHeaderFieldValue to = sipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_TO));
        String fromTag = from.getParam(new SipHeaderParamName(RFC3261.PARAM_TAG));
        String toTag = to.getParam(new SipHeaderParamName(RFC3261.PARAM_TAG));
        Dialog dialog;
        if (sipHeaders.get(new SipHeaderFieldName(RFC3261.HDR_VIA)) == null) {
            //createDialog is called from UAS side, in layer Transaction User
            dialog = new Dialog(callID, toTag, fromTag, logger);
        } else {
            //createDialog is called from UAC side, in syntax encoding layer
            dialog = new Dialog(callID, fromTag, toTag, logger);
        }
        dialogs.put(dialog.getId(), dialog);
        return dialog;
    }

    public void removeDialog(String dialogId) {
        dialogs.remove(dialogId);
    }

    public synchronized Dialog getDialog(SipMessage sipMessage) {
        SipHeaders sipHeaders = sipMessage.getSipHeaders();
        String callID = sipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_CALLID)).toString();
        SipHeaderFieldValue from = sipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_FROM));
        SipHeaderFieldValue to = sipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_TO));
        SipHeaderParamName tagName = new SipHeaderParamName(RFC3261.PARAM_TAG);
        String fromTag = from.getParam(tagName);
        String toTag = to.getParam(tagName);
        Dialog dialog = dialogs.get(getDialogId(callID, fromTag, toTag));
        if (dialog != null) {
            return dialog;
        }
        return dialogs.get(getDialogId(callID, toTag, fromTag));
    }

    public synchronized Dialog getDialog(String callId) {
        for (Dialog dialog : dialogs.values()) {
            if (dialog.getCallId().equals(callId)) {
                return dialog;
            }
        }
        return null;
    }

    private String getDialogId(String callID, String localTag, String remoteTag) {
        StringBuffer buf = new StringBuffer();
        buf.append(callID);
        buf.append(Dialog.ID_SEPARATOR);
        buf.append(localTag);
        buf.append(Dialog.ID_SEPARATOR);
        buf.append(remoteTag);
        return buf.toString();
    }

    public Collection<Dialog> getDialogCollection() {
        return dialogs.values();
    }
}
