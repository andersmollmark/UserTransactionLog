package com.delaval.usertransactionlogserver.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by delaval on 2017-05-02.
 */
public final class SessionController {

    private static final Object SESSION_LOCK = new Object();
    private final AtomicReference<Map<Session, Date>> sessions = new AtomicReference<>();
    private final AtomicReference<Map<String, List<Session>>> sessionsPerHost = new AtomicReference<>();

    private static SessionController _INSTANCE;

    public static synchronized SessionController getInstance(){
        if(_INSTANCE == null){
            _INSTANCE = new SessionController();
        }
        return _INSTANCE;
    }

    private SessionController(){
        sessions.set(new HashMap<>());
        sessionsPerHost.set(new HashMap<>());
    }

    public void remove(Session session){
        synchronized (SESSION_LOCK){
            sessions.get().remove(session);
            String hostAdress = getRemoteAddress(session);
            List<Session> sessionsOnThisHost = sessionsPerHost.get().get(hostAdress);
            if(sessionsOnThisHost != null){
                sessionsOnThisHost.remove(session);
            }
        }
    }

    public void add(Session session){
        synchronized (SESSION_LOCK){
            sessions.get().put(session, new Date());
            String hostAdress = getRemoteAddress(session);
            List<Session> sessionsOnThisHost = sessionsPerHost.get().get(hostAdress);
            if(sessionsOnThisHost == null){
                sessionsOnThisHost = new ArrayList<>();
                sessionsPerHost.get().put(hostAdress, sessionsOnThisHost);
            }
            sessionsOnThisHost.add(session);
        }
    }

    public Date getCreated(Session session){
        return sessions.get().get(session);
    }

    public List<Session> getSessionsPerHost(Session session){
        return sessionsPerHost.get().get(getRemoteAddress(session));
    }

    public String getRemoteAddress(Session session) {
        return session.getRemoteAddress() != null ? session.getRemoteAddress().getAddress().toString() : session.toString();
    }

    public String getRemotePort(Session session) {
        return session.getRemoteAddress() != null ? "" + session.getRemoteAddress().getPort() : "unknown port";
    }

}
