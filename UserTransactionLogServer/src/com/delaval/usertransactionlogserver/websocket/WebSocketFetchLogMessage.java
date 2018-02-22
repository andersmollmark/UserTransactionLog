package com.delaval.usertransactionlogserver.websocket;

import com.delaval.usertransactionlogserver.util.DateUtil;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Dataholder for the message that fetch logs from the registered log-users (delpro, utls-tool)
 */
public class WebSocketFetchLogMessage extends WebSocketType{


    String fromInMillis;
    String toInMillis;

    public LocalDateTime getFrom(){
        return DateUtil.getLocalDateTimeFromMillis(Long.parseLong(fromInMillis));
    }

    public String getFromAsString(){
        return DateUtil.formatLocalDateTime(getFrom());
    }

    public String getToAsString(){
        return DateUtil.formatLocalDateTime(getTo());
    }

    public LocalDateTime getTo(){
        return DateUtil.getLocalDateTimeFromMillis(Long.parseLong(toInMillis));
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder(this.getClass().getCanonicalName());
        sb.append(", Messtype:").append(getType())
        .append(" from in UTC:")
        .append(getFromAsString())
        .append(" to in UTC:")
        .append(getToAsString());
        return sb.toString();
    }
}
