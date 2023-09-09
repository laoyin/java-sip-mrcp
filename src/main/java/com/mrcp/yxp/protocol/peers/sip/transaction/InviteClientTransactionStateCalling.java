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


public class InviteClientTransactionStateCalling extends InviteClientTransactionState {

    public InviteClientTransactionStateCalling(String id,
            InviteClientTransaction inviteClientTransaction, Logger logger) {
        super(id, inviteClientTransaction, logger);
    }

    @Override
    public void timerAFires() {
        InviteClientTransactionState nextState = inviteClientTransaction.CALLING;
        inviteClientTransaction.setState(nextState);
        inviteClientTransaction.sendRetrans();
    }

    @Override
    public void timerBFires() {
        timerBFiresOrTransportError();
    }

    @Override
    public void transportError() {
        timerBFiresOrTransportError();
    }

    private void timerBFiresOrTransportError() {
        InviteClientTransactionState nextState = inviteClientTransaction.TERMINATED;
        inviteClientTransaction.setState(nextState);
        inviteClientTransaction.transactionUser.transactionTimeout(
                inviteClientTransaction);
    }

    @Override
    public void received2xx() {
        InviteClientTransactionState nextState = inviteClientTransaction.TERMINATED;
        inviteClientTransaction.setState(nextState);
        inviteClientTransaction.transactionUser.successResponseReceived(
                inviteClientTransaction.getLastResponse(), inviteClientTransaction);
    }

    @Override
    public void received1xx() {
        InviteClientTransactionState nextState = inviteClientTransaction.PROCEEDING;
        inviteClientTransaction.setState(nextState);
        inviteClientTransaction.transactionUser.provResponseReceived(
                inviteClientTransaction.getLastResponse(), inviteClientTransaction);
    }

    @Override
    public void received300To699() {
        InviteClientTransactionState nextState = inviteClientTransaction.COMPLETED;
        inviteClientTransaction.setState(nextState);
        inviteClientTransaction.createAndSendAck();
        inviteClientTransaction.transactionUser.errResponseReceived(
                inviteClientTransaction.getLastResponse());
    }


}
