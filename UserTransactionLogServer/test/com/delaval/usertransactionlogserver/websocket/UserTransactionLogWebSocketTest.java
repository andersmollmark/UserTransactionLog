package com.delaval.usertransactionlogserver.websocket;

import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import org.eclipse.jetty.websocket.api.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


/**
 * Created by delaval on 2016-02-18.
 */
public class UserTransactionLogWebSocketTest {


  @Before
  public void setUp() {
    UtlsLogUtil.sessions.clear();
    UtlsLogUtil.sessionsPerHost.clear();
  }

  @Test
  public void testOnconnectWithOneSessionShallSaveOneSession() throws Exception {
    String ip = "10.34.666";
    Session testSession = new MySession(ip);
    assertThat(UtlsLogUtil.sessionsPerHost.values().size(), is(0));
    assertThat(UtlsLogUtil.sessions.values().size(), is(0));
    UserTransactionLogWebSocket testInstance = new UserTransactionLogWebSocket();
    testInstance.onconnect(testSession);
    assertThat(UtlsLogUtil.sessionsPerHost.values().size(), is(1));
    assertThat(UtlsLogUtil.sessionsPerHost.get(ip).size(), is(1));
    assertThat(UtlsLogUtil.sessions.values().size(), is(1));
  }

  @Test
  public void testOnconnectWithTwoSessionsShallSaveTwoSessionsOnSameHost() throws Exception {
    String ip = "10.34.666";
    Session testSession1 = new MySession(ip);
    Session testSession2 = new MySession(ip);
    assertThat(UtlsLogUtil.sessionsPerHost.values().size(), is(0));
    assertThat(UtlsLogUtil.sessions.values().size(), is(0));
    UserTransactionLogWebSocket testInstance = new UserTransactionLogWebSocket();
    testInstance.onconnect(testSession1);
    testInstance.onconnect(testSession2);
    assertThat(UtlsLogUtil.sessionsPerHost.values().size(), is(1));
    assertThat(UtlsLogUtil.sessionsPerHost.get(ip).size(), is(2));
    assertThat(UtlsLogUtil.sessions.values().size(), is(2));
    assertThat(UtlsLogUtil.sessions.get(testSession1), notNullValue());
    assertThat(UtlsLogUtil.sessions.get(testSession2), notNullValue());
  }


  @Test
  public void testOnconnectWithOneSessionOnTwoHostsShallSaveTwoSessionsOnDifferentHosts() throws Exception {
    String ip1 = "10.34.666";
    String ip2 = "666.34.10";
    Session testSession1 = new MySession(ip1);
    Session testSession2 = new MySession(ip2);
    assertThat(UtlsLogUtil.sessionsPerHost.values().size(), is(0));
    assertThat(UtlsLogUtil.sessions.values().size(), is(0));
    UserTransactionLogWebSocket testInstance = new UserTransactionLogWebSocket();
    testInstance.onconnect(testSession1);
    testInstance.onconnect(testSession2);
    assertThat(UtlsLogUtil.sessionsPerHost.values().size(), is(2));
    assertThat(UtlsLogUtil.sessionsPerHost.get(ip1).size(), is(1));
    assertThat(UtlsLogUtil.sessionsPerHost.get(ip2).size(), is(1));
    assertThat(UtlsLogUtil.sessions.values().size(), is(2));
    assertThat(UtlsLogUtil.sessions.get(testSession1), notNullValue());
    assertThat(UtlsLogUtil.sessions.get(testSession2), notNullValue());
  }


  @Test
  public void testOnCloseWithThreeWebsocketsShallCloseAll() throws Exception {
    String ip = "10.34.666";
    List<UserTransactionLogWebSocket> socketsWithSessions = addSessionOnHost(ip, 3);
    assertThat(UtlsLogUtil.sessions.size(), is(3));
    assertThat(UtlsLogUtil.sessionsPerHost.size(), is(1));
    assertThat(UtlsLogUtil.sessionsPerHost.get(ip).size(), is(3));
    socketsWithSessions.forEach(i -> i.onClose(1, "testReason"));
    assertThat(UtlsLogUtil.sessions.size(), is(0));
    assertThat(UtlsLogUtil.sessionsPerHost.size(), is(1));
    assertThat(UtlsLogUtil.sessionsPerHost.get(ip).size(), is(0));

  }

  @Test
  public void testOnClose3With6WebsocketsShallClose3() throws Exception {
    String ip = "10.34.666";
    List<UserTransactionLogWebSocket> socketsWithSessions = addSessionOnHost(ip, 6);
    assertThat(UtlsLogUtil.sessions.size(), is(6));
    assertThat(UtlsLogUtil.sessionsPerHost.size(), is(1));
    assertThat(UtlsLogUtil.sessionsPerHost.get(ip).size(), is(6));
    socketsWithSessions.subList(0, 3).forEach(i -> i.onClose(1, "testReason"));
    assertThat(UtlsLogUtil.sessions.size(), is(3));
    assertThat(UtlsLogUtil.sessionsPerHost.size(), is(1));
    assertThat(UtlsLogUtil.sessionsPerHost.get(ip).size(), is(3));
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