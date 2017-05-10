package com.delaval.usertransactionlogserver.jms.consumer;

import com.delaval.usertransactionlogserver.persistence.dao.OperationDAO;
import com.delaval.usertransactionlogserver.persistence.operation.CreateClickLogOperation;
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
public class ClickLogListener {

    public static final String NAME_OF_PROCESS_METHOD = "processClickLogMessage";

    public void processClickLogMessage(String text) {
        UtlsLogUtil.debug(this.getClass(), "processing clickLogMessage:", text);
        try{
            WebSocketMessage webSocketMessage = new Gson().fromJson(text, WebSocketMessage.class);
            OperationParam<CreateClickLogOperation> operationParam = OperationFactory.getCreateClickLogParam(webSocketMessage);
            OperationDAO.getInstance().doCreateUpdate(operationParam);
        }
        catch (Exception e){
            UtlsLogUtil.error(this.getClass(), "Something went wrong while parsing clickLog-json from message:", e.getMessage());
        }
    }

}