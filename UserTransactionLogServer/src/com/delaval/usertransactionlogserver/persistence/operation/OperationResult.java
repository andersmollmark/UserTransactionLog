package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.domain.InternalEntityRepresentation;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps a result from an read-operation.
 */
public class OperationResult<T extends InternalEntityRepresentation> {

    private List<T> result;

    public OperationResult(List<T> result){
        this.result = result;
    }

    public List<T> getResult(){
        return isResultOk() ? result : new ArrayList<>();
    }

    /**
     *
     * @return true if its a result or not inside this
     */
    public boolean isResultOk(){
        return result != null;
    }
}
