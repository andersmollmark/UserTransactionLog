package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.domain.InternalEntityRepresentation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by delaval on 1/18/2016.
 */
public interface ReadOperation<T extends InternalEntityRepresentation> extends Operation {



    default void setOperationParameters(List<OperationParameter> readParameters) {
//        do nothing
    }

    @Override
    default boolean isCreateUpdate(){
        return false;
    }

    OperationResult<T> getResult();

    void setNotOkResult(OperationResult<T> notOkResult);

    default List<OperationParameter> getOperationParameters(){
        return new ArrayList<>();
    }


}
