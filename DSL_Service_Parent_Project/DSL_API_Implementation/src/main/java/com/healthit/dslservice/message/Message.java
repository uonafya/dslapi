package com.healthit.dslservice.message;

/**
 * Hold message and status types
 *
 * @author duncan
 */
public class Message {

    private String messageType;
    private String mesageContent;

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMesageContent() {
        return mesageContent;
    }

    public void setMesageContent(String mesageContent) {
        this.mesageContent = mesageContent;
    }

}
