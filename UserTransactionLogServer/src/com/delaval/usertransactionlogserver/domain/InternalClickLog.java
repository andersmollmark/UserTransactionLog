package com.delaval.usertransactionlogserver.domain;

import com.delaval.usertransactionlogserver.persistence.entity.ClickLog;

import java.util.Date;

/**
 * Created by delaval on 12/9/2015.
 */
public class InternalClickLog implements InternalEntityRepresentation {


    private final String id;
    private final String x;
    private final String y;
    private final String cssClassName;
    private final String elementId;
    private final String userTransactionKeyId;
    private final Date timestamp;
    private final String tab;
    private final String host;

    public InternalClickLog(ClickLog clickLog){
        id = clickLog.getId();
        x = clickLog.getX();
        y = clickLog.getY();
        cssClassName = clickLog.getCssClassName();
        elementId = clickLog.getElementId();
        tab = clickLog.getTab();
        userTransactionKeyId = clickLog.getUserTransactionKeyId();
        timestamp = clickLog.getTimestamp();
        host = clickLog.getHost();
    }


    public String getId() {
        return id;
    }

    public String getX() {
        return x;
    }

    public String getY() {
        return y;
    }

    public String getCssClassName() {
        return cssClassName;
    }

    public String getElementId() {
        return elementId;
    }

    public String getUserTransactionKeyId() {
        return userTransactionKeyId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getTab(){ return tab;}

    public String getHost(){ return host;}
}
