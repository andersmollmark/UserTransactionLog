package main;

import com.delaval.usertransactionlogserver.domain.InternalClickLog;
import com.delaval.usertransactionlogserver.domain.InternalEventLog;
import com.delaval.usertransactionlogserver.domain.InternalUserTransactionKey;
import com.delaval.usertransactionlogserver.persistence.dao.OperationDAO;
import com.delaval.usertransactionlogserver.persistence.operation.*;
import com.delaval.usertransactionlogserver.websocket.MessTypes;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import com.google.gson.Gson;

import java.util.List;

/**
 * Created by delaval on 12/2/2015.
 */
public class SimpleORMTest {


    public SimpleORMTest() {
//        doTheSelectCount();


    }

    void testGetClickLogs() {
        testInsertClickLogs();

        GetClickLogsWithUserTransactionKeyOperation operation = new GetClickLogsWithUserTransactionKeyOperation();
        operation.setOperationParameter(new StringParameter("LEIF USERklientenIpad"));
        OperationResult<InternalClickLog> operationResult = OperationDAO.getInstance().doRead(operation);
        List<InternalClickLog> allLogs = operationResult.getResult();
        for (InternalClickLog l : allLogs) {
            System.out.println("ClickLog:\n");
            StringBuilder sb = new StringBuilder();
            sb.append("x:").append(l.getX()).append("\n").
                    append("tab:").append(l.getTab()).append("\n").
                    append("y:").append(l.getY()).append("\n").
                    append("timestamp:").append(l.getTimestamp()).append("\n");
            System.out.println(sb.toString());

        }
    }

    void testGetEventLogs() {
        testInsertEventLogs();

        GetEventLogsWithUserTransactionKeyOperation operation = new GetEventLogsWithUserTransactionKeyOperation();
        operation.setOperationParameter(new StringParameter("LEIF USERklientenIpad"));

        OperationResult<InternalEventLog> operationResult = OperationDAO.getInstance().doRead(operation);
        List<InternalEventLog> allLogs = operationResult.getResult();
        for (InternalEventLog l : allLogs) {
            System.out.println("EventLog:\n");
            StringBuilder sb = new StringBuilder();
            sb.append("name:").append(l.getName()).append("\n").
                    append("tab:").append(l.getTab()).append("\n").
                    append("category:").append(l.getCategory()).append("\n").
                    append("label:").append(l.getLabel()).append("\n").
                    append("timestamp:").append(l.getTimestampAsDate()).append("\n");
            System.out.println(sb.toString());

        }
    }


    void testGetUserTransactionKey() {
        GetAllUserTransactionKeysOperation operation = new GetAllUserTransactionKeysOperation();
        OperationResult<InternalUserTransactionKey> operationResult = OperationDAO.getInstance().doRead(operation);

        List<InternalUserTransactionKey> allUserTransactionKeys = operationResult.getResult();
        String firstId = null;
        for (InternalUserTransactionKey l : allUserTransactionKeys) {
            System.out.println("UserTransactionKey:\n");
            StringBuilder sb = new StringBuilder();
            sb.append("username:").append(l.getUsername()).append("\n").
                    append("client:").append(l.getClient()).append("\n").
                    append("timestamp:").append(l.getTimestamp()).append("\n");
            System.out.println(sb.toString());
            if (firstId == null) {
                firstId = l.getId();
            }

        }

        if (firstId != null) {
            GetClickLogsWithUserTransactionKeyOperation clickOperation = new GetClickLogsWithUserTransactionKeyOperation();
            clickOperation.setOperationParameter(new StringParameter(firstId));
            OperationResult<InternalClickLog> clickLogOperationResult = OperationDAO.getInstance().doRead(clickOperation);
            List<InternalClickLog> contents = clickLogOperationResult.getResult();
            for (InternalClickLog content : contents) {
                System.out.println("LogContent:\n");
                StringBuilder sb = new StringBuilder();
                sb.append("xpos:").append(content.getX()).append("\n").
                        append("ypos:").append(content.getY()).append("\n").
                        append("timestamp:").append(content.getTimestamp()).append("\n");
                System.out.println(sb.toString());
            }
        }

    }


    void testInsertClickLogs() {
        MyWebSocketMessage webSocketMessage = new MyWebSocketMessage();
        webSocketMessage.setClient("klienten");
        webSocketMessage.setUsername("LEIF USER");
        webSocketMessage.setTarget("Ipad");
        webSocketMessage.setMessType(MessTypes.CLICK_LOG.getMyValue());

        MyClickLogContent testContent = new MyClickLogContent();
        testContent.setCssClassName("cssClassName");
        testContent.setElementId("ettElementId");
        testContent.setX("x");
        testContent.setY("y");


//        for (int i = 0; i < 10; i++) {
            java.util.Date date = new java.util.Date();
            testContent.setTimestamp(Long.toString(date.getTime()));
            String jsonContent = new Gson().toJson(testContent);
            webSocketMessage.setJsonContent(jsonContent);
            doInsert(new CreateClickLogOperation(), webSocketMessage);
//        }

    }

    void testInsertEventLogs() {
        MyWebSocketMessage webSocketMessage = new MyWebSocketMessage();
        webSocketMessage.setClient("klienten");
        webSocketMessage.setUsername("LEIF USER");
        webSocketMessage.setTarget("Ipad");
        webSocketMessage.setMessType(MessTypes.EVENT_LOG.getMyValue());

        MyEventLogContent testContent = new MyEventLogContent();
        testContent.setTab("milktab");
        testContent.setEventCategory("aCategory");
        testContent.setEventLabel("aLabel");
        testContent.setEventName("aName");


        java.util.Date date = new java.util.Date();
        testContent.setTimestamp(Long.toString(date.getTime()));
        String jsonContent = new Gson().toJson(testContent);
        webSocketMessage.setJsonContent(jsonContent);

        doInsert(new CreateEventLogOperation(), webSocketMessage);
    }


    private <T extends CreateUpdateOperation> void doInsert(T crudOperation, WebSocketMessage webSocketMessage) {
        crudOperation.setMessage(webSocketMessage);
        OperationDAO.getInstance().doCreateUpdate(crudOperation);
    }

    private static class MyWebSocketMessage extends WebSocketMessage {
        private String jsonContent;
        private String client;
        private String username;
        private String messType;


        public String getClient() {
            return client;
        }


        public String getUsername() {
            return username;
        }


        public String getTarget() {
            return target;
        }


        public String getJsonContent() {
            return jsonContent;
        }


        public String getType() {
            return messType;
        }

        private String target;


        public void setJsonContent(String jsonContent) {
            this.jsonContent = jsonContent;
        }

        public void setClient(String client) {
            this.client = client;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setMessType(String messType) {
            this.messType = messType;
        }

        public void setTarget(String target) {
            this.target = target;
        }
    }

    private static class MyClickLogContent {
        private String x;
        private String y;
        private String elementId;
        private String cssClassName;
        private String timestamp;

        public String getX() {
            return x;
        }

        public void setX(String x) {
            this.x = x;
        }


        public String getY() {
            return y;
        }

        public void setY(String y) {
            this.y = y;
        }


        public String getElementId() {
            return elementId;
        }

        public void setElementId(String elementId) {
            this.elementId = elementId;
        }


        public String getCssClassName() {
            return cssClassName;
        }

        public void setCssClassName(String cssClassName) {
            this.cssClassName = cssClassName;
        }


        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }

    private static class MyEventLogContent {
        private String eventName;
        private String eventCategory;
        private String eventLabel;
        private String timestamp;
        private String tab;

        public String getEventName() {
            return eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }

        public String getEventCategory() {
            return eventCategory;
        }

        public void setEventCategory(String eventCategory) {
            this.eventCategory = eventCategory;
        }

        public String getEventLabel() {
            return eventLabel;
        }

        public void setEventLabel(String eventLabel) {
            this.eventLabel = eventLabel;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getTab() {
            return tab;
        }

        public void setTab(String tab) {
            this.tab = tab;
        }
    }

    public static void main(String[] args) {

        SimpleORMTest test = new SimpleORMTest();
        test.testInsertClickLogs();
        test.testInsertEventLogs();
        test.testGetUserTransactionKey();
        test.testGetEventLogs();
        test.testGetClickLogs();
    }

}
