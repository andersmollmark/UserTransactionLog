package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.domain.InternalEventLog;
import com.delaval.usertransactionlogserver.persistence.entity.EventLog;
import com.delaval.usertransactionlogserver.persistence.entity.UserTransactionKey;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import simpleorm.dataset.SQuery;
import simpleorm.dataset.SQueryResult;
import simpleorm.sessionjdbc.SSessionJdbc;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Returns all Eventlogs that exist in db within a timespan
 */
public class GetEventLogsWithinTimespanOperation implements ReadOperation<InternalEventLog> {

    private SSessionJdbc jdbcSession;
    List<OperationParameter> readParameters;
    private OperationResult<InternalEventLog> operationResult;
    private String from;
    private String to;
    private ZoneId zoneId;


    /**
     * @see Operation#setJdbcSession(SSessionJdbc)
     */
    @Override
    public void setJdbcSession(SSessionJdbc session) {
        jdbcSession = session;
    }

    /**
     * @see Operation#validate()
     */
    @Override
    public void validate() {
        if (jdbcSession == null) {
            throw new IllegalStateException("The operation-instance cant have null as a jdbc-session");
        } else if (readParameters == null || readParameters.size() < 2) {
            throw new IllegalStateException(this.getClass() + ", The operation-instance must have a from- and to-date");
        }
        from = readParameters.get(0).getValue();
        to = readParameters.get(1).getValue();
        if(readParameters.size() == 3){
            zoneId = ZoneId.of(readParameters.get(2).getValue());
        }

    }

    /**
     * Fetches all eventlogs within a certain timespan and creates InternalEventLog for the inner-domain representation.
     */
    @Override
    public void execute() {
        UtlsLogUtil.debug(this.getClass(), "Get all eventlogs between two dates:", " from:", from, " to:", to);

        Map<String, UserTransactionKey> allUserTransactionIds = getUserTransactionIds();
        final List<InternalEventLog> all = new ArrayList<>();
        SQueryResult<EventLog> result = getLogsWithinTimespan();
        if(zoneId != null){
            result.forEach(logContent -> all.add(new InternalEventLog(logContent, allUserTransactionIds.get(logContent.getUserTransactionKeyId()), zoneId)));
        }
        else{
            result.forEach(logContent -> all.add(new InternalEventLog(logContent, allUserTransactionIds.get(logContent.getUserTransactionKeyId()))));
        }
        operationResult = new OperationResult<>(all);
    }

    @Override
    public void setOperationParameters(List<OperationParameter> readParameters) {
        this.readParameters = readParameters;
    }

    /**
     * Fetch all usertransaction-ids
     * @return
     */
    private Map<String, UserTransactionKey> getUserTransactionIds() {
        SQueryResult<UserTransactionKey> result = jdbcSession.query(new SQuery(UserTransactionKey.USER_TRANSACTION_KEY));
        Map<String, UserTransactionKey> userTransactionKeyMap = result.stream().collect(
          Collectors.toMap(key -> key.getId(), key -> key));
        return userTransactionKeyMap;
    }

    /**
     * Filter out the logs within the timespan
     * @return the logs that are inside timespan
     */
    private SQueryResult<EventLog> getLogsWithinTimespan() {
        SQuery<EventLog> theQuery = new SQuery<>(EventLog.EVENT_LOG)
          .ge(EventLog.TIMESTAMP, from)
          .le(EventLog.TIMESTAMP, to)
          .ascending(EventLog.USER_TRANSACTION_KEY_ID);
        return jdbcSession.query(theQuery);
    }

    /**
     * This is called from exception-handling when something goes wrong.
     * @param notOkResult
     */
    @Override
    public void setNotOkResult(OperationResult<InternalEventLog> notOkResult) {
        operationResult = notOkResult;
    }


    @Override
    public OperationResult<InternalEventLog> getResult() {
        return operationResult;
    }

}
