package com.delaval.usertransactionlogserver.websocket;

import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import main.TestUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


/**
 * Created by delaval on 2016-02-18.
 */
public class UserTransactionLogWebSocketTest {


    SessionController sessionController = SessionController.getInstance();

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        AtomicReference<Map<Session, Date>> sessions = TestUtils.getFieldValue("sessions", SessionController.class, sessionController);
        sessions.set(new HashMap<>());
        AtomicReference<Map<String, List<Session>>> sessionsPerHost = TestUtils.getFieldValue("sessionsPerHost", SessionController.class, sessionController);
        sessionsPerHost.set(new HashMap<>());
    }


    @Test
    public void testOnconnectWithOneSessionShallSaveOneSession() throws Exception {
        String ip = "10.34.666";
        Session testSession = new MySession(ip);
        assertThat(sessionController.getSessionsPerHost(testSession).size(), is(0));
        UserTransactionLogWebSocket testInstance = new UserTransactionLogWebSocket();
        testInstance.onconnect(testSession);
        assertThat(sessionController.getSessionsPerHost(testSession).size(), is(1));
    }

    @Test
    public void testOnconnectWithTwoSessionsShallSaveTwoSessionsOnSameHost() throws Exception {
        String ip = "10.34.666";
        Session testSession1 = new MySession(ip);
        Session testSession2 = new MySession(ip);
        assertThat(sessionController.getSessionsPerHost(testSession1).size(), is(0));
        UserTransactionLogWebSocket testInstance = new UserTransactionLogWebSocket();
        testInstance.onconnect(testSession1);
        testInstance.onconnect(testSession2);
        assertThat(sessionController.getSessionsPerHost(testSession1).size(), is(2));
        assertThat(sessionController.getSessionsPerHost(testSession2).size(), is(2));
        assertThat(sessionController.getRemoteAddress(testSession1), is(sessionController.getRemoteAddress(testSession2)));
    }


    @Test
    public void testOnconnectWithOneSessionOnTwoHostsShallSaveTwoSessionsOnDifferentHosts() throws Exception {
        String ip1 = "10.34.666";
        String ip2 = "666.34.10";
        Session testSession1 = new MySession(ip1);
        Session testSession2 = new MySession(ip2);
        assertThat(sessionController.getSessionsPerHost(testSession1).size(), is(0));
        assertThat(sessionController.getSessionsPerHost(testSession2).size(), is(0));
        UserTransactionLogWebSocket testInstance = new UserTransactionLogWebSocket();
        testInstance.onconnect(testSession1);
        testInstance.onconnect(testSession2);
        assertThat(sessionController.getSessionsPerHost(testSession1).size(), is(1));
        assertThat(sessionController.getSessionsPerHost(testSession2).size(), is(1));
        Assert.assertNotEquals(sessionController.getRemoteAddress(testSession1), sessionController.getRemoteAddress(testSession2));
    }


    @Test
    public void testOnCloseWithThreeWebsocketsShallCloseAll() throws Exception {
        String ip = "10.34.666";
        List<UserTransactionLogWebSocket> socketsWithSessions = addSessionOnHost(ip, 3);
        Session oneSession = socketsWithSessions.get(0).mySession;
        List<Session> sessionsPerHost = sessionController.getSessionsPerHost(oneSession);
        assertThat(sessionsPerHost.size(), is(3));
        Assert.assertEquals(sessionController.getRemoteAddress(sessionsPerHost.get(0)), sessionController.getRemoteAddress(sessionsPerHost.get(1)));

        socketsWithSessions.forEach(i -> i.onClose(1, "testReason"));
        List<Session> sessionsPerHostAfter = sessionController.getSessionsPerHost(oneSession);
        assertThat(sessionsPerHost.size(), is(0));
    }

    @Test
    public void testOnClose3With6WebsocketsShallClose3() throws Exception {
        String ip = "10.34.666";
        List<UserTransactionLogWebSocket> socketsWithSessions = addSessionOnHost(ip, 6);
        Session oneSession = socketsWithSessions.get(0).mySession;
        List<Session> sessionsPerHost = sessionController.getSessionsPerHost(oneSession);
        assertThat(sessionsPerHost.size(), is(6));

        String remoteAddress = sessionController.getRemoteAddress(oneSession);
        for(Session aSession : sessionsPerHost){
            Assert.assertEquals(sessionController.getRemoteAddress(aSession), remoteAddress);
        }
        socketsWithSessions.subList(0, 3).forEach(i -> i.onClose(1, "testReason"));
        assertThat(sessionsPerHost.size(), is(3));
    }


    private List<UserTransactionLogWebSocket> addSessionOnHost(String ip, int numberOfSessions) {
        List<UserTransactionLogWebSocket> sockets = IntStream.range(0, numberOfSessions)
          .mapToObj(i -> new UserTransactionLogWebSocket())
          .collect(Collectors.toList());
        sockets.forEach(i -> i.onconnect(new MySession(ip)));

        return sockets;
    }


    private static class MySession implements Session {

        private String address;

        public MySession(String ip) {
            address = ip;
        }

        @Override
        public void close() {

        }

        @Override
        public void close(CloseStatus closeStatus) {

        }

        @Override
        public void close(int i, String s) {

        }

        @Override
        public void disconnect() throws IOException {

        }

        @Override
        public long getIdleTimeout() {
            return 0;
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return null;
        }

        @Override
        public WebSocketPolicy getPolicy() {
            return null;
        }

        @Override
        public String getProtocolVersion() {
            return null;
        }

        @Override
        public RemoteEndpoint getRemote() {
            return null;
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return null;
        }

        @Override
        public UpgradeRequest getUpgradeRequest() {
            return null;
        }

        @Override
        public UpgradeResponse getUpgradeResponse() {
            return null;
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public void setIdleTimeout(long l) {

        }

        @Override
        public SuspendToken suspend() {
            return null;
        }

        @Override
        public String toString() {
            return address;
        }
    }
}