package com.krish.empower.util;

import com.krish.empower.jdocs.BaseUtils;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Locale;

public class DateUtils {
    public static final String DATE_FORMAT = "yy-MM-dd HH:mm:ss";
    public static final String UTC_TZ_FORMAT = "yy-MMM-dd HH:mm:ss.SSS zzz";
    public static final String LAST_JOB_RUNTIME_FMT = "yy-MM-dd HH:mm:ss z";
    public static final String LAST_JOB_RUNTIME_DEFAULT_VALUE = "1970-01-01 00:00:00 UTC";

    public static Timestamp getNextCalenderDay(Timestamp ts, int noOfDays){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ts.getTime());
        cal.add(Calendar.DAY_OF_MONTH, noOfDays);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Timestamp(cal.getTime().getTime());
    }

    public static Timestamp getNextBusinessDay(Timestamp ts, int noOfDays){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ts.getTime());
        int count=0;
        while(count<noOfDays){
            cal.add(Calendar.DAY_OF_WEEK, 1);
            while(cal.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY){
                cal.add(Calendar.DAY_OF_WEEK, 1);
            }
            count++;
        }
        return new Timestamp(cal.getTime().getTime());
    }

    public static Timestamp getTimestampFromString(String dateStr, String dateFormat){
        ZonedDateTime zdt = BaseUtils.getZonedDateTimeFromString(dateStr, dateFormat, Locale.ENGLISH);
        return Timestamp.from(zdt.toInstant());
    }
}
