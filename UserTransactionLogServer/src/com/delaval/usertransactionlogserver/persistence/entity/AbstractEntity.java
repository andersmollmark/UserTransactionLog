package com.delaval.usertransactionlogserver.persistence.entity;

import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import simpleorm.dataset.SFieldScalar;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;

/**
 */
public abstract class AbstractEntity extends SRecordInstance {

    @Override
    public SRecordMeta<?> getMeta() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public abstract String getTablename();


    public SFieldScalar getPrimaryKey() {
        return getMeta().getPrimaryKeys()[0];
    }

    public String getPrimaryKeyValue() {
        return getString(getPrimaryKey());
    }

    public abstract void createUserTransactionId(WebSocketMessage webSocketMessage);

    protected static String getUserTransactionKeyId(WebSocketMessage webSocketMessage) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(webSocketMessage.getUsername().toLowerCase()).
                append(webSocketMessage.getClient()).
                append(webSocketMessage.getTarget());
        return stringBuilder.toString();
    }


}
