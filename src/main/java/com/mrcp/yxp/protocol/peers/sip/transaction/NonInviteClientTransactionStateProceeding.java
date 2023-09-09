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

import com.mrcp.yxp.protocol.peers.Logger;
import com.mrcp.yxp.protocol.peers.sip.RFC3261;
import com.mrcp.yxp.protocol.peers.sip.transport.SipResponse;

public class NonInviteClientTransactionStateProceeding extends
        NonInviteClientTransactionState {

    public NonInviteClientTransactionStateProceeding(String id,
            NonInviteClientTransaction nonInviteClientTransaction,
            Logger logger) {
        super(id, nonInviteClientTransaction, logger);
    }

    @Override
    public void timerEFires() {
        NonInviteClientTransactionState nextState = nonInviteClientTransaction.PROCEEDING;
        nonInviteClientTransaction.setState(nextState);
        ++nonInviteClientTransaction.nbRetrans;
        nonInviteClientTransaction.sendRetrans(RFC3261.TIMER_T2);
    }

    @Override
    public void timerFFires() {
        timerFFiresOrTransportError();
    }

    @Override
    public void transportError() {
        timerFFiresOrTransportError();
    }

    private void timerFFiresOrTransportError() {
        NonInviteClientTransactionState nextState = nonInviteClientTransaction.TERMINATED;
        nonInviteClientTransaction.setState(nextState);
        nonInviteClientTransaction.transactionUser.transactionTimeout(
                nonInviteClientTransaction);
    }

    @Override
    public void received1xx() {
        NonInviteClientTransactionState nextState = nonInviteClientTransaction.PROCEEDING;
        nonInviteClientTransaction.setState(nextState);
    }

    @Override
    public void received200To699() {
        NonInviteClientTransactionState nextState = nonInviteClientTransaction.COMPLETED;
        nonInviteClientTransaction.setState(nextState);
        SipResponse response = nonInviteClientTransaction.getLastResponse();
        int code = response.getStatusCode();
        if (code < RFC3261.CODE_MIN_REDIR) {
            nonInviteClientTransaction.transactionUser.successResponseReceived(
                    response, nonInviteClientTransaction);
        } else {
            nonInviteClientTransaction.transactionUser.errResponseReceived(
                    response);
        }
    }

}
