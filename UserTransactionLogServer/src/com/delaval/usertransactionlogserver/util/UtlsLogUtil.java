package com.delaval.usertransactionlogserver.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Handles logging in Utl-server
 */
public class UtlsLogUtil {

    private static final String SEPARATOR = " | ";
    private static final Pattern LF = Pattern.compile("([^\\r])\\n");

    private static final Object LOCK = new Object();

    private static final Logger LOGGER = LoggerFactory.getLogger("utlserver");

    public static void warn(Class clazz, String... mess) {
        synchronized (LOCK){
            LOGGER.warn(getLogRow(clazz,  getLogString(mess)));
        }
    }

    public static void info(Class clazz, String... mess) {
        synchronized (LOCK) {
            LOGGER.info(getLogRow(clazz, getLogString(mess)));
        }
    }

    public static void error(Class clazz, String... mess) {
        synchronized (LOCK) {
            LOGGER.error(getLogRow(clazz, getLogString(mess)));
        }
    }

    public static void debug(Class clazz, String... mess) {
        synchronized (LOCK) {
            LOGGER.debug(getLogRow(clazz, getLogString(mess)));
        }
    }

    public static void trace(Class clazz, String... mess) {
        synchronized (LOCK) {
            LOGGER.trace(getLogRow(clazz, getLogString(mess)));
        }
    }

    public static boolean isDebug(){
        return LOGGER.isDebugEnabled();
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

    private static String getLogString(String... strings){
        StringBuilder result = new StringBuilder();
        Arrays.asList(strings).stream().forEach(string -> result.append(string));
        return result.toString();
    }

}
