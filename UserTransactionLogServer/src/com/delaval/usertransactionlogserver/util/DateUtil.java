package com.delaval.usertransactionlogserver.util;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Handles the formatting of dates and time
 */
public class DateUtil {


    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static DateTimeFormatter guiFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy HH:mm:ss");

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

    public static String formatLocalDateTime(LocalDateTime ldt){
        return ldt.format(dtf);
    }

    public static String formatTimeStamp(ZonedDateTime zdt){
        return zdt.format(dtf);
    }

    public static String formatTimeStampToGuiString(LocalDateTime ldt){
        return ldt.format(guiFormatter);
    }

    public static LocalDateTime getLocalDateTimeFromMillis(long millis){
          return Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDateTime();
    }
}
