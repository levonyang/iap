package com.haizhi.iap.search.constant;

/**
* @description 常用字段
* @author liulu
* @date 2018/12/24
*/
public enum CommonFields {

    COMPANY("company","公司名称"),
    IS_LISTED("is_listed","是否上市"),
    PUBLIC_SECTOR("public_sector","上市板块"),
    BUSINESS_STATUS("business_status","企业经营状态"),
    IS_ABNORMAL_STATUS("is_abnormal_status","经营状态是否异常"),
    BELONG_INNER("belong_inner","行内(授信客户)tag"),
    ID("id","唯一Id"),
    KEY_PERSON_NAME("key_person_name","高管人员"),
    KEY_PERSON_POSITION("key_person_position","高管人员的职位"),

    _FROM("_from","起点"),
    _TO("_to","终点"),
    _ID("_id","唯一Id"),
    SRC_("src_","源"),
    DST_("dst_","目标"),
    _FROM_ID("_from_id","源Id"),
    _TO_ID("_to_id","目标Id"),

    GROUP_NAME("group_name","族谱名称"),
    GROUP_SUB_TYPE("type","族谱子类型(暂无实际意义)"),
    GROUP_PATHS("paths","族谱关系"),
    SRC_NAME("src_name","起点实体名称"),
    DST_NAME("dst_name","终点实体名称"),
    SRC_BELONG_INNER("src_belong_inner","起点实体是否在行内"),
    DST_BELONG_INNER("dst_belong_inner","终点实体是否在行内"),
    ;

    private String value;

    private String desc;

    CommonFields(String value,String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    /**
     * @description 根据值查找枚举
     * @param value
     * @return com.haizhi.iap.search.constants.CommonFields
     * @author liulu
     * @date 2018/12/20
     */
    public static CommonFields byValue(Integer value){
        for(CommonFields commonFields : CommonFields.values()){
            if (commonFields.getValue().equals(value)){
                return commonFields;
            }
        }
        return null;

    }
}
