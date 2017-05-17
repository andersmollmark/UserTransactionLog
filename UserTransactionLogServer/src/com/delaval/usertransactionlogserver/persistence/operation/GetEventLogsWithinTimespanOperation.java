package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.domain.InternalEventLog;
import com.delaval.usertransactionlogserver.persistence.entity.EventLog;
import com.delaval.usertransactionlogserver.persistence.entity.UserTransactionKey;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import simpleorm.dataset.SQuery;
import simpleorm.dataset.SQueryResult;
import simpleorm.sessionjdbc.SSessionJdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Returns all Eventlogs that exist in db with a certain usertransactionkey
 */
public class GetEventLogsWithinTimespanOperation implements ReadOperation<InternalEventLog> {

    private SSessionJdbc jdbcSession;
    List<OperationParameter> readParameters;
    private OperationResult<InternalEventLog> operationResult;
    private String from;
    private String to;


    @Override
    public void setJdbcSession(SSessionJdbc session) {
        jdbcSession = session;
    }

    @Override
    public void validate() {
        if (jdbcSession == null) {
            throw new IllegalStateException("The operation-instance cant have null as a jdbc-session");
        } else if (readParameters == null || readParameters.size() != 2) {
            throw new IllegalStateException(this.getClass() + ", The operation-instance must have a from- and to-date");
        }
        from = readParameters.get(0).getValue();
        to = readParameters.get(1).getValue();
    }

    @Override
    public void execute() {
        UtlsLogUtil.debug(this.getClass(), "Get all eventlogs between two dates:", " from:", from, " to:", to);

        Map<String, UserTransactionKey> allUserTransactionIds = getUserTransactionIdsWithinTimespan();
        final List<InternalEventLog> all = new ArrayList<>();
        SQueryResult<EventLog> result = getLogsWithinTimespan();
        result.forEach(logContent -> all.add(new InternalEventLog(logContent, allUserTransactionIds.get(logContent.getUserTransactionKeyId()))));
        operationResult = new OperationResult<>(all);
    }

    @Override
    public void setOperationParameters(List<OperationParameter> readParameters) {
        this.readParameters = readParameters;
    }

    private Map<String, UserTransactionKey> getUserTransactionIdsWithinTimespan() {
        SQuery<UserTransactionKey> idsWithinTime = new SQuery<>(UserTransactionKey.USER_TRANSACTION_KEY)
          .gt(UserTransactionKey.TIMESTAMP, from)
          .lt(UserTransactionKey.TIMESTAMP, to);

        SQueryResult<UserTransactionKey> result = jdbcSession.query(idsWithinTime);
        Map<String, UserTransactionKey> userTransactionKeyMap = result.stream().collect(
          Collectors.toMap(key -> key.getId(), key -> key));
        return userTransactionKeyMap;
    }

    private SQueryResult<EventLog> getLogsWithinTimespan() {
        SQuery<EventLog> theQuery = new SQuery<>(EventLog.EVENT_LOG)
          .gt(EventLog.TIMESTAMP, from)
          .lt(EventLog.TIMESTAMP, to)
          .ascending(EventLog.USER_TRANSACTION_KEY_ID);
        return jdbcSession.query(theQuery);
    }

    @Override
    public void setNotOkResult(OperationResult<InternalEventLog> notOkResult) {
        operationResult = notOkResult;
    }


    @Override
    public OperationResult<InternalEventLog> getResult() {
        return operationResult;
    }

}
