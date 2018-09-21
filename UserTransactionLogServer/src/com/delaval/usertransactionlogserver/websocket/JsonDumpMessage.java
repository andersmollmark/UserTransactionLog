package com.delaval.usertransactionlogserver.websocket;

/**
 * Created by delaval on 2016-08-31.
 */
public class JsonDumpMessage {

    String messType;
    String timezoneId;

    String jsondump;


    public JsonDumpMessage(MessTypes messTypes){
        messType = messTypes.getMyValue();
    }

    public void setJsondump(String json){
        jsondump = json;
    }

    public void setTimezoneId(String timezoneId) {
        this.timezoneId = timezoneId;
    }

}
