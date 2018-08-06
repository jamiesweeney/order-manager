package org.m3.js.Messages;

public class FailedMessage extends Message {

    private String failText;

    public FailedMessage(String text) {
        this.failText = text;
    }

    public String getText(){
        return failText;
    }
}
