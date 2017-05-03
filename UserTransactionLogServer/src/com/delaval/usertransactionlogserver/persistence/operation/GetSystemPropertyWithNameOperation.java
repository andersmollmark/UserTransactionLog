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
public class GetSystemPropertyWithNameOperation implements ReadOperation {

    private SSessionJdbc jdbcSession;
    private List<InternalSystemProperty> operationResult;
    private String name;


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
        operationResult = all;
    }

    @Override
    public void setReadParameter(String parameter) {
        this.name = parameter;
    }


    @Override
    public List<InternalSystemProperty> getResult() {
        return isResultOk() ? operationResult : new ArrayList<>();
    }

    @Override
    public boolean isResultOk() {
        return operationResult != null;
    }

}
