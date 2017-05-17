package com.delaval.usertransactionlogserver.persistence.operation;

/**
 * Inparameter and data-carrier for operation-classes
 */
public class StringParameter implements OperationParameter {
    String value;

    public StringParameter(String value){
        this.value = value;
    }

    @Override
    public boolean isStringParam() {
        return true;
    }

    @Override
    public String getValue(){
        return value;
    }

}
