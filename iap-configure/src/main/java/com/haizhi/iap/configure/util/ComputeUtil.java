package com.haizhi.iap.configure.util;

import lombok.extern.slf4j.Slf4j;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;

/**
 * @Author dmy
 * @Date 2017/5/18 上午11:05.
 */
@Slf4j
public class ComputeUtil {
    public static String computeSum(List<String> values) {
        double sum = 0.0;
        for(String v : values) {
            try {
                Number number = NumberFormat.getInstance().parse(v);

                if(number instanceof Double) {
                    sum += Double.parseDouble(number.toString());
                } else if (number instanceof Float) {
                    sum += Float.parseFloat(number.toString());
                } else if (number instanceof Integer) {
                    sum += Integer.parseInt(number.toString());
                } else if (number instanceof Long) {
                    sum += Long.parseLong(number.toString());
                }

            } catch (ParseException e) {
                log.info("e", e.getMessage());
            }
        }
        return Double.toString(sum);
    }

    public static String computeAvg(List<String> values) {
        if (values.size() == 0) {
            return Double.toString(0);
        }
        double sum = Double.valueOf(computeSum(values));
        double avg = sum/(double)values.size();
        return Double.toString(avg);
    }

}