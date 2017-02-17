package com.delaval.usertransactionlogserver.websocket;

/**
 * Created by delaval on 2016-08-31.
 */
public class JsonDumpMessage {

    String messType = MessTypes.JSON_DUMP.getMyValue();

    String jsondump;

    public void setJsondump(String json){
        jsondump = json;
    }
}
