package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.persistence.entity.EventLog;
import com.delaval.usertransactionlogserver.persistence.entity.UserTransactionKey;
import com.delaval.usertransactionlogserver.util.DateUtil;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import com.delaval.usertransactionlogserver.websocket.EventLogContent;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import com.google.gson.Gson;
import simpleorm.sessionjdbc.SSessionJdbc;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Creates a log and saves it to db.
 */
public class CreateEventLogOperation implements CreateUpdateOperation {

    SSessionJdbc jdbcSession;
    WebSocketMessage webSocketMessage;
    EventLogContent eventLogContent;

    /**
     * @see Operation#setJdbcSession(SSessionJdbc)
     */
    @Override
    public void setJdbcSession(SSessionJdbc session) {
        jdbcSession = session;
    }

    @Override
    public void setMessage(WebSocketMessage message) {
        this.webSocketMessage = message;
    }

    @Override
    public WebSocketMessage getWebSocketMessage() {
        return webSocketMessage;
    }

    /**
     * @see Operation#validate()
     */
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
            UtlsLogUtil.error(this.getClass(),
              "Tried to create an eventlog with  a to large label:",
              Integer.toString(eventLogContent.getEventLabel().length()),
              ":", eventLogContent.getEventLabel(), ", have to make it smaller");
            String shortenedLabel = eventLogContent.getEventLabel().substring(0, EventLog.LABEL.getMaxSize() - 1);
            eventLogContent.setEventLabel(shortenedLabel);
        }
    }

    @Override
    public void execute() {
        UserTransactionKey key = UserTransactionKey.findOrCreateKey(jdbcSession, webSocketMessage);
        UtlsLogUtil.info(this.getClass(), "Creating eventlog: ", eventLogContent.toString());
        EventLog newContent = (EventLog) jdbcSession.create(EventLog.EVENT_LOG, getEventLogId(webSocketMessage, eventLogContent));
        newContent.createUserTransactionId(webSocketMessage);
        long timestamp = Long.parseLong(eventLogContent.getTimestamp());
        ZonedDateTime utc = Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC);

        UtlsLogUtil.debug(this.getClass(), "incoming time:", DateUtil.formatTimeStamp(timestamp), " utc time:", DateUtil.formatTimeStamp(utc));

        newContent.setString(EventLog.NAME, eventLogContent.getEventName());
        newContent.setString(EventLog.CATEGORY, eventLogContent.getEventCategory());
        newContent.setString(EventLog.HOST, eventLogContent.getHost());
        newContent.setString(EventLog.TARGET_MS, eventLogContent.getTargetMs());
        newContent.setString(EventLog.LABEL, eventLogContent.getEventLabel());
        newContent.setString(EventLog.TIMESTAMP, DateUtil.formatTimeStamp(timestamp));
        newContent.setString(EventLog.TAB, eventLogContent.getTab());

        if(UtlsLogUtil.isDebug()){
            UtlsLogUtil.debug(this.getClass(), newContent.getTimestamp().toString(),
              ", Created eventlog in db with content:", webSocketMessage.toString());
        }

    }


    private String getEventLogId(WebSocketMessage webSocketMessage, EventLogContent eventLogContent){
        StringBuilder sb = new StringBuilder();
        sb.append(webSocketMessage.getUsername()).append(eventLogContent.getTimestamp());
        return sb.toString();
    }
}
