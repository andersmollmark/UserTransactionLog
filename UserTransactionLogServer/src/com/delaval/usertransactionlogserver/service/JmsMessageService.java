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


    public void sendCachedJmsMessages() {
        JmsResourceFactory.startSendingJms();
        ConcurrentHashMap<WebSocketMessage, String> cachedLogs = JmsTempCache.getInstance().getAndClearCache();
        cachedLogs.keySet().forEach(key -> createJmsMessage(key, cachedLogs.get(key)));
    }

    public void createJmsMessage(WebSocketMessage webSocketMessage, String jsonMessage) {
        if (JmsResourceFactory.isJmsStopped()) {
            UtlsLogUtil.info(this.getClass(), "sending to jms is stopped, adding jms-message to cache instead");
            JmsTempCache.getInstance().addLog(webSocketMessage, jsonMessage);
        } else if (MessTypes.CLICK_LOG.isSame(webSocketMessage.getType())) {
            sendJmsTemplate(jsonMessage, ServerProperties.PropKey.JMS_QUEUE_DEST_CLICK, JmsResourceFactory.getClickLogInstance());
        } else if (MessTypes.EVENT_LOG.isSame(webSocketMessage.getType())) {
            sendJmsTemplate(jsonMessage, ServerProperties.PropKey.JMS_QUEUE_DEST_EVENT, JmsResourceFactory.getEventLogInstance());
        } else {
            UtlsLogUtil.info(JmsMessageService.class, "Messtype is not one of clicklog or eventlog, so were not creating jms, type:" + webSocketMessage.getType());
        }
    }

    public void cacheJmsMessage(WebSocketMessage webSocketMessage) {
        ConnectionTimeoutService.stopJmsAndStartTimer();
        UtlsLogUtil.info(JmsMessageService.class, "Trying to cache message due to db-problems with type:" + webSocketMessage.getType());
        String jsonMessage = new Gson().toJson(webSocketMessage);
        JmsTempCache.getInstance().addLog(webSocketMessage, jsonMessage);
    }

    private void sendJmsTemplate(String jsonMessage, ServerProperties.PropKey jmsDest, JmsResourceFactory jmsResourceFactory) {
        UtlsLogUtil.debug(JmsMessageService.class, "Create a jms from message:" + jsonMessage + " to dest:" + jmsDest.name());
        JmsMessageCreator messageCreator = new JmsMessageCreator(jsonMessage);
        JmsTemplate jmsTemplate = jmsResourceFactory.getJmsTemplate();
        String jmsDestination = getProp(jmsDest);
        jmsTemplate.send(jmsDestination, messageCreator);
    }

    private String getProp(ServerProperties.PropKey propKey) {
        return ServerProperties.getInstance().getProp(propKey);
    }
}
