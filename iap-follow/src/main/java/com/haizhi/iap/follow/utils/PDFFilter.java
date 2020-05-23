package com.haizhi.iap.follow.utils;

import com.haizhi.iap.common.annotation.ProguardIgnore;
import org.elasticsearch.common.Strings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by zhutianpeng on 17/10/10.
 */
@ProguardIgnore
public class PDFFilter {
    public String PDFFilter(String methodStr, String param1) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = this.getClass().getDeclaredMethod("filter" + methodStr, new Class[]{String.class});
        return (String) method.invoke(this, new Object[]{param1});
    }

    public void PDFFilter(String methodStr, String param1, String param2) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = this.getClass().getDeclaredMethod("filter" + methodStr, new Class[]{String.class, String.class});
        method.invoke(this, new Object[]{param1, param2});
    }

    public String filterNullToString(String param1) {
        if (!Strings.isNullOrEmpty(param1)) {
            return param1.replaceAll("\r|\n|\t", "");
        } else {
            return "--";
        }
    }
    public String filterNullToStringAll(String param1) {
        if (!Strings.isNullOrEmpty(param1)) {
            return param1.replaceAll("\\s*|\r|\n|\t", "");
        } else {
            return "--";
        }
    }

    public String filterNullToIndex(String param1) {
        return param1;
    }

    public String filterNullToStringAndTextTo99(String param1) {
        if (param1 != null && !param1.equals("")) {
            if (param1.length() < 99) {
                return param1;
            } else {
                return param1.substring(0, 99) + "...";
            }
        } else {
            return "--";
        }
    }

    public String filterNullToArray(String param) {
        String init = param.replaceAll("[\\[\\]]", "");
        String[] param1 = init.split(",");
        if (param1 != null || param1.length != 0) {
            String data = "";
            for (int i = 0; i < param1.length; i++) {
                data = data + param1[i] + (i < param1.length - 1 ? "," : "");
            }
            return data;
        } else {
            return "--";
        }
    }

    public String filterMoreOmit(String param) {
        int stringlength = param.length();
        final char[] chr = param.trim().toCharArray();
        if (param == null) {
            return "--";
        }
        if (stringlength > 6) {
            String data = "";
            for (int i = 0; i < 6; i++) {
                data = data + chr[i];
            }
            return data + "...";
        } else {
            return param;
        }
    }

    public String filterTimeToAll(String param1) throws ParseException {
        try {
            if (param1.length() == 10) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                return sdf.format(sdf.parse(param1)).toString();
            } else if (param1.length() == 19) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return sdf.format(sdf.parse(param1)).toString();
            } else if (param1.equals("")) {
                return "--";
            } else {
                return param1;
            }
        } catch (Exception e) {
            return "--";
        }
    }

    public String filterTimeToDate(String param1) {
        if (param1 != null && !param1.equals("")) {
            if (param1.length() > 10) {
                return param1.substring(0, 10);
            } else {
                return param1;
            }
        } else {
            return "--";
        }
    }

    protected String filterYearToAge(String param1) throws ParseException {
        try {
            SimpleDateFormat sdf;
            if (param1.length() == 10) {
                sdf = new SimpleDateFormat("yyyy-MM-dd");
            } else if (param1.length() == 19) {
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            } else if (param1.length() == 4) {
                sdf = new SimpleDateFormat("yyyy");
            } else {
                return "--";
            }
            int age = 0;
            Date birthday = sdf.parse(param1);
            Calendar now = Calendar.getInstance();
            now.setTime(new Date());// 当前时间

            Calendar birth = Calendar.getInstance();
            birth.setTime(birthday);
            age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            if (now.get(Calendar.DAY_OF_YEAR) > birth.get(Calendar.DAY_OF_YEAR)) {
                age += 1;
            }
            return Integer.toString(age);
        } catch (Exception e) {
            e.getMessage();
            return "--";
        }
    }

    protected String filterCheckStockCode(String param1) {
        String dest = "";
        if (Pattern.matches("^300.*", param1)) {
            dest = "创业板";
        } else if (Pattern.matches("^601.*", param1)) {
            dest = "沪市A股";
        } else if (Pattern.matches("^600.*", param1)) {
            dest = "沪市A股";
        } else if (Pattern.matches("^603.*", param1)) {
            dest = "沪市A股";
        } else if (Pattern.matches("^900.*", param1)) {
            dest = "沪市B股";
        } else if (Pattern.matches("^000.*", param1)) {
            dest = "深市A股";
        } else if (Pattern.matches("^001.*", param1)) {
            dest = "深市A股";
        } else if (Pattern.matches("^002.*", param1)) {
            dest = "中小板";
        } else if (Pattern.matches("^200.*", param1)) {
            dest = "深市B股";
        }
        return dest;
    }
}
