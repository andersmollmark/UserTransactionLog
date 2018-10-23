package com.delaval.usertransactionlogserver.service;

import com.delaval.usertransactionlogserver.ServerProperties;
import com.delaval.usertransactionlogserver.domain.FetchLogDTO;
import com.delaval.usertransactionlogserver.domain.InternalEventLog;
import com.delaval.usertransactionlogserver.domain.InternalSystemProperty;
import com.delaval.usertransactionlogserver.domain.InternalUserTransactionKey;
import com.delaval.usertransactionlogserver.persistence.dao.OperationDAO;
import com.delaval.usertransactionlogserver.persistence.operation.*;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import com.delaval.usertransactionlogserver.websocket.JsonDumpMessage;
import com.delaval.usertransactionlogserver.websocket.MessTypes;
import com.delaval.usertransactionlogserver.websocket.WebSocketFetchLogMessage;
import com.delaval.usertransactionlogserver.websocket.WebSocketTimezoneFetchLogMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by delaval on 2016-08-30.
 */
public class FetchAllEventLogsService {

    public static final String DEFAULT_FILE_PATH_TMP = "/tmp/";
    public static final String DEFAULT_FILENAME_JSONDUMP = "jsonDump.json";
    private static final String LAST_DAY_FILENAME = "dataLastDay.enc";

    public void writeJsonDumpOnDefaultFile() {
        writeJsonDumpOnFile(DEFAULT_FILE_PATH_TMP, DEFAULT_FILENAME_JSONDUMP);
    }


    /**
     * Creates a file saved locally with all logs.
     */
    public void writeJsonDumpOnFile(String filepath, String filename) {
        UtlsLogUtil.info(this.getClass(), "Writing jsonfile, path:", filepath, " filename:", filename);
        Path path = Paths.get(filepath + filename);
        String allLogs = getJsonDumpMessage();
        writeDataToFile(path, allLogs);
        UtlsLogUtil.info(this.getClass(), "Created file, ", filepath, filename,
          " with db-content in json-format");

    }

    /**
     * Creates a file saved locally with all logs the last day.
     */
    public void writeLastDayDataOnFile(){
        UtlsLogUtil.info(this.getClass(), "Writing jsonfile, path:", DEFAULT_FILE_PATH_TMP, " filename:", LAST_DAY_FILENAME);
        Path path = Paths.get(DEFAULT_FILE_PATH_TMP + LAST_DAY_FILENAME);
        String logsLastDay = getEncryptedJsonLogsLastDay();
        writeDataToFile(path, logsLastDay);
        UtlsLogUtil.info(this.getClass(), "Created file, ", DEFAULT_FILE_PATH_TMP, LAST_DAY_FILENAME,
          " with db-content from last day in encrypted format");
        System.out.println("\nCreated file " + LAST_DAY_FILENAME + " at path " + DEFAULT_FILE_PATH_TMP);

    }

    private void writeDataToFile(Path path, String data) {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(data);
        } catch (IOException e) {
            UtlsLogUtil.error(this.getClass(), "something went wrong while writing to file:", path.toString(),
              " \nException:" + e.toString());
        }
    }

    /**
     * Fetch the logs in en encrypted json-way
     *
     * @return JsonDumpMessage with the dump as a encrypted string from logs
     */
    public synchronized String getEncryptedJsonLogs(WebSocketFetchLogMessage webSocketFetchLogMessage) {
        FetchLogDTO fetchLogDTO = new FetchLogDTO();
        fetchLogDTO.setFrom(webSocketFetchLogMessage.getFrom());
        fetchLogDTO.setTo(webSocketFetchLogMessage.getTo());
        String encryptedDump = getEncryptedDump(fetchLogDTO);
        JsonDumpMessage dump = new JsonDumpMessage(MessTypes.FETCH_ENCRYPTED_LOGS);
        dump.setJsondump(encryptedDump);
        TimeZone utc = TimeZone.getTimeZone("UTC");
        dump.setTimezoneId(utc.getID());
        return new Gson().toJson(dump);
    }

    /**
     * Fetch the logs with timezone
     * @return JsonDumpMessage with the dump as a encrypted string from logs
     */
    public synchronized String getEncryptedJsonLogsWithTimezone(WebSocketTimezoneFetchLogMessage webSocketTimezoneFetchLogMessage) {
        TimeZone timeZone = TimeZone.getTimeZone(webSocketTimezoneFetchLogMessage.getTimezone());
        ZonedDateTime fromAtLocation = Instant.ofEpochMilli(webSocketTimezoneFetchLogMessage.getFromInMillis()).atZone(timeZone.toZoneId());
        ZonedDateTime toAtLocation = Instant.ofEpochMilli(webSocketTimezoneFetchLogMessage.getToInMillis()).atZone(timeZone.toZoneId());

        ZonedDateTime from = fromAtLocation.withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime to = toAtLocation.withZoneSameInstant(ZoneOffset.UTC);

        FetchLogDTO fetchLogDTO = new FetchLogDTO();
        fetchLogDTO.setFrom(from.toLocalDateTime());
        fetchLogDTO.setTo(to.toLocalDateTime());
        fetchLogDTO.setZoneId(timeZone.toZoneId());

        JsonDumpMessage dump = new JsonDumpMessage(MessTypes.FETCH_ENCRYPTED_LOGS_WITH_TIMEZONE);
        dump.setJsondump(getEncryptedDump(fetchLogDTO));
        dump.setTimezoneId(webSocketTimezoneFetchLogMessage.getTimezone());
        return new Gson().toJson(dump);
    }

    /**
     * Fetch the logs with timezone
     * @return JsonDumpMessage with the dump as a encrypted string from logs
     */
    public synchronized String getEncryptedJsonLogsLastDay() {
        UtlsLogUtil.info(this.getClass(), "get encrypted logs...");
        JsonDumpMessage dump = new JsonDumpMessage(MessTypes.FETCH_ENCRYPTED_LOGS_LAST_DAY);
        TimeZone timeZone = TimeZone.getDefault();
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(1L);

        FetchLogDTO fetchLogDTO = new FetchLogDTO();
        fetchLogDTO.setFrom(from);
        fetchLogDTO.setTo(to);
        fetchLogDTO.setZoneId(timeZone.toZoneId());

        UtlsLogUtil.info(this.getClass(), fetchLogDTO.toString());
        dump.setJsondump(getEncryptedDump(fetchLogDTO));
        dump.setTimezoneId(timeZone.getID());
        return new Gson().toJson(dump);
    }

    private String getEncryptedDump(FetchLogDTO fetchLogDTO){
        Gson gson = new Gson();
        try {
            String userToolKeyName = ServerProperties.getInstance().getProp(ServerProperties.PropKey.FETCH_LOG_USER_TOOL);
            GetSystemPropertyWithNameOperation operation = OperationFactory.getSystemPropertyWithName(userToolKeyName);
            OperationResult<InternalSystemProperty> operationResult = OperationDAO.getInstance().doRead(operation);

            if (!operationResult.isResultOk()) {
                throw new IllegalArgumentException("Illegal clientkey used while trying to fetch logs:" + userToolKeyName);
            }

            JsonArray allEventLogsAsEncryptedJson = getAllEventLogsWithinTimespanAsJson(fetchLogDTO);
            String fetchedLogs = gson.toJson(allEventLogsAsEncryptedJson);

            return encryptData(fetchedLogs, operationResult.getResult().get(0).getValue());

        } catch (Exception e) {
            UtlsLogUtil.error(this.getClass(), "Something went wrong while fetching logs:", e.getMessage());
        }
        throw new IllegalArgumentException("Something went wrong while trying to fetch logs");

    }

    private String encryptData(String orig, String clientKey) throws Exception {
        byte[] originalData = orig.getBytes("utf-8");

        // Secret client-key
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] thedigest = md.digest(clientKey.getBytes("UTF-8"));
        SecretKeySpec skc = new SecretKeySpec(thedigest, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skc);

        byte[] cipherText = new byte[cipher.getOutputSize(originalData.length)];
        int ctLength = cipher.update(originalData, 0, originalData.length, cipherText, 0);
        cipher.doFinal(cipherText, ctLength);

        String encryptedData = Base64.encodeBase64String(cipherText);
        return encryptedData;
    }


    public String getJsonDumpMessage() {
        JsonArray allEventLogsAsJson = getAllEventLogsAsJson();
        Gson gson = new Gson();
        JsonDumpMessage dump = new JsonDumpMessage(MessTypes.JSON_DUMP);
        dump.setJsondump(gson.toJson(allEventLogsAsJson));
        TimeZone utc = TimeZone.getTimeZone("UTC");
        dump.setTimezoneId(utc.getID());
        return gson.toJson(dump);
    }

    JsonArray getAllEventLogsWithinTimespanAsJson(FetchLogDTO fetchLogDTO) {
        GetEventLogsWithinTimespanOperation operation = OperationFactory.getEventLogsWithinTimespan(fetchLogDTO);
        OperationResult<InternalEventLog> operationResult = OperationDAO.getInstance().doRead(operation);

        UtlsLogUtil.info(this.getClass(), "Number of eventlogs found:", Integer.toString(operationResult.getResult().size()));
        Gson gson = new Gson();
        JsonElement element = gson.toJsonTree(operationResult.getResult(), new TypeToken<List<InternalEventLog>>() {
        }.getType());
        return element.getAsJsonArray();
    }

    JsonArray getAllEventLogsAsJson() {
        UtlsLogUtil.info(this.getClass(), "Get all eventlogs and creating json-format");
        List<InternalUserTransactionKey> userTransactionIds = getUserTransactionIds();
        JsonArray arrayWithAllLogs = new JsonArray();
        userTransactionIds.forEach(id -> arrayWithAllLogs.addAll(getJsonEventLogsWithUserTransactionId(id)));
        return arrayWithAllLogs;
    }

    List<InternalUserTransactionKey> getUserTransactionIds() {
        GetAllUserTransactionKeysOperation operation = OperationFactory.getAllUserTransactionKeys();
        OperationResult<InternalUserTransactionKey> operationResult = OperationDAO.getInstance().doRead(operation);
        return operationResult.getResult();
    }

    JsonArray getJsonEventLogsWithUserTransactionId(InternalUserTransactionKey aKey) {
        List<InternalEventLog> eventLogsWithUserTransactionId = getEventLogsWithUserTransactionId(aKey);
        UtlsLogUtil.debug(this.getClass(), "Number of eventlogs found:", Integer.toString(eventLogsWithUserTransactionId.size()));
        eventLogsWithUserTransactionId.forEach(log -> {
            log.setUsername(aKey.getUsername());
            log.setTarget(aKey.getTarget());
            log.setDeviceIp(aKey.getClient());
        });
        Gson gson = new Gson();
        JsonElement element = gson.toJsonTree(eventLogsWithUserTransactionId, new TypeToken<List<InternalEventLog>>() {
        }.getType());
        JsonArray jsonArray = element.getAsJsonArray();
        return jsonArray;
    }

    List<InternalEventLog> getEventLogsWithUserTransactionId(InternalUserTransactionKey aKey) {
        GetEventLogsWithUserTransactionKeyOperation operation = OperationFactory.getEventLogsWithUserTransactionKey(aKey);
        OperationResult<InternalEventLog> operationResult = OperationDAO.getInstance().doRead(operation);
        return operationResult.getResult();
    }
}
