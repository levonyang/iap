package com.haizhi.iap.search.utils;

/**
 * Created by chenbo on 17/4/10.
 */
public class NumberUtil {
    public static Double tryParseDouble(String doubleStr){
        try{
            return Double.parseDouble(doubleStr);
        }catch (NumberFormatException ex){
            return 0.0;
        }
    }

    public static Float tryParseFloat(String floatStr){
        try{
            return Float.parseFloat(floatStr);
        }catch (NumberFormatException ex){
            return 0.0f;
        }
    }
}
