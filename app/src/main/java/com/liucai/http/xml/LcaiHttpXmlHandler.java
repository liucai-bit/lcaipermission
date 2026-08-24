package com.liucai.http.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liucai
 * @program lcpermission
 * @description RxHttpXmlHandler
 * @Date 2025-08-26 14:39
 **/
public class LcaiHttpXmlHandler extends DefaultHandler {

    private String xmlName;
    Map<String, String> map;
    List<Map<String, String>> maps;

    public LcaiHttpXmlHandler(String xmlName) {
        this.xmlName =xmlName;
        this.map = new HashMap();
    }

    public List<Map<String, String>> getMaps() {
        return this.maps;
    }

    public void startDocument() throws SAXException {
        this.maps = new ArrayList();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.map = new HashMap();
        if (this.xmlName.equals(qName)) {
            for(int i = 0; i < attributes.getLength(); ++i) {
                String qname = attributes.getQName(i);
                String value = attributes.getValue(qname);
                this.map.put(qname, value);
            }
        }

    }

    public void characters(char[] ch, int start, int length) throws SAXException {
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (this.map != null) {
            this.maps.add(this.map);
        }

        this.map = null;
    }
}
