package com.delaval.usertransactionlogserver.service;

import com.delaval.usertransactionlogserver.ServerProperties;
import com.delaval.usertransactionlogserver.jms.JmsResourceFactory;
import com.delaval.usertransactionlogserver.jms.JmsTempCache;
import com.delaval.usertransactionlogserver.jms.producer.JmsMessageCreator;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import com.delaval.usertransactionlogserver.websocket.MessTypes;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import com.google.gson.Gson;
import org.springframework.jms.core.JmsTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by delaval on 2016-11-15.
 */
public class JmsMessageService {

    private static JmsMessageService _jmsMessageService;

    private JmsMessageService() {
    }

    public static synchronized JmsMessageService getInstance() {
        if (_jmsMessageService == null) {
            _jmsMessageService = new JmsMessageService();
        }
        return _jmsMessageService;
    }


    public Map<WebSocketMessage, String> sendCachedJmsMessages() {
        JmsResourceFactory.startSendingJms();
        ConcurrentHashMap<WebSocketMessage, String> cachedLogs = JmsTempCache.getInstance().getAndClearCache();
        List<WebSocketMessage> keys = new ArrayList<>(cachedLogs.keySet());
        cachedLogs.keySet().forEach(key -> {
            boolean result = createJmsMessageFromCache(key, cachedLogs.get(key));
            if (!result) {
                return;
            }
            keys.remove(key);
        });
        Map<WebSocketMessage, String> result = new HashMap<>();
        keys.stream().forEach(messageKey -> result.put(messageKey, cachedLogs.get(messageKey)));
        return result;
    }

    public boolean createJmsMessageFromCache(WebSocketMessage webSocketMessage, String jsonMessage) {
        if (JmsResourceFactory.isJmsStopped()) {
            UtlsLogUtil.info(this.getClass(), "sending to jms is stopped, returning");
            return false;
        }
        try {
            sendJmsTemplate(jsonMessage);
        }
        catch (Throwable ex) {
            UtlsLogUtil.debug(JmsMessageService.class, "Something went wrong while sending cached jms", ex.getMessage());
            return false;
        }
        return true;
    }

    public void createJmsMessage(WebSocketMessage webSocketMessage, String jsonMessage) {
        if (JmsResourceFactory.isJmsStopped()) {
            UtlsLogUtil.info(this.getClass(), "sending to jms is stopped, adding jms-message to cache instead");
            JmsTempCache.getInstance().addLog(webSocketMessage, jsonMessage);
        } else if (!MessTypes.EVENT_LOG.isSame(webSocketMessage.getType())) {
            UtlsLogUtil.info(JmsMessageService.class, "Messtype is not one of clicklog or eventlog, so were not creating jms, type:",
              webSocketMessage.getType());
        } else {
            try {
                sendJmsTemplate(jsonMessage);
            }
            catch (Throwable ex) {
                UtlsLogUtil.debug(JmsMessageService.class, "Something went wrong while sending jms", ex.getMessage());
                cacheJmsMessage(webSocketMessage);
            }
        }
    }

    public void cacheJmsMessage(WebSocketMessage webSocketMessage) {
        if (webSocketMessage != null) {
            ConnectionTimeoutService.stopJmsAndStartTimer();
            UtlsLogUtil.info(JmsMessageService.class, "Trying to cache message with type:", webSocketMessage.getType());
            String jsonMessage = new Gson().toJson(webSocketMessage);
            JmsTempCache.getInstance().addLog(webSocketMessage, jsonMessage);
        }
    }

    private void sendJmsTemplate(String jsonMessage) {
        ServerProperties.PropKey jmsDest = ServerProperties.PropKey.JMS_QUEUE_DEST_EVENT;
        UtlsLogUtil.debug(JmsMessageService.class, "Create a jms from message:", jsonMessage, " to dest:", jmsDest.name());
        JmsResourceFactory jmsResourceFactory = JmsResourceFactory.getEventLogInstance();
        JmsMessageCreator messageCreator = new JmsMessageCreator(jsonMessage);
        JmsTemplate jmsTemplate = jmsResourceFactory.getJmsTemplate();
        String jmsDestination = getProp(jmsDest);
        jmsTemplate.send(jmsDestination, messageCreator);


    }

    private String getProp(ServerProperties.PropKey propKey) {
        return ServerProperties.getInstance().getProp(propKey);
    }
}
