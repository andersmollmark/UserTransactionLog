package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.domain.InternalEventLog;
import com.delaval.usertransactionlogserver.persistence.entity.EventLog;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import simpleorm.dataset.SQuery;
import simpleorm.dataset.SQueryResult;
import simpleorm.sessionjdbc.SSessionJdbc;

import java.util.ArrayList;
import java.util.List;

/**
 * Returns all Eventlogs that exist in db with a certain usertransactionkey
 */
public class GetEventLogsWithUserTransactionKeyOperation implements ReadOperation {

    private SSessionJdbc jdbcSession;
    private List<InternalEventLog> operationResult;
    private String userTransactionKeyId;

    @Override
    public void setJdbcSession(SSessionJdbc session) {
        jdbcSession = session;
    }

    @Override
    public void validate() {
        if (jdbcSession == null) {
            throw new IllegalStateException("The operation-instance cant have null as a jdbc-session");
        } else if (userTransactionKeyId == null) {
            throw new IllegalStateException("The operation-instance must have a userTransactionKeyId");
        }
    }

    @Override
    public void execute() {
        UtlsLogUtil.debug(this.getClass(), "Get all eventlogs with usertransactionkey:" + userTransactionKeyId);
        final List<InternalEventLog> all = new ArrayList<>();
        SQueryResult<EventLog> result = jdbcSession.query(new SQuery(EventLog.EVENT_LOG).eq(EventLog.USER_TRANSACTION_KEY_ID, userTransactionKeyId));
        result.forEach(logContent -> all.add(new com.delaval.usertransactionlogserver.domain.InternalEventLog(logContent)));
        operationResult = all;
    }

    @Override
    public void setReadParameter(String parameter) {
        this.userTransactionKeyId = parameter;
    }


    @Override
    public List<InternalEventLog> getResult() {
        return isResultOk() ? operationResult : new ArrayList<>();
    }

    @Override
    public boolean isResultOk() {
        return operationResult != null;
    }

}
