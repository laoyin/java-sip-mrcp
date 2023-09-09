package com.mrcp.yxp.protocol.socketclient;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class UmsCollection {
    private static ConcurrentHashMap<String, Integer> umsDict = new ConcurrentHashMap<String, Integer>();

    public static void pubUmsDict(String port, Integer flag){
        umsDict.put(port, flag);
    }

    public static Integer getUmsDict(String port){
        return umsDict.get(port);
    }
}
