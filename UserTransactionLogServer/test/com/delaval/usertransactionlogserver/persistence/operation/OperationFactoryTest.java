package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.domain.InternalSystemProperty;
import com.delaval.usertransactionlogserver.persistence.entity.SystemProperty;
import com.delaval.usertransactionlogserver.util.DateUtil;
import com.delaval.usertransactionlogserver.websocket.EventLogContent;
import com.delaval.usertransactionlogserver.websocket.JsonContent;
import com.delaval.usertransactionlogserver.websocket.SystemPropertyContent;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import com.google.gson.Gson;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Created by delaval on 2016-01-22.
 */
public class OperationFactoryTest {



    @Test
    public void testGetCreateSystemPropertyParamForSystem() throws Exception {
        String name = "Name";
        String value = "Value";
        String system = SystemProperty.SYSTEM_USER;
        String id = "id";
        InternalSystemProperty systemProperty = new InternalSystemProperty();
        systemProperty.setName(name);
        systemProperty.setValue(value);
        systemProperty.setId(id);

        CreateSystemPropertyOperation operation = OperationFactory.getCreateSystemPropertyForSystem(systemProperty);
        SystemPropertyContent contentFromJson = new Gson().fromJson(operation.getWebSocketMessage().getJsonContent(), SystemPropertyContent.class);
        assertThat(contentFromJson.getName(), is(name));
        assertThat(contentFromJson.getValue(), is(value));
        WebSocketMessage webSocketMessage = operation.getWebSocketMessage();
        assertThat(webSocketMessage.getClient(), is(system));
        assertThat(webSocketMessage.getTarget(), is(system));
        assertThat(webSocketMessage.getUsername(), is(system.toLowerCase()));
    }

    @Test
    public void testGetCreateEventLogParam() throws Exception {
        WebSocketMessage websocketMessage = getWebsocketMessage(getEventLogContent());
        CreateEventLogOperation operation = OperationFactory.getCreateEventLog(websocketMessage);
        assertThat(operation.getWebSocketMessage(), is(notNullValue()));
    }


    private EventLogContent getEventLogContent(){
        EventLogContent eventLogContent = new EventLogContent();
        eventLogContent.setTab("tab");
        eventLogContent.setEventCategory("category");
        eventLogContent.setEventLabel("label");
        eventLogContent.setEventName("eventname");
        return eventLogContent;
    }


    private SystemPropertyContent getSystemPropertyContent(String name, String value) {
        SystemPropertyContent content = new SystemPropertyContent();
        content.setTimestamp(DateUtil.formatTimeStamp(new Date()));
        content.setName(name);
        content.setValue(value);
        return content;
    }

    private WebSocketMessage getWebsocketMessage(JsonContent jsonContent) {
        WebSocketMessage webSocketMessage = new WebSocketMessage();
        webSocketMessage.setUsername("test");
        webSocketMessage.setClient("client");
        webSocketMessage.setTarget("target");
        webSocketMessage.setMessType("Testtype");
        webSocketMessage.setJsonContent(new Gson().toJson(jsonContent));
        return webSocketMessage;
    }


}