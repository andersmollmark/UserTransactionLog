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
 * Handles the communication with jms-producer and cache, and it creates the messages to be put on activemq
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


    /**
     * Tries to send all cached messages to activemq. It can send parts of or the hole cache. All messages thats not being sent is cached again.
     * @return a map with all messages that wasnt sent to activemq
     */
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
        Map<WebSocketMessage, String> leftOvers = new HashMap<>();
        keys.stream().forEach(messageKey -> leftOvers.put(messageKey, cachedLogs.get(messageKey)));
        return leftOvers;
    }

    /**
     * Try to send amessage from cache and send it to activemq
     * @param webSocketMessage
     * @param jsonMessage
     * @return true if it went well
     */
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

    /**
     * Creates a new message and sends it to activemq, if its up and running. Otherwise it caches it locally until activemq is up again
     * @param webSocketMessage is the message to handle
     * @param jsonMessage is the message in json-format
     */
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

    /**
     * Caches a message because something has happened with either database or acrivemq. When its cached it can be handled later.
     * @param webSocketMessage the message to cache
     */
    public void cacheJmsMessage(WebSocketMessage webSocketMessage) {
        if (webSocketMessage != null) {
            ConnectionTimeoutService.stopJmsAndStartTimer();
            UtlsLogUtil.info(JmsMessageService.class, "Trying to cache message with type:", webSocketMessage.getType());
            String jsonMessage = new Gson().toJson(webSocketMessage);
            JmsTempCache.getInstance().addLog(webSocketMessage, jsonMessage);
        }
    }

    /**
     * Sends a jmstemplate based on message on to jms-producer (activemq)
     * @param jsonMessage the message in json-format
     */
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
