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

package com.mrcp.yxp.protocol.peers.nat.api;

public abstract class PeersClient {

    /**
     * creates a new peers client
     * @param myId the string identifier corresponding
     *        to the computer or to a person (email).
     * @param dataReceiver object that will receive incoming traffic.
     */
    public PeersClient(String myId, DataReceiver dataReceiver) {

    }

    /**
     * creates a UDP connection to a peer.
     * @param peerId unique peer identifier (email for example).
     * @return an object that allows to send data to the peer.
     */
    public abstract UDPTransport createUDPTransport(String peerId);

    /**
     * creates a TCP connection to a peer.
     * @param peerId unique peer identifier (email for example).
     * @return an object that allows to send data to the peer.
     */
    public abstract TCPTransport createTCPTransport(String peerId);
}
