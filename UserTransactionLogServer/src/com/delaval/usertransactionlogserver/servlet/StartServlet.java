package com.delaval.usertransactionlogserver.servlet;

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
@WebServlet("/servlet")
public class StartServlet extends HttpServlet {



    public void doGet(HttpServletRequest request,
                       HttpServletResponse response)
            throws ServletException, IOException {
        // Set response content type
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();

        String title = "Startingpoint for testservlets in usertransactionLogServer";
        String docType = "<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">";
        StringBuilder result = new StringBuilder(docType);
        result.append("<html>").append("<head><title>").append(title).append("</title></head>")
                .append("<body bgcolor=\"#f0f0f0\">")
                .append("<h1 align=\"center\">").append(title).append("</h1>");

            final StringBuilder context = new StringBuilder();
                    context.append("<a href=\"/servlet/getUserTransactionKey")
                    .append("\">")
                    .append("Look at which users and targets has logs (UserTransactionKey)")
                    .append("</a>")
                    .append("<br/>");

            context.append("<a href=\"/servlet/testServlet")
                    .append("\">")
                    .append("Create a testlog that via activemq shall end up in db")
                    .append("</a>")
                    .append("<br/>");

            result.append(context);
            result.append("</body></html>");

        out.println(result.toString());
    }
}
