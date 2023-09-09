package com.mrcp.yxp.protocol.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class XmlParse {
    private static Logger log = LogManager.getLogger(XmlParse.class);
    static public String readAsrData(String asrxml) {
        if(asrxml==null || "null".equals(asrxml.trim()) || "".equals(asrxml.trim())){
            return "";
        }
        //log.debug("asrxml=" + asrxml);
        Document doc = null;
        try {
            doc = DocumentHelper.parseText(asrxml);
        } catch (DocumentException e) {
            log.error("asrxml=" + asrxml, e);
        }
        Element rootElt = doc.getRootElement();
        //Element memberElm = rootElt.element("result");
        Element interpretation = rootElt.element("interpretation");
        return interpretation.elementText("input");
    }
}
