package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.persistence.entity.SystemProperty;
import com.delaval.usertransactionlogserver.persistence.entity.UserTransactionKey;
import com.delaval.usertransactionlogserver.util.DateUtil;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import com.delaval.usertransactionlogserver.websocket.SystemPropertyContent;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import com.google.gson.Gson;
import simpleorm.sessionjdbc.SSessionJdbc;

import java.util.Optional;

/**
 * Created by delaval on 1/13/2016. TODO
 */
public class CreateSystemPropertyOperation implements CreateUpdateOperation {

    private SSessionJdbc jdbcSession;
    private WebSocketMessage webSocketMessage;

    @Override
    public void setJdbcSession(SSessionJdbc session) {
        jdbcSession = session;
    }

    @Override
    public void setMessage(WebSocketMessage message) {
        this.webSocketMessage = message;
    }

    @Override
    public void validate() {
        if(webSocketMessage == null){
            throw new IllegalStateException("The operation-instance cant have null as a websocketmessage");
        }
        else if(jdbcSession == null){
            throw new IllegalStateException("The operation-instance cant have null as a jdbc-session");
        }
    }

    @Override
    public void execute() {
        UserTransactionKey.findOrCreateKey(jdbcSession, webSocketMessage);
        SystemPropertyContent systemPropertyContent = new Gson().fromJson(webSocketMessage.getJsonContent(), SystemPropertyContent.class);
        Optional<SystemProperty> optSystemProperty = SystemProperty.find(jdbcSession, systemPropertyContent.getName());
        SystemProperty systemProperty;
        if(optSystemProperty.isPresent()){
            systemProperty = optSystemProperty.get();
        }
        else{ // create new
            systemProperty = (SystemProperty) jdbcSession.create(SystemProperty.SYSTEM_PROPERTY, systemPropertyContent.getName());
            systemProperty.setString(SystemProperty.NAME, systemPropertyContent.getName());
        }
        systemProperty.createUserTransactionId(webSocketMessage);
        systemProperty.setString(SystemProperty.VALUE, systemPropertyContent.getValue());
        long timestamp = Long.parseLong(systemPropertyContent.getTimestamp());
        systemProperty.setString(SystemProperty.TIMESTAMP, DateUtil.formatTimeStamp(timestamp));
        UtlsLogUtil.debug(this.getClass(), systemProperty.getTimestamp() + ", Creating systemproperty with content:" + webSocketMessage.toString());
    }

}
