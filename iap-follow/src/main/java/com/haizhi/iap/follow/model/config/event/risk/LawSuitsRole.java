package com.haizhi.iap.follow.model.config.event.risk;

/**
 * Created by chenbo on 2017/12/8.
 */
public enum LawSuitsRole {
    PLAINTIFF, DEFENDANT, ALL;

    public static boolean contains(String roleType) {
        for (LawSuitsRole role : LawSuitsRole.values()) {
            if (role.name().toLowerCase().equals(roleType)) {
                return true;
            }
        }
        return false;
    }

    public String getName(){
        return this.name().toLowerCase();
    }
}