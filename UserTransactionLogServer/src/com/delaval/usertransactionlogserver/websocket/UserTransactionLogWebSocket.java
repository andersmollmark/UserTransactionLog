package com.delaval.usertransactionlogserver.websocket;

import com.delaval.usertransactionlogserver.service.CryptoKeyService;
import com.delaval.usertransactionlogserver.service.FetchAllEventLogsService;
import com.delaval.usertransactionlogserver.service.JmsMessageService;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.util.List;

/**
 * The websocket that the w ebclient sends the message to.
 * If the message is a log that shall be saved, it post the message to the jms-queue.
 */
@WebSocket(maxTextMessageSize = 64 * 1024, maxIdleTime = 1000000)
public class UserTransactionLogWebSocket {

    protected Session mySession;
    private SessionController sessionController = SessionController.getInstance();

    @OnWebSocketConnect
    public void onconnect(Session session) {
        mySession = session;
        UtlsLogUtil.info(this.getClass(), "Connecting session:", sessionController.getRemoteAddress(mySession));
        sessionController.add(mySession);
        checkSessionPerHost();
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        UtlsLogUtil.error(this.getClass(), "WebSocketError inside onError, reason:", t.toString(), ", session:" + sessionController.getRemoteAddress(mySession) + " is closed");
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        UtlsLogUtil.info(this.getClass(), "Closing websocket inside onClose:",
                sessionController.getRemoteAddress(mySession),
                ", status:", Integer.toString(statusCode),
                " reason:", reason,
                " session.isOpen?", Boolean.toString(mySession.isOpen()));
        closeWebsocket(mySession);
    }

    void closeWebsocket(Session session) {
        try {
            if (session.isOpen()) {
                UtlsLogUtil.info(this.getClass(), "session is open: " + sessionController.getRemoteAddress(session) + ", closing it...");
                session.close();
            }
            if (mySession.isOpen()) {
                UtlsLogUtil.info(this.getClass(), "mySession is open: " + sessionController.getRemoteAddress(mySession) + ", closing it...");
                mySession.close();
            }
            sessionController.remove(mySession);
            sessionController.remove(session);
            checkSessionPerHost();
            mySession = null;
        }
        catch (Exception e) {
            UtlsLogUtil.error(this.getClass(), "Closing websocket went wrong due to error:", e.getMessage());
        }
    }

    private void checkSessionPerHost() {
        List<Session> sessionList = sessionController.getSessionsPerHost(mySession);
        String remoteAddress = sessionController.getRemoteAddress(mySession);
        if (UtlsLogUtil.isDebug()) {
            UtlsLogUtil.debug(this.getClass(),
                    "number of sessions in host:", remoteAddress,
                    " is:", Integer.toString(sessionList.size()),
                    " and they are created as follows:");
            sessionList.forEach(session -> {
                UtlsLogUtil.debug(this.getClass(), remoteAddress,
                        " and port:", sessionController.getRemotePort(session),
                        ", created:", sessionController.getCreated(session),
                        " isOpen?", Boolean.toString(session.isOpen()));

            });

        }
    }


    @OnWebSocketMessage
    public void handleMessage(Session session, String jsonMessage) {
        try {
            WebSocketType webSocketType = new Gson().fromJson(jsonMessage, WebSocketType.class);
            if (MessTypes.IDLE_POLL.isSame(webSocketType.getType()) || MessTypes.AUTHORIZE_REQ.isSame(webSocketType.getType())) {
                session.getRemote().sendStringByFuture(jsonMessage);
                return;
            }
            UtlsLogUtil.debug(this.getClass(), "Incoming message:", webSocketType.toString());
            if (MessTypes.EVENT_LOG.isSame(webSocketType.getType())) {
                UtlsLogUtil.info(this.getClass(), "Incoming EventLog:", webSocketType.toString());
                WebSocketMessage webSocketMessage = new Gson().fromJson(jsonMessage, WebSocketMessage.class);
                UtlsLogUtil.info(this.getClass(), "Putting websocketmess on mq:", webSocketMessage.toString());
                JmsMessageService.getInstance().createJmsMessage(webSocketMessage, jsonMessage);
            } else if (MessTypes.GET_PUBLIC_KEY.isSame(webSocketType.getType())) {
                session.getRemote().sendStringByFuture(CryptoKeyService.getInstance().getPublicKeyAsJson());
            } else if (MessTypes.FETCH_ENCRYPTED_LOGS.isSame(webSocketType.getType())) {
                WebSocketFetchLogMessage webSocketFetchLogMessage = new Gson().fromJson(jsonMessage, WebSocketFetchLogMessage.class);
                FetchAllEventLogsService logsService = new FetchAllEventLogsService();
                session.getRemote().sendStringByFuture(logsService.getEncryptedJsonLogs(webSocketFetchLogMessage));
            } else if (MessTypes.FETCH_ENCRYPTED_LOGS_LAST_DAY.isSame(webSocketType.getType())) {
                UtlsLogUtil.info(this.getClass(), "Fetching logs last day:");
                FetchAllEventLogsService logsService = new FetchAllEventLogsService();
                session.getRemote().sendStringByFuture(logsService.getEncryptedJsonLogsLastDay());
            } else if (MessTypes.FETCH_ENCRYPTED_LOGS_WITH_TIMEZONE.isSame(webSocketType.getType())) {
                WebSocketTimezoneFetchLogMessage webSocketFetchLogMessage = new Gson().fromJson(jsonMessage, WebSocketTimezoneFetchLogMessage.class);
                UtlsLogUtil.info(this.getClass(), "Fetching logs with timezone:", webSocketFetchLogMessage.toString());
                FetchAllEventLogsService logsService = new FetchAllEventLogsService();
                session.getRemote().sendStringByFuture(
                        logsService.getEncryptedJsonLogsWithTimezone(webSocketFetchLogMessage));
            } else {
                UtlsLogUtil.info(this.getClass(), "Unknown message:", webSocketType.getType());
            }

        }
        catch (Exception e) {
            UtlsLogUtil.info(this.getClass(), "Exception while handle websocketmessage:", e.getMessage());
        }
    }


}
