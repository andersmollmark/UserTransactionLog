package com.delaval.usertransactionlogserver.testobject;

import com.delaval.usertransactionlogserver.persistence.entity.UserTransactionKey;

import java.util.Date;

public class MyUserTransactionKey extends UserTransactionKey {
    String id;
    String username;
    String target;
    String client;

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public String getClient() {
        return client;
    }

    Date timestamp;


    @Override
    public Date getTimestamp() {
        return timestamp;
    }
}
