package com.delaval.usertransactionlogserver.persistence.entity;

import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import simpleorm.dataset.SFieldString;
import simpleorm.dataset.SFieldTimestamp;
import simpleorm.dataset.SRecordMeta;
import simpleorm.sessionjdbc.SSessionJdbc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static simpleorm.dataset.SFieldFlags.SDESCRIPTIVE;
import static simpleorm.dataset.SFieldFlags.SPRIMARY_KEY;

/**
 * Entity that mirrors ClickLog-table
 */
public class SystemProperty extends AbstractEntity {

    public static final SRecordMeta SYSTEM_PROPERTY = new SRecordMeta(SystemProperty.class, "SystemProperty");
    public static final SFieldString ID = new SFieldString(SYSTEM_PROPERTY, "id", 100, SPRIMARY_KEY);
    public static final SFieldString NAME = new SFieldString(SYSTEM_PROPERTY, "name", 30, SDESCRIPTIVE);
    public static final SFieldString VALUE = new SFieldString(SYSTEM_PROPERTY, "value", 5000, SDESCRIPTIVE);
    public static final SFieldString USER_TRANSACTION_KEY_ID = new SFieldString(SYSTEM_PROPERTY, "userTransactionKeyId", 100, SDESCRIPTIVE);
    public static final SFieldTimestamp TIMESTAMP = new SFieldTimestamp(SYSTEM_PROPERTY, "timestamp").overrideSqlDataType("TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)");

    public static final String VALUE_COLUMN = "value";
    public static final String SYSTEM_USER = "System";

    private static final List<SFieldString> varcharColumns = new ArrayList<>();

    static{
        varcharColumns.add(NAME);
        varcharColumns.add(VALUE);
        varcharColumns.add(USER_TRANSACTION_KEY_ID);
    }

    public static List<SFieldString> getVarcharColumns(){
        return varcharColumns;
    }

    public String getId(){
        return getString(ID);
    }

    public String getName(){
        return getString(NAME);
    }

    public String getValue(){
        return getString(VALUE);
    }

    public Date getTimestamp(){
        Date date = new Date();
        date.setTime(getTimestamp(TIMESTAMP).getTime());
        return date;
    }


    public String getTablename(){return SYSTEM_PROPERTY.getTableName();}

    @Override
    public SRecordMeta<SystemProperty> getMeta() {
        return SYSTEM_PROPERTY;
    }

    @Override
    public void createUserTransactionId(WebSocketMessage webSocketMessage) {
        setString(SystemProperty.USER_TRANSACTION_KEY_ID, getUserTransactionKeyId(webSocketMessage));
    }

    public static Optional<SystemProperty> find(SSessionJdbc ses, String id) {
        SystemProperty systemProperty = (SystemProperty)ses.find(SystemProperty.SYSTEM_PROPERTY, id);
        return Optional.ofNullable(systemProperty);
    }


}
