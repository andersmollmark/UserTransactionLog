package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.domain.InternalEntityRepresentation;
import simpleorm.sessionjdbc.SSessionJdbc;

/**
 * All operations towards the db should be divided into operations
 */
public interface Operation<T extends InternalEntityRepresentation> {

    /**
     * Sets the jdbc-session to use when communicating with the db
     * @param session
     */
    void setJdbcSession(SSessionJdbc session);

    /**
     * Validates all values so that it should be ok to call execute
     * @throws IllegalStateException if its not valid
     */
    void validate();

    void execute();

    /**
     *
     * @return true if the operation is a create or a update-operation
     */
    boolean isCreateUpdate();

    default void setOperationParameter(OperationParameter readParameter) {
//        do nothing
    }


}
