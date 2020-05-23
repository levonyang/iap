package com.haizhi.iap.search.constant;

/**
* @description 是否是行内(即授信)、行外客户
* @author liulu
* @date 2018/12/25
*/
public enum BelongInnerStatus {

    Y(1,"True","行内(授信)客户"),
    N(0,"False","行外客户"),
    ;

    private Integer value;

    private String boolValue;

    private String desc;

    BelongInnerStatus(Integer value,String boolValue,String desc) {
        this.value = value;
        this.boolValue = boolValue;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public String getBoolValue() {
        return boolValue;
    }

    /**
     * @description 根据值查找枚举
     * @param value
     * @return com.haizhi.iap.search.constants.BelongInnerStatus
     * @author liulu
     * @date 2018/12/20
     */
    public static BelongInnerStatus byValue(Integer value){
        for(BelongInnerStatus belongInnerStatus : BelongInnerStatus.values()){
            if (belongInnerStatus.getValue().equals(value)){
                return belongInnerStatus;
            }
        }
        return null;

    }

    /**
     * @description 根据值查找枚举
     * @param value
     * @return com.haizhi.iap.search.constants.BelongInnerStatus
     * @author liulu
     * @date 2018/12/20
     */
    public static BelongInnerStatus byBoolValue(String value){
        for(BelongInnerStatus belongInnerStatus : BelongInnerStatus.values()){
            if (belongInnerStatus.getBoolValue().equalsIgnoreCase(value)){
                return belongInnerStatus;
            }
        }
        return null;

    }
}
