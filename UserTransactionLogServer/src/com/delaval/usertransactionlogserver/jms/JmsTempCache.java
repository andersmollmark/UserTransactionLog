package com.delaval.usertransactionlogserver.jms;

import com.delaval.usertransactionlogserver.ServerProperties;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by delaval on 2016-11-10.
 */
public class JmsTempCache {

    private static JmsTempCache _instance;
    private ConcurrentHashMap<WebSocketMessage, String> listOfLogs = new ConcurrentHashMap<>();
    private ConcurrentHashMap<WebSocketMessage, String> logsCopy;
    private static final Object LOG_LOCK = new Object();
    private final int maxNumberOfLogs;

    private JmsTempCache() {
//        singleton
        String maxSize = ServerProperties.getInstance().getProp(ServerProperties.PropKey.UTLS_LOG_CACHE_MAX_SIZE);
        maxNumberOfLogs = Integer.parseInt(maxSize);
    }

    /**
     * Singleton
     *
     * @return
     */
    public static synchronized JmsTempCache getInstance() {
        if (_instance == null) {
            _instance = new JmsTempCache();
        }
        return _instance;
    }

    public void addLog(WebSocketMessage message, String jsonMessage) {
        synchronized (LOG_LOCK) {
            if (listOfLogs.size() == maxNumberOfLogs) {
                UtlsLogUtil.error(this.getClass(), "Max number of logs is in cache (" + maxNumberOfLogs + "). No more will be temporary saved in this cache.");
            } else {
                listOfLogs.put(message, jsonMessage);
            }
        }
    }

    public synchronized ConcurrentHashMap<WebSocketMessage, String> getAndClearCache() {
        synchronized (LOG_LOCK) {
            logsCopy = listOfLogs;
            listOfLogs = new ConcurrentHashMap<>();
            UtlsLogUtil.info(this.getClass(), "Trying to recreate " + logsCopy.size() + " jms-messages");
        }
        return logsCopy;
    }

}
