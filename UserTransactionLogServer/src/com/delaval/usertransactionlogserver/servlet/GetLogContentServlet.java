package com.delaval.usertransactionlogserver.servlet;

import com.delaval.usertransactionlogserver.domain.InternalClickLog;
import com.delaval.usertransactionlogserver.domain.InternalEventLog;
import com.delaval.usertransactionlogserver.persistence.dao.OperationDAO;
import com.delaval.usertransactionlogserver.persistence.operation.GetClickLogsWithUserTransactionKeyOperation;
import com.delaval.usertransactionlogserver.persistence.operation.GetEventLogsWithUserTransactionKeyOperation;
import com.delaval.usertransactionlogserver.persistence.operation.OperationParam;
import com.delaval.usertransactionlogserver.util.DateUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by delaval on 12/7/2015.
 */
@WebServlet("/servlet/getLogContent")
public class GetLogContentServlet extends HttpServlet {

    public void doGet(HttpServletRequest request,
                       HttpServletResponse response)
            throws ServletException, IOException {
        // Set response content type
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();

        ServletHelper helper = new ServletHelper();

        String user = helper.getParam(request, "user");
        String target = helper.getParam(request, "target");
        String userTransactionKeyId = helper.getParam(request, "userTransactionKeyId");

        String title = "Content in db for user:" + user + " with a " + target;
        String docType =
                "<!doctype html public \"-//w3c//dtd html 4.0 " +
                        "transitional//en\">\n";

        StringBuilder page = new StringBuilder(docType);
        page.append("<html>\n")
                        .append("<head><title>").append(title).append("</title></head>")
                        .append("<body bgcolor=\"#f0f0f0\">")
                .append("<h1 align=\"center\">").append(title).append("</h1>");
        
        StringBuilder eventContent = new StringBuilder();
        eventContent.append("<table border='1'>")
                    .append("<caption>EventLogs</caption><tr>")
                        .append(helper.getTHTag("name"))
                        .append(helper.getTHTag("category"))
                        .append(helper.getTHTag("label"))
                        .append(helper.getTHTag("timestamp"))
                        .append(helper.getTHTag("tab"))
                        .append("</tr>");

        StringBuilder clickContent = new StringBuilder();
        clickContent.append("<table border='1'>")
                .append("<caption>ClickLogs</caption><tr>")
                .append(helper.getTHTag("x-pos"))
                .append(helper.getTHTag("y-pos"))
                .append(helper.getTHTag("css-class"))
                .append(helper.getTHTag("elementId"))
                .append(helper.getTHTag("timestamp"))
                .append(helper.getTHTag("tab"))
                .append("</tr>");

        try {
            OperationParam<GetClickLogsWithUserTransactionKeyOperation> clickOperationParam = new OperationParam<>(GetClickLogsWithUserTransactionKeyOperation.class);
            clickOperationParam.setParameter(userTransactionKeyId);
            GetClickLogsWithUserTransactionKeyOperation clickOperation = OperationDAO.getInstance().executeOperation(clickOperationParam);
            List<InternalClickLog> allClickLogs = clickOperation.getResult();
            for (InternalClickLog l : allClickLogs) {
                StringBuilder logContent = new StringBuilder();
                logContent.append(helper.getTDTag(l.getX()))
                        .append(helper.getTDTag(l.getY()))
                        .append(helper.getTDTag(l.getCssClassName()))
                        .append(helper.getTDTag(l.getElementId()))
                        .append(helper.getTDTag(DateUtil.formatTimeStamp(l.getTimestamp())))
                        .append(helper.getTDTag(l.getTab()));
                clickContent.append(helper.getTRTag(logContent.toString()));
            }

            OperationParam<GetEventLogsWithUserTransactionKeyOperation> operationParam = new OperationParam<>(GetEventLogsWithUserTransactionKeyOperation.class);
            operationParam.setParameter(userTransactionKeyId);
            GetEventLogsWithUserTransactionKeyOperation getEventLogsWithUserTransactionKey = OperationDAO.getInstance().executeOperation(operationParam);
            List<InternalEventLog> allEventLogs = getEventLogsWithUserTransactionKey.getResult();
            for (InternalEventLog l : allEventLogs) {
                StringBuilder logContent = new StringBuilder();
                logContent.append(helper.getTDTag(l.getName()))
                        .append(helper.getTDTag(l.getCategory()))
                        .append(helper.getTDTag(l.getLabel()))
                        .append(helper.getTDTag(DateUtil.formatTimeStamp(l.getTimestampAsDate())))
                        .append(helper.getTDTag(l.getTab()));
                eventContent.append(helper.getTRTag(logContent.toString()));
            }
            page.append(eventContent)
                    .append("<br/><br/><br/><br/>")
                    .append(clickContent)
                    .append("</body></html>");
        } catch (Exception ex) {
            page = new StringBuilder(docType);
            page.append("<html>")
                    .append("<head><title>").append(title).append("</title></head>")
                    .append("<body bgcolor=\"#f0f0f0\">")
                    .append("<h1 align=\"center\">").append(title).append("</h1>")
                    .append("Something went wrong:")
                    .append(ex.getMessage())
                    .append("</body></html>");
        }

        out.println(page.toString());
    }
}
