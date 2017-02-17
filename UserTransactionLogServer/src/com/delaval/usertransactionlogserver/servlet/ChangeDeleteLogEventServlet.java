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
@WebServlet("/servlet/changeDeleteLogEvent")
public class ChangeDeleteLogEventServlet extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException {
        // Set response content type
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        String title = "Configuration of delete log-event";
        String docType =
                "<!doctype html public \"-//w3c//dtd html 4.0 " +
                        "transitional//en\">\n";

        ServletHelper helper = new ServletHelper();

        StringBuilder eventValues = new StringBuilder();
        eventValues.append(helper.getTRTag(helper.getDisabledInputTextTag(MessTypes.CLICK_LOG.getMyValue())));
        for(ServletHelper.ClickLogValues aValue : ServletHelper.ClickLogValues.values()){
            eventValues.append(helper.getTRTag(helper.getInputTextTag(aValue.getMyValue())));
        }

    }


}
