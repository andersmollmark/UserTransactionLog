package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.domain.FetchLogDTO;
import com.delaval.usertransactionlogserver.domain.InternalEntityRepresentation;
import com.delaval.usertransactionlogserver.domain.InternalSystemProperty;
import com.delaval.usertransactionlogserver.domain.InternalUserTransactionKey;
import com.delaval.usertransactionlogserver.persistence.entity.SystemProperty;
import com.delaval.usertransactionlogserver.util.DateUtil;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import com.delaval.usertransactionlogserver.websocket.MessTypes;
import com.delaval.usertransactionlogserver.websocket.SystemPropertyContent;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import com.google.gson.Gson;

import java.util.Arrays;

/**
 * Creates the right operation-class for calling code
 */
public class OperationFactory {

    public static <T extends InternalEntityRepresentation> OperationResult<T> getNotOkResult(Operation<T> operation) throws IllegalAccessException, InstantiationException {
        return new OperationResult<T>(null);
    }

    /**
     * Creates a {@link CreateSystemPropertyOperation}
     * @param internalSystemProperty describes a systemproperty in the system-domain
     * @return a {@link CreateSystemPropertyOperation}
     */
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

    /**
     * Creates a {@link CreateEventLogOperation}
     * @param webSocketMessage is the messages that should be used when creating the log to the db
     * @return the operation-class
     */
    public static CreateEventLogOperation getCreateEventLog(WebSocketMessage webSocketMessage) {
        CreateEventLogOperation operation = new CreateEventLogOperation();
        operation.setMessage(webSocketMessage);
        return operation;
    }

    /**
     * Creates a {@link GetSystemPropertyWithNameOperation}
     * @param name is the name of the systemproperty to fetch
     * @return the operation-class
     */
    public static GetSystemPropertyWithNameOperation getSystemPropertyWithName(String name) {
        GetSystemPropertyWithNameOperation operation = new GetSystemPropertyWithNameOperation();
        operation.setOperationParameter(new StringParameter(name));
        return operation;
    }

    /**
     * Creates a {@link GetAllUserTransactionKeysOperation}
     * @return
     */
    public static GetAllUserTransactionKeysOperation getAllUserTransactionKeys() {
        return new GetAllUserTransactionKeysOperation();
    }

    /**
     * Creates a {@link GetEventLogsWithUserTransactionKeyOperation}
     * @param userTransactionKey is the inner representation of a userTransactionKey
     * @return the operation-class
     */
    public static GetEventLogsWithUserTransactionKeyOperation getEventLogsWithUserTransactionKey(InternalUserTransactionKey userTransactionKey) {
        GetEventLogsWithUserTransactionKeyOperation operation = new GetEventLogsWithUserTransactionKeyOperation();
        operation.setOperationParameter(new StringParameter(userTransactionKey.getId()));
        return operation;
    }

    /**
     * Creates a {@link GetEventLogsWithinTimespanOperation}
     * @param fetchLogDTO contains the data neeeded to be able to fetch the specific timespan and timezone
     * @return the operation-class
     */
    public static GetEventLogsWithinTimespanOperation getEventLogsWithinTimespan(FetchLogDTO fetchLogDTO) {
        GetEventLogsWithinTimespanOperation operation = new GetEventLogsWithinTimespanOperation();
        StringParameter fromParam = new StringParameter(DateUtil.formatLocalDateTime(fetchLogDTO.getFrom()));
        StringParameter toParam = new StringParameter(DateUtil.formatLocalDateTime(fetchLogDTO.getTo()));

        UtlsLogUtil.info(OperationFactory.class, "Get all eventlogs within timespan and creating json-format, ",
          "from:", fromParam.value, " to:", toParam.value);

        if(fetchLogDTO.getZoneId() != null){
            StringParameter zoneId = new StringParameter(fetchLogDTO.getZoneId().getId());
            operation.setOperationParameters(Arrays.asList(fromParam, toParam, zoneId));
        }
        else{
            operation.setOperationParameters(Arrays.asList(fromParam, toParam));
        }
        return operation;
    }

}
