package com.haizhi.iap.search.enums;

import lombok.Getter;
import org.elasticsearch.common.Strings;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * 集团特征类型 分为行业 城市 省份
 *
 * @author xuguoqin
 * @date 2018/11/5 2:20 PM
 */
@Getter
public enum GroupFeatureStatusEnum {

    /**
     * 行业类型
     */
    INDUSTRY("$industry", "行业"),

    /**
     * 城市类型
     */
    CITY("$city", "城市"),

    /**
     * 省份类型
     */
    PROVINCE("$province", "省份");

    private String operator;
    private String msg;

    private GroupFeatureStatusEnum(String operator, String msg) {
        this.operator = operator;
        this.msg = msg;
    }

    public static String getOperatorByType(String type) {
        GroupFeatureStatusEnum[] values = GroupFeatureStatusEnum.values();
        for (GroupFeatureStatusEnum groupFeatureStatusEnum : values) {
            if (groupFeatureStatusEnum.getMsg().equals(type)) {
                return groupFeatureStatusEnum.getOperator();
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String operatorByType = getOperatorByType(null);
        System.out.println(operatorByType);
    }
}
