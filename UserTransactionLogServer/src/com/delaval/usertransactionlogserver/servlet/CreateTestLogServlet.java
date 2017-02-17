package com.delaval.usertransactionlogserver.servlet;

import com.delaval.usertransactionlogserver.websocket.MessTypes;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by delaval on 12/7/2015.
 */
@WebServlet("/servlet/testServlet")
public class CreateTestLogServlet extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException {
        // Set response content type
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        String titleClickLog = "Create a clicklog";
        String titleEventLog = "Create a eventlog";
        String docType =
                "<!doctype html public \"-//w3c//dtd html 4.0 " +
                        "transitional//en\">\n";

        ServletHelper helper = new ServletHelper();
        StringBuilder clickLogInputs = new StringBuilder();
        clickLogInputs.append(helper.getTRTag(helper.getDisabledInputTextTag(MessTypes.CLICK_LOG.getMyValue())));
        for(ServletHelper.ClickLogValues aValue : ServletHelper.ClickLogValues.values()){
            clickLogInputs.append(helper.getTRTag(helper.getInputTextTag(aValue.getMyValue())));
        }

        StringBuilder eventLogInputs = new StringBuilder();
        eventLogInputs.append(helper.getTRTag(helper.getDisabledInputTextTag(MessTypes.EVENT_LOG.getMyValue())));
        for(ServletHelper.EventLogValues aValue : ServletHelper.EventLogValues.values()){
            eventLogInputs.append(helper.getTRTag(helper.getInputTextTag(aValue.getMyValue())));
        }

        out.println(docType +
                        "<html>\n" +
                        "<head><title>" + titleClickLog + "</title></head>\n" +
                        "<body bgcolor=\"#f0f0f0\">\n" +
                        "<h1 align=\"center\">" + titleClickLog + "</h1>\n" +
                        "<form method=\"POST\" action=\"/servlet/saveTestLog\">" +
                        "<table>\n" +
                        clickLogInputs.toString() +
                        "</table>\n" +
                "<input type='hidden' name='savetype' value='click' />" +
                        "<input type=\"submit\" value=\"Save log\">" +
                        "</form>" +
                        "<br/>" +
                        "<h1 align=\"center\">" + titleEventLog + "</h1>\n" +
                "<form method=\"POST\" action=\"/servlet/saveTestLog\">" +
                "<table>\n" +
                eventLogInputs.toString() +
                "</table>\n" +
                "<input type='hidden' name='savetype' value='event' />" +
                "<input type=\"submit\" value=\"Save log\">" +
                "</form>" +

                "</body></html>");
    }


}
