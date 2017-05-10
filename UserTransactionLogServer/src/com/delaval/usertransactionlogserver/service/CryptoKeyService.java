package com.delaval.usertransactionlogserver.service;

import com.delaval.usertransactionlogserver.ServerProperties;
import com.delaval.usertransactionlogserver.domain.InternalSystemProperty;
import com.delaval.usertransactionlogserver.persistence.dao.OperationDAO;
import com.delaval.usertransactionlogserver.persistence.operation.CreateSystemPropertyOperation;
import com.delaval.usertransactionlogserver.persistence.operation.GetSystemPropertyWithNameOperation;
import com.delaval.usertransactionlogserver.persistence.operation.OperationFactory;
import com.delaval.usertransactionlogserver.persistence.operation.OperationParam;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import com.delaval.usertransactionlogserver.websocket.PublicKeyAsJsonMessage;
import com.google.gson.Gson;

import java.security.*;
import java.util.Date;

/**
 * Handles the public and private key for utl-server.
 *
 */
public class CryptoKeyService {

    private static CryptoKeyService _cryptoKeyServiceInstance;
    private KeyPair keyPair;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private static final int KEYLENGTH = 1024;
    private static final String PUBLIC_KEY_NAME = "publicKey";
    private static final String PRIVATE_KEY_NAME = "privateKey";

    private CryptoKeyService() {
        generateKeys();
    }

    public static synchronized void init(){
        getInstance();
    }

    public static synchronized CryptoKeyService getInstance() {
        if (_cryptoKeyServiceInstance == null) {
            _cryptoKeyServiceInstance = new CryptoKeyService();
        }
        return _cryptoKeyServiceInstance;
    }

    private void generateKeys() {
        if (this.keyPair == null) {
            try {
                createKeys();
//                saveKeyToDB(PUBLIC_KEY_NAME, getPublicKey().getEncoded());
//                saveKeyToDB(PRIVATE_KEY_NAME, getPrivateKey().getEncoded());
            } catch (NoSuchAlgorithmException e) {
                UtlsLogUtil.error(this.getClass(), e.getMessage());
            }
        }
    }


    private void createKeys() throws NoSuchAlgorithmException {
        String cryptoAlgorithm = ServerProperties.getInstance().getProp(ServerProperties.PropKey.RSA_KEY_GEN);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(cryptoAlgorithm);
        keyPairGenerator.initialize(KEYLENGTH);
        this.keyPair = keyPairGenerator.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public String getPublicKeyAsJson() {
        String publicKeyAsString = new String(getPublicKey().getEncoded());
        PublicKeyAsJsonMessage result = new PublicKeyAsJsonMessage();
        Gson gson = new Gson();
        result.setPublicKey(publicKeyAsString);
        return gson.toJson(result);
    }

    private void saveKeyToDB(String name, byte[] key) {
        InternalSystemProperty keyProperty = new InternalSystemProperty();
        keyProperty.setName(name);
        keyProperty.setValue(new String(key));
        keyProperty.setTimestamp(new Date());
        OperationParam<CreateSystemPropertyOperation> createKeyForSystem = OperationFactory.getCreateSystemPropertyParamForSystem(keyProperty);
        OperationDAO.getInstance().doCreateUpdate(createKeyForSystem);
    }


}
