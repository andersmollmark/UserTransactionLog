package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.domain.InternalEntityRepresentation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by delaval on 2017-05-16.
 */
public class OperationResult<T extends InternalEntityRepresentation> {

    private List<T> result;

    public OperationResult(List<T> result){
        this.result = result;
    }

    public List<T> getResult(){
        return isResultOk() ? result : new ArrayList<>();
    }

    public boolean isResultOk(){
        return result != null;
    }
}
