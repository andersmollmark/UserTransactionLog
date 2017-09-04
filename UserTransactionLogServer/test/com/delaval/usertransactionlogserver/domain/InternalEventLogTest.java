package com.delaval.usertransactionlogserver.domain;

import com.delaval.usertransactionlogserver.TestObjectFactory;
import com.delaval.usertransactionlogserver.testobject.MyEventLog;
import com.delaval.usertransactionlogserver.testobject.MyUserTransactionKey;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class InternalEventLogTest {

    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Test
    public void testCreateWithTimezoneShouldChangeTimestampFromUtc(){
        String datestringFromClient = "2017-09-01T13:58:00+02:00";
        String expectedDatestring = "2017-09-01 13:58:00.000";
        String expectedUtcFromDB = "2017-09-01 11:58:00.000";
        String timezone = "Europe/Stockholm";
        ZonedDateTime parsed = ZonedDateTime.parse(datestringFromClient);
        long dateInMillis = parsed.toEpochSecond() * 1000;

        TimeZone timeZone = TimeZone.getTimeZone(timezone);
        ZonedDateTime timeAtLocation = Instant.ofEpochMilli(dateInMillis).atZone(timeZone.toZoneId());

        ZonedDateTime utc = timeAtLocation.withZoneSameInstant(ZoneOffset.UTC);
        LocalDateTime utcLocalDateTime = utc.toLocalDateTime();

//        Assertions before test
        assertEquals(timeAtLocation.format(dtf), expectedDatestring);
        assertEquals(expectedUtcFromDB, utc.format(dtf));
        assertEquals(expectedUtcFromDB, utcLocalDateTime.format(dtf));

        MyUserTransactionKey userTransactionKey = TestObjectFactory.getUserTransactionKey();
        MyEventLog eventLog = TestObjectFactory.createEventLog("id1", "userTrans1");
        eventLog.setTimestamp(utcLocalDateTime);

        InternalEventLog testLog = new InternalEventLog(eventLog, userTransactionKey, timeZone.toZoneId());
        assertEquals(expectedDatestring, testLog.getTimestampAsDate().format(dtf));


    }

    @Test
    public void testCreateWithoutTimezoneShouldHaveUtcTimestamp(){
        String datestringFromClient = "2017-09-01T13:58:00+02:00";
        String expectedDatestring = "2017-09-01 11:58:00.000";
        String expectedUtcFromDB = "2017-09-01 11:58:00.000";
        ZonedDateTime parsed = ZonedDateTime.parse(datestringFromClient);
        long dateInMillis = parsed.toEpochSecond() * 1000;

        ZonedDateTime utc = Instant.ofEpochMilli(dateInMillis).atZone(ZoneId.of("UTC"));
        LocalDateTime utcLocalDateTime = utc.toLocalDateTime();

//        Assertions before test
        assertEquals(utcLocalDateTime.format(dtf), expectedDatestring);
        assertEquals(expectedUtcFromDB, utc.format(dtf));

        MyUserTransactionKey userTransactionKey = TestObjectFactory.getUserTransactionKey();
        MyEventLog eventLog = TestObjectFactory.createEventLog("id1", "userTrans1");
        eventLog.setTimestamp(utcLocalDateTime);

        InternalEventLog testLog = new InternalEventLog(eventLog, userTransactionKey);
        assertEquals(expectedDatestring, testLog.getTimestampAsDate().format(dtf));


    }
}