package com.delaval.usertransactionlogserver.domain;

import javax.crypto.Cipher;
import java.util.Date;

/**
 * Here is the criteria for how the logs shall be fetched.
 * From and to-dates and if it shall be encrypted or not.
 */
public class FetchLogCriteria {

    protected boolean encrypt;
    protected byte[] decryptedClientKey;
    protected Cipher rsaCipher;
    protected Cipher aesCipher;
    protected Date from;
    protected Date to;

    protected FetchLogCriteria(){}

    public boolean isEncrypt() {
        return encrypt;
    }

    public byte[] getDecryptedClientKey() {
        return decryptedClientKey;
    }

    public Cipher getRsaCipher() {
        return rsaCipher;
    }

    public Date getFrom() {
        return from;
    }

    public Date getTo() {
        return to;
    }

    public Cipher getAesCipher() {
        return aesCipher;
    }
}
