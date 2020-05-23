package com.haizhi.iap.follow.utils;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by chenbo on 17/6/13.
 */
public class CompanyUtil {
    public static List<String> trickName(String companyName) {
        List<String> names = Lists.newArrayList();
        if(companyName != null) {
            names.add(companyName);
            if (companyName.indexOf("（") > 0 || companyName.indexOf("）") > 0) {
                names.add(companyName.replaceAll("（", "(").replaceAll("）", ")"));
            } else if (companyName.indexOf("(") > 0 || companyName.indexOf(")") > 0) {
                names.add(companyName.replaceAll("\\(", "（").replaceAll("\\)", "）"));
            }
        }
        return names;
    }

    public static String ignoreENBrackets(String companyName) {
        if(companyName != null){
            return companyName.replaceAll("\\(", "（").replaceAll("\\)", "）");
        }else {
            return companyName;
        }
    }

    public static String ignoreZHBrackets(String companyName) {
        if (companyName != null){
            return companyName.replaceAll("（", "(").replaceAll("）", ")");
        }else {
            return companyName;
        }
    }

    public static void main(String[] args) {
        String company = "亿宝德通讯技术(深圳）有限公司";
        System.out.println(ignoreENBrackets(company));
        System.out.println(ignoreZHBrackets(company));
    }
}
