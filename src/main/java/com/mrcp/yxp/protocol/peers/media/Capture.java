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

    Copyright 2008, 2009, 2010, 2011 Yohann Martineau
*/

package com.mrcp.yxp.protocol.peers.media;;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;

import com.mrcp.yxp.protocol.peers.Logger;


public class Capture implements Runnable {

    public static final int SAMPLE_SIZE = 16;
    public static final int BUFFER_SIZE = SAMPLE_SIZE * 20;

    private PipedOutputStream rawData;
    private volatile boolean isStopped;
    private SoundSource soundSource;
    private Logger logger;
    private CountDownLatch latch;

    public Capture(PipedOutputStream rawData, SoundSource soundSource,
            Logger logger, CountDownLatch latch) {
        this.rawData = rawData;
        this.soundSource = soundSource;
        this.logger = logger;
        this.latch = latch;
        isStopped = false;
    }

    public void run() {
        byte[] buffer;

        while (!isStopped) {
            buffer = soundSource.readData();
            try {
                if (buffer == null) {
                    break;
                }
                rawData.write(buffer);
                rawData.flush();
            } catch (IOException e) {
                logger.error("input/output error", e);
                return;
            }
        }
//        latch.countDown();
//        if (latch.getCount() != 0) {
//            try {
//                latch.await();
//            } catch (InterruptedException e) {
//                logger.error("interrupt exception", e);
//            }
//        }
    }

    public synchronized void setStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

}
