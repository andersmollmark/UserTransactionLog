package com.delaval.usertransactionlogserver.websocket;

import com.delaval.usertransactionlogserver.persistence.dao.OperationDAO;
import com.delaval.usertransactionlogserver.persistence.operation.CreateSystemPropertyOperation;
import com.delaval.usertransactionlogserver.persistence.operation.OperationFactory;
import com.delaval.usertransactionlogserver.persistence.operation.OperationParam;
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
        UtlsLogUtil.info(this.getClass(), "CONNECTING session:", sessionController.getRemoteAddress(mySession));
        sessionController.add(mySession);
        checkSessionPerHost();
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        UtlsLogUtil.error(this.getClass(), "WebSocketError, reason:", t.toString());
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        UtlsLogUtil.info(this.getClass(),
          "CLOSING websocket:",
          sessionController.getRemoteAddress(mySession),
          ", status:", Integer.toString(statusCode),
          " reason:", reason,
          " session.isOpen?", Boolean.toString(mySession.isOpen()));
        sessionController.remove(mySession);
        checkSessionPerHost();
        mySession = null;
    }

    private void checkSessionPerHost() {
        List<Session> sessionList = sessionController.getSessionsPerHost(mySession);
        String remoteAddress = sessionController.getRemoteAddress(mySession);
        String remotePort = sessionController.getRemotePort(mySession);
        if (UtlsLogUtil.isDebug()) {
            UtlsLogUtil.debug(this.getClass(),
              "number of sessions in host:", remoteAddress,
              " is:", Integer.toString(sessionList.size()),
              " and they are created as follows:");
            sessionList.forEach(session -> {
                UtlsLogUtil.debug(this.getClass(), remoteAddress,
                  " and port:", remotePort,
                  ", created:", sessionController.getCreated(session),
                  " isOpen?", Boolean.toString(session.isOpen()));

            });

        }
    }


    @OnWebSocketMessage
    public void handleMessage(Session session, String jsonMessage) {
        try {
            WebSocketType webSocketType = new Gson().fromJson(jsonMessage, WebSocketType.class);
            if (MessTypes.IDLE_POLL.isSame(webSocketType.getType())) {
                session.getRemote().sendStringByFuture(jsonMessage);
                return;
            }
            UtlsLogUtil.debug(this.getClass(), "Incoming message:", webSocketType.toString());
            if (MessTypes.CLICK_LOG.isSame(webSocketType.getType()) || MessTypes.EVENT_LOG.isSame(webSocketType.getType())) {
                WebSocketMessage webSocketMessage = new Gson().fromJson(jsonMessage, WebSocketMessage.class);
                JmsMessageService.getInstance().createJmsMessage(webSocketMessage, jsonMessage);
            } else if (MessTypes.SYSTEM_PROPERTY.isSame(webSocketType.getType())) {
                WebSocketMessage webSocketMessage = new Gson().fromJson(jsonMessage, WebSocketMessage.class);
                OperationParam<CreateSystemPropertyOperation> createSystemPropertyParam = OperationFactory.getCreateSystemPropertyParam(webSocketMessage);
                OperationDAO.getInstance().doCreateUpdate(createSystemPropertyParam);
            } else if (MessTypes.JSON_DUMP.isSame(webSocketType.getType())) {
                FetchAllEventLogsService logsService = new FetchAllEventLogsService();
                session.getRemote().sendStringByFuture(logsService.getJsonDumpMessage());
            }else if (MessTypes.GET_PUBLIC_KEY.isSame(webSocketType.getType())) {
                session.getRemote().sendStringByFuture(CryptoKeyService.getInstance().getPublicKeyAsJson());
            }
            else if (MessTypes.FETCH_LOGS.isSame(webSocketType.getType())) {
                WebSocketFetchLogMessage webSocketFetchLogMessage = new Gson().fromJson(jsonMessage, WebSocketFetchLogMessage.class);
                FetchAllEventLogsService logsService = new FetchAllEventLogsService();
                session.getRemote().sendStringByFuture(logsService.getEncryptedJsonLogs(webSocketFetchLogMessage.getEncryptedClientKey()));
            }
            else {
                UtlsLogUtil.info(this.getClass(), "Unknown message:", webSocketType.getType());
            }

        } catch (Exception e) {
            UtlsLogUtil.info(this.getClass(), "Exception while handle websocketmessage:", e.getMessage());
        }
    }


}
