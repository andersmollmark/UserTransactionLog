package com.delaval.usertransactionlogserver.service;

import com.delaval.usertransactionlogserver.ServerProperties;
import com.delaval.usertransactionlogserver.TestObjectFactory;
import com.delaval.usertransactionlogserver.domain.FetchLogDTO;
import com.delaval.usertransactionlogserver.domain.InternalEventLog;
import com.delaval.usertransactionlogserver.domain.InternalSystemProperty;
import com.delaval.usertransactionlogserver.domain.InternalUserTransactionKey;
import com.delaval.usertransactionlogserver.persistence.dao.OperationDAO;
import com.delaval.usertransactionlogserver.persistence.operation.*;
import com.delaval.usertransactionlogserver.testobject.MyEventLog;
import com.delaval.usertransactionlogserver.testobject.MyUserTransactionKey;
import com.delaval.usertransactionlogserver.websocket.JsonDumpMessage;
import com.delaval.usertransactionlogserver.websocket.WebSocketFetchLogMessage;
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
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by delaval on 2016-08-30.
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*"})
@PrepareForTest({OperationDAO.class, CryptoKeyService.class, ServerProperties.class, OperationFactory.class})
public class FetchAllEventLogsServiceTest {

    @Mock
    GetEventLogsWithUserTransactionKeyOperation getEventLogsWithUserTransactionKeyOperationMock;

    @Mock
    GetAllUserTransactionKeysOperation getAllUserTransactionKeysOperationMock;

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

    @Mock
    GetEventLogsWithinTimespanOperation mockGetEventLogsOperation;

    @Mock
    OperationFactory mockFactory;

    @Mock
    WebSocketFetchLogMessage mockLogMessage;


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
        Mockito.when(mockServerProperties.getProp(ServerProperties.PropKey.FETCH_LOG_USER_TOOL)).thenReturn("utlsT00l2017rule");

        PowerMockito.mockStatic(OperationFactory.class);
        Mockito.when(OperationFactory.getEventLogsWithinTimespan(Mockito.any(FetchLogDTO.class))).thenReturn(mockGetEventLogsOperation);
        Mockito.when(OperationFactory.getSystemPropertyWithName(Mockito.anyString())).thenReturn(mockWithNameOperation);

    }

    @Test
    public void getEncryptedJsonLogs() throws Exception {
        // What to return as mock when fetching logs
        FetchAllEventLogsService fetchService = Mockito.spy(FetchAllEventLogsService.class);
        List<InternalUserTransactionKey> internalUserTransactionKeyList = getInternalUserTransactionKeyList();
        List<InternalEventLog> eventLogsWithOneUserTransactionId = getEventLogsWithOneUserTransactionId(internalUserTransactionKeyList.get(0).getId());
        JsonArray expectedJsonArray = createMockArray(internalUserTransactionKeyList, eventLogsWithOneUserTransactionId);
        Mockito.doReturn(expectedJsonArray).when(fetchService).getAllEventLogsAsJson();

        OperationResult<InternalEventLog> eventLogResult = new OperationResult<>(eventLogsWithOneUserTransactionId);
        Mockito.doReturn(eventLogResult).when(mockWithNameOperation).getResult();

        String clientKeyString = "utlsT00l2017rule";
        InternalSystemProperty mockResult = new InternalSystemProperty();
        mockResult.setValue(clientKeyString);
        List<InternalSystemProperty> clientKey = Arrays.asList(mockResult);
        OperationResult<InternalSystemProperty> systemPropResult = new OperationResult<>(clientKey);
        Mockito.doReturn(systemPropResult).when(mockWithNameOperation).getResult();
        PowerMockito.mockStatic(OperationDAO.class);
        PowerMockito.when(OperationDAO.getInstance()).thenReturn(operationDAO);
        Mockito.when(operationDAO.doRead(mockWithNameOperation)).thenReturn(systemPropResult);
        Mockito.when(operationDAO.doRead(mockGetEventLogsOperation)).thenReturn(eventLogResult);

        // get encrypted data/logs
        LocalDateTime now = LocalDateTime.now();

        Mockito.when(mockLogMessage.getFrom()).thenReturn(now);
        Mockito.when(mockLogMessage.getTo()).thenReturn(now);
        String jsonDumpMessageWithEncryptedData = fetchService.getEncryptedJsonLogs(mockLogMessage);
        JsonDumpMessage jsonDumpMessage = new Gson().fromJson(jsonDumpMessageWithEncryptedData, JsonDumpMessage.class);
        String encryptedLogs = TestUtils.getFieldValue("jsondump", JsonDumpMessage.class, jsonDumpMessage);

        // decrypt data/logs
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] thedigest = md.digest(clientKeyString.getBytes("UTF-8"));
        SecretKeySpec secretKey = new SecretKeySpec(thedigest, "AES");

        Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        aesCipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodeBase64 = Base64.decodeBase64(encryptedLogs);
        byte[] output = aesCipher.doFinal(decodeBase64);

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
        List<InternalUserTransactionKey> dummyResult = getInternalUserTransactionKeyList();
        OperationResult<InternalUserTransactionKey> result = new OperationResult<>(dummyResult);
        Mockito.when(operationDAO.doRead(Mockito.any(GetAllUserTransactionKeysOperation.class))).thenReturn(result);
        PowerMockito.when(getAllUserTransactionKeysOperationMock.getResult()).thenReturn(result);
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
        OperationResult<InternalEventLog> result = new OperationResult<>(eventLogsWithOneUserTransactionId);
        PowerMockito.mockStatic(OperationDAO.class);
        PowerMockito.when(OperationDAO.getInstance()).thenReturn(operationDAO);
        Mockito.when(operationDAO.doRead(Mockito.any(GetEventLogsWithUserTransactionKeyOperation.class))).thenReturn(result);

        PowerMockito.when(getEventLogsWithUserTransactionKeyOperationMock.getResult()).thenReturn(result);
        FetchAllEventLogsService testService = new FetchAllEventLogsService();
        List<InternalEventLog> eventLogsWithUserTransactionId = testService.getEventLogsWithUserTransactionId(getInternalUserTransactionKeyList().get(0));
        assertEquals(eventLogsWithUserTransactionId.size(), eventLogsWithOneUserTransactionId.size());

    }

    private List<InternalEventLog> getEventLogsWithOneUserTransactionId(String userTransId) {
        List<InternalEventLog> result = new ArrayList<>();
        MyEventLog eventLog1 = TestObjectFactory.createEventLog("id1", userTransId);
        InternalEventLog log1 = new InternalEventLog(eventLog1);
        result.add(log1);
        MyEventLog eventLog2 = TestObjectFactory.createEventLog("id2", userTransId);
        InternalEventLog log2 = new InternalEventLog(eventLog2);
        result.add(log2);
        return result;
    }


    private List<InternalUserTransactionKey> getInternalUserTransactionKeyList() {
        MyUserTransactionKey test1 = TestObjectFactory.getUserTransactionKey();
        test1.setUsername("Chuck Norris");
        test1.setTarget("Everything");
        test1.setClient("Noone");
        test1.setId("TheOneAndOnly");
        test1.setTimestamp(new Date());

        MyUserTransactionKey test2 = new MyUserTransactionKey();
        test2.setUsername("Sly Stallone");
        test2.setTarget("Nothing");
        test2.setClient("Someone");
        test2.setId("Rambo");
        test2.setTimestamp(new Date());
        InternalUserTransactionKey internal1 = new InternalUserTransactionKey(test1);
        InternalUserTransactionKey internal2 = new InternalUserTransactionKey(test2);
        List<InternalUserTransactionKey> result = new ArrayList<>();
        result.add(internal1);
        result.add(internal2);
        return result;
    }

}