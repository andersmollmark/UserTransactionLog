package com.delaval.usertransactionlogserver.testobject;

import com.delaval.usertransactionlogserver.persistence.entity.EventLog;

import java.time.LocalDateTime;

public class MyEventLog extends EventLog {

    String id;
    String name;
    String category;
    String label;
    String tab;
    String userTransactionKeyId;
    LocalDateTime timestamp;
    String host;
    String targetMs;

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setTab(String tab) {
        this.tab = tab;
    }

    public void setUserTransactionKeyId(String userTransactionKeyId) {
        this.userTransactionKeyId = userTransactionKeyId;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setTargetMs(String targetMs) {
        this.targetMs = targetMs;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getTab() {
        return tab;
    }

    @Override
    public String getUserTransactionKeyId() {
        return userTransactionKeyId;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getTargetMs() {
        return targetMs;
    }

}
