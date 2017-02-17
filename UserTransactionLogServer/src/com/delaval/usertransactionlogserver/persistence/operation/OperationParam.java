package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;

/**
 * Created by delaval on 1/18/2016.
 */
public class OperationParam<T extends Operation> {

    private WebSocketMessage webSocketMessage;
    private String parameter;
    private Class<T> operationClass;


    public OperationParam(Class<T> clazz, WebSocketMessage webSocketMessage){
        this.webSocketMessage = webSocketMessage;
        operationClass = clazz;
    }

    public OperationParam(Class<T> clazz){
        operationClass = clazz;
    }

    public Class<T> getOperationClass() {
        return operationClass;
    }

    public WebSocketMessage getWebSocketMessage() {
        return webSocketMessage;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public boolean isCreateUpdate(){
        return webSocketMessage != null;
    }
}
