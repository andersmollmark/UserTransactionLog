package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.domain.InternalClickLog;
import com.delaval.usertransactionlogserver.persistence.entity.ClickLog;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import simpleorm.dataset.SQuery;
import simpleorm.dataset.SQueryResult;
import simpleorm.sessionjdbc.SSessionJdbc;

import java.util.ArrayList;
import java.util.List;

/**
 * Returns all Clicklogs that exist in db with a certain usertransactionkey
 */
public class GetClickLogsWithUserTransactionKeyOperation implements ReadOperation {

    private SSessionJdbc jdbcSession;
    private List<InternalClickLog> operationResult;
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
        UtlsLogUtil.debug(this.getClass(), "Get all clicklogs with usertransactionkey:" + userTransactionKeyId);
        final List<InternalClickLog> all = new ArrayList<>();
        SQueryResult<ClickLog> result = jdbcSession.query(new SQuery(ClickLog.CLICK_LOG).eq(ClickLog.USER_TRANSACTION_KEY_ID, userTransactionKeyId));
        result.forEach(logContent -> all.add(new InternalClickLog(logContent)));
        operationResult = all;
    }

    @Override
    public void setReadParameter(String parameter) {
        this.userTransactionKeyId = parameter;
    }


    @Override
    public List<InternalClickLog> getResult() {
        return isResultOk() ? operationResult : new ArrayList<>();
    }

    @Override
    public boolean isResultOk() {
        return operationResult != null;
    }

}
