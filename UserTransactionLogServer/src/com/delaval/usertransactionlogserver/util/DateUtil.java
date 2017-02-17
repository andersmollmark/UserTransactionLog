package com.delaval.usertransactionlogserver.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Handles the formatting of dates and time
 */
public class DateUtil {


    /**
     * Formats miliseconds since 1970 for SFieldTimestamp
     * @param d
     * @return
     */
    public static String formatTimeStamp(Long d) {
        return formatTimeStamp(new Date(d));
    }
    /**
     * Formats java.util.Date for SFieldTimestamp
     * @param d
     * @return
     */
    public static String formatTimeStamp(Date d) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(d);
    }

}
