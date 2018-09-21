package com.delaval.usertransactionlogserver.websocket;

/**
 * Dataholder for the message that fetch logs from the registered log-users (delpro, utls-tool)
 */
public class WebSocketTimezoneFetchLogMessage extends WebSocketFetchLogMessage{

    String timezone;

    public long getFromInMillis(){
        return Long.parseLong(fromInMillis);
    }

    public long getToInMillis(){
        return Long.parseLong(toInMillis);
    }

    public String getTimezone() {
        return timezone;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(" timezone:").append(timezone);
        return sb.toString();
    }
}
