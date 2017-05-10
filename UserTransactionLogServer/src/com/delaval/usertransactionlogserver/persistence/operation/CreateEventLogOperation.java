package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.persistence.entity.EventLog;
import com.delaval.usertransactionlogserver.persistence.entity.UserTransactionKey;
import com.delaval.usertransactionlogserver.util.DateUtil;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import com.delaval.usertransactionlogserver.websocket.EventLogContent;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import com.google.gson.Gson;
import simpleorm.sessionjdbc.SSessionJdbc;

/**
 * Created by delaval on 1/13/2016.
 */
public class CreateEventLogOperation implements CreateUpdateOperation {

    SSessionJdbc jdbcSession;
    WebSocketMessage webSocketMessage;
    EventLogContent eventLogContent;

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

        eventLogContent = new Gson().fromJson(webSocketMessage.getJsonContent(), EventLogContent.class);
        if(eventLogContent.getEventLabel() != null && eventLogContent.getEventLabel().length() > EventLog.LABEL.getMaxSize()){
            UtlsLogUtil.info(this.getClass(),
              "Tried to create a eventlabel with size ",
              Integer.toString(eventLogContent.getEventLabel().length()),
              ":", eventLogContent.getEventLabel());
            String shortenedLabel = eventLogContent.getEventLabel().substring(0, EventLog.LABEL.getMaxSize() - 1);
            eventLogContent.setEventLabel(shortenedLabel);
        }
    }

    @Override
    public void execute() {
        UtlsLogUtil.debug(this.getClass(), "execute CreateEventLogOperation");
        UserTransactionKey.findOrCreateKey(jdbcSession, webSocketMessage);
        EventLog newContent = (EventLog) jdbcSession.create(EventLog.EVENT_LOG, getEventLogId(webSocketMessage, eventLogContent));
        newContent.createUserTransactionId(webSocketMessage);
        long timestamp = Long.parseLong(eventLogContent.getTimestamp());
        newContent.setString(EventLog.NAME, eventLogContent.getEventName());
        newContent.setString(EventLog.CATEGORY, eventLogContent.getEventCategory());
        newContent.setString(EventLog.HOST, eventLogContent.getHost());
        newContent.setString(EventLog.LABEL, eventLogContent.getEventLabel());
        newContent.setString(EventLog.TIMESTAMP, DateUtil.formatTimeStamp(timestamp));
        newContent.setString(EventLog.TAB, eventLogContent.getTab());
        UtlsLogUtil.debug(this.getClass(), newContent.getTimestamp().toString(),
          ", Creating eventlog with content:", webSocketMessage.toString());
    }


    private String getEventLogId(WebSocketMessage webSocketMessage, EventLogContent eventLogContent){
        StringBuilder sb = new StringBuilder();
        sb.append(webSocketMessage.getUsername()).append(eventLogContent.getTimestamp());
        return sb.toString();
    }
}
