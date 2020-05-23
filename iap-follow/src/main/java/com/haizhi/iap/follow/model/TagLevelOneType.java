package com.haizhi.iap.follow.model;

/**
 * Created by chenbo on 2017/12/14.
 */
public enum TagLevelOneType {

    ALL, ADMIN_REGION, INDUSTRY, HOT_WORDS, PRODUCT;

    public static boolean contains(String type){
        for(TagLevelOneType tagLevel : TagLevelOneType.values()){
            if(tagLevel.name().toLowerCase().equals(type)){
                return true;
            }
        }
        return false;
    }

    public static TagLevelOneType get(String type){
        for(TagLevelOneType tagLevel : TagLevelOneType.values()){
            if(tagLevel.name().toLowerCase().equals(type)){
                return tagLevel;
            }
        }
        return null;
    }

    public String getName() {
        return this.name().toLowerCase();
    }
}
