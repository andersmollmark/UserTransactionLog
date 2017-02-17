package com.delaval.usertransactionlogserver.service;

import com.delaval.usertransactionlogserver.domain.InternalEventLog;
import com.delaval.usertransactionlogserver.domain.InternalUserTransactionKey;
import com.delaval.usertransactionlogserver.persistence.dao.OperationDAO;
import com.delaval.usertransactionlogserver.persistence.operation.GetAllUserTransactionKeysOperation;
import com.delaval.usertransactionlogserver.persistence.operation.GetEventLogsWithUserTransactionKeyOperation;
import com.delaval.usertransactionlogserver.persistence.operation.OperationParam;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import com.delaval.usertransactionlogserver.websocket.JsonDumpMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

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
        UtlsLogUtil.info(this.getClass(), "Writing jsonfile, path:" + filepath + " filename:" + filename);
        Path path = Paths.get(filepath + filename);
        try(BufferedWriter writer = Files.newBufferedWriter(path)){
            JsonArray allEventLogsAsJson = getAllEventLogsAsJson();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(allEventLogsAsJson));
            UtlsLogUtil.info(this.getClass(), "Created file, " + filepath + filename + " with db-content in json-format");
        } catch (IOException e) {
            UtlsLogUtil.error(this.getClass(), "something went wrong while writing to file:" + filepath + filename + " \nException:" + e.toString());
        }
    }

    public String getJsonDumpMessage(){
        JsonArray allEventLogsAsJson = getAllEventLogsAsJson();
        Gson gson = new Gson();
        JsonDumpMessage dump = new JsonDumpMessage();
        dump.setJsondump(gson.toJson(allEventLogsAsJson));
        return gson.toJson(dump);
    }


    JsonArray getAllEventLogsAsJson(){
        List<InternalUserTransactionKey> userTransactionIds = getUserTransactionIds();
        JsonArray arrayWithAllLogs = new JsonArray();
        userTransactionIds.forEach(id -> arrayWithAllLogs.addAll(getJsonEventLogsWithUserTransactionId(id)));
        return arrayWithAllLogs;
    }

    List<InternalUserTransactionKey> getUserTransactionIds(){
        OperationParam<GetAllUserTransactionKeysOperation> operationParam = new OperationParam<>(GetAllUserTransactionKeysOperation.class);
        GetAllUserTransactionKeysOperation operation = OperationDAO.getInstance().executeOperation(operationParam);
        return operation.getResult();
    }

    JsonArray getJsonEventLogsWithUserTransactionId(InternalUserTransactionKey aKey){
        List<InternalEventLog> eventLogsWithUserTransactionId = getEventLogsWithUserTransactionId(aKey);
        Gson gson = new Gson();
        JsonElement element = gson.toJsonTree(eventLogsWithUserTransactionId, new TypeToken<List<InternalEventLog>>(){}.getType());
        JsonArray jsonArray = element.getAsJsonArray();
        return  jsonArray;
    }

    List<InternalEventLog> getEventLogsWithUserTransactionId(InternalUserTransactionKey aKey){
        OperationParam<GetEventLogsWithUserTransactionKeyOperation> operationParam = new OperationParam<>(GetEventLogsWithUserTransactionKeyOperation.class);
        operationParam.setParameter(aKey.getId());
        GetEventLogsWithUserTransactionKeyOperation getEventLogsWithUserTransactionKey = OperationDAO.getInstance().executeOperation(operationParam);
        return getEventLogsWithUserTransactionKey.getResult();
    }
}
