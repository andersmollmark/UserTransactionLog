package com.delaval.usertransactionlogserver.service;

import com.delaval.usertransactionlogserver.websocket.PublicKeyAsJsonMessage;
import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.security.PublicKey;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by delaval on 2017-05-04.
 */
@RunWith(PowerMockRunner.class)
public class CryptoKeyServiceTest {

    CryptoKeyService cryptoKeyService = CryptoKeyService.getInstance();


    @Mock
    PublicKey mockKey;

    @Test
    public void testGetPublicKeyAsJson(){
        CryptoKeyService mockedKeyService = mock(CryptoKeyService.class);
        String expectedKey = "expectedKey";
        PublicKeyAsJsonMessage expectedResult = new PublicKeyAsJsonMessage();
        expectedResult.setPublicKey(expectedKey);
        String expectedJson = new Gson().toJson(expectedResult);

        when(mockKey.getEncoded()).thenReturn(expectedKey.getBytes());
        when(mockedKeyService.getPublicKey()).thenReturn(mockKey);
        when(mockedKeyService.getPublicKeyAsJson()).thenCallRealMethod();
        String publicKeyAsJson = mockedKeyService.getPublicKeyAsJson();
        System.out.println("json:" + publicKeyAsJson);
        assertEquals(expectedJson, publicKeyAsJson);

    }

}