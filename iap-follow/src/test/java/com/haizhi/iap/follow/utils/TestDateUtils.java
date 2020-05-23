package com.haizhi.iap.follow.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by haizhi on 2017/10/16.
 */
public class TestDateUtils {

    @Test
    public void testGetOneMonthBefore() {
        Calendar last = Calendar.getInstance(TimeZone.getTimeZone("Asia/shanghai"));
        last.add(Calendar.MONTH, -1);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

        String lastMonth = DateUtils.getOneMonthBefore();

        Assert.assertEquals(format.format(last.getTime()).toString(),lastMonth);
    }

    @Test
    public void testFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date date = new Date();

        Assert.assertEquals(format.format(date),DateUtils.format(date,format));
    }

}
