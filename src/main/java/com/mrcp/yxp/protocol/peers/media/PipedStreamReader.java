package com.mrcp.yxp.protocol.peers.media;

import com.mrcp.yxp.protocol.peers.Logger;

import java.io.*;

public class PipedStreamReader implements SoundSource {

    public final static int BUFFER_SIZE = 256;
    private Logger logger;
    private PipedInputStream pipedInputStream;

    public PipedStreamReader(PipedOutputStream out, Logger logger) {
        this.logger = logger;
        try {
            this.pipedInputStream = new PipedInputStream(out);
        } catch (IOException e) {
            logger.error("error", e);
            logger.error("pip stream error  " , e);
        }
    }

    public synchronized void close() {
        if (pipedInputStream != null) {
            try {
                pipedInputStream.close();
            } catch (IOException e) {
                logger.error("io exception", e);
            }
            pipedInputStream = null;
        }
    }

    @Override
    public synchronized byte[] readData() {
//        log.debug("start read yxp");
        if (pipedInputStream == null) {
            return null;
        }
        byte buffer[] = new byte[BUFFER_SIZE];
        try {
            if (pipedInputStream.read(buffer) >= 0) {
                Thread.sleep(15);
//                log.debug("read buffer"+buffer.toString());
                return buffer;
            } else {
                pipedInputStream.close();
                pipedInputStream = null;
            }
        } catch (IOException e) {
            logger.error("io exception", e);
        } catch (InterruptedException e) {
            logger.debug("file reader interrupted");
        }
        return null;
    }
}
