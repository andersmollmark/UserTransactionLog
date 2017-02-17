package com.delaval.usertransactionlogserver.websocket;

import com.delaval.usertransactionlogserver.persistence.dao.OperationDAO;
import com.delaval.usertransactionlogserver.persistence.operation.CreateSystemPropertyOperation;
import com.delaval.usertransactionlogserver.persistence.operation.OperationFactory;
import com.delaval.usertransactionlogserver.persistence.operation.OperationParam;
import com.delaval.usertransactionlogserver.service.FetchAllEventLogsService;
import com.delaval.usertransactionlogserver.service.JmsMessageService;
import com.delaval.usertransactionlogserver.util.DateUtil;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The websocket that the w ebclient sends the message to.
 * If the message is a log that shall be saved, it post the message to the jms-queue.
 */
@WebSocket(maxTextMessageSize = 64 * 1024, maxIdleTime = 1000000)
public class UserTransactionLogWebSocket {

    private Session mySession;

    @OnWebSocketConnect
    public void onconnect(Session session) {
        mySession = session;

        UtlsLogUtil.info(this.getClass(), "CONNECTING session:" + getRemoteAddress(mySession));
        UtlsLogUtil.sessions.put(mySession, new Date());
        List<Session> sessionList = UtlsLogUtil.sessionsPerHost.get(getRemoteAddress(mySession));
        if (sessionList == null) {
            sessionList = new ArrayList<>();
            UtlsLogUtil.sessionsPerHost.put(getRemoteAddress(mySession), sessionList);
        }
        sessionList.add(mySession);
        checkSessionPerHost(sessionList);
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        UtlsLogUtil.error(this.getClass(), "WebSocketError, reason:" + t.toString());
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        UtlsLogUtil.info(this.getClass(), "CLOSING websocket: " + getRemoteAddress(mySession) + ", status:" + statusCode + " reason:" + reason + " session.isOpen?" + mySession.isOpen());
        UtlsLogUtil.sessions.remove(mySession);
        List<Session> sessionList = UtlsLogUtil.sessionsPerHost.get(getRemoteAddress(mySession));
        sessionList.remove(mySession);
        checkSessionPerHost(sessionList);
        mySession = null;
    }

    private void checkSessionPerHost(List<Session> sessionList) {
        UtlsLogUtil.debug(this.getClass(), "number of sessions in host:" + getRemoteAddress(mySession) + " is:" + sessionList.size() + " and they are created as follows:");
        sessionList.forEach(session ->
          UtlsLogUtil.debug(this.getClass(), getRemoteAddress(session) + " and port:" + getRemotePort(session) + ", created:" + DateUtil.formatTimeStamp(UtlsLogUtil.sessions.get(session)) + " isOpen?" + session.isOpen()));
    }

    private String getRemoteAddress(Session session) {
        return session.getRemoteAddress() != null ? session.getRemoteAddress().getAddress().toString() : session.toString();
    }

    private String getRemotePort(Session session) {
        return session.getRemoteAddress() != null ? "" + session.getRemoteAddress().getPort() : "unknown port";
    }


    @OnWebSocketMessage
    public void handleMessage(Session session, String jsonMessage) {
        try {
            WebSocketMessage webSocketMessage = new Gson().fromJson(jsonMessage, WebSocketMessage.class);
            if (MessTypes.IDLE_POLL.isSame(webSocketMessage.getType())) {
//                if(getRemoteAddress().equals("/10.34.34.82")) {
//                    UtlsLogUtil.debug(this.getClass(), "Idle poll for:" + getRemoteAddress());
//                }
                session.getRemote().sendStringByFuture(jsonMessage);
                return;
            }
            UtlsLogUtil.debug(this.getClass(), "Incoming message:" + webSocketMessage.toString());
            // TODO authorization
            if (MessTypes.CLICK_LOG.isSame(webSocketMessage.getType()) || MessTypes.EVENT_LOG.isSame(webSocketMessage.getType())) {
                JmsMessageService.getInstance().createJmsMessage(webSocketMessage, jsonMessage);
            } else if (MessTypes.SYSTEM_PROPERTY.isSame(webSocketMessage.getType())) {
                OperationParam<CreateSystemPropertyOperation> createSystemPropertyParam = OperationFactory.getCreateSystemPropertyParam(webSocketMessage);
                OperationDAO.getInstance().executeOperation(createSystemPropertyParam);
            } else if (MessTypes.JSON_DUMP.isSame(webSocketMessage.getType())) {
                FetchAllEventLogsService logsService = new FetchAllEventLogsService();
                session.getRemote().sendStringByFuture(logsService.getJsonDumpMessage());
            } else {
                UtlsLogUtil.info(this.getClass(), "Unknown message:" + webSocketMessage.getType());
            }

        } catch (Exception e) {
            UtlsLogUtil.info(this.getClass(), "Exception while handle websocketmessage:" + e.getMessage());
        }
    }

}
