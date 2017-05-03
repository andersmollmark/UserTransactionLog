package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.persistence.entity.ClickLog;
import com.delaval.usertransactionlogserver.persistence.entity.UserTransactionKey;
import com.delaval.usertransactionlogserver.util.DateUtil;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import com.delaval.usertransactionlogserver.websocket.ClickLogContent;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import com.google.gson.Gson;
import simpleorm.sessionjdbc.SSessionJdbc;

/**
 * Created by delaval on 1/13/2016.
 */
public class CreateClickLogOperation implements CreateUpdateOperation {

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
        ClickLogContent clickLogContent = new Gson().fromJson(webSocketMessage.getJsonContent(), ClickLogContent.class);
        ClickLog newContent = (ClickLog) jdbcSession.create(ClickLog.CLICK_LOG, getClickLogId(webSocketMessage, clickLogContent));
        newContent.createUserTransactionId(webSocketMessage);
        newContent.setString(ClickLog.CSS_CLASSNAME, clickLogContent.getCssClassName());
        newContent.setString(ClickLog.ELEMENT_ID, clickLogContent.getElementId());
        newContent.setString(ClickLog.INTERACTION_TYPE, "ToBeChosen");
        long timestamp = Long.parseLong(clickLogContent.getTimestamp());
        newContent.setString(ClickLog.TIMESTAMP, DateUtil.formatTimeStamp(timestamp));
        newContent.setString(ClickLog.X, clickLogContent.getX());
        newContent.setString(ClickLog.Y, clickLogContent.getY());
        newContent.setString(ClickLog.TAB, clickLogContent.getTab());
        newContent.setString(ClickLog.HOST, clickLogContent.getHost());
        UtlsLogUtil.debug(this.getClass(),
          newContent.getTimestamp().toString(),
          ", Creating clickLog with content:", webSocketMessage.toString());
    }

    private static String getClickLogId(WebSocketMessage webSocketMessage, ClickLogContent clickLogContent){
        StringBuilder sb = new StringBuilder();
        sb.append(webSocketMessage.getUsername()).append(clickLogContent.getTimestamp());
        return sb.toString();
    }}
