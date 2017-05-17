package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.domain.InternalEntityRepresentation;
import com.delaval.usertransactionlogserver.domain.InternalSystemProperty;
import com.delaval.usertransactionlogserver.domain.InternalUserTransactionKey;
import com.delaval.usertransactionlogserver.persistence.entity.SystemProperty;
import com.delaval.usertransactionlogserver.util.DateUtil;
import com.delaval.usertransactionlogserver.websocket.MessTypes;
import com.delaval.usertransactionlogserver.websocket.SystemPropertyContent;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Created by delaval on 1/13/2016.
 */
public class OperationFactory {

    public static <T extends InternalEntityRepresentation> OperationResult<T> getNotOkResult(Operation<T> operation) throws IllegalAccessException, InstantiationException {
        return new OperationResult<T>(null);
    }

    public static CreateSystemPropertyOperation getCreateSystemPropertyForSystem(InternalSystemProperty internalSystemProperty) {
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
        CreateSystemPropertyOperation operation = new CreateSystemPropertyOperation();
        operation.setMessage(webSocketMessage);
        return operation;
    }


    public static CreateEventLogOperation getCreateEventLog(WebSocketMessage webSocketMessage) {
        CreateEventLogOperation operation = new CreateEventLogOperation();
        operation.setMessage(webSocketMessage);
        return operation;
    }

    public static CreateClickLogOperation getCreateClickLog(WebSocketMessage webSocketMessage) {
        CreateClickLogOperation operation = new CreateClickLogOperation();
        operation.setMessage(webSocketMessage);
        return operation;
    }

    public static GetSystemPropertyWithNameOperation getSystemPropertyWithName(String name) {
        GetSystemPropertyWithNameOperation operation = new GetSystemPropertyWithNameOperation();
        operation.setOperationParameter(new StringParameter(name));
        return operation;
    }

    public static GetAllUserTransactionKeysOperation getAllUserTransactionKeys() {
        return new GetAllUserTransactionKeysOperation();
    }

    public static GetEventLogsWithUserTransactionKeyOperation getEventLogsWithUserTransactionKey(InternalUserTransactionKey userTransactionKey) {
        GetEventLogsWithUserTransactionKeyOperation operation = new GetEventLogsWithUserTransactionKeyOperation();
        operation.setOperationParameter(new StringParameter(userTransactionKey.getId()));
        return operation;
    }

    public static GetEventLogsWithinTimespanOperation getEventLogsWithinTimespan(LocalDateTime from, LocalDateTime to) {
        GetEventLogsWithinTimespanOperation operation = new GetEventLogsWithinTimespanOperation();
        StringParameter fromParam = new StringParameter(DateUtil.formatLocalDateTime(from));
        StringParameter toParam = new StringParameter(DateUtil.formatLocalDateTime(to));
        operation.setOperationParameters(Arrays.asList(fromParam, toParam));
        return operation;
    }

}
