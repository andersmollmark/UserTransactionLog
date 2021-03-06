package com.delaval.usertransactionlogserver.persistence.dao;

import com.delaval.usertransactionlogserver.domain.InternalEntityRepresentation;
import com.delaval.usertransactionlogserver.persistence.ConnectionFactory;
import com.delaval.usertransactionlogserver.persistence.operation.*;
import com.delaval.usertransactionlogserver.service.JmsMessageService;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import simpleorm.sessionjdbc.SSessionJdbc;

import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * Singleton.
 * Handles the persistence-logic of the tables.
 * Handles the operation-classes in a generic way so that all logic can be reused
 */
public class OperationDAO {

    private static final String LOG_SERVER_CONN_NAME = "LogServer";

    private Object LOCK = new Object();

    private static OperationDAO _instance;

    private OperationDAO() {
        // Empty by design (Singleton)
    }

    public static synchronized OperationDAO getInstance() {
        if (_instance == null) {
            _instance = new OperationDAO();
        }
        return _instance;
    }

    /**
     * Executes a create or update-operation
     * @param operation is the operation-class
     */
    public void doCreateUpdate(CreateUpdateOperation operation) {
        synchronized (LOCK) {
            executeOperation(operation);
        }
    }

    /**
     * Executes a read-operation and returns the result
     * @param readOperation is the read-operation-class
     * @return the result that contains an internal representation of the entity/entities.
     */
    public <T extends InternalEntityRepresentation> OperationResult<T> doRead(ReadOperation<T> readOperation) {
        synchronized (LOCK) {
            executeOperation(readOperation);
            return readOperation.getResult();
        }
    }

    /**
     * Handles generically an operation towards the db. The result of a read-operation will be stored inside the operation-object to be handled from calling code
     * @param operation is the operation-class
     */
    private <T extends InternalEntityRepresentation> void executeOperation(Operation<T> operation) {
        SSessionJdbc ses = null;
        try {
            ConnectionFactory.checkIfConnectionIsOk();
            ses = ConnectionFactory.getInstance().getSession(LOG_SERVER_CONN_NAME);
            ses.getStatistics();
            doExecute(operation, ses);
        } catch (SQLException sqlException) {
            logException(sqlException, "Something went wrong in db-communication:", operation);
            if(operation.isCreateUpdate()){
                JmsMessageService.getInstance().cacheJmsMessage(((CreateUpdateOperation)operation).getWebSocketMessage());
            }
            doCommonExceptionHandling(ses, operation);

        } catch (Exception e) {
            logException(e, "Something went wrong while executing the operation:", operation);
            doCommonExceptionHandling(ses, operation);
        } finally {
            if (ses != null) {
                ses.close();
            }
        }
    }

    private <T extends InternalEntityRepresentation> void logException(Exception e, String mess, Operation<T> operation){
        String error = "";
        if(operation instanceof ReadOperation){
            ReadOperation<T> readOperation = (ReadOperation<T>) operation;
            String parameterString = readOperation.getOperationParameters()
              .stream()
              .map(param -> param.getValue())
              .collect(Collectors.joining(","));
            error = "OperationClass and parameters:" + operation.getClass().getName() + " " + parameterString;
        }
        else if(operation instanceof CreateUpdateOperation){
            CreateUpdateOperation crudOperation = (CreateUpdateOperation)operation;
            error = "OperationClass and messageType:" + crudOperation.getClass().getName() + " " + crudOperation.getMesstype();
        }
        else{
            error = "unknown operation! class:" + operation.getClass().getName();
        }

        UtlsLogUtil.error(this.getClass(),
          mess,
          e.getMessage(), " ", error);

    }

    private <T extends InternalEntityRepresentation> void doCommonExceptionHandling(SSessionJdbc ses, Operation<T> operation) {
        try {
            if (ses != null) {
                ses.rollback();
            }
            if(!operation.isCreateUpdate()){
                ((ReadOperation<T>)operation).setNotOkResult(OperationFactory.getNotOkResult(operation));
            }

        } catch (Exception e) {
            UtlsLogUtil.error(this.getClass(), "Something went really wrong! Couldnt create notOkResultOperation due to:", e.getMessage());
            throw new RuntimeException("Something went really wrong! Couldnt create notOkResultOperation due to:" + e.getMessage());
        }
    }

    /**
     * Makes the actual calls to the operation-class and handles the db-session
     */
    private void doExecute(Operation operation, SSessionJdbc ses) throws InstantiationException, IllegalAccessException, SQLException {
        if (operation.isCreateUpdate()) {
            CreateUpdateOperation crudOperation = (CreateUpdateOperation)operation;
            if (ConnectionFactory.getInstance().isTableLocked(crudOperation.getWebSocketMessage())) {
                UtlsLogUtil.debug(OperationDAO.class, " the table is locked:", crudOperation.getMesstype());
                throw new SQLException(" the table is locked:" + crudOperation.getMesstype());
            }

        }
        operation.setJdbcSession(ses);
        operation.validate();
        try {
            ses.begin();
            operation.execute();
        } finally {
            ses.flush();
            ses.commit();
        }
    }


}
