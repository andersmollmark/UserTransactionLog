package com.delaval.usertransactionlogserver.websocket;

/**
 * Dataholder for the message that fetch logs from the registered log-users (delpro, utls-tool)
 */
public class WebSocketFetchLogMessage extends WebSocketType{


    private byte[] encryptedClientKey;

    public byte[] getEncryptedClientKey() {
        return encryptedClientKey;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder(this.getClass().getCanonicalName());
        sb.append(", Messtype:").append(getType())
        .append(" has KeyParameter?")
        .append(encryptedClientKey != null);
        return sb.toString();
    }
}
