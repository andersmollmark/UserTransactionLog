package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.domain.InternalSystemProperty;
import com.delaval.usertransactionlogserver.persistence.entity.SystemProperty;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import simpleorm.dataset.SQuery;
import simpleorm.dataset.SQueryResult;
import simpleorm.sessionjdbc.SSessionJdbc;

import java.util.ArrayList;
import java.util.List;

/**
 * Returns all Clicklogs that exist in db with a certain usertransactionkey
 */
public class GetSystemPropertyWithNameOperation implements ReadOperation<InternalSystemProperty> {

    private SSessionJdbc jdbcSession;
    private String name;
    private OperationResult<InternalSystemProperty> operationResult;


    @Override
    public void setJdbcSession(SSessionJdbc session) {
        jdbcSession = session;
    }

    @Override
    public void validate() {
        if (jdbcSession == null) {
            throw new IllegalStateException("The operation-instance cant have null as a jdbc-session");
        } else if (name == null) {
            throw new IllegalStateException("The operation-instance must have a userTransactionKeyId");
        }
    }

    @Override
    public void execute() {
        UtlsLogUtil.debug(this.getClass(), "Get systemproperty with name:", name);
        final List<InternalSystemProperty> all = new ArrayList<>();
        SQueryResult<SystemProperty> result = jdbcSession.query(new SQuery(SystemProperty.SYSTEM_PROPERTY).eq(SystemProperty.NAME, name));
        result.forEach(logContent -> all.add(new InternalSystemProperty(logContent)));
        operationResult = new OperationResult<>(all);
    }

    @Override
    public void setOperationParameter(OperationParameter readParameter) {
        this.name = readParameter.getValue();
    }

    @Override
    public void setNotOkResult(OperationResult<InternalSystemProperty> notOkResult) {
        operationResult = notOkResult;
    }


    @Override
    public OperationResult<InternalSystemProperty> getResult() {
        return operationResult;
    }
}
