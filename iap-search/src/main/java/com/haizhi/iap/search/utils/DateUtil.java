package com.haizhi.iap.search.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

	private static ThreadLocal<SimpleDateFormat> threadLocal1 = new ThreadLocal<SimpleDateFormat>();
	private static ThreadLocal<SimpleDateFormat> threadLocal2 = new ThreadLocal<SimpleDateFormat>();
	private static ThreadLocal<SimpleDateFormat> threadLocal3 = new ThreadLocal<SimpleDateFormat>();
	private static ThreadLocal<SimpleDateFormat> threadLocal4 = new ThreadLocal<SimpleDateFormat>();
	private static ThreadLocal<SimpleDateFormat> threadLocal5 = new ThreadLocal<SimpleDateFormat>();

	private static final String formatStr1 = "yyyy-MM-dd";
	private static final String formatStr2 = "yyyy-MM-dd HH:mm:ss";
	private static final String formatStr3 = "yyyy/MM/dd";
	private static final String formatStr4 = "EEEE";
	private static final String formatStr5 = "yyyyMMdd";

	public static int getWeekOfDate(Date dt) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		return cal.get(Calendar.DAY_OF_WEEK) - 1;
	}
	
	/**
	 * 
	 * @return yyyyMMdd
	 */
	public static SimpleDateFormat getFormat5() {
		SimpleDateFormat df = threadLocal5.get();
		if (df == null) {
			df = new SimpleDateFormat(formatStr5);
			threadLocal5.set(df);
		}
		return df;
	}

	/**
	 * 
	 * @return EEEE(星期)
	 */
	public static SimpleDateFormat getFormat4() {
		SimpleDateFormat df = threadLocal4.get();
		if (df == null) {
			df = new SimpleDateFormat(formatStr4);
			threadLocal4.set(df);
		}
		return df;
	}

	/**
	 * 
	 * @return yyyy/MM/dd
	 */
	public static SimpleDateFormat getFormat3() {
		SimpleDateFormat df = threadLocal3.get();
		if (df == null) {
			df = new SimpleDateFormat(formatStr3);
			threadLocal3.set(df);
		}
		return df;
	}

	/**
	 * 
	 * @return yyyy-MM-dd HH:mm:ss
	 */
	public static SimpleDateFormat getFormat2() {
		SimpleDateFormat df = threadLocal2.get();
		if (df == null) {
			df = new SimpleDateFormat(formatStr2);
			threadLocal2.set(df);
		}
		return df;
	}

	/**
	 * 
	 * @return yyyy-MM-dd
	 */
	public static SimpleDateFormat getFormat1() {
		SimpleDateFormat df = threadLocal1.get();
		if (df == null) {
			df = new SimpleDateFormat(formatStr1);
			threadLocal1.set(df);
		}
		return df;
	}

	public static String formatYesterday() {
		return getFormat1().format(yesterday());
	}

	/**
	 * yyyy/MM/dd
	 * 
	 * @return
	 */
	public static String formatYesterdayDaily() {
		return getFormat3().format(yesterday());
	}
	
	/**
	 * yyyy/MM/dd
	 * 
	 * @return
	 */
	public static String formatDaily() {
		return getFormat3().format(new Date());
	}

	public static String formatToday() {
		return getFormat1().format(new Date());
	}

	public static String dateToStr(Date time) {
		return getFormat2().format(time);
	}

	/**
	 *
	 * 查询当前日期前(后)x天的日期
	 *
	 * @param date
	 *            日期
	 * @param day
	 *            天数（如果day数为负数,说明是此日期前的天数）
	 */
	public static Date addDay(Date date, int day) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_YEAR, day);
		return c.getTime();
	}

	/**
	* 查询当前日期前(后)x月的日期
	 * @param date
	 * @param month （如果month数为负数,说明是此日期前的月数）
	* @return java.util.Date
	* @author caochao
	* @Date 2018/8/24
	*/
	public static Date addMonth(Date date, int month) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, month);
		return c.getTime();
	}

	public static Date beforeXDay(Date date, int x) {
		return addDay(date, -x);
	}

	public static Date yesterday() {
		return beforeXDay(new Date(), 1);
	}

	/**
	 * 零点00：00：00
	 * 
	 * @return
	 * @throws ParseException
	 */
	public static Date yesterdayZero() throws ParseException {
		return getFormat1().parse(yesterdayZeroStr());
	}

	public static Date getTime(String date) throws ParseException {
		return getFormat3().parse(date);
	}

	/**
	 * 零点00：00：00
	 * 
	 * @return
	 * @throws ParseException
	 */
	public static String yesterdayZeroStr() {
		return getFormat1().format(yesterday());
	}

	public static int gapDay(Date latestday, Date parse) {
		long l = latestday.getTime() - parse.getTime();
		int days = new Long(l / (1000 * 60 * 60 * 24)).intValue();
		return days;
	}

	public static void main(String[] args) throws ParseException {
		int days = gapDay(new Date(), getFormat1().parse("2016-06-10 23:59:21"));
		System.out.println(days);
		
		getWeekOfDate(new Date());
	}
}
