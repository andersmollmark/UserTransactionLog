package com.delaval.usertransactionlogserver.websocket;

/**
 * Created by delaval on 2016-08-31.
 */
public class PublicKeyAsJsonMessage {

    String messType = MessTypes.GET_PUBLIC_KEY.getMyValue();

    String publicKey;

    public void setPublicKey(String publicKey){
        this.publicKey = publicKey;
    }

}
