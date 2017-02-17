package com.delaval.usertransactionlogserver.util;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by delaval on 2016-02-04.
 */
public class UtlsLogUtil {

    private static final String SEPARATOR = " | ";
    private static final Pattern LF = Pattern.compile("([^\\r])\\n");

    public static final Map<Session, Date> sessions = new HashMap<>();
    public static final Map<String, List<Session>> sessionsPerHost = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger("utlserver");

    public static void warn(Class clazz, String mess) {
        LOGGER.warn(getLogRow(clazz,  ": " + mess));
    }

    public static void warn(Class clazz, Exception e) {
        warn(clazz, e.getMessage());
    }

    public static void info(Class clazz, String mess) {
        LOGGER.info(getLogRow(clazz,  ": " + mess));
    }

    public static void info(Class clazz, Exception e) {
        info(clazz, e.getMessage());
    }

    public static void error(Class clazz, String mess) {
        LOGGER.error(getLogRow(clazz,  ": " + mess));
    }

    public static void error(Class clazz, Exception e) {
        error(clazz, e.getMessage());
    }

    public static void debug(Class clazz, String mess) {
        LOGGER.debug(getLogRow(clazz,  ": " + mess));
    }

    public static void debug(Class clazz, Exception e) {
        debug(clazz, e.getMessage());
    }

    private static String replaceLF(String s) {
        return LF.matcher(s).replaceAll("$1\r\n");
    }

    private static String getLogRow(Class clazz, String msg) {
        Throwable t = new Throwable();
        StackTraceElement[] st = t.getStackTrace();
        Integer lineNr = st[2].getLineNumber();

        StringBuilder sb = new StringBuilder();
        sb.append(Thread.currentThread().getName()).append(SEPARATOR)
          .append(clazz.getSimpleName()).append(SEPARATOR)
          .append(lineNr.toString()).append(SEPARATOR)
          .append(replaceLF(msg)).append(" ");
        return sb.toString();
    }

}
