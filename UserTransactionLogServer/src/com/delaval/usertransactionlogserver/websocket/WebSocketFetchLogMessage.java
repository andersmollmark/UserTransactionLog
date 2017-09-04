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
        LocalDateTime from =
          Instant.ofEpochMilli(Long.parseLong(fromInMillis)).atZone(ZoneOffset.UTC).toLocalDateTime();
        return from;
    }

    public String getFromAsString(){
        return DateUtil.formatLocalDateTime(getFrom());
    }

    public String getToAsString(){
        return DateUtil.formatLocalDateTime(getTo());
    }

    public LocalDateTime getTo(){
        LocalDateTime to =
          Instant.ofEpochMilli(Long.parseLong(toInMillis)).atZone(ZoneOffset.UTC).toLocalDateTime();
        return to;
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
