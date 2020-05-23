package com.haizhi.iap.search.controller.model2;

/**
 * Created by chenbo on 2017/11/8.
 */
public enum SearchType {
    /**画像信息、基本信息、关联关系、风险信息、营销信息、舆情信息**/
    PROFILE_INFO, BASIC_INFO, ASSOCIATED_RELATION, RISK_INFO, MARKET_INFO, PUBLIC_SENTIMENT,

    //用于一次性返回计数专门给出一个type
    ALL;

    public String getName(){
        return this.name().toLowerCase();
    }

    public static boolean contains(String typeName){
        for (SearchType type : SearchType.values()) {
            if(type.getName().equals(typeName)){
                return true;
            }
        }
        return false;
    }

    public static SearchType get(String typeName) {
        for (SearchType type : SearchType.values()) {
            if(type.getName().equalsIgnoreCase(typeName)){
                return type;
            }
        }
        return null;
    }
}
