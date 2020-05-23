package com.haizhi.iap.search.constant;

/**
* @description 实体类型
* @author liulu
* @date 2018/12/20
*/
public enum EntityType {

    COMPANY("Company","企业实体"),
    PERSON("Person","自然人实体"),
    ALL("All","所有"),
    ;

    private String value;

    private String desc;

    EntityType(String value,String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    /**
     * @description 根据值查找枚举
     * @param value
     * @return com.haizhi.iap.search.constants.EntityType
     * @author liulu
     * @date 2018/12/20
     */
    public static EntityType byValue(Integer value){
        for(EntityType entityType : EntityType.values()){
            if (entityType.getValue().equals(value)){
                return entityType;
            }
        }
        return null;

    }
}
