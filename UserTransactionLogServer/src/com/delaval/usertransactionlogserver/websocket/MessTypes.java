package com.delaval.usertransactionlogserver.websocket;

/**
 * Different types of messages that can enter this application through a websocket.
 */
public enum MessTypes {

    AUTHORIZE_REQ("AuthorizeReq"),
    IDLE_POLL("IdlePoll"),
    SYSTEM_PROPERTY("systemProperty"),
    JSON_DUMP("jsonDump"),
    GET_PUBLIC_KEY("getPublicKey"),
    FETCH_ENCRYPTED_LOGS("fetchLogs"),
    FETCH_ENCRYPTED_LOGS_LAST_DAY("fetchLogsLastDay"),
    FETCH_ENCRYPTED_LOGS_WITH_TIMEZONE("fetchLogsWithTimezone"),
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
