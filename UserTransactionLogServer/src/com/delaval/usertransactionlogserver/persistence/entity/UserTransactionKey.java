package com.delaval.usertransactionlogserver.persistence.entity;

import com.delaval.usertransactionlogserver.util.DateUtil;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import simpleorm.dataset.SFieldString;
import simpleorm.dataset.SFieldTimestamp;
import simpleorm.dataset.SRecordMeta;
import simpleorm.sessionjdbc.SSessionJdbc;

import java.util.Date;
import java.util.Optional;

import static simpleorm.dataset.SFieldFlags.SDESCRIPTIVE;
import static simpleorm.dataset.SFieldFlags.SPRIMARY_KEY;

/**
 * Entity that mirrors UserTransactionKey-table
 */
public class UserTransactionKey extends AbstractEntity {

    public static final SRecordMeta USER_TRANSACTION_KEY = new SRecordMeta(UserTransactionKey.class, "UserTransactionKey");
    public static final SFieldString ID = new SFieldString(USER_TRANSACTION_KEY, "id", 100, SPRIMARY_KEY);
    public static final SFieldString USERNAME = new SFieldString(USER_TRANSACTION_KEY, "username", 40, SDESCRIPTIVE);
    public static final SFieldString TARGET = new SFieldString(USER_TRANSACTION_KEY, "target", 40, SDESCRIPTIVE);
    public static final SFieldString CLIENT = new SFieldString(USER_TRANSACTION_KEY, "client", 40, SDESCRIPTIVE).setInitialValue("");
    public static final SFieldTimestamp TIMESTAMP = new SFieldTimestamp(USER_TRANSACTION_KEY, "timestamp").overrideSqlDataType("TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)");

    @Override
	public SRecordMeta<UserTransactionKey> getMeta() {
            return USER_TRANSACTION_KEY;
        }

    @Override
    public void createUserTransactionId(WebSocketMessage webSocketMessage) {
        // do nothing because this is the UserTransactionKey
    }

    public String getTablename(){return USER_TRANSACTION_KEY.getTableName();}

    public String getId(){
        return getString(ID);
    }

    public String getUsername(){
        return getString(USERNAME);
    }

    public String getTarget(){
        return getString(TARGET);
    }

    public String getClient(){
        return getString(CLIENT);
    }

    public Date getTimestamp(){
        Date date = new Date();
        date.setTime(getTimestamp(TIMESTAMP).getTime());
        return date;
    }

    /**
     * Finds or creates an instance of UserTransactionKey.
     * @param jdbcSession
     * @param webSocketMessage
     * @return
     */
    public static UserTransactionKey findOrCreateKey(SSessionJdbc jdbcSession, WebSocketMessage webSocketMessage){
        String logId = getUserTransactionKeyId(webSocketMessage);
        boolean setSessionToBegin = false;
        if(jdbcSession.hasBegun()) {
            setSessionToBegin = true;
        }
        else {
            jdbcSession.begin();
        }
        Optional<UserTransactionKey> persistedUserTransactionKey = find(jdbcSession, logId);
        UserTransactionKey userTransactionKey = persistedUserTransactionKey.isPresent() ? persistedUserTransactionKey.get() : create(webSocketMessage, jdbcSession);
        jdbcSession.flush();
        jdbcSession.commit();
        if(setSessionToBegin){
            jdbcSession.begin();
        }
        return userTransactionKey;
    }


    public static Optional<UserTransactionKey> find(SSessionJdbc ses, String id) {
        UserTransactionKey userTransactionKey = (UserTransactionKey)ses.find(UserTransactionKey.USER_TRANSACTION_KEY, id);
        return Optional.ofNullable(userTransactionKey);
    }

    private static UserTransactionKey create(WebSocketMessage webSocketMessage, SSessionJdbc ses) {
        UserTransactionKey userTransactionKey = (UserTransactionKey) ses.create(UserTransactionKey.USER_TRANSACTION_KEY, getUserTransactionKeyId(webSocketMessage));
        userTransactionKey.setString(UserTransactionKey.USERNAME, webSocketMessage.getUsername());
        userTransactionKey.setString(UserTransactionKey.CLIENT, webSocketMessage.getClient());
        userTransactionKey.setString(UserTransactionKey.TIMESTAMP, DateUtil.formatTimeStamp(new Date()));
        userTransactionKey.setString(UserTransactionKey.TARGET, webSocketMessage.getTarget());
        return userTransactionKey;
    }

}
