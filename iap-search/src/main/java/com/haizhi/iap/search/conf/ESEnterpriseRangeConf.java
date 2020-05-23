package com.haizhi.iap.search.conf;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenbo on 16/12/23.
 */
@Data
@NoArgsConstructor
public class ESEnterpriseRangeConf extends ESRangeConf{

    private static Map<String, ESEnterpriseRangeConf> foundConfigs = new HashMap<>();
    private static Map<String, ESEnterpriseRangeConf> dateConfigs = new HashMap<>();

    public ESEnterpriseRangeConf(String key, Comparable from, Comparable to) {
        super(key, from, to);
    }

    /**
     * { 'key': '200万以下',     'to': 200e4 },
     { 'key': '200万-500万',   'from': 200e4,  'to': 500e4 },
     { 'key': '500万-1000万',  'from': 500e4,  'to': 1000e4 },
     { 'key': '1000万-1500万', 'from': 1000e4, 'to': 1500e4 },
     { 'key': '1500万-3000万', 'from': 1500e4, 'to': 3000e4 },
     { 'key': '3000万以上',    'from': 3000e4 },
     */
    private static String LT200W = "200万以下";
    private static String GTE200W_LT500W = "200万-500万";
    private static String GTE500W_LT1000W = "500万-1000万";
    private static String GTE1000W_LT1500W = "1000万-1500万";
    private static String GTE1500W_LT3000W = "1500万-3000万";
    private static String GTE3000W = "3000万以上";

    /**
     * { 'key': '1年内',    'from': 'now-1y' },
     { 'key': '1-3年',    'to': 'now-1y', 'from': 'now-3y' },
     { 'key': '3-5年',    'to': 'now-3y', 'from': 'now-5y' },
     { 'key': '5-10年',   'to': 'now-5y', 'from': 'now-10y' },
     { 'key': '10年以上', 'to': 'now-10y' },
     */
    private static String LT1Y = "1年内";
    private static String GTE1Y_LT3Y = "1-3年";
    private static String GT3Y_LT5Y = "3-5年";
    private static String GT5Y_LT10Y = "5-10年";
    private static String GT10Y = "10年以上";

    static {
        foundConfigs.put(LT200W, new ESEnterpriseRangeConf(LT200W, 0d, 2000000d));
        foundConfigs.put(GTE200W_LT500W, new ESEnterpriseRangeConf(GTE200W_LT500W, 2000000d, 5000000d));
        foundConfigs.put(GTE500W_LT1000W, new ESEnterpriseRangeConf(GTE500W_LT1000W, 5000000d, 10000000d));
        foundConfigs.put(GTE1000W_LT1500W, new ESEnterpriseRangeConf(GTE1000W_LT1500W, 10000000d, 15000000d));
        foundConfigs.put(GTE1500W_LT3000W, new ESEnterpriseRangeConf(GTE1500W_LT3000W, 15000000d, 30000000d));
        foundConfigs.put(GTE3000W, new ESEnterpriseRangeConf(GTE3000W, 30000000d, null));

        dateConfigs.put(LT1Y, new ESEnterpriseRangeConf(LT1Y, "now-1y", null));
        dateConfigs.put(GTE1Y_LT3Y, new ESEnterpriseRangeConf(GTE1Y_LT3Y, "now-3y", "now-1y"));
        dateConfigs.put(GT3Y_LT5Y, new ESEnterpriseRangeConf(GT3Y_LT5Y, "now-5y", "now-3y"));
        dateConfigs.put(GT5Y_LT10Y, new ESEnterpriseRangeConf(GT5Y_LT10Y, "now-10y", "now-5y"));
        dateConfigs.put(GT10Y, new ESEnterpriseRangeConf(GT10Y, null, "now-10y"));

    }

    public static Map<String, ESEnterpriseRangeConf> getFoundConfigs() {
        return foundConfigs;
    }

    public static Map<String, ESEnterpriseRangeConf> getDateConfigs() {
        return dateConfigs;
    }
}
