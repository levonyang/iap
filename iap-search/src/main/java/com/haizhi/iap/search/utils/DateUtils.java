package com.haizhi.iap.search.utils;

import com.google.common.collect.Lists;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class DateUtils {

    public static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final DateFormat FORMAT_TZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static final DateFormat FORMAT_T = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static final DateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd");

    public static final DateFormat FORMAT_YEAR = new SimpleDateFormat("yyyy");

    public static final DateFormat FORMAT_MONTH = new SimpleDateFormat("yyyy-MM");

    public static final DateFormat FORMAT_HOUR = new SimpleDateFormat("yyyy-MM-dd HH");


    public static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static Date getTodaysBegin() {
        Calendar today = instanceCalendar();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today.getTime();
    }

    public static Date now() {
        return instanceCalendar().getTime();
    }

    public static String dateTime2String(Long dateTime, Long particle) {
        if (particle < 86400000l) {
            return FORMAT_HOUR.format(new Date(dateTime));
        } else if (particle < 2160000000l) {
            return FORMAT_DATE.format(new Date(dateTime));
        } else {
            return FORMAT_MONTH.format(new Date(dateTime));
        }
    }

    public static String format(Date date, DateFormat format) {
        return format.format(date);
    }

    public static Calendar instanceCalendar() {
        return Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    //"\\d{4}" yyyy
    //"\\d{4}[/|-]\\d{1,2}" yyyy-MM
    //"\\d{4}[/|-]\\d{1,2}[/|-]\\d{1,2}" yyyy-MM-dd
    //"\\d{4}[/|-]\\d{1,2}[/|-]\\d{1,2} \\d{1,2}" yyyy-MM-dd HH
    //"\\d{4}[/|-]\\d{1,2}[/|-]\\d{1,2} \\d{1,2}:\\d{1,2}" yyyy-MM-dd HH:mm
    //"\\d{4}[/|-]\\d{1,2}[/|-]\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}" yyyy-MM-dd HH:mm:ss
    private static Pattern patternYear = Pattern.compile("\\d{4}");
    private static Pattern patternMonth = Pattern.compile("\\d{4}[/|-]\\d{1,2}");
    private static Pattern patternDay = Pattern.compile("\\d{4}[/|-]\\d{1,2}[/|-]\\d{1,2}");
    private static Pattern patternHour = Pattern.compile("\\d{4}[/|-]\\d{1,2}[/|-]\\d{1,2} \\d{1,2}");
    private static Pattern patternMinute = Pattern.compile("\\d{4}[/|-]\\d{1,2}[/|-]\\d{1,2} \\d{1,2}:\\d{1,2}");
    private static Pattern patternSeconds = Pattern.compile("\\d{4}[/|-]\\d{1,2}[/|-]\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}");
    public static final List<Pattern> ALL_PATTERN_LIST = Lists.newArrayList(patternYear, patternMonth, patternDay,
            patternHour, patternMinute, patternSeconds);

    public static boolean isLegalTimeStr(String str) {
        if (str == null || str.equals("")) {
            return false;
        }
        boolean isLegal = false;
        for (Pattern pattern : ALL_PATTERN_LIST) {
                if (pattern.matcher(str).matches()){
                    isLegal = true;
                    break;
                }
        }
        return isLegal;
    }
}