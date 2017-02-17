/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import com.delaval.usertransactionlogserver.ServerProperties;
import com.delaval.usertransactionlogserver.jms.JmsResourceFactory;
import com.delaval.usertransactionlogserver.jms.producer.JmsMessageCreator;
import com.delaval.usertransactionlogserver.websocket.ClickLogContent;
import com.delaval.usertransactionlogserver.websocket.EventLogContent;
import com.delaval.usertransactionlogserver.websocket.UserTransactionLogWebSocket;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import com.google.gson.Gson;
import org.springframework.jms.core.JmsTemplate;

import java.util.Date;

/**
 *
 * @author delaval
 */
public class TestMain {
//    AnnotationConfigApplicationContext ctx;


    public TestMain(){
        // init spring context
//        ApplicationContext ctx = new ClassPathXmlApplicationContext("app-context.xml");
        // get bean from context
//        jmsMessageSender = (JmsMessageSender) ctx.getBean("jmsMessageSender");


//        ctx = new AnnotationConfigApplicationContext(AppConfigClickLog.class);
        sendClickEventsWithJmsCreator(50, 100);
        sendEventsLogsWithJmsCreator(50, 100);
//        sendWithResourceFactory();
//        sendWithJmsCreator();
//        sendWithJmsMessageSender();
//        listenToQueue();
        // close spring application context
//        ((ClassPathXmlApplicationContext) ctx).close();
//        ctx.close();
    }

    public void sendWithResourceFactory(){

        String content = "{\"messType\":\"userLog\",\"content\":\"{\\\"x\\\":640,\\\"y\\\":680,\\\"id\\\":\\\"\\\",\\\"className\\\":\\\"row\\\"}\"}";
        UserTransactionLogWebSocket userTransactionLogWebSocket = new UserTransactionLogWebSocket();
        userTransactionLogWebSocket.handleMessage(null, content);
//        try {
//            JmsMessageCreator messageCreator = new JmsMessageCreator("pinging jms-queue with factory");
//            JmsTemplate jmsTemplate = JmsResourceFactory.getInstance().getJmsTemplate();
//            jmsTemplate.send(AppConfig.JMS_DESTINATION, messageCreator);
//        }
//        finally {
//            JmsResourceFactory.getInstance().closeContext();
//        }
    }

   

    public void sendClickEventsWithJmsCreator(int nrOfUsers, int nrOfLogsPerUser){
        for(int i=0; i<nrOfUsers; i++){
            for(int j=1; j<=nrOfLogsPerUser; j++){
                JmsMessageCreator messageCreator = new JmsMessageCreator(getClickLogWebSocketMessageAsJson(j));
                JmsTemplate jmsTemplate = JmsResourceFactory.getClickLogInstance().getJmsTemplate();
                jmsTemplate.send(ServerProperties.getInstance().getProp(ServerProperties.PropKey.JMS_QUEUE_DEST_CLICK), messageCreator);
            }
        }
    }

    public void sendEventsLogsWithJmsCreator(int nrOfUsers, int nrOfLogsPerUser){
        for(int i=0; i<nrOfUsers; i++){
            for(int j=1; j<=nrOfLogsPerUser; j++){
                JmsMessageCreator messageCreator = new JmsMessageCreator(getEventLogWebSocketMessageAsJson(j));
                JmsTemplate jmsTemplate = JmsResourceFactory.getEventLogInstance().getJmsTemplate();
                jmsTemplate.send(ServerProperties.getInstance().getProp(ServerProperties.PropKey.JMS_QUEUE_DEST_EVENT), messageCreator);
            }
        }
    }


    private String getClickLogWebSocketMessageAsJson(int j){
        WebSocketMessage webSocketMessage = new WebSocketMessage();
        webSocketMessage.setMessType("clickLog");
        webSocketMessage.setTarget("testklient");
        webSocketMessage.setClient("testtarget.localhost." + j);
        webSocketMessage.setUsername("theTester" + j);

        EventLogContent eventLogContent = new EventLogContent();
        eventLogContent.setEventName("testEvent");
        eventLogContent.setEventLabel("testLabel");
        eventLogContent.setEventCategory("testCategory");
        eventLogContent.setTab("testTab");
        eventLogContent.setTimestamp(Long.toString(new Date().getTime()));
        String json = new Gson().toJson(eventLogContent);
        webSocketMessage.setJsonContent(json);
        return new Gson().toJson(webSocketMessage);
    }

    private String getEventLogWebSocketMessageAsJson(int j){
        WebSocketMessage webSocketMessage = new WebSocketMessage();
        webSocketMessage.setMessType("clickLog");
        webSocketMessage.setTarget("testklient");
        webSocketMessage.setClient("testtarget.localhost." + j);
        webSocketMessage.setUsername("theTester" + j);

        ClickLogContent clickLogContent = new ClickLogContent();
        clickLogContent.setX("x");
        clickLogContent.setY("y");
        clickLogContent.setTab("testtab");
        clickLogContent.setElementId("testElementId");
        clickLogContent.setCssClassName("testcss");
        clickLogContent.setTimestamp(Long.toString(new Date().getTime()));
        String json = new Gson().toJson(clickLogContent);
        webSocketMessage.setJsonContent(json);
        return new Gson().toJson(webSocketMessage);
    }


    public void listenToQueue(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        TestMain demoMain = new TestMain();
    }
    
}
