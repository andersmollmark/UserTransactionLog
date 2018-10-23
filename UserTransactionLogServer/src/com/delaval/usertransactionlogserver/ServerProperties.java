package com.delaval.usertransactionlogserver;

import com.delaval.usertransactionlogserver.persistence.dao.InitDAO;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * Singleton.
 * Holds all the properties needed in the UserTransactionLogServer.
 * Loads all properties that exist on file when system boots, if it exists any file.
 * Updates the file with new properties if there are more in the system than on the file.
 */
public class ServerProperties {

    private static ServerProperties _instance;

    private static final String PROP_FILE_NAME = "UserTransactionLogServer.properties";
    private static final String DEFAULT_UTLS_CACE_FILE_PATH = "/opt/utls/data/";
    private static final String logCache = "logCache";

    public enum PropKey {
        DB_SERVER_HOST("dbServerHost", "localhost"),
        DB_SERVER_PORT("dbServerPort", "3306"),
        DB_NAME("dbName", "user_transaction_log_server"),
        DB_USER("dbUser", "root"),
        DB_PWD("dbPassword", "delavalpwd"),
        JMS_CONNECTION("jmsConnection", "failover:(tcp://localhost:61616)?initialReconnectDelay=200&startupMaxReconnectAttempts=1"),
        JMS_QUEUE_DEST_EVENT("jmsQueueEventLog", "UserTransactionEventLog"),
        THREAD_POOL_SIZE("threadPoolSize", "500"),
        LOGGING_KEY("loggingKey", "utlserver"),
        DELETE_LOGS_EVENT_START("eventStartDay", "'2016-01-13 10:55:00'"),
        DELETE_LOGS_INTERVAL_DEFAULT_IN_DAYS("deleteLogsIntervalDefault", InitDAO.DEFAULT_DELETE_INTERVAL_IN_DAYS),
        SYSTEM_PROPERTY_NAME_DELETE_EVENT_LOGS_INTERVAL("deleteEventLogsIntervalName", "deleteEventLogsIntervalName"),
        CONNECTION_TIMEOUT_IN_SECONDS("connectionTimeoutInSeconds", "10"),
        UTLS_LOG_CACHE_MAX_SIZE("utlsLogCacheMaxSize", "1000"),
        FETCH_LOG_USER_DELPRO("fetchLogUserDelpro", "delPr0backup2017"), // 0 is a zero,
        FETCH_LOG_USER_TOOL("fetchLogUserTool", "utlsT00l2017rule"), // 0 is zeros
        RSA_ALGORITHM("rsaAlgorithm", "RSA/ECB/PKCS1Padding"),
        RSA_KEY_GEN("rsaKeyGen", "RSA"),
        AES_SECRET_KEY_ALGORITHM("AES", "AES"),
        AES_CIPHER_ALGORITHM("AES_CIPHER_ALGORITHM", "AES/ECB/PKCS5Padding"),
        UTLS_CACHE_FILE_PATH("utlsCacheFilePath", DEFAULT_UTLS_CACE_FILE_PATH + ServerProperties.logCache),
        WEBSOCKET_PORT("websocketPort", "8085");

        private final String myKey;
        private final String defaultValue;

        PropKey(String value, String defaultValue) {
            myKey = value;
            this.defaultValue = defaultValue;
        }

        public String getMyKey() {
            return myKey;
        }

        public String getDefaultValue() {
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
        String result = prop.getProperty(propType.getMyKey());
        if(result == null) {
            UtlsLogUtil.warn(this.getClass(),"property is missing in file, adding it:", propType.myKey);
            result = getPropAndAddItToFile(propType);
        }
        return result;
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

        Path propPath = Paths.get(PROP_FILE_NAME);
        if(Files.exists(propPath)){
            loadPropertiesFromFile(propPath);
        }
        else {
            initProperties();
            createPropertyFile(propPath);
        }

        return prop;
    }

    void loadPropertiesFromFile(Path propPath) {
        try(BufferedReader reader = Files.newBufferedReader(propPath)){
            prop.load(reader);
        }
        catch (IOException e) {
            System.out.println("Got problem creating " + PROP_FILE_NAME + " file! ");
            UtlsLogUtil.error(this.getClass(),"couldnt read propertiesfile:", PROP_FILE_NAME);
        }
    }

    void initProperties() {
        for(PropKey key: PropKey.values()) {
            prop.setProperty(key.getMyKey(), key.defaultValue);
        }
    }

    void createPropertyFile(Path propPath) {
        try (BufferedWriter writer = Files.newBufferedWriter(propPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            prop.store(writer, "Creating new propertiesfile");
            UtlsLogUtil.info(this.getClass(), "Creating new propertiesfile");
        }
        catch (IOException e) {
            UtlsLogUtil.error(this.getClass(), "something went wrong while creating propertiesfile:", PROP_FILE_NAME, " Exception:", e.toString());
        }

    }

    String getPropAndAddItToFile(PropKey propKey) {
        Path propPath = Paths.get(PROP_FILE_NAME);
        prop.setProperty(propKey.getMyKey(), propKey.defaultValue);
        try (BufferedWriter writer = Files.newBufferedWriter(propPath, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            writer.write(propKey.getMyKey() + "=" + propKey.getDefaultValue());
            writer.newLine();
            UtlsLogUtil.info(this.getClass(), "adding property:", propKey.myKey, " to file:", PROP_FILE_NAME);
        }
        catch (IOException e) {
            UtlsLogUtil.error(this.getClass(), "something went wrong when adding property ", propKey.getMyKey(), " to file:", PROP_FILE_NAME, " Exception:", e.toString());
        }
        return propKey.defaultValue;
    }

}
