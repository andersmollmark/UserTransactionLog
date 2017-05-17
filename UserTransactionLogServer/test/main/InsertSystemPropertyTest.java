package main;

import com.delaval.usertransactionlogserver.domain.InternalSystemProperty;
import com.delaval.usertransactionlogserver.persistence.dao.OperationDAO;
import com.delaval.usertransactionlogserver.persistence.operation.*;
import com.delaval.usertransactionlogserver.websocket.MessTypes;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import com.google.gson.Gson;
import org.junit.Assert;

import java.util.List;

/**
 * Created by delaval on 12/2/2015.
 */
public class InsertSystemPropertyTest {


    public InsertSystemPropertyTest() {
    }

    void testGetSystemProperty() {
        String name = "propName";
        GetSystemPropertyWithNameOperation operation = new GetSystemPropertyWithNameOperation();
        operation.setOperationParameter(new StringParameter(name));
        OperationResult<InternalSystemProperty> operationResult = OperationDAO.getInstance().doRead(operation);
        List<InternalSystemProperty> result = operationResult.getResult();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.size() > 0);
    }

    void testInsertSystemProperty() {
        MyWebSocketMessage webSocketMessage = new MyWebSocketMessage();
        webSocketMessage.setClient("klienten");
        webSocketMessage.setUsername("LEIF USER");
        webSocketMessage.setTarget("Ipad");
        webSocketMessage.setMessType(MessTypes.EVENT_LOG.getMyValue());

        MySystemPropertyContent testContent = new MySystemPropertyContent();
        testContent.setName("propName");
        testContent.setValue("propValue");


        java.util.Date date = new java.util.Date();
        testContent.setTimestamp(Long.toString(date.getTime()));
        String jsonContent = new Gson().toJson(testContent);
        webSocketMessage.setJsonContent(jsonContent);

        CreateSystemPropertyOperation operation = new CreateSystemPropertyOperation();
        operation.setMessage(webSocketMessage);

        OperationDAO.getInstance().doCreateUpdate(operation);
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

    private static class MySystemPropertyContent {
        private String value;
        private String name;
        private String timestamp;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }


    public static void main(String[] args) {

        InsertSystemPropertyTest test = new InsertSystemPropertyTest();
        test.testInsertSystemProperty();
        test.testGetSystemProperty();
    }

}
