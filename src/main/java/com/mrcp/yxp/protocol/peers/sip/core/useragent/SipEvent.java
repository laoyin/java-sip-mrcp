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

    Copyright 2007, 2008, 2009, 2010, 2011 Yohann Martineau
*/

package com.mrcp.yxp.protocol.peers.sip.core.useragent;

import com.mrcp.yxp.protocol.peers.sip.transport.SipMessage;

public class SipEvent {

    public enum EventType {
        ERROR, RINGING, INCOMING_CALL, CALLEE_PICKUP;
    }

    private EventType eventType;
    private SipMessage sipMessage;

    public SipEvent(EventType type, SipMessage sipMessage) {
        this.eventType = type;
        this.sipMessage = sipMessage;
    }

    public SipMessage getSipMessage() {
        return sipMessage;
    }

    public EventType getEventType() {
        return eventType;
    }

}
