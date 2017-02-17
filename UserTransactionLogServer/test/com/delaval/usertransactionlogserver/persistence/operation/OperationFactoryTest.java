package com.delaval.usertransactionlogserver.persistence.operation;

import com.delaval.usertransactionlogserver.domain.InternalSystemProperty;
import com.delaval.usertransactionlogserver.util.DateUtil;
import com.delaval.usertransactionlogserver.websocket.*;
import com.google.gson.Gson;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by delaval on 2016-01-22.
 */
public class OperationFactoryTest {

    @Test
    public void testGetCreateUpdateOperationWithCreateSystemProperty() throws Exception {
        OperationParam<CreateSystemPropertyOperation> test = new OperationParam<>(CreateSystemPropertyOperation.class, new WebSocketMessage());
        CreateUpdateOperation createUpdateOperation = OperationFactory.getCreateUpdateOperation(null, test);
        assertThat(createUpdateOperation instanceof CreateSystemPropertyOperation, is(true));
    }

    @Test
    public void testGetCreateUpdateOperationWithCreateEventLog() throws Exception {
        OperationParam<CreateEventLogOperation> test = new OperationParam<>(CreateEventLogOperation.class, new WebSocketMessage());
        CreateUpdateOperation createUpdateOperation = OperationFactory.getCreateUpdateOperation(null, test);
        assertThat(createUpdateOperation instanceof CreateEventLogOperation, is(true));
    }

    @Test
    public void testGetCreateUpdateOperationWithCreateClickLog() throws Exception {
        OperationParam<CreateClickLogOperation> test = new OperationParam<>(CreateClickLogOperation.class, new WebSocketMessage());
        CreateUpdateOperation createUpdateOperation = OperationFactory.getCreateUpdateOperation(null, test);
        assertThat(createUpdateOperation instanceof CreateClickLogOperation, is(true));
    }


    @Test
    public void testGetReadOperationWithSystemProperty() throws Exception {
        OperationParam<GetSystemPropertyWithNameOperation> test = new OperationParam<>(GetSystemPropertyWithNameOperation.class, new WebSocketMessage());
        ReadOperation readOperation = OperationFactory.getReadOperation(null, test);
        assertThat(readOperation instanceof GetSystemPropertyWithNameOperation, is(true));
    }

    @Test
    public void testGetReadOperationWithClickLog() throws Exception {
        OperationParam<GetClickLogsWithUserTransactionKeyOperation> test = new OperationParam<>(GetClickLogsWithUserTransactionKeyOperation.class, new WebSocketMessage());
        ReadOperation readOperation = OperationFactory.getReadOperation(null, test);
        assertThat(readOperation instanceof GetClickLogsWithUserTransactionKeyOperation, is(true));
    }

    @Test
    public void testGetReadOperationWithEventLog() throws Exception {
        OperationParam<GetEventLogsWithUserTransactionKeyOperation> test = new OperationParam<>(GetEventLogsWithUserTransactionKeyOperation.class, new WebSocketMessage());
        ReadOperation readOperation = OperationFactory.getReadOperation(null, test);
        assertThat(readOperation instanceof GetEventLogsWithUserTransactionKeyOperation, is(true));
    }

    @Test
    public void testGetReadOperationWithUserInteractionKey() throws Exception {
        OperationParam<GetAllUserTransactionKeysOperation> test = new OperationParam<>(GetAllUserTransactionKeysOperation.class, new WebSocketMessage());
        ReadOperation readOperation = OperationFactory.getReadOperation(null, test);
        assertThat(readOperation instanceof GetAllUserTransactionKeysOperation, is(true));
    }

    @Test
    public void testGetCreateSystemPropertyParam() throws Exception {
        String name = "Name";
        String value = "Value";
        SystemPropertyContent content = getSystemPropertyContent(name, value);
        WebSocketMessage websocketMessage = getWebsocketMessage(content);
        OperationParam<CreateSystemPropertyOperation> createSystemPropertyParam = OperationFactory.getCreateSystemPropertyParam(websocketMessage);
        assertThat(createSystemPropertyParam.getParameter(), nullValue());
        SystemPropertyContent contentFromJson = new Gson().fromJson(createSystemPropertyParam.getWebSocketMessage().getJsonContent(), SystemPropertyContent.class);
        assertThat(contentFromJson.getName(), is(name));
        assertThat(contentFromJson.getValue(), is(value));
    }

    @Test
    public void testGetCreateSystemPropertyParamForSystem() throws Exception {
        String name = "Name";
        String value = "Value";
        String system = "System";
        String id = "id";
        InternalSystemProperty systemProperty = new InternalSystemProperty();
        systemProperty.setName(name);
        systemProperty.setValue(value);
        systemProperty.setId(id);

        OperationParam<CreateSystemPropertyOperation> createSystemPropertyParam = OperationFactory.getCreateSystemPropertyParamForSystem(systemProperty);
        assertThat(createSystemPropertyParam.getParameter(), nullValue());
        SystemPropertyContent contentFromJson = new Gson().fromJson(createSystemPropertyParam.getWebSocketMessage().getJsonContent(), SystemPropertyContent.class);
        assertThat(contentFromJson.getName(), is(name));
        assertThat(contentFromJson.getValue(), is(value));
        WebSocketMessage webSocketMessage = createSystemPropertyParam.getWebSocketMessage();
        assertThat(webSocketMessage.getClient(), is(system));
        assertThat(webSocketMessage.getTarget(), is(system));
        assertThat(webSocketMessage.getUsername(), is(system));
    }

    @Test
    public void testGetCreateEventLogParam() throws Exception {
        WebSocketMessage websocketMessage = getWebsocketMessage(getEventLogContent());
        OperationParam<CreateEventLogOperation> createEventLogParam = OperationFactory.getCreateEventLogParam(websocketMessage);
        assertThat(createEventLogParam.getParameter(), is(nullValue()));
        assertThat(createEventLogParam.getWebSocketMessage(), is(notNullValue()));
    }

    @Test
    public void testGetCreateClickLogParam() throws Exception {
        WebSocketMessage websocketMessage = getWebsocketMessage(getClickLogContent());
        OperationParam<CreateClickLogOperation> createClickLogParam = OperationFactory.getCreateClickLogParam(websocketMessage);
        assertThat(createClickLogParam.getParameter(), is(nullValue()));
        assertThat(createClickLogParam.getWebSocketMessage(), is(notNullValue()));
    }

    private EventLogContent getEventLogContent(){
        EventLogContent eventLogContent = new EventLogContent();
        eventLogContent.setTab("tab");
        eventLogContent.setEventCategory("category");
        eventLogContent.setEventLabel("label");
        eventLogContent.setEventName("eventname");
        return eventLogContent;
    }

    private ClickLogContent getClickLogContent(){
        ClickLogContent clickLogContent = new ClickLogContent();
        clickLogContent.setCssClassName("css");
        clickLogContent.setElementId("elementId");
        clickLogContent.setTab("tab");
        clickLogContent.setX("x");
        clickLogContent.setY("y");
        return clickLogContent;
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