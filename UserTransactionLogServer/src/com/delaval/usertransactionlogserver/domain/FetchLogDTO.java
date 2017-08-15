package com.delaval.usertransactionlogserver.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Created by delaval on 2017-06-20.
 */
public class FetchLogDTO {

    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private LocalDateTime from;
    private LocalDateTime to;
    private ZoneId zoneId;

    public LocalDateTime getFrom() {
        return from;
    }

    public void setFrom(LocalDateTime from) {
        this.from = from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public void setTo(LocalDateTime to) {
        this.to = to;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder(FetchLogDTO.class.getCanonicalName());
        sb.append(", from:").append(from.format(dtf))
          .append(", to:").append(to.format(dtf))
          .append(", zone:").append(TimeZone.getTimeZone(zoneId).getDisplayName());
        return sb.toString();
    }
}
