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

    Copyright 2007-2013 Yohann Martineau
*/

package com.mrcp.yxp.protocol.peers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mrcp.yxp.protocol.peers.sip.Utils;

public class FileLogger implements Logger {

    public final static String LOG_FILE = File.separator + "logs"
        + File.separator + "peers.log";
    public final static String NETWORK_FILE = File.separator + "logs"
        + File.separator + "transport.log";

    private PrintWriter logWriter;
    private PrintWriter networkWriter;
    private Object logMutex;
    private Object networkMutex;
    private SimpleDateFormat logFormatter;
    private SimpleDateFormat networkFormatter;
    private boolean genLogFlag = true;

    public FileLogger(String peersHome) {
        if (peersHome == null) {
            peersHome = Utils.DEFAULT_PEERS_HOME;
        }
//        if(peersHome.equals(Utils.DEFAULT_PEERS_HOME)){
//            this.genLogFlag = false;
//            return;
//        }
        try {
            logWriter = new PrintWriter(new BufferedWriter(
                    new FileWriter(peersHome + LOG_FILE)));
            networkWriter = new PrintWriter(new BufferedWriter(
                    new FileWriter(peersHome + NETWORK_FILE)));
        } catch (IOException e) {
            //log.debug("logging to stdout");
            logWriter = new PrintWriter(System.out);
            networkWriter = new PrintWriter(System.out);
        }
        logMutex = new Object();
        networkMutex = new Object();
        logFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        networkFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    }

    @Override
    public final void debug(String message) {
        if(!genLogFlag){
            return;
        }
        synchronized (logMutex) {
            logWriter.write(genericLog(message.toString(), "DEBUG"));
            logWriter.flush();
        }
    }

    @Override
    public final void info(String message) {
        if(!genLogFlag){
            return;
        }
        synchronized (logMutex) {
            logWriter.write(genericLog(message.toString(), "INFO "));
            logWriter.flush();
        }
    }

    @Override
    public final void error(String message) {
        if(!genLogFlag){
            return;
        }
        synchronized (logMutex) {
            logWriter.write(genericLog(message.toString(), "ERROR"));
            logWriter.flush();
        }
    }

    @Override
    public final void error(String message, Exception exception) {
        if(!genLogFlag){
            return;
        }
        synchronized (logMutex) {
            logWriter.write(genericLog(message, "ERROR"));
            exception.printStackTrace(logWriter);
            logWriter.flush();
        }
    }

    private final String genericLog(String message, String level) {
        if(!genLogFlag){
            return "";
        }
        StringBuffer buf = new StringBuffer();
        buf.append(logFormatter.format(new Date()));
        buf.append(" ");
        buf.append(level);
        buf.append(" [");
        buf.append(Thread.currentThread().getName());
        buf.append("] ");
        buf.append(message);
        buf.append("\n");
        return buf.toString();
    }

    @Override
    public final void traceNetwork(String message, String direction) {
        if(!genLogFlag){
            return;
        }
        synchronized (networkMutex) {
            StringBuffer buf = new StringBuffer();
            buf.append(networkFormatter.format(new Date()));
            buf.append(" ");
            buf.append(direction);
            buf.append(" [");
            buf.append(Thread.currentThread().getName());
            buf.append("]\n\n");
            buf.append(message);
            buf.append("\n");
            networkWriter.write(buf.toString());
            networkWriter.flush();
        }
    }

}
