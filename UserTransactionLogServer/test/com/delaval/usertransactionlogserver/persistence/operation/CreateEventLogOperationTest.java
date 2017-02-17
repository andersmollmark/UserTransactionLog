package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.persistence.entity.EventLog;
import com.delaval.usertransactionlogserver.persistence.entity.UserTransactionKey;
import com.delaval.usertransactionlogserver.util.DateUtil;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import com.delaval.usertransactionlogserver.websocket.EventLogContent;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import simpleorm.dataset.SFieldString;
import simpleorm.dataset.SRecordMeta;
import simpleorm.sessionjdbc.SSessionJdbc;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by delaval on 2016-08-26.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({UserTransactionKey.class, UtlsLogUtil.class, CreateEventLogOperation.class})
public class CreateEventLogOperationTest {

    @Mock
    SSessionJdbc mockSession;

    @Mock
    EventLog mockLog;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    WebSocketMessage webSocketMessage = new WebSocketMessage();
    EventLogContent eventLogContent = new EventLogContent();

    @Test
    public void execute() throws Exception {
        CreateEventLogOperation testOperation = new CreateEventLogOperation();

        PowerMockito.mockStatic(UserTransactionKey.class);
        PowerMockito.mockStatic(UtlsLogUtil.class);
//        PowerMockito.doNothing().when(UserTransactionKey.findOrCreateKey(mockSession, webSocketMessage));
//        PowerMockito.doNothing().when(UtlsLogUtil.debug(CreateEventLogOperation.class, Mockito.anyString()));

        webSocketMessage.setUsername("Testuser");

        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.from(now);
        eventLogContent.setTimestamp("" + timestamp.getTime());
        eventLogContent.setEventCategory("Category");
        eventLogContent.setEventLabel("Label");
        eventLogContent.setEventName("Eventname");
        eventLogContent.setHost("Host");
        eventLogContent.setTab("Tab");

        Mockito.when(mockSession.create(Mockito.any(SRecordMeta.class), Mockito.anyString())).thenReturn(mockLog);


        testOperation.jdbcSession = mockSession;
        testOperation.webSocketMessage = webSocketMessage;
        testOperation.eventLogContent = eventLogContent;

        ArgumentCaptor<SFieldString> nameFieldCaptor = ArgumentCaptor.forClass(SFieldString.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);

        testOperation.execute();

        Mockito.verify(mockLog, Mockito.times(6)).setString(nameFieldCaptor.capture(), valueCaptor.capture());

        List<SFieldString> nameFieldCaptorAllValues = nameFieldCaptor.getAllValues();
        assertEquals(EventLog.NAME, nameFieldCaptorAllValues.get(0));
        assertEquals(EventLog.CATEGORY, nameFieldCaptorAllValues.get(1));
        assertEquals(EventLog.HOST, nameFieldCaptorAllValues.get(2));
        assertEquals(EventLog.LABEL, nameFieldCaptorAllValues.get(3));
        assertEquals(EventLog.TIMESTAMP, nameFieldCaptorAllValues.get(4));
        assertEquals(EventLog.TAB, nameFieldCaptorAllValues.get(5));

        List<String> valueCaptorAllValues = valueCaptor.getAllValues();
        assertEquals(eventLogContent.getEventName(), valueCaptorAllValues.get((0)));
        assertEquals(eventLogContent.getEventCategory(), valueCaptorAllValues.get((1)));
        assertEquals(eventLogContent.getHost(), valueCaptorAllValues.get((2)));
        assertEquals(eventLogContent.getEventLabel(), valueCaptorAllValues.get((3)));
        assertEquals(DateUtil.formatTimeStamp(timestamp.getTime()), valueCaptorAllValues.get((4)));
        assertEquals(eventLogContent.getTab(), valueCaptorAllValues.get((5)));
    }


}