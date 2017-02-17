package com.delaval.usertransactionlogserver.servlet;

import com.delaval.usertransactionlogserver.websocket.UserTransactionLogWebSocket;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.annotation.WebServlet;

/**
 * Servlet that registers the websocket-class and makes it possible for the client to connect via websocket
 * to UTL-servern
 */
@SuppressWarnings("serial")
@WebServlet(name = "LogWebsocketServlet WebSocket Servlet", urlPatterns = { "/ws" })
public class LogWebSocketServlet extends WebSocketServlet {

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.register(UserTransactionLogWebSocket.class);
    }
}
