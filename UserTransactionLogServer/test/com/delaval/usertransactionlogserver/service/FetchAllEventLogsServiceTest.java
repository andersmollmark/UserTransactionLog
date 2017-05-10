package com.delaval.usertransactionlogserver.service;

import com.delaval.usertransactionlogserver.ServerProperties;
import com.delaval.usertransactionlogserver.domain.FetchLogCriteria;
import com.delaval.usertransactionlogserver.domain.FetchLogCriteriaBuilder;
import com.delaval.usertransactionlogserver.domain.InternalEventLog;
import com.delaval.usertransactionlogserver.domain.InternalUserTransactionKey;
import com.delaval.usertransactionlogserver.persistence.dao.OperationDAO;
import com.delaval.usertransactionlogserver.persistence.entity.EventLog;
import com.delaval.usertransactionlogserver.persistence.entity.UserTransactionKey;
import com.delaval.usertransactionlogserver.persistence.operation.GetAllUserTransactionKeysOperation;
import com.delaval.usertransactionlogserver.persistence.operation.GetEventLogsWithUserTransactionKeyOperation;
import com.delaval.usertransactionlogserver.persistence.operation.GetSystemPropertyWithNameOperation;
import com.delaval.usertransactionlogserver.persistence.operation.OperationParam;
import com.delaval.usertransactionlogserver.websocket.JsonDumpMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import main.TestUtils;
import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by delaval on 2016-08-30.
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*"})
@PrepareForTest({OperationDAO.class, CryptoKeyService.class, ServerProperties.class})
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

    @Mock
    CryptoKeyService mockCryptoKeyService;

    @Mock
    ServerProperties mockServerProperties;

    @Mock
    GetSystemPropertyWithNameOperation mockWithNameOperation;

    private static final String cryptoAlgorithm = "AES";

    @Before
    public void init() {
        PowerMockito.mockStatic(CryptoKeyService.class);
        Mockito.when(CryptoKeyService.getInstance()).thenReturn(mockCryptoKeyService);
        PowerMockito.mockStatic(ServerProperties.class);
        Mockito.when(ServerProperties.getInstance()).thenReturn(mockServerProperties);
        Mockito.when(mockServerProperties.getProp(ServerProperties.PropKey.AES_SECRET_KEY_ALGORITHM)).thenReturn("AES");
        Mockito.when(mockServerProperties.getProp(ServerProperties.PropKey.RSA_ALGORITHM)).thenReturn("RSA");
        Mockito.when(mockServerProperties.getProp(ServerProperties.PropKey.AES_CIPHER_ALGORITHM)).thenReturn("AES/ECB/PKCS5Padding");


    }

    @Test
    public void getEncryptedJsonLogs() throws Exception {
        // What to return as mock when fetching logs
        FetchAllEventLogsService fetchService = Mockito.spy(FetchAllEventLogsService.class);
        List<InternalUserTransactionKey> internalUserTransactionKeyList = getInternalUserTransactionKeyList();
        List<InternalEventLog> eventLogsWithOneUserTransactionId = getEventLogsWithOneUserTransactionId(internalUserTransactionKeyList.get(0).getId());
        JsonArray expectedJsonArray = createMockArray(internalUserTransactionKeyList, eventLogsWithOneUserTransactionId);
        Mockito.doReturn(expectedJsonArray).when(fetchService).getAllEventLogsAsJson();

        Mockito.doReturn(true).when(mockWithNameOperation).isResultOk();
        PowerMockito.mockStatic(OperationDAO.class);
        PowerMockito.when(OperationDAO.getInstance()).thenReturn(operationDAO);
        Mockito.when(operationDAO.doRead(Mockito.any())).thenReturn(mockWithNameOperation);

        // mock the utl-server private key
        KeyPair utlKeys = createCryptoKeyPair();
        Mockito.when(mockCryptoKeyService.getPrivateKey()).thenReturn(utlKeys.getPrivate());

        // Encrypt the key from client
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, utlKeys.getPublic());
        Key clientBlowfishKey = createClientBlowfishKey();
        byte[] encryptedClientKey = cipher.doFinal(clientBlowfishKey.getEncoded());

        // get encrypted data/logs
        String jsonDumpMessageWithEncryptedData = fetchService.getEncryptedJsonLogs(encryptedClientKey);
        JsonDumpMessage jsonDumpMessage = new Gson().fromJson(jsonDumpMessageWithEncryptedData, JsonDumpMessage.class);
        String encryptedLogs = TestUtils.getFieldValue("jsondump", JsonDumpMessage.class, jsonDumpMessage);

        // decrypt data/logs
        Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKey secretKey = new SecretKeySpec(clientBlowfishKey.getEncoded(), "AES");
        aesCipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] output = aesCipher.doFinal(Base64.decodeBase64(encryptedLogs));

        // -> should be the logs again
        String expectedDecryptedResult = new Gson().toJson(expectedJsonArray);
        assertEquals(expectedDecryptedResult, new String(output));
    }

    private KeyPair createCryptoKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        return keyPairGenerator.generateKeyPair();
    }

    private Key createClientBlowfishKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("Blowfish");
        keyGenerator.init(128);
        return keyGenerator.generateKey();
    }

    private JsonArray createMockArray(List<InternalUserTransactionKey> internalUserTransactionKeyList, List<InternalEventLog> eventLogsWithOneUserTransactionId) {
        InternalUserTransactionKey aKey = internalUserTransactionKeyList.get(0);
        eventLogsWithOneUserTransactionId.forEach(log -> {
            log.setUsername(aKey.getUsername());
            log.setTarget(aKey.getTarget());
        });
        Gson gson = new Gson();
        JsonElement element = gson.toJsonTree(eventLogsWithOneUserTransactionId, new TypeToken<List<InternalEventLog>>() {
        }.getType());
        return element.getAsJsonArray();
    }

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

    private void assertOneJsonElement(String jsonElementAsString, InternalEventLog internalEventLog) {
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
        Mockito.when(operationDAO.doRead(Mockito.any())).thenReturn(getAllUserTransactionKeysOperationMock);
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
        Mockito.when(operationDAO.doRead(Mockito.any())).thenReturn(getEventLogsWithUserTransactionKeyOperationMock);

        PowerMockito.when(getEventLogsWithUserTransactionKeyOperationMock.getResult()).thenReturn(eventLogsWithOneUserTransactionId);
        FetchAllEventLogsService testService = new FetchAllEventLogsService();
        List<InternalEventLog> eventLogsWithUserTransactionId = testService.getEventLogsWithUserTransactionId(getInternalUserTransactionKeyList().get(0));
        assertEquals(eventLogsWithUserTransactionId.size(), eventLogsWithOneUserTransactionId.size());

    }

    private List<InternalEventLog> getEventLogsWithOneUserTransactionId(String userTransId) {
        List<InternalEventLog> result = new ArrayList<>();
        InternalEventLog log1 = new InternalEventLog(createEventLog("id1", userTransId));
        result.add(log1);
        InternalEventLog log2 = new InternalEventLog(createEventLog("id2", userTransId));
        result.add(log2);
        return result;
    }

    private MyEventLog createEventLog(String id, String userTransId) {
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

    private List<InternalUserTransactionKey> getInternalUserTransactionKeyList() {
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

    private static class MyEventLog extends EventLog {
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

    private static class MyUserTransactionKey extends UserTransactionKey {
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
        public Date getTimestamp() {
            return timestamp;
        }
    }

}