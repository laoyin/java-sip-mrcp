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

public class InviteServerTransactionStateCompleted extends
        InviteServerTransactionState {

    public InviteServerTransactionStateCompleted(String id,
            InviteServerTransaction inviteServerTransaction, Logger logger) {
        super(id, inviteServerTransaction, logger);
    }

    @Override
    public void timerGFires() {
        InviteServerTransactionState nextState = inviteServerTransaction.COMPLETED;
        inviteServerTransaction.setState(nextState);
        inviteServerTransaction.sendLastResponse();
        long delay = (long)Math.pow(2,
                ++inviteServerTransaction.nbRetrans) * RFC3261.TIMER_T1;
        inviteServerTransaction.timer.schedule(
                inviteServerTransaction.new TimerG(),
                Math.min(delay, RFC3261.TIMER_T2));
    }

    @Override
    public void timerHFiresOrTransportError() {
        InviteServerTransactionState nextState = inviteServerTransaction.TERMINATED;
        inviteServerTransaction.setState(nextState);
        inviteServerTransaction.serverTransactionUser.transactionFailure();
    }

    @Override
    public void receivedAck() {
        InviteServerTransactionState nextState = inviteServerTransaction.CONFIRMED;
        inviteServerTransaction.setState(nextState);
        int delay;
        if (RFC3261.TRANSPORT_UDP.equals(inviteServerTransaction.transport)) {
            delay = RFC3261.TIMER_T4;
        } else {
            delay = 0;
        }
        inviteServerTransaction.timer.schedule(
                inviteServerTransaction.new TimerI(), delay);
    }

    @Override
    public void receivedInvite() {
        InviteServerTransactionState nextState = inviteServerTransaction.COMPLETED;
        inviteServerTransaction.setState(nextState);
        // retransmission
        inviteServerTransaction.sendLastResponse();
    }

}
