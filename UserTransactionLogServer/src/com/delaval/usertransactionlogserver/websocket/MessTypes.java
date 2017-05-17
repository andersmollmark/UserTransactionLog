package com.delaval.usertransactionlogserver.websocket;

/**
 * Different types of messages that can enter this application through a websocket.
 */
public enum MessTypes {

    IDLE_POLL("IdlePoll"),
    CLICK_LOG("clickLog"),
    SYSTEM_PROPERTY("systemProperty"),
    JSON_DUMP("jsonDump"),
    GET_PUBLIC_KEY("getPublicKey"),
    FETCH_ENCRYPTED_LOGS("fetchLogs"),
    BACKUP("backup"),
    EVENT_LOG("eventLog"),
    UNDEFINED("undefined");

    private String myValue;

    MessTypes(String value){
        myValue = value;
    }

    public String getMyValue(){
        return myValue;
    }


    public boolean isSame(String value){
        return myValue.equals(value);
    }

    public static MessTypes getType(String value){
        for(MessTypes type: MessTypes.values()){
            if(type.getMyValue().equals(value)){
                return type;
            }
        }
        return UNDEFINED;
    }
}
