package com.haizhi.iap.follow.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("Asia/Shanghai");

    public static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final DateFormat FORMAT_TZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static final DateFormat FORMAT_T = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static final DateFormat FORMAT_DAY = new SimpleDateFormat("yyyy-MM-dd");

    public static final DateFormat FORMAT_MONTH = new SimpleDateFormat("yyyy-MM");

    public static final DateFormat FORMAT_HOUR = new SimpleDateFormat("yyyy-MM-dd HH");

    public static final DateFormat FORMAT_YMS = new SimpleDateFormat("yyyyMMddHHmmss");

    public static final Integer LATEST_DAY = -2;

    static {
        FORMAT.setTimeZone(DEFAULT_TIME_ZONE);
        FORMAT_TZ.setTimeZone(DEFAULT_TIME_ZONE);
        FORMAT_T.setTimeZone(DEFAULT_TIME_ZONE);
        FORMAT_DAY.setTimeZone(DEFAULT_TIME_ZONE);
        FORMAT_MONTH.setTimeZone(DEFAULT_TIME_ZONE);
        FORMAT_HOUR.setTimeZone(DEFAULT_TIME_ZONE);
    }

    public static Date getTodaysBegin(){
        Calendar today = instanceCalendar();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today.getTime();
    }

    public static String getOneMonthBefore() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/shanghai"));
        calendar.add(Calendar.MONTH, -1);
        return FORMAT.format(calendar.getTime());
    }

    public static Date now(){
        return instanceCalendar().getTime();
    }

    public static String format(Date date, DateFormat format){
        return format.format(date);
    }

    public static Calendar instanceCalendar(){
        return Calendar.getInstance(DEFAULT_TIME_ZONE);
    }

    public static Date getLatestDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -LATEST_DAY);

        Date date = cal.getTime();

        String dateString = FORMAT_DAY.format(date) + " 00:00:00";
        Date threeDate = null;
        try {
            threeDate = FORMAT.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;//for test
    }

    /**
     * 获取偏移时间(时间格式:YYYY-MM-dd hh:mm:ss)
     * @param expireUnit 偏移时间单位(DAY,HOUR,MINUTE,SECOND)
     * @param expire 偏移时间
     * @return
     */
    public static String getOffsetTime(String expireUnit,int expire){
        Calendar calendar = Calendar.getInstance();
        switch (expireUnit){
            case "DAY":calendar.add(Calendar.DAY_OF_MONTH,expire);break;
            case "HOUR":calendar.add(Calendar.HOUR,expire);break;
            case "MINUTE":calendar.add(Calendar.MINUTE,expire);break;
            case "SECOND":calendar.add(Calendar.SECOND,expire);break;
            default:break;
        }
        Date time = calendar.getTime();
        DateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        return format.format(time);
    }
}