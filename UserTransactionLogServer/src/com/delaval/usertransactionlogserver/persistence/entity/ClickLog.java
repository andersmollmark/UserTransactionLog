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
 * Entity that mirrors ClickLog-table
 */
public class ClickLog extends AbstractEntity {

    public static final SRecordMeta CLICK_LOG = new SRecordMeta(ClickLog.class, "ClickLog");
    public static final SFieldString ID = new SFieldString(CLICK_LOG, "id", 100, SPRIMARY_KEY);
    public static final SFieldString INTERACTION_TYPE = new SFieldString(CLICK_LOG, "interactionType", 40, SDESCRIPTIVE);
    public static final SFieldString X = new SFieldString(CLICK_LOG, "x", 10, SDESCRIPTIVE);
    public static final SFieldString Y = new SFieldString(CLICK_LOG, "y", 10, SDESCRIPTIVE);
    public static final SFieldString CSS_CLASSNAME = new SFieldString(CLICK_LOG, "cssClassname", 100, SDESCRIPTIVE);
    public static final SFieldString ELEMENT_ID = new SFieldString(CLICK_LOG, "elementId", 50, SDESCRIPTIVE);
    public static final SFieldString USER_TRANSACTION_KEY_ID = new SFieldString(CLICK_LOG, "userTransactionKeyId", 100, SDESCRIPTIVE);
    public static final SFieldString TAB = new SFieldString(CLICK_LOG, "tab", 20, SDESCRIPTIVE);
    public static final SFieldString HOST = new SFieldString(CLICK_LOG, "host", 40, SDESCRIPTIVE);
    public static final SFieldTimestamp TIMESTAMP = new SFieldTimestamp(CLICK_LOG, "timestamp").overrideSqlDataType("TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)");

    private static final List<SFieldString> varcharColumns = new ArrayList<>();

    static{
        varcharColumns.add(INTERACTION_TYPE);
        varcharColumns.add(X);
        varcharColumns.add(Y);
        varcharColumns.add(CSS_CLASSNAME);
        varcharColumns.add(ELEMENT_ID);
        varcharColumns.add(USER_TRANSACTION_KEY_ID);
        varcharColumns.add(TAB);
        varcharColumns.add(HOST);
    }

    public static List<SFieldString> getVarcharColumns(){
        return varcharColumns;
    }

    public String getTablename(){return CLICK_LOG.getTableName();}


    public String getId(){
        return getString(ID);
    }

    public String interactionType(){
        return getString(INTERACTION_TYPE);
    }

    public String getX(){
        return getString(X);
    }

    public String getY(){
        return getString(Y);
    }

    public String getCssClassName(){
        return getString(CSS_CLASSNAME);
    }

    public String getElementId(){
        return getString(ELEMENT_ID);
    }

    public String getUserTransactionKeyId(){
        return getString(USER_TRANSACTION_KEY_ID);
    }

    public String getTab(){
        return getString(TAB);
    }

    public String getHost(){
        return getString(HOST);
    }


    public Date getTimestamp(){
        Date date = new Date();
        date.setTime(getTimestamp(TIMESTAMP).getTime());
        return date;
    }


    @Override
    public SRecordMeta<ClickLog> getMeta() {
        return CLICK_LOG;
    }

    @Override
    public void createUserTransactionId(WebSocketMessage webSocketMessage) {
        setString(ClickLog.USER_TRANSACTION_KEY_ID, getUserTransactionKeyId(webSocketMessage));
    }


}
