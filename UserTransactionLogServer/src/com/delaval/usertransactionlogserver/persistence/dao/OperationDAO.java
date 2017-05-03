package com.delaval.usertransactionlogserver.persistence.dao;

import com.delaval.usertransactionlogserver.persistence.ConnectionFactory;
import com.delaval.usertransactionlogserver.persistence.operation.Operation;
import com.delaval.usertransactionlogserver.persistence.operation.OperationFactory;
import com.delaval.usertransactionlogserver.persistence.operation.OperationParam;
import com.delaval.usertransactionlogserver.service.JmsMessageService;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import simpleorm.sessionjdbc.SSessionJdbc;

import java.sql.SQLException;

/**
 * Singleton.
 * Handles the persistence-logic of the tables.
 */
public class OperationDAO {

    private static final String LOG_SERVER_CONN_NAME = "LogServer";

    private static OperationDAO _instance;

    private OperationDAO() {
        // Empty by design
    }

    public static synchronized OperationDAO getInstance() {
        if (_instance == null) {
            _instance = new OperationDAO();
        }
        return _instance;
    }

    public synchronized <T extends Operation> T executeOperation(OperationParam<T> operationParam) {
        SSessionJdbc ses = null;
        Class<T> operationClass = operationParam.getOperationClass();
        Operation operation = null;
        try {
            ConnectionFactory.checkIfConnectionIsOk();
            ses = ConnectionFactory.getInstance().getSession(LOG_SERVER_CONN_NAME);
            ses.getStatistics();
            operation = doExecute(operationParam, ses);
        } catch (SQLException sqlException) {
            String error = operationClass.getName() + " " +
              (operationParam.isCreateUpdate() ? operationParam.getWebSocketMessage().toString() : " parameter:" + operationParam.getParameter());
            UtlsLogUtil.error(this.getClass(), "Something went wrong in db-communication:", sqlException.getMessage(), " ", error);
            JmsMessageService.getInstance().cacheJmsMessage(operationParam.getWebSocketMessage());
            operation = doCommonExceptionHandling(ses, operationParam);

        } catch (Exception e) {
            String error = operationClass.getName() + " " +
              (operationParam.isCreateUpdate() ? operationParam.getWebSocketMessage().toString() : " parameter:" + operationParam.getParameter());
            UtlsLogUtil.error(this.getClass(),
              "Something went wrong while executing the operation:",
              e.getMessage(), " ", error);
            operation = doCommonExceptionHandling(ses, operationParam);
        } finally {
            if (ses != null) {
                ses.close();
            }
        }
        return operationClass.cast(operation);
    }

    private <T extends Operation> T doCommonExceptionHandling(SSessionJdbc ses, OperationParam<T> operationParam){
        try {
            if (ses != null) {
                ses.rollback();
            }

            T notOk = (T) OperationFactory.getNotOkResultOperation(operationParam);
            return notOk;
        } catch (Exception e) {
            UtlsLogUtil.error(this.getClass(), "Something went really wrong! Couldnt create notOkResultOperation due to:", e.getMessage());
            throw new RuntimeException("Something went really wrong! Couldnt create notOkResultOperation due to:" + e.getMessage());
        }
    }


    private Operation doExecute(OperationParam operationParam, SSessionJdbc ses) throws InstantiationException, IllegalAccessException, SQLException {
        Operation operation;
        if (operationParam.isCreateUpdate()) {
            if(ConnectionFactory.getInstance().isTableLocked(operationParam.getWebSocketMessage())){
                UtlsLogUtil.debug(OperationDAO.class, " the table is locked:", operationParam.getWebSocketMessage().getMessType());
                throw new SQLException(" the table is locked:" + operationParam.getWebSocketMessage().getMessType());
            }
            operation = OperationFactory.getCreateUpdateOperation(ses, operationParam);
        } else {
            operation = OperationFactory.getReadOperation(ses, operationParam);
        }
        operation.validate();
        try {
            ses.begin();
            operation.execute();
        } finally {
            ses.flush();
            ses.commit();
        }
        return operation;
    }


}
