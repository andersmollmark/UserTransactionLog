package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.domain.InternalEntityRepresentation;
import simpleorm.sessionjdbc.SSessionJdbc;

/**
 * Created by delaval on 1/13/2016. TODO
 */
public interface Operation<T extends InternalEntityRepresentation> {

    void setJdbcSession(SSessionJdbc session);

    /**
     * Validates the operation-class and it will throw IllegalStateException if its not valid
     */
    void validate();

    void execute();

    boolean isCreateUpdate();

    default void setOperationParameter(OperationParameter readParameter) {
//        do nothing
    }


}
