package com.delaval.usertransactionlogserver.service;

import com.delaval.usertransactionlogserver.domain.InternalEventLog;
import com.delaval.usertransactionlogserver.domain.InternalUserTransactionKey;
import com.delaval.usertransactionlogserver.persistence.dao.OperationDAO;
import com.delaval.usertransactionlogserver.persistence.entity.EventLog;
import com.delaval.usertransactionlogserver.persistence.entity.UserTransactionKey;
import com.delaval.usertransactionlogserver.persistence.operation.GetAllUserTransactionKeysOperation;
import com.delaval.usertransactionlogserver.persistence.operation.GetEventLogsWithUserTransactionKeyOperation;
import com.delaval.usertransactionlogserver.persistence.operation.OperationParam;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
/**
 * Created by delaval on 2016-08-30.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({OperationDAO.class})
public class FetchAllEventLogsServiceTest {

    @Mock
    GetEventLogsWithUserTransactionKeyOperation getEventLogsWithUserTransactionKeyOperationMock;

    @Mock
    GetAllUserTransactionKeysOperation getAllUserTransactionKeysOperationMock;

    @Mock
    OperationParam<GetAllUserTransactionKeysOperation> mockParam;

    @Mock
    OperationDAO operationDAO;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();



    @Test
    public void getAllEventLogsAsJson() throws Exception {
        FetchAllEventLogsService fetchService = Mockito.spy(FetchAllEventLogsService.class);
        List<InternalUserTransactionKey> internalUserTransactionKeyList = getInternalUserTransactionKeyList();
        Mockito.doReturn(internalUserTransactionKeyList).when(fetchService).getUserTransactionIds();

        List<InternalEventLog> eventLogsWithOneUserTransactionId1 = getEventLogsWithOneUserTransactionId(internalUserTransactionKeyList.get(0).getId());
//        JsonElement element1 = gson.toJsonTree(eventLogsWithOneUserTransactionId1, new TypeToken<List<InternalEventLog>>(){}.getType());
//        JsonArray jsonArray1 = element1.getAsJsonArray();
//        Mockito.doReturn(jsonArray1).when(fetchService).getJsonEventLogsWithUserTransactionId(internalUserTransactionKeyList.get(0));
        List<InternalEventLog> eventLogsWithOneUserTransactionId2 = getEventLogsWithOneUserTransactionId(internalUserTransactionKeyList.get(1).getId());

        Mockito.doReturn(eventLogsWithOneUserTransactionId1).when(fetchService).getEventLogsWithUserTransactionId(internalUserTransactionKeyList.get(0));
        Mockito.doReturn(eventLogsWithOneUserTransactionId2).when(fetchService).getEventLogsWithUserTransactionId(internalUserTransactionKeyList.get(1));

//        List<InternalEventLog> eventLogsWithOneUserTransactionId2 = getEventLogsWithOneUserTransactionId(internalUserTransactionKeyList.get(1).getId());
//        JsonElement element2 = gson.toJsonTree(eventLogsWithOneUserTransactionId2, new TypeToken<List<InternalEventLog>>(){}.getType());
//        JsonArray jsonArray2 = element2.getAsJsonArray();
//        Mockito.doReturn(jsonArray2).when(fetchService).getJsonEventLogsWithUserTransactionId(internalUserTransactionKeyList.get(1));

        JsonArray allEventLogsAsJson = fetchService.getAllEventLogsAsJson();
        assertEquals(allEventLogsAsJson.size(), 4);

        assertOneJsonElement(allEventLogsAsJson.get(0).toString(), eventLogsWithOneUserTransactionId1.get(0));
        assertOneJsonElement(allEventLogsAsJson.get(1).toString(), eventLogsWithOneUserTransactionId1.get(1));
        assertOneJsonElement(allEventLogsAsJson.get(2).toString(), eventLogsWithOneUserTransactionId2.get(0));
        assertOneJsonElement(allEventLogsAsJson.get(3).toString(), eventLogsWithOneUserTransactionId2.get(1));

        Gson pretty = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("pretty\n" + pretty.toJson(allEventLogsAsJson));
        System.out.println(allEventLogsAsJson.toString());

    }

    @Test
    public void createJsonDumpTest() throws Exception {
        FetchAllEventLogsService fetchService = Mockito.spy(FetchAllEventLogsService.class);
        List<InternalUserTransactionKey> internalUserTransactionKeyList = getInternalUserTransactionKeyList();
        Mockito.doReturn(internalUserTransactionKeyList).when(fetchService).getUserTransactionIds();

        Gson gson = new Gson();
        List<InternalEventLog> eventLogsWithOneUserTransactionId1 = getEventLogsWithOneUserTransactionId(internalUserTransactionKeyList.get(0).getId());
        List<InternalEventLog> eventLogsWithOneUserTransactionId2 = getEventLogsWithOneUserTransactionId(internalUserTransactionKeyList.get(1).getId());

        Mockito.doReturn(eventLogsWithOneUserTransactionId1).when(fetchService).getEventLogsWithUserTransactionId(internalUserTransactionKeyList.get(0));
        Mockito.doReturn(eventLogsWithOneUserTransactionId2).when(fetchService).getEventLogsWithUserTransactionId(internalUserTransactionKeyList.get(1));

        String jsonDump = fetchService.getJsonDumpMessage();

        Gson pretty = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("pretty\n" + jsonDump);

    }

    private void assertOneJsonElement(String jsonElementAsString, InternalEventLog internalEventLog){
        Gson gson = new Gson();
        InternalEventLog resultFromJson = gson.fromJson(jsonElementAsString, InternalEventLog.class);
        assertEquals(internalEventLog.getId(), resultFromJson.getId());
        assertEquals(internalEventLog, resultFromJson);
    }

    @Test
    public void fetchAllEventLogs() throws Exception {

    }

    @Test
    public void getUserTransactionIds() throws Exception {
        PowerMockito.mockStatic(OperationDAO.class);
        PowerMockito.when(OperationDAO.getInstance()).thenReturn(operationDAO);
        Mockito.when(operationDAO.executeOperation(Mockito.any())).thenReturn(getAllUserTransactionKeysOperationMock);
        List<InternalUserTransactionKey> dummyResult = getInternalUserTransactionKeyList();
        PowerMockito.when(getAllUserTransactionKeysOperationMock.getResult()).thenReturn(dummyResult);
        FetchAllEventLogsService testService = new FetchAllEventLogsService();
        List<InternalUserTransactionKey> userTransactionIds = testService.getUserTransactionIds();
        assertEquals(userTransactionIds.size(), dummyResult.size());

    }

    @Test
    public void getJsonEventLogsWithUserTransactionId_withOneUserTransactionId() throws Exception {
        InternalUserTransactionKey internalUserTransactionKey = getInternalUserTransactionKeyList().get(0);
        FetchAllEventLogsService fetchService = Mockito.spy(FetchAllEventLogsService.class);
        List<InternalEventLog> eventLogsWithOneUserTransactionId = getEventLogsWithOneUserTransactionId(internalUserTransactionKey.getId());
        Mockito.doReturn(eventLogsWithOneUserTransactionId).when(fetchService).getEventLogsWithUserTransactionId(Mockito.any());
        JsonArray eventLogsAsJson = fetchService.getJsonEventLogsWithUserTransactionId(internalUserTransactionKey);
        assertEquals(eventLogsAsJson.size(), 2);
        Gson gson = new Gson();
        InternalEventLog resultFromJson = gson.fromJson(eventLogsAsJson.get(0).toString(), InternalEventLog.class);
        assertEquals(eventLogsWithOneUserTransactionId.get(0).getId(), resultFromJson.getId());
        assertEquals(eventLogsWithOneUserTransactionId.get(0), resultFromJson);

    }

    @Test
    public void getEventLogsWithUserTransactionId_WithOneUserTransactionId() throws Exception {
        String userTransactionId = "userTrans1";
        List<InternalEventLog> eventLogsWithOneUserTransactionId = getEventLogsWithOneUserTransactionId(userTransactionId);

        PowerMockito.mockStatic(OperationDAO.class);
        PowerMockito.when(OperationDAO.getInstance()).thenReturn(operationDAO);
        Mockito.when(operationDAO.executeOperation(Mockito.any())).thenReturn(getEventLogsWithUserTransactionKeyOperationMock);

        PowerMockito.when(getEventLogsWithUserTransactionKeyOperationMock.getResult()).thenReturn(eventLogsWithOneUserTransactionId);
        FetchAllEventLogsService testService = new FetchAllEventLogsService();
        List<InternalEventLog> eventLogsWithUserTransactionId = testService.getEventLogsWithUserTransactionId(getInternalUserTransactionKeyList().get(0));
        assertEquals(eventLogsWithUserTransactionId.size(), eventLogsWithOneUserTransactionId.size());

    }

    private List<InternalEventLog> getEventLogsWithOneUserTransactionId(String userTransId){
        List<InternalEventLog> result = new ArrayList<>();
        InternalEventLog log1 = new InternalEventLog(createEventLog("id1", userTransId));
        result.add(log1);
        InternalEventLog log2 = new InternalEventLog(createEventLog("id2", userTransId));
        result.add(log2);
        return result;
    }

    private MyEventLog createEventLog(String id, String userTransId){
        MyEventLog result = new MyEventLog();
        result.id = id;
        result.userTransactionKeyId = userTransId;
        result.category = "category";
        result.host = "host";
        result.timestamp = new Date();
        result.name = "name";
        result.label = "label";
        result.tab = "tab";
        return result;
    }

    private List<InternalUserTransactionKey> getInternalUserTransactionKeyList(){
        MyUserTransactionKey test1 = new MyUserTransactionKey();
        test1.username = "Chuck Norris";
        test1.target = "Everything";
        test1.client = "Noone";
        test1.id = "TheOneAndOnly";
        test1.timestamp = new Date();

        MyUserTransactionKey test2 = new MyUserTransactionKey();
        test2.username = "Sly Stallone";
        test2.target = "Nothing";
        test2.client = "Someone";
        test2.id = "Rambo";
        test2.timestamp = new Date();
        InternalUserTransactionKey internal1 = new InternalUserTransactionKey(test1);
        InternalUserTransactionKey internal2 = new InternalUserTransactionKey(test2);
        List<InternalUserTransactionKey> result = new ArrayList<>();
        result.add(internal1);
        result.add(internal2);
        return result;
    }

    private static class MyEventLog extends EventLog{
        String id;
        String name;
        String category;
        String label;
        String tab;
        String userTransactionKeyId;
        Date timestamp;
        String host;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getCategory() {
            return category;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public String getTab() {
            return tab;
        }

        @Override
        public String getUserTransactionKeyId() {
            return userTransactionKeyId;
        }

        @Override
        public Date getTimestamp() {
            return timestamp;
        }

        @Override
        public String getHost() {
            return host;
        }

    }

    private static class MyUserTransactionKey extends UserTransactionKey{
        String id;
        String username;
        String target;
        String client;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getTarget() {
            return target;
        }

        @Override
        public String getClient() {
            return client;
        }

        Date timestamp;


        @Override
        public Date getTimestamp(){
            return timestamp;
        }
    }

}