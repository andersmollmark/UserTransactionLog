package com.delaval.usertransactionlogserver.domain;

import com.delaval.usertransactionlogserver.persistence.entity.EventLog;
import com.delaval.usertransactionlogserver.persistence.entity.UserTransactionKey;
import org.eclipse.jetty.io.ssl.SslHandshakeListener;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Created by delaval on 12/9/2015.
 */
public class InternalEventLog implements InternalEntityRepresentation {

    private String id;
    private String username;
    private String name;
    private String category;
    private String label;
    private String userTransactionKeyId;
    private long timestamp;
    private Date timestampAsDate;
    private String tab;
    private String host;
    private String targetMs;
    private String target;

    public InternalEventLog(EventLog eventLog) {
        setCommon(eventLog);
        timestampAsDate = eventLog.getTimestamp();
        username = "unknown";
        target = "unknown";
    }

    public InternalEventLog(EventLog eventLog, UserTransactionKey myKey) {
        setCommon(eventLog);
        timestampAsDate = eventLog.getTimestamp();
        username = myKey.getUsername();
        target = myKey.getTarget();
    }

    public InternalEventLog(EventLog eventLog, UserTransactionKey myKey, ZoneId localZoneId) {
        setCommon(eventLog);
        ZonedDateTime localTime = Instant.ofEpochMilli(timestamp).atZone(localZoneId);
        timestampAsDate = Date.from(localTime.toInstant());
        timestamp = timestampAsDate.getTime();
        username = myKey.getUsername();
        target = myKey.getTarget();
    }

    private void setCommon(EventLog eventLog){
        id = eventLog.getId();
        name = eventLog.getName();
        category = eventLog.getCategory();
        label = eventLog.getLabel();
        tab = eventLog.getTab();
        userTransactionKeyId = eventLog.getUserTransactionKeyId();
        timestamp = eventLog.getTimestamp().getTime();
        host = eventLog.getHost();
        targetMs = eventLog.getTargetMs();

    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getLabel() {
        return label;
    }

    public String getUserTransactionKeyId() {
        return userTransactionKeyId;
    }

    public Date getTimestampAsDate() {
        return timestampAsDate;
    }

    public String getTab() {
        return tab;
    }

    public String getHost() {
        return host;
    }


    @Override
    public int hashCode() {
        StringBuilder allValues = new StringBuilder();
        allValues.append(getId()).append(getName()).append(getCategory()).append(getLabel()).
          append(getUserTransactionKeyId()).append(getTab()).append(getHost()).append(getTimestamp());
        return allValues.toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof InternalEventLog) {
            InternalEventLog other = (InternalEventLog) o;
            return other.hashCode() == this.hashCode();
        }
        return false;
    }
}
