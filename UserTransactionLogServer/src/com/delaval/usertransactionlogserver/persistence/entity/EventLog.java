package com.delaval.usertransactionlogserver.persistence.entity;

import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import simpleorm.dataset.SFieldString;
import simpleorm.dataset.SFieldTimestamp;
import simpleorm.dataset.SRecordMeta;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static simpleorm.dataset.SFieldFlags.SDESCRIPTIVE;
import static simpleorm.dataset.SFieldFlags.SPRIMARY_KEY;

/**
 * Entity that mirrors LogContent-table
 */
public class EventLog extends AbstractEntity {

    public static final SRecordMeta EVENT_LOG = new SRecordMeta(EventLog.class, "EventLog");
    public static final SFieldString ID = new SFieldString(EVENT_LOG, "id", 100, SPRIMARY_KEY);
    public static final SFieldString NAME = new SFieldString(EVENT_LOG, "name", 40, SDESCRIPTIVE);
    public static final SFieldString CATEGORY = new SFieldString(EVENT_LOG, "category", 40, SDESCRIPTIVE);
    public static final SFieldString LABEL = new SFieldString(EVENT_LOG, "label", 512, SDESCRIPTIVE);
    public static final SFieldString TAB = new SFieldString(EVENT_LOG, "tab", 20, SDESCRIPTIVE);
    public static final SFieldString HOST = new SFieldString(EVENT_LOG, "host", 40, SDESCRIPTIVE);
    public static final SFieldString TARGET_MS = new SFieldString(EVENT_LOG, "targetMs", 40, SDESCRIPTIVE);
    public static final SFieldString USER_TRANSACTION_KEY_ID = new SFieldString(EVENT_LOG, "userTransactionKeyId", 100, SDESCRIPTIVE);
    public static final SFieldTimestamp TIMESTAMP = new SFieldTimestamp(EVENT_LOG, "timestamp").overrideSqlDataType("TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)");

    private static final List<SFieldString> varcharColumns = new ArrayList<>();

    static{
        varcharColumns.add(NAME);
        varcharColumns.add(CATEGORY);
        varcharColumns.add(LABEL);
        varcharColumns.add(TAB);
        varcharColumns.add(HOST);
        varcharColumns.add(TARGET_MS);
        varcharColumns.add(USER_TRANSACTION_KEY_ID);
    }

    public static List<SFieldString> getVarcharColumns(){
        return varcharColumns;
    }


    public String getTablename(){return EVENT_LOG.getTableName();}

    public String getId(){
        return getString(ID);
    }

    public String getUserTransactionKeyId(){
        return getString(USER_TRANSACTION_KEY_ID);
    }

    public String getTab(){
        return getString(TAB);
    }

    public String getName(){
        return getString(NAME);
    }

    public String getCategory(){
        return getString(CATEGORY);
    }

    public String getHost(){
        return getString(HOST);
    }

    public String getLabel(){
        return getString(LABEL);
    }

    public String getTargetMs(){
        return getString(TARGET_MS);
    }

    public Date getTimestamp(){
        Date date = new Date();
        date.setTime(getTimestamp(TIMESTAMP).getTime());
        return date;
    }


    @Override
    public SRecordMeta<EventLog> getMeta() {
        return EVENT_LOG;
    }

    @Override
    public void createUserTransactionId(WebSocketMessage webSocketMessage) {
        setString(EventLog.USER_TRANSACTION_KEY_ID, getUserTransactionKeyId(webSocketMessage));
    }


}
