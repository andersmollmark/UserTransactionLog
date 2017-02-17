/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.delaval.usertransactionlogserver;

import ch.qos.logback.classic.Level;
import com.delaval.usertransactionlogserver.jms.JmsResourceFactory;
import com.delaval.usertransactionlogserver.persistence.ConnectionFactory;
import com.delaval.usertransactionlogserver.persistence.dao.InitDAO;
import com.delaval.usertransactionlogserver.service.FetchAllEventLogsService;
import com.delaval.usertransactionlogserver.servlet.*;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import simpleorm.utils.SLogSlf4j;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * that initializes the Jetty-server.
 */
public class UserTransactionLogServer {
    /**
     * ANTDATA is auto updated by build script.
     * Edit Build.xml to change these values
     */
    private static final String[] ANTDATA = {"XX", "0.0", "0000"};    //ANT REPLACE
    /**
     * Application name, set by build script.
     */
    public static final String application = ANTDATA[0];
    /**
     * Application version, set by build script.
     */
    public static final String version = ANTDATA[1];
    /**
     * Application revision, build script retrieves from SVN.
     */
    public static final String revision = ANTDATA[2];


    private UserTransactionLogServer() {
        // empty by design
    }


    public void init() {
        try {
            initDB();
        } catch (Exception e) {
            UtlsLogUtil.error(this.getClass(), "FATAL ERROR, something went wrong while creating tables in db:" + e.getMessage());
            tryToCloseConnectionFactory();
            throw new RuntimeException("FATAL ERROR, something went wrong while creating tables in db:" + e.getMessage());
        }
        initServer();
    }

    private void initDB() throws Exception {
        InitDAO initDAO = InitDAO.getInstance();
        if (initDAO.isCreateTables()) {
            initDAO.createTables();
        }
        initDAO.createDeleteLogEvent();
        initDAO.alterTables();
    }

    private void initServer() {
        QueuedThreadPool threadPool = new QueuedThreadPool(); // TODO is this necessary??
        int threadPoolSize = Integer.parseInt(ServerProperties.getInstance().getProp(ServerProperties.PropKey.THREAD_POOL_SIZE));
        threadPool.setMaxThreads(threadPoolSize);

        int websocketPort = Integer.parseInt(ServerProperties.getInstance().getProp(ServerProperties.PropKey.WEBSOCKET_PORT));
        Server server = new Server(websocketPort);

        initLogLevel();
        server.setHandler(createServletContextHandler());

        try {
            server.start();
            JmsResourceFactory.initApplicationContext();
            server.join();

        } catch (Exception e) {
            UtlsLogUtil.error(this.getClass(), "FATAL ERROR, something happened while starting server, shutting down:" + e.getMessage());
            tryToCloseConnectionFactory();
            throw new RuntimeException("FATAL ERROR, something happened while starting server, shutting down:" + e.getMessage());
        }

    }

    private void tryToCloseConnectionFactory() {
        try {
            ConnectionFactory.closeFactory();
        } catch (SQLException e1) {
            throw new RuntimeException("FATAL ERROR, something happened while starting server, couldnt close connectionfactory:" + e1.getMessage());
        }
    }

    private ServletContextHandler createServletContextHandler() {
        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase("app");
        context.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
        context.setInitParameter("cacheControl", "no-cache");
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        context.addServlet(DefaultServlet.class, "/");
        context.addServlet(StartServlet.class, "/servlet");
        context.addServlet(CreateTestLogServlet.class, "/servlet/testServlet");
        context.addServlet(SaveTestLogServlet.class, "/servlet/saveTestLog");
        context.addServlet(GetUserTransactionKeyServlet.class, "/servlet/getUserTransactionKey");
        context.addServlet(GetLogContentServlet.class, "/servlet/getLogContent");
        context.addServlet(ChangeDeleteLogEventServlet.class, "/servlet/changeDeleteLogEvent");


        ServletHolder ws = context.addServlet(LogWebSocketServlet.class, "/ws");
        ws.setInitParameter("classpath", context.getClassPath());
        return context;
    }

    private void initLogLevel() {
        // TODO handle this in groovy as the rest of the servers?
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("org.eclipse.jetty");
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;
            logbackLogger.setLevel(Level.INFO);
        } else {
            throw new IllegalStateException("Logger-class was not instance of logback-Logger");
        }
        // set log-level to SimpleORM, > 40 all transactions will endup in error_log
        SLogSlf4j.getSessionlessLogger().setLevel(0);

    }


    public static String getApplicationNameVersionAndRevision() {
        return application + " " + version + " r" + revision;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        List<String> argsAsList = Arrays.asList(args);
        for (int i = 0; i < argsAsList.size(); i++) {
            if (argsAsList.get(i).startsWith("-V")) {
                System.out.println(getApplicationNameVersionAndRevision());
                System.exit(0);
            } else if (argsAsList.get(i).startsWith("-help")) {
                System.out.println("--- Arguments that can be used ----");
                System.out.println("-V");
                System.out.println("writes out version of server");
                System.out.println("-dump");
                System.out.println("writes db-content in jsonformat on defaultlocation and defaultfile /tmp/jsonDump");
                System.out.println("-dump path filename");
                System.out.println("writes db-content in jsonformat on location specified in path and file specified in filename");
                System.exit(0);
            } else if (argsAsList.get(i).startsWith("-dump")) {
                if (argsAsList.size() > i + 2) {
                    String path = argsAsList.get(i + 1);
                    String filename = argsAsList.get(i + 2);
                    FetchAllEventLogsService logsService = new FetchAllEventLogsService();
                    logsService.writeJsonDumpOnFile(path, filename);
                    System.out.println("\nCreated jsonfile " + filename + " at path " + path);
                } else {
                    FetchAllEventLogsService logsService = new FetchAllEventLogsService();
                    logsService.writeJsonDumpOnDefaultFile();
                    System.out.println("\nCreated jsonfile " + FetchAllEventLogsService.DEFAULT_FILENAME_JSONDUMP + " at path " + FetchAllEventLogsService.DEFAULT_FILE_PATH_TMP);
                }

                System.exit(0);
            }
        }
        new UserTransactionLogServer().init();
    }


}








