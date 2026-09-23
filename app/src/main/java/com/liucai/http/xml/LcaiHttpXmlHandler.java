package com.liucai.http.xml;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析指定根标签下每一个同级元素的属性。
 *
 * @author liucai
 */
public class LcaiHttpXmlHandler extends DefaultHandler {

    private final String targetName;
    private final List<Map<String, String>> maps = new ArrayList<>();

    @Nullable
    private Map<String, String> current;

    public LcaiHttpXmlHandler(@NonNull String targetName) {
        this.targetName = targetName;
    }

    @NonNull
    public List<Map<String, String>> getMaps() {
        return maps;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (targetName.equals(qName)) {
            Map<String, String> map = new HashMap<>();
            for (int i = 0; i < attributes.getLength(); i++) {
                map.put(attributes.getQName(i), attributes.getValue(i));
            }
            current = map;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (targetName.equals(qName) && current != null) {
            maps.add(current);
            current = null;
        }
    }
}