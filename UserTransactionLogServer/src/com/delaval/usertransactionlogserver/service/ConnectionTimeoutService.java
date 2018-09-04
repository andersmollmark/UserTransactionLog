package com.delaval.usertransactionlogserver.service;

import com.delaval.usertransactionlogserver.ServerProperties;
import com.delaval.usertransactionlogserver.jms.JmsResourceFactory;
import com.delaval.usertransactionlogserver.jms.JmsTempCache;
import com.delaval.usertransactionlogserver.persistence.ConnectionFactory;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Handles a timeout-functionality when checking both connection to mysql and activemq. And the timeout-time can be altered in properties-file UserTransactionLogServer.properties
 */
public class ConnectionTimeoutService {

    private static ExecutorService executorService;

    private static final Object LOCK = new Object();



    public static void stopJmsAndStartTimer(){
        synchronized (LOCK) {
            if (!isRunning()) {
                JmsResourceFactory.stopSendingJms();
                startNew();
            }
        }
    }

    /**
     * Starts a new timer
     */
    private static void startNew() {
        synchronized (LOCK) {
            executorService = Executors.newSingleThreadExecutor();
            UtlsLogUtil.debug(ConnectionTimeoutService.class, " starting a new connection timeout");
            executorService.execute(new TimeoutTask());
        }
    }

    /**
     * Stops the timer, if there is any
     */
    public static void stop() {
        synchronized (LOCK) {
            if (isRunning()) {
                UtlsLogUtil.debug(ConnectionTimeoutService.class, " shutting down connection timeout");
                executorService.shutdownNow();
            }
        }
    }

    private static boolean isRunning(){
        synchronized (LOCK){
            return executorService != null && !executorService.isTerminated();
        }
    }

    /**
     * Checks if connection to mysql is up and ok and it tries to send, if it exist, cached messages to activemq again.
     */
    private static class TimeoutTask implements Runnable {

        private final int timeoutInSeconds;

        TimeoutTask() {
            String timeoutString = ServerProperties.getInstance().getProp(ServerProperties.PropKey.CONNECTION_TIMEOUT_IN_SECONDS);
            timeoutInSeconds = Integer.parseInt(timeoutString);
        }

        @Override
        public void run() {
            boolean success = true;
            Map<WebSocketMessage, String> messThatsNotSent = new HashMap<>();
            try {
                TimeUnit.SECONDS.sleep(timeoutInSeconds);
                UtlsLogUtil.debug(ConnectionTimeoutService.class, "trying to get connection again");
                ConnectionFactory.checkIfConnectionIsOk();
                messThatsNotSent = JmsMessageService.getInstance().sendCachedJmsMessages();
                if(messThatsNotSent.size() > 0) {
                    success = false;
                }
            } catch (InterruptedException e) {
                UtlsLogUtil.debug(ConnectionTimeoutService.class, "interruptedException:", e.getMessage());
                e.printStackTrace();
            }
            catch (SQLException sqlException){
                success = false;
                UtlsLogUtil.error(ConnectionTimeoutService.class, "connection is still down");
            }
            finally {
                synchronized (LOCK) {
                    if (isRunning()) {
                        executorService.shutdown();
                    }
                }
                if(!success){
                    JmsTempCache.getInstance().addMessageListThatsNotBeeingSent(messThatsNotSent);
                    ConnectionTimeoutService.startNew();
                }


            }
        }

    }

}
