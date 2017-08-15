package com.delaval.usertransactionlogserver.websocket;

import com.delaval.usertransactionlogserver.util.DateUtil;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(" timezone:").append(timezone);
        return sb.toString();
    }
}
