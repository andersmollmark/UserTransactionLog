package com.delaval.usertransactionlogserver.persistence.operation;

/**
 * Created by delaval on 2017-05-15.
 */
public interface OperationParameter {

    boolean isStringParam();

    String getValue();
}
