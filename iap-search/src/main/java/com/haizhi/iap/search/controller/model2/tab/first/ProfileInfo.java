package com.haizhi.iap.search.controller.model2.tab.first;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.tab.second.AssociatedInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 一级tab -- 画像信息
 * Created by chenbo on 2017/11/10.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileInfo {

    @JsonProperty("self_info")
    DataItem selfInfo;

    @JsonProperty("associated_info")
    AssociatedInfo associatedInfo;

    @JsonProperty("dynamic_info")
    DataItem dynamicInfo;

    public enum ProfileInfoSubType{

        SELF_INFO, ASSOCIATED_INFO, DYNAMIC_INFO;

        public String getName() {
            return this.name().toLowerCase();
        }

        public static boolean contains(String typeName) {
            for (ProfileInfoSubType type : ProfileInfoSubType.values()) {
                if (type.getName().equals(typeName)) {
                    return true;
                }
            }
            return false;
        }

        public static ProfileInfoSubType get(String typeName) {
            for (ProfileInfoSubType type : ProfileInfoSubType.values()) {
                if (type.getName().equalsIgnoreCase(typeName)) {
                    return type;
                }
            }
            return null;
        }
    }
}
