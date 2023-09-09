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
package com.mrcp.yxp.protocol.mrcp.mrcp4j.client;

import com.mrcp.yxp.protocol.mrcp.mrcp4j.message.MrcpEvent;

/**
 * The listener interface for receiving MRCPv2 event messages.  This interface should be
 * implemented by any class intended to be notified of any MRCP events received from
 * an MRCP resource by an {@link MrcpChannel} instance.
 *
 * @author Niels Godfredsen {@literal <}<a href="mailto:ngodfredsen@users.sourceforge.net">ngodfredsen@users.sourceforge.net</a>{@literal >}
 * @see MrcpChannel#addEventListener(MrcpEventListener)
 * @see MrcpChannel#removeEventListener(MrcpEventListener)
 */
public interface MrcpEventListener {

    /**
     * Called when an MRCP event is received from a MRCP resource.
     * @param event the event received from the MRCP resource
     */
    public void eventReceived(MrcpEvent event);

}
