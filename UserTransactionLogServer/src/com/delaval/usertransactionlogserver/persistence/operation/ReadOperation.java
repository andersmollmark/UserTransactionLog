package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.persistence.entity.AbstractEntity;

import java.util.List;

/**
 * Created by delaval on 1/18/2016.
 */
public interface ReadOperation<T extends AbstractEntity> extends Operation {

    void setReadParameter(String parameter);

    List<T> getResult();

    boolean isResultOk();

}
