package com.delaval.usertransactionlogserver.service;

import com.delaval.usertransactionlogserver.ServerProperties;
import com.delaval.usertransactionlogserver.jms.JmsResourceFactory;
import com.delaval.usertransactionlogserver.persistence.ConnectionFactory;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Handles the timeout-functionality connected to remotecontrol. And the timeout-time can be altered in properties-file (vmsFarm.properties
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
            UtlsLogUtil.info(ConnectionTimeoutService.class, " starting a new connection timeout");
            executorService.execute(new TimeoutTask());
        }
    }

    /**
     * Stops the timer, if there is any
     */
    public static void stop() {
        synchronized (LOCK) {
            if (isRunning()) {
                UtlsLogUtil.info(ConnectionTimeoutService.class, " shutting down connection timeout");
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
     * Handles what to do when rc is timing out
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
            try {
                TimeUnit.SECONDS.sleep(timeoutInSeconds);
                UtlsLogUtil.info(ConnectionTimeoutService.class, " trying to get connection again");
                ConnectionFactory.checkIfConnectionIsOk();
                UtlsLogUtil.info(ConnectionTimeoutService.class, " the connection should be up again and we try to send cached jms-messages");
                JmsMessageService.getInstance().sendCachedJmsMessages();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            catch (SQLException sqlException){
                success = false;
                UtlsLogUtil.error(ConnectionTimeoutService.class, " connection is still down");
            }
            finally {
                synchronized (LOCK) {
                    if (isRunning()) {
                        executorService.shutdown();
                    }
                }
                if(!success){
                    ConnectionTimeoutService.startNew();
                }


            }
        }

//        private void checkIfConnectionIsOk() throws SQLException {
//            ConnectionFactory.getInstance().getConnection();
//            if(ConnectionFactory.getInstance().isAnyLogTableLocked()){
//                throw new SQLException("a logtable is locked");
//            }
//        }
    }

}
