package com.delaval.usertransactionlogserver.jms;

import com.delaval.usertransactionlogserver.ServerProperties;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles all logs we cant save to database or send to activemq. They are saved temporary in both memory and on disc
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
        if(!existCacheFile()){
            createEmptyCachefile();
        }

    }

    /**
     * Singleton
     *
     * @return
     */
    public static synchronized JmsTempCache getInstance() {
        if (_instance == null) {
            _instance = new JmsTempCache();
            _instance.createLogsFromCache();

        }
        return _instance;
    }

    private boolean existCacheFile() {
        return Files.exists(getLogCachePath(), LinkOption.NOFOLLOW_LINKS);

    }

    private Path getLogCachePath() {
        return Paths.get(ServerProperties.getInstance().getProp(ServerProperties.PropKey.UTLS_CACHE_FILE_PATH));
    }

    /**
     * Create logs to cache from the cachefile if there are any
     */
    private void createLogsFromCache() {
        synchronized (LOG_LOCK) {
            Path path = getLogCachePath();
            try {
                List<String> allLogs = Files.readAllLines(path);
                allLogs.stream().forEach(jsonMessage -> {
                    WebSocketMessage webSocketMessage = new Gson().fromJson(jsonMessage, WebSocketMessage.class);
                    listOfLogs.put(webSocketMessage, jsonMessage);
                });

            }
            catch (IOException e) {
                UtlsLogUtil.error(this.getClass(), "create logs from cache didnt work:", e.getMessage());
            }
        }
    }

    /**
     * Add a log to the cache, both in memory and on disc
     *
     * @param message
     * @param jsonMessage
     */
    public void addLog(WebSocketMessage message, String jsonMessage) {
        UtlsLogUtil.debug(this.getClass(), "********** Adding log to cache, and type is:", message.getType(), " and number of logs in cache is:", "" + (listOfLogs.size() + 1));
        synchronized (LOG_LOCK) {
            if (listOfLogs.size() == maxNumberOfLogs) {
                UtlsLogUtil.error(this.getClass(),
                  "Max number of logs is in cache (",
                  Integer.toString(maxNumberOfLogs),
                  "). No more will be temporary saved in this cache.");
            } else {
                listOfLogs.put(message, jsonMessage);
                writeLogToDisc(jsonMessage);
            }
        }
    }

    /**
     * Writes a log to disc (cachefile)
     *
     * @param jsonMessage
     */
    private void writeLogToDisc(String jsonMessage) {
        Path path = getLogCachePath();
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            writer.write(jsonMessage);
            writer.newLine();
            UtlsLogUtil.debug(this.getClass(), "added log to temporary cachefile, ", path.toString());
        }
        catch (IOException e) {
            UtlsLogUtil.error(this.getClass(), "something went wrong while adding log to file:", path.toString(),
              " Exception:" + e.toString());
        }
    }

    public synchronized ConcurrentHashMap<WebSocketMessage, String> getAndClearCache() {
        synchronized (LOG_LOCK) {
            logsCopy = listOfLogs;
            listOfLogs = new ConcurrentHashMap<>();
            createEmptyCachefile();
            UtlsLogUtil.info(this.getClass(), "Trying to recreate ", Integer.toString(logsCopy.size()), " jms-messages from cache");
        }
        return logsCopy;
    }

    private void createEmptyCachefile() {
        try (PrintWriter pw = new PrintWriter(ServerProperties.getInstance().getProp(ServerProperties.PropKey.UTLS_CACHE_FILE_PATH))) {
            UtlsLogUtil.info(this.getClass(), "a new cachefile is created");
        }
        catch (FileNotFoundException e) {
            UtlsLogUtil.error(this.getClass(), "something went wrong while creating file for cached logs:", " Exception:" + e.toString());
        }
    }


    public synchronized void addMessageListThatsNotBeeingSent(Map<WebSocketMessage, String> messages) {
        messages.forEach(this::addLog);
    }
}
