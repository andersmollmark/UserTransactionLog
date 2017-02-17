package com.delaval.usertransactionlogserver.servlet;

import com.delaval.usertransactionlogserver.domain.InternalUserTransactionKey;
import com.delaval.usertransactionlogserver.persistence.dao.OperationDAO;
import com.delaval.usertransactionlogserver.persistence.operation.GetAllUserTransactionKeysOperation;
import com.delaval.usertransactionlogserver.persistence.operation.OperationParam;

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
@WebServlet("/servlet/getUserTransactionKey")
public class GetUserTransactionKeyServlet extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException {
        // Set response content type
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();

        String title = "Messages in db";
        String docType = "<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">";
        StringBuilder result = new StringBuilder(docType);
        result.append("<html>").append("<head><title>").append(title).append("</title></head>")
                .append("<body bgcolor=\"#f0f0f0\">")
                .append("<h1 align=\"center\">").append(title).append("</h1>");

        try {
            OperationParam<GetAllUserTransactionKeysOperation> operationParam = new OperationParam<>(GetAllUserTransactionKeysOperation.class);
            GetAllUserTransactionKeysOperation operation = OperationDAO.getInstance().executeOperation(operationParam);
            List<InternalUserTransactionKey> allUserTransactionKeys = operation.getResult();
            final StringBuilder context = new StringBuilder();
            allUserTransactionKeys.forEach(userTransactionKey ->
                    context.append("<a href=\"/servlet/getLogContent")
                            .append("?user=").append(userTransactionKey.getUsername())
                            .append("&target=").append(userTransactionKey.getTarget())
                            .append("&userTransactionKeyId=").append(userTransactionKey.getId()).append("\">")
                            .append(userTransactionKey.getUsername())
                            .append(" with target:").append(userTransactionKey.getTarget())
                            .append(" and client target:").append(userTransactionKey.getClient())
                            .append("</a>")
                            .append(" timestamp:").append(userTransactionKey.getTimestamp())
                            .append("<br/>")
            );

            result.append(context);
            result.append("</body></html>");
        } catch (Exception ex) {
            result = new StringBuilder(docType);
            result.append("<html>").append("<head><title>").append(title).append("</title></head>")
                    .append("<body bgcolor=\"#f0f0f0\">")
                    .append("<h1 align=\"center\">").append(title).append("</h1>")
                    .append("Something went wrong:")
                    .append(ex.getMessage())
                    .append("</body></html>");
        }

        out.println(result.toString());
    }
}
