package com.delaval.usertransactionlogserver.servlet;

import com.delaval.usertransactionlogserver.ServerProperties;
import com.delaval.usertransactionlogserver.jms.JmsResourceFactory;
import com.delaval.usertransactionlogserver.jms.producer.JmsMessageCreator;
import com.delaval.usertransactionlogserver.util.DateUtil;
import com.delaval.usertransactionlogserver.websocket.MessTypes;
import com.google.gson.Gson;
import org.springframework.jms.core.JmsTemplate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Created by delaval on 12/7/2015.
 */
@WebServlet("/servlet/saveTestLog")
public class SaveTestLogServlet extends HttpServlet {

    public void doGet(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException {
        // Set response content type
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();

        ServletHelper helper = new ServletHelper();

        MyWebSocketMessage webSocketMessage = new MyWebSocketMessage();



        webSocketMessage.setClient(helper.getParam(request, ServletHelper.ClickLogValues.CLIENT.getMyValue()));
        webSocketMessage.setUsername(helper.getParam(request, ServletHelper.ClickLogValues.USERNAME.getMyValue()));
        webSocketMessage.setTarget(helper.getParam(request, ServletHelper.ClickLogValues.TARGET.getMyValue()));

        String savetype = helper.getParam(request, "savetype");
        if("event".equals(savetype)){
            webSocketMessage.setJsonContent(getJsonContentEventlog(request, helper));
            webSocketMessage.setMessType(MessTypes.EVENT_LOG.getMyValue());
        }
        else{
            webSocketMessage.setJsonContent(getJsonContentClicklog(request, helper));
            webSocketMessage.setMessType(MessTypes.CLICK_LOG.getMyValue());
        }

        String title = "Saving log to db";
        String docType =
                "<!doctype html public \"-//w3c//dtd html 4.0 " +
                        "transitional//en\">\n";

        String content = "<html>\n" +
                "<head><title>" + title + "</title></head>\n" +
                "<body bgcolor=\"#f0f0f0\">\n" +
                "<h1 align=\"center\">" + title + "</h1>\n" +
                "<form method=\"GET\" action=\"/servlet/getUserTransactionKey\">" +
                "Mess saved:\n" +
                webSocketMessage.toString() +
                "<br><input type=\"submit\" value=\"Get mess\">" +
                "</form>" +
                "</body></html>";

        try{
            String jsonMessage = new Gson().toJson(webSocketMessage);
            if (MessTypes.EVENT_LOG.isSame(webSocketMessage.getMessType())) {
                sendJmsTemplate(jsonMessage, ServerProperties.PropKey.JMS_QUEUE_DEST_EVENT, JmsResourceFactory.getEventLogInstance());
            } else {
                sendJmsTemplate(jsonMessage, ServerProperties.PropKey.JMS_QUEUE_DEST_CLICK, JmsResourceFactory.getClickLogInstance());
            }
        }
        catch (Exception ex){
            content = "<html>\n" +
                    "<head><title>" + title + "</title></head>\n" +
                    "<body bgcolor=\"#f0f0f0\">\n" +
                    "<h1 align=\"center\">" + title + "</h1>\n" +
                    "Something went wrong:\n" +
                    ex.getMessage() +
                    "</body></html>";
        }

        out.println(docType + content);
    }

    private void sendJmsTemplate(String jsonMessage, ServerProperties.PropKey jmsDest, JmsResourceFactory jmsResourceFactory) {
        JmsMessageCreator messageCreator = new JmsMessageCreator(jsonMessage);
        JmsTemplate jmsTemplate = jmsResourceFactory.getJmsTemplate();
        String jmsDestination = getProp(jmsDest);
        jmsTemplate.send(jmsDestination, messageCreator);
    }
    private String getProp(ServerProperties.PropKey propKey) {
        return ServerProperties.getInstance().getProp(propKey);
    }

    private String getJsonContentClicklog(HttpServletRequest request, ServletHelper helper){
        MyClickLogContent testContent = new MyClickLogContent();
        testContent.setCssClassName(helper.getParam(request, ServletHelper.ClickLogValues.CSS.getMyValue()));
        testContent.setElementId(helper.getParam(request, ServletHelper.ClickLogValues.ELEMENT_ID.getMyValue()));
        testContent.setX(helper.getParam(request, ServletHelper.ClickLogValues.X_POS.getMyValue()));
        testContent.setY(helper.getParam(request, ServletHelper.ClickLogValues.Y_POS.getMyValue()));
        testContent.setTab(helper.getParam(request, ServletHelper.ClickLogValues.TAB.getMyValue()));
        Date date = new Date();
        testContent.setTimestamp(Long.toString(date.getTime()));
        return new Gson().toJson(testContent);
    }

    private String getJsonContentEventlog(HttpServletRequest request, ServletHelper helper){
        MyEventLogContent testContent = new MyEventLogContent();
        testContent.setEventName(helper.getParam(request, ServletHelper.EventLogValues.NAME.getMyValue()));
        testContent.setEventCategory(helper.getParam(request, ServletHelper.EventLogValues.CATEGORY.getMyValue()));
        testContent.setEventLabel(helper.getParam(request, ServletHelper.EventLogValues.LABEL.getMyValue()));
        testContent.setTab(helper.getParam(request, ServletHelper.ClickLogValues.TAB.getMyValue()));
        Date date = new Date();
        testContent.setTimestamp(Long.toString(date.getTime()));
        return new Gson().toJson(testContent);
    }


    private static class MyWebSocketMessage  {
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

        public String getMessType() {
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

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append("Messtype:").append(getMessType())
                    .append(", client:").append(getClient())
                    .append(", username:").append(getUsername())
                    .append(", target:").append(getTarget())
                    .append(", Content:").append(getJsonContent());
            return sb.toString();
        }

    }

    private static class MyClickLogContent {
        private String x;
        private String y;
        private String elementId;
        private String cssClassName;
        private String timestamp;
        private String tab;

        public String getTab() {
            return tab;
        }

        public void setTab(String tab) {
            this.tab = tab;
        }

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

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Content:")
                    .append("x-pos:").append(getX())
                    .append(", y-pos:").append(getY())
                    .append(", elementId:").append(getElementId())
                    .append(", cssClassName:").append(getCssClassName())
                    .append(", tab:").append(getTab())
                    .append(", time:").append(DateUtil.formatTimeStamp(Long.parseLong(getTimestamp())));
            return sb.toString();
        }
    }

    private static class MyEventLogContent{
        private String eventName;
        private String eventCategory;
        private String eventLabel;
        private String timestamp;
        private String tab;

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }

        public void setEventCategory(String eventCategory) {
            this.eventCategory = eventCategory;
        }

        public void setEventLabel(String eventLabel) {
            this.eventLabel = eventLabel;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public void setTab(String tab) {
            this.tab = tab;
        }

        public String getTab() {
            return tab;
        }

        public String getEventName() {
            return eventName;
        }

        public String getEventCategory() {
            return eventCategory;
        }

        public String getEventLabel() {
            return eventLabel;
        }

        public String getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("EventLogContent:")
                    .append("eventName:").append(getEventName())
                    .append("eventCategory:").append(getEventCategory())
                    .append("eventLabel").append(getEventLabel())
                    .append("activetab").append(getTab())
                    .append(", time:").append(DateUtil.formatTimeStamp(Long.parseLong(getTimestamp())));
            return sb.toString();
        }
    }
}
