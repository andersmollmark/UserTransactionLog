package com.delaval.usertransactionlogserver;

import com.delaval.usertransactionlogserver.testobject.MyEventLog;
import com.delaval.usertransactionlogserver.testobject.MyUserTransactionKey;
import org.junit.Ignore;

import java.time.LocalDateTime;

@Ignore
public class TestObjectFactory {

    public static MyEventLog createEventLog(String id, String userTransId) {
        MyEventLog result = new MyEventLog();
        result.setId(id);
        result.setUserTransactionKeyId(userTransId);
        result.setCategory("category");
        result.setHost("host");
        result.setTargetMs("targetMs");
        result.setTimestamp(LocalDateTime.now());
        result.setName("name");
        result.setLabel("label");
        result.setTab("tab");
        return result;
    }

    public static MyUserTransactionKey getUserTransactionKey(){
        return new MyUserTransactionKey();
    }
}
