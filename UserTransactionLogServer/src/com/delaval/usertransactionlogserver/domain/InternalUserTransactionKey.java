package com.delaval.usertransactionlogserver.domain;

import com.delaval.usertransactionlogserver.persistence.entity.UserTransactionKey;

import java.util.Date;

/**
 * Created by delaval on 12/8/2015.
 */
public class InternalUserTransactionKey implements InternalEntityRepresentation{


    private String id;
    private String username;
    private String target;
    private String client;
    private Date timestamp;

    public InternalUserTransactionKey(UserTransactionKey userTransactionKey){
        id = userTransactionKey.getId();
        username = userTransactionKey.getUsername();
        target = userTransactionKey.getTarget();
        client = userTransactionKey.getClient();
        timestamp = userTransactionKey.getTimestamp();
    }


    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getTarget() {
        return target;
    }

    public String getClient() {
        return client;
    }

    public Date getTimestamp() {
        return timestamp;
    }


}
