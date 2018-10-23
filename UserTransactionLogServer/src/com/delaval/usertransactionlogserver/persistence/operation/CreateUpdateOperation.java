package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;

/**
 * Implement this when creating operations that does create or update operations towards db
 */
public interface CreateUpdateOperation extends Operation {
    void setMessage(WebSocketMessage message);

    WebSocketMessage getWebSocketMessage();

    /**
     * @see Operation#isCreateUpdate()
     */
    @Override
    default boolean isCreateUpdate(){
        return true;
    }

    default String getMesstype() {
        return getWebSocketMessage() != null ? getWebSocketMessage().getMessType() : null;
    }


}
