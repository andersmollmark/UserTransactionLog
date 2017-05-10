package com.delaval.usertransactionlogserver.service;

import com.delaval.usertransactionlogserver.ServerProperties;
import com.delaval.usertransactionlogserver.domain.InternalEventLog;
import com.delaval.usertransactionlogserver.domain.InternalUserTransactionKey;
import com.delaval.usertransactionlogserver.persistence.dao.OperationDAO;
import com.delaval.usertransactionlogserver.persistence.operation.*;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import com.delaval.usertransactionlogserver.websocket.JsonDumpMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by delaval on 2016-08-30.
 */
public class FetchAllEventLogsService {

    public static final String DEFAULT_FILE_PATH_TMP = "/tmp/";
    public static final String DEFAULT_FILENAME_JSONDUMP = "jsonDump";

    public void writeJsonDumpOnDefaultFile(){
        writeJsonDumpOnFile(DEFAULT_FILE_PATH_TMP, DEFAULT_FILENAME_JSONDUMP);
    }

    public void writeJsonDumpOnFile(String filepath, String filename){
        UtlsLogUtil.info(this.getClass(),
          "Writing jsonfile, path:", filepath,
          " filename:", filename);
        Path path = Paths.get(filepath + filename);
        try(BufferedWriter writer = Files.newBufferedWriter(path)){
            JsonArray allEventLogsAsJson = getAllEventLogsAsJson();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(allEventLogsAsJson));
            UtlsLogUtil.info(this.getClass(), "Created file, ", filepath, filename,
              " with db-content in json-format");
        } catch (IOException e) {
            UtlsLogUtil.error(this.getClass(), "something went wrong while writing to file:", filepath, filename,
              " \nException:" + e.toString());
        }
    }

    /**
     * Fetch the logs in en encrypted json-way
     * @param encryptedClientPublicKey This key is encrypted with Utl-serverns public-key
     * @return
     */
    public synchronized String getEncryptedJsonLogs(byte[] encryptedClientPublicKey){
        JsonDumpMessage dump = new JsonDumpMessage();
        Gson gson = new Gson();
        try {
            byte[] decryptedClientKey = getDecryptedClientKey(encryptedClientPublicKey);

            OperationParam<GetSystemPropertyWithNameOperation> systemPropertyWithNameParam = OperationFactory.getSystemPropertyWithNameParam(new String(decryptedClientKey));
            GetSystemPropertyWithNameOperation getClientKey = OperationDAO.getInstance().doRead(systemPropertyWithNameParam);

            if(!getClientKey.isResultOk()){
                throw new IllegalArgumentException("Illegal clientkey used while trying to fetch logs:" + new String(decryptedClientKey));
            }

            JsonArray allEventLogsAsEncryptedJson = getAllEventLogsAsJson();
            String fetchedLogs = gson.toJson(allEventLogsAsEncryptedJson);
            String encryptedDump = encryptData(fetchedLogs, decryptedClientKey);
            dump.setJsondump(encryptedDump);

        } catch (Exception e) {
            UtlsLogUtil.error(this.getClass(), "Something went wrong while fetching logs:", e.getMessage());
        }
        return gson.toJson(dump);

    }

    private String encryptData(String orig, byte[] decryptedClientKey) throws Exception {
        byte[] crypted = null;
        try {
            String aesSecretKeyAlgorithm = ServerProperties.getInstance().getProp(ServerProperties.PropKey.AES_SECRET_KEY_ALGORITHM);
            SecretKey secretKey = new SecretKeySpec(decryptedClientKey, aesSecretKeyAlgorithm);

            String aesCipherAlgorithm = ServerProperties.getInstance().getProp(ServerProperties.PropKey.AES_CIPHER_ALGORITHM);
            Cipher cipher = Cipher.getInstance(aesCipherAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            crypted = cipher.doFinal(orig.getBytes());
        } catch (Exception e) {
           UtlsLogUtil.error(this.getClass(), "Something went wrong while encrypting logs:", e.getMessage());
           throw e;
        }
        return new String(Base64.encodeBase64(crypted));
    }

    private byte[] getDecryptedClientKey(byte[] encryptedClientPublicKey) throws Exception {
        byte[] key = null;
        try {
            String rsaAlgorithm = ServerProperties.getInstance().getProp(ServerProperties.PropKey.RSA_ALGORITHM);
            Cipher rsaCipher = Cipher.getInstance(rsaAlgorithm);
            rsaCipher.init(Cipher.DECRYPT_MODE, CryptoKeyService.getInstance().getPrivateKey());
            key = rsaCipher.doFinal(encryptedClientPublicKey);
        }
        catch(Exception e ) {
            UtlsLogUtil.error(this.getClass(), "exception decrypting key: ", e.getMessage());
            throw e;
        }
        return key;
    }

    public String getJsonDumpMessage(){
        JsonArray allEventLogsAsJson = getAllEventLogsAsJson();
        Gson gson = new Gson();
        JsonDumpMessage dump = new JsonDumpMessage();
        dump.setJsondump(gson.toJson(allEventLogsAsJson));
        return gson.toJson(dump);
    }


    JsonArray getAllEventLogsAsJson(){
        UtlsLogUtil.info(this.getClass(),"Get all eventlogs and creating json-format");
        List<InternalUserTransactionKey> userTransactionIds = getUserTransactionIds();
        JsonArray arrayWithAllLogs = new JsonArray();
        userTransactionIds.forEach(id -> arrayWithAllLogs.addAll(getJsonEventLogsWithUserTransactionId(id)));
        return arrayWithAllLogs;
    }

    List<InternalUserTransactionKey> getUserTransactionIds(){
        OperationParam<GetAllUserTransactionKeysOperation> operationParam = OperationFactory.getAllUserTransactionKeyParam();
        GetAllUserTransactionKeysOperation operation = OperationDAO.getInstance().doRead(operationParam);
        return operation.getResult();
    }

    JsonArray getJsonEventLogsWithUserTransactionId(InternalUserTransactionKey aKey){
        List<InternalEventLog> eventLogsWithUserTransactionId = getEventLogsWithUserTransactionId(aKey);
        UtlsLogUtil.info(this.getClass(), "Number of eventlogs found:", Integer.toString(eventLogsWithUserTransactionId.size()));
        eventLogsWithUserTransactionId.forEach(log -> {
            log.setUsername(aKey.getUsername());
            log.setTarget(aKey.getTarget());
        });
        Gson gson = new Gson();
        JsonElement element = gson.toJsonTree(eventLogsWithUserTransactionId, new TypeToken<List<InternalEventLog>>(){}.getType());
        JsonArray jsonArray = element.getAsJsonArray();
        return  jsonArray;
    }

    List<InternalEventLog> getEventLogsWithUserTransactionId(InternalUserTransactionKey aKey){
        OperationParam<GetEventLogsWithUserTransactionKeyOperation> operationParam = OperationFactory.getEventLogsWithUserTransactionKeyParam(aKey);
        GetEventLogsWithUserTransactionKeyOperation getEventLogsWithUserTransactionKey = OperationDAO.getInstance().doRead(operationParam);
        return getEventLogsWithUserTransactionKey.getResult();
    }
}
