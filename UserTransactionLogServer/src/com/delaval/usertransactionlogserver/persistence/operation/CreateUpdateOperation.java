package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;

/**
 * Created by delaval on 1/18/2016.
 */
public interface CreateUpdateOperation extends Operation {
    void setMessage(WebSocketMessage message);

    WebSocketMessage getWebSocketMessage();

    @Override
    default boolean isCreateUpdate(){
        return true;
    }

    default String getMesstype() {
        return getWebSocketMessage() != null ? getWebSocketMessage().getMessType() : null;
    }


}
