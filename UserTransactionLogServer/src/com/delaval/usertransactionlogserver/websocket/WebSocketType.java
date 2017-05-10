package com.delaval.usertransactionlogserver.websocket;

/**
 * Dataholder for the messagetype from the webclient
 */
public class WebSocketType {

    private String messType;


    public void setMessType(String messType) {
        this.messType = messType;
    }

    public String getMessType() {
        return messType;
    }

    public String getType() {

        return messType;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder(this.getClass().getCanonicalName());
        sb.append(", Messtype:").append(getType());
        return sb.toString();
    }
}
