package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.domain.InternalSystemProperty;
import com.delaval.usertransactionlogserver.domain.InternalUserTransactionKey;
import com.delaval.usertransactionlogserver.persistence.entity.SystemProperty;
import com.delaval.usertransactionlogserver.websocket.MessTypes;
import com.delaval.usertransactionlogserver.websocket.SystemPropertyContent;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import com.google.gson.Gson;
import simpleorm.sessionjdbc.SSessionJdbc;

/**
 * Created by delaval on 1/13/2016.
 */
public class OperationFactory {

    public static <T extends CreateUpdateOperation> CreateUpdateOperation getCreateUpdateOperation(SSessionJdbc jdbcSession, OperationParam<T> operationParam) throws IllegalAccessException, InstantiationException {
        Class<T> operationClass = operationParam.getOperationClass();
        T t = operationClass.newInstance();
        t.setJdbcSession(jdbcSession);
        t.setMessage(operationParam.getWebSocketMessage());
        return t;
    }

    public static <T extends ReadOperation> ReadOperation getReadOperation(SSessionJdbc jdbcSession, OperationParam<T> operationParam) throws IllegalAccessException, InstantiationException {
        Class<T> readOperationClass = operationParam.getOperationClass();
        T t = readOperationClass.newInstance();
        t.setReadParameter(operationParam.getParameter());
        t.setJdbcSession(jdbcSession);
        return t;
    }

    public static <T extends Operation> Operation getNotOkResultOperation(OperationParam<T> operationParam) throws IllegalAccessException, InstantiationException {
        Class<T> operationClass = operationParam.getOperationClass();
        T t = operationClass.newInstance();
        return t;
    }


    public static OperationParam<CreateSystemPropertyOperation> getCreateSystemPropertyParam(WebSocketMessage webSocketMessage){
        return new OperationParam<>(CreateSystemPropertyOperation.class, webSocketMessage);
    }

    public static OperationParam<CreateSystemPropertyOperation> getCreateSystemPropertyParamForSystem(InternalSystemProperty internalSystemProperty){
        SystemPropertyContent content = new SystemPropertyContent();
        content.setValue(internalSystemProperty.getValue());
        content.setName(internalSystemProperty.getName());
        content.setTimestamp(Long.toString(internalSystemProperty.getTimestampAsLong()));
        WebSocketMessage webSocketMessage = new WebSocketMessage();
        webSocketMessage.setMessType(MessTypes.SYSTEM_PROPERTY.getMyValue());
        webSocketMessage.setClient(SystemProperty.SYSTEM_USER);
        webSocketMessage.setTarget(SystemProperty.SYSTEM_USER);
        webSocketMessage.setUsername(SystemProperty.SYSTEM_USER);
        webSocketMessage.setJsonContent(new Gson().toJson(content));
        return new OperationParam<>(CreateSystemPropertyOperation.class, webSocketMessage);
    }


    public static OperationParam<CreateEventLogOperation> getCreateEventLogParam(WebSocketMessage webSocketMessage){
        return new OperationParam<>(CreateEventLogOperation.class, webSocketMessage);
    }

    public static OperationParam<CreateClickLogOperation> getCreateClickLogParam(WebSocketMessage webSocketMessage){
        return new OperationParam<>(CreateClickLogOperation.class, webSocketMessage);
    }

    public static OperationParam<GetSystemPropertyWithNameOperation> getSystemPropertyWithNameParam(String name){
        OperationParam<GetSystemPropertyWithNameOperation> operationParam = new OperationParam<>(GetSystemPropertyWithNameOperation.class);
        operationParam.setParameter(name);
        return operationParam;
    }

    public static OperationParam<GetAllUserTransactionKeysOperation> getAllUserTransactionKeyParam(){
        return new OperationParam<>(GetAllUserTransactionKeysOperation.class);
    }

    public static OperationParam<GetEventLogsWithUserTransactionKeyOperation> getEventLogsWithUserTransactionKeyParam(InternalUserTransactionKey userTransactionKey){
        OperationParam<GetEventLogsWithUserTransactionKeyOperation> operationParam = new OperationParam<>(GetEventLogsWithUserTransactionKeyOperation.class);
        operationParam.setParameter(userTransactionKey.getId());
        return operationParam;
    }

}
