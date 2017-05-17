package com.delaval.usertransactionlogserver;

import com.delaval.usertransactionlogserver.persistence.dao.InitDAO;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * Singleton.
 * Holds all the properties needed in the UserTransactionLogServer.
 */
public class ServerProperties {

    private static ServerProperties _instance;

    private static final String PROP_FILE_NAME = "UserTransactionLogServer.properties";

    public enum PropKey {
        DB_SERVER_HOST("dbServerHost", "localhost"),
        DB_SERVER_PORT("dbServerPort", "3306"),
        DB_NAME("dbName", "user_transaction_log_server"),
        DB_USER("dbUser", "root"),
        DB_PWD("dbPassword", "delavalpwd"),
        JMS_CONNECTION("jmsConnection", "failover:(tcp://localhost:61616)?startupMaxReconnectAttempts=1"),
        JMS_QUEUE_DEST_CLICK("jmsQueueClickLog", "UserTransactionClickLog"),
        JMS_QUEUE_DEST_EVENT("jmsQueueEventLog", "UserTransactionEventLog"),
        THREAD_POOL_SIZE("threadPoolSize", "500"),
        LOGGING_KEY("loggingKey", "utlserver"),
        DELETE_LOGS_EVENT_START("eventStartDay", "'2016-01-13 10:55:00'"),
        DELETE_LOGS_INTERVAL_DEFAULT_IN_DAYS("deleteLogsIntervalDefault", InitDAO.DEFAULT_DELETE_INTERVAL_IN_DAYS),
        SYSTEM_PROPERTY_NAME_DELETE_CLICK_LOGS_INTERVAL("deleteClickLogsIntervalName", "deleteClickLogsIntervalName"),
        SYSTEM_PROPERTY_NAME_DELETE_EVENT_LOGS_INTERVAL("deleteEventLogsIntervalName", "deleteEventLogsIntervalName"),
        CONNECTION_TIMEOUT_IN_SECONDS("connectionTimeoutInSeconds", "10"),
        UTLS_LOG_CACHE_MAX_SIZE("utlsLogCacheMaxSize", "500"),
        FETCH_LOG_USER_DELPRO("fetchLogUserDelpro", "delPr0backup2017"), // 0 is a zero,
        FETCH_LOG_USER_TOOL("fetchLogUserTool", "utlsT00l2017rule"), // 0 is zeros
        RSA_ALGORITHM("rsaAlgorithm", "RSA/ECB/PKCS1Padding"),
        RSA_KEY_GEN("rsaKeyGen", "RSA"),
        AES_SECRET_KEY_ALGORITHM("AES", "AES"),
        AES_CIPHER_ALGORITHM("AES_CIPHER_ALGORITHM", "AES/ECB/PKCS5Padding"),
        WEBSOCKET_PORT("websocketPort", "8085");

        private String myName;
        private String defaultValue;

        PropKey(String name, String defaultValue) {
            myName = name;
            this.defaultValue = defaultValue;
        }

        public String getMyName() {
            return myName;
        }

        public String getDefaultValue(){
            return defaultValue;
        }
    }

    private Properties prop = new Properties();

    private ServerProperties() {
        setProperties();
    }

    public synchronized static ServerProperties getInstance() {
        if (_instance == null) {
            _instance = new ServerProperties();
        }
        return _instance;
    }

    private String get(PropKey propType) {
        return prop.getProperty(propType.getMyName());
    }

    /**
     * Get serverproperty with a certain key
     *
     * @param propkey
     * @return the value of the property with this key
     */
    public String getProp(PropKey propkey) {
        return get(propkey);
    }

    private Properties setProperties() {
        FileInputStream fIn = null;
        FileOutputStream fOut = null;
        try {
            fIn = new FileInputStream(PROP_FILE_NAME);
            prop.load(fIn);
            addMissingProperty();
            writePropFile(fOut);

        } catch (IOException ex) {
            try {
                // Properties-file doesnt exist, create default values
                Arrays.stream(PropKey.values()).forEach(propKey -> {
                    set(propKey);
                });
                writePropFile(fOut);

            } catch (IOException e) {
                System.out.println("Got problem creating " + PROP_FILE_NAME + " file! ");
            }
        }
        finally {
            if(fIn != null){
                try {
                    fIn.close();
                } catch (IOException e) {
                    UtlsLogUtil.error(ServerProperties.class, "Couldnt close FileInputStream:", e.getMessage());
                }
            }
            if(fOut != null){
                try {
                    fOut.close();
                } catch (IOException e) {
                    UtlsLogUtil.error(ServerProperties.class, "Couldnt close FileOutputStream:", e.getMessage());
                }
            }
        }
        return prop;
    }

    private void writePropFile(FileOutputStream fOut) throws IOException {
        fOut = new FileOutputStream(PROP_FILE_NAME);
        prop.store(fOut, "Serverproperties needed");
    }

    private void addMissingProperty(){
        Arrays.stream(PropKey.values()).forEach(propKey -> {
            if(isMissing(propKey)){
                set(propKey);
            }
        });
    }

    private boolean isMissing(PropKey propKey){
        return get(propKey) == null;
    }

    private void set(PropKey propType) {
        prop.setProperty(propType
          .getMyName(), propType.getDefaultValue());
    }
}
