package com.delaval.usertransactionlogserver.jms.consumer;

import com.delaval.usertransactionlogserver.persistence.dao.OperationDAO;
import com.delaval.usertransactionlogserver.persistence.operation.CreateEventLogOperation;
import com.delaval.usertransactionlogserver.persistence.operation.OperationFactory;
import com.delaval.usertransactionlogserver.persistence.operation.OperationParam;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;

/**
 * Listens to a jms-queue and process the message as soon it gets a message.
 */
@Service
public class EventLogListener {

    public static final String NAME_OF_PROCESS_METHOD = "processEventLogMessage";

    public void processEventLogMessage(String text) {
        UtlsLogUtil.debug(this.getClass(), "processing eventLogMessage:" + text);
        try{
            WebSocketMessage webSocketMessage = new Gson().fromJson(text, WebSocketMessage.class);
            OperationParam<CreateEventLogOperation> operationParam = OperationFactory.getCreateEventLogParam(webSocketMessage);
            OperationDAO.getInstance().executeOperation(operationParam);
        }
        catch (Exception e){
            UtlsLogUtil.error(this.getClass(), "Something went wrong while parsing eventLog-json from message:" + e.getMessage());
        }
    }

}