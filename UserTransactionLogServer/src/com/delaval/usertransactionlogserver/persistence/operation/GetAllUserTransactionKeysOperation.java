package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.domain.InternalUserTransactionKey;
import com.delaval.usertransactionlogserver.persistence.dao.InitDAO;
import com.delaval.usertransactionlogserver.persistence.entity.UserTransactionKey;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import simpleorm.dataset.SQuery;
import simpleorm.dataset.SQueryResult;
import simpleorm.sessionjdbc.SSessionJdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Returns all UserTransactionKey that exist in db
 */
public class GetAllUserTransactionKeysOperation implements ReadOperation {

    private SSessionJdbc jdbcSession;
    private List<InternalUserTransactionKey> operationResult;

    @Override
    public void setJdbcSession(SSessionJdbc session) {
        jdbcSession = session;
    }

    @Override
    public void validate() {
        if (jdbcSession == null) {
            throw new IllegalStateException("The operation-instance cant have null as a jdbc-session");
        }
    }

    @Override
    public void execute() {
        UtlsLogUtil.debug(this.getClass(), "Get all userTransactionKeys");
        SQueryResult<UserTransactionKey> result = jdbcSession.query(new SQuery(UserTransactionKey.USER_TRANSACTION_KEY));
        List<UserTransactionKey> resultList = new ArrayList<>();
        List<String> allUserTransactionKeysThatLacksLogs = InitDAO.getInstance().getAllUserTransactionKeysThatLacksLogs();
        resultList.addAll(result);

        List<InternalUserTransactionKey> allWithLogs = resultList.stream()
          .filter(key -> allUserTransactionKeysThatLacksLogs.indexOf(key.getId()) == -1)
          .map(keyThatHasLogs -> new InternalUserTransactionKey(keyThatHasLogs))
          .collect(Collectors.toList());

        operationResult = allWithLogs;
    }

    @Override
    public void setReadParameter(String parameter) {
        // does nothing
    }

    @Override
    public List<InternalUserTransactionKey> getResult() {
        return isResultOk() ? operationResult : new ArrayList<>();
    }

    @Override
    public boolean isResultOk() {
        return operationResult != null;
    }

}
