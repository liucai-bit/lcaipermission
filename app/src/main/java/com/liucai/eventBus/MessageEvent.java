package com.liucai.eventBus;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description
 * @Date 2026/7/15
 */
public class MessageEvent {
    public String msgId;

    public String msgContent;

    public MessageEvent(String msgId, String msgContent) {
        this.msgId = msgId;
        this.msgContent = msgContent;
    }
}
