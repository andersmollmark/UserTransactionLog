package com.delaval.usertransactionlogserver.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Created by delaval on 2017-06-20.
 */
public class FetchLogDTO {

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
}
