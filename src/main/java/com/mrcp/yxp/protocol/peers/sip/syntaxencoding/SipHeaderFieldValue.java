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

package com.mrcp.yxp.protocol.peers.sip.syntaxencoding;

import java.util.HashMap;

import com.mrcp.yxp.protocol.peers.sip.RFC3261;


public class SipHeaderFieldValue {

    private String value;

    private HashMap<SipHeaderParamName, String> params;

    public SipHeaderFieldValue(String value) {
        int startPos = value.indexOf(RFC3261.RIGHT_ANGLE_BRACKET);
        int pos;
        if (startPos > -1) {
            pos = value.indexOf(RFC3261.PARAM_SEPARATOR, startPos);
        } else {
            pos = value.indexOf(RFC3261.PARAM_SEPARATOR);
        }
        String paramsString;
        if (pos > -1) {
            this.value = value.substring(0,pos);
            paramsString = value.substring(pos);
        } else {
            this.value = value;
            paramsString = "";
        }
        params = new HashMap<SipHeaderParamName, String>();
        if (paramsString.contains(RFC3261.PARAM_SEPARATOR)) {
            String[] arr = paramsString.split(RFC3261.PARAM_SEPARATOR);
            if (arr.length > 1) {
                for (int i = 1; i < arr.length; ++i) {
                    String paramName = arr[i];
                    String paramValue = "";
                    pos = paramName.indexOf(RFC3261.PARAM_ASSIGNMENT);
                    if (pos > -1) {
                        paramName = arr[i].substring(0, pos);
                        paramValue = arr[i].substring(pos + 1);
                    }
                    params.put(new SipHeaderParamName(paramName), paramValue);
                }
            }
        }
    }

    public String getParam(SipHeaderParamName name) {
        return params.get(name);
    }

    public void addParam(SipHeaderParamName name, String value) {
        params.put(name, value);
    }

    public void removeParam(SipHeaderParamName name) {
        params.remove(name);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        if (params == null || params.isEmpty()) {
            return value;
        }
        StringBuffer buf = new StringBuffer(value);
        for (SipHeaderParamName name: params.keySet()) {
            buf.append(RFC3261.PARAM_SEPARATOR).append(name);
            String value = params.get(name);
            if (!"".equals(value.trim())) {
                buf.append(RFC3261.PARAM_ASSIGNMENT).append(value);
            }
        }
        return buf.toString();
    }

}
