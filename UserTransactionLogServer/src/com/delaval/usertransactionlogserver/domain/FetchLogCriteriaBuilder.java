package com.delaval.usertransactionlogserver.domain;

import javax.crypto.Cipher;
import java.util.Date;

/**
 * Created by delaval on 2017-05-05.
 */
public class FetchLogCriteriaBuilder {
    private FetchLogCriteria criteria;

    public FetchLogCriteriaBuilder(){
        criteria = new FetchLogCriteria();
    }

    public FetchLogCriteriaBuilder encrypt(boolean shallEncrypt){
        criteria.encrypt = shallEncrypt;
        return this;
    }

    public FetchLogCriteriaBuilder clientKey(byte[] clientKey){
        criteria.decryptedClientKey = clientKey;
        return this;
    }

    public FetchLogCriteriaBuilder rsaCipher(Cipher rsaCipher){
        criteria.rsaCipher = rsaCipher;
        return this;
    }

    public FetchLogCriteriaBuilder aesCipher(Cipher aesCipher){
        criteria.aesCipher = aesCipher;
        return this;
    }

    public FetchLogCriteriaBuilder from(Date from){
        criteria.from = from;
        return this;
    }

    public FetchLogCriteriaBuilder to(Date to){
        criteria.to = to;
        return this;
    }

    public FetchLogCriteria build(){
        return criteria;
    }
}
