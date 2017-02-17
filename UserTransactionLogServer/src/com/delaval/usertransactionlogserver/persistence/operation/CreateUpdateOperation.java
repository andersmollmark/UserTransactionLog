package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;

/**
 * Created by delaval on 1/18/2016.
 */
public interface CreateUpdateOperation extends Operation {
    void setMessage(WebSocketMessage message);

}
