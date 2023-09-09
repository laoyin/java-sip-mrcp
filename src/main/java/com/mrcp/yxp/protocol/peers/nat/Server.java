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

package com.mrcp.yxp.protocol.peers.nat;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.LogManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Server {
    private static org.apache.log4j.Logger log = LogManager.getLogger(Server.class);

    public static final String SERVER_HOST = "peers.sourceforge.net";
    public static final String PREFIX = "/peers";
    //public static final int SOCKET_TIMEOUT = 30000;//millis

    //private InetAddress localAddress;
    //private int localPort;
    private InetAddress remoteAddress;
    private int remotePort;

    private Socket socket;

    //TODO constructor without parameters
    public Server(InetAddress localAddress, int localPort) throws IOException {
        super();
        //this.localAddress = localAddress;
        //this.localPort = localPort;
        this.remoteAddress = InetAddress.getByName(SERVER_HOST);
        this.remotePort = 80;
        socket = new Socket(remoteAddress, remotePort, localAddress, localPort);
        //socket.setSoTimeout(SOCKET_TIMEOUT);
    }

    /**
     * This method will update public address on the web server.
     * @param email user identifier
     */
    public void update(String email) {
        String encodedEmail;
        try {
            encodedEmail = URLEncoder.encode(email, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        StringBuffer urlEnd = new StringBuffer();
        urlEnd.append("update2.php?email=");
        urlEnd.append(encodedEmail);
        get(urlEnd.toString());
        close();
    }

    public Document getPeers(String email) {
        String encodedEmail;
        try {
            encodedEmail = URLEncoder.encode(email, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        StringBuffer urlBuf = new StringBuffer();
        urlBuf.append("http://");
        urlBuf.append(SERVER_HOST);
        urlBuf.append(PREFIX);
        urlBuf.append("/getassocasxml.php?email=");
        urlBuf.append(encodedEmail);
        URL url;
        try {
            url = new URL(urlBuf.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        log.debug("retrieved peers");
        DocumentBuilderFactory documentBuilderFactory
            = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        try {
            URLConnection urlConnection = url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            return documentBuilder.parse(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String get(String urlEnd) {
        StringBuffer get = new StringBuffer();
        get.append("GET ");
        get.append(PREFIX);
        get.append('/');
        get.append(urlEnd);
        get.append(" HTTP/1.1\r\n");
        get.append("Host: ");
        get.append(SERVER_HOST);
        get.append("\r\n");
        get.append("\r\n");

        try {
            socket.getOutputStream().write(get.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        log.debug("> sent:\n" + get.toString());

        StringBuffer result = new StringBuffer();
        try {
            byte[] buf = new byte[256];
            int read = 0;
            while ((read = socket.getInputStream().read(buf)) > -1) {
                byte[] exactBuf = new byte[read];
                System.arraycopy(buf, 0, exactBuf, 0, read);
                result.append(new String(exactBuf));
            }
        } catch (SocketTimeoutException e) {
            log.debug("socket timeout");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        log.debug("< received:\n" + result.toString());
        return result.toString();
    }

    public void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
