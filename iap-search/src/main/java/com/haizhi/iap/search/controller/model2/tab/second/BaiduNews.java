package com.haizhi.iap.search.controller.model2.tab.second;

import com.haizhi.iap.search.controller.model.DataItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 2017/11/9.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaiduNews {
    DataItem positive;

    DataItem negative;

    DataItem neutral;

    public enum BaiduNewsType{

        POSITIVE, NEGATIVE, NEUTRAL;

        public String getName() {
            return this.name().toLowerCase();
        }

        public static boolean contains(String typeName) {
            for (BaiduNewsType type : BaiduNewsType.values()) {
                if (type.getName().equals(typeName)) {
                    return true;
                }
            }
            return false;
        }

        public static BaiduNewsType get(String typeName) {
            for (BaiduNewsType type : BaiduNewsType.values()) {
                if (type.getName().equalsIgnoreCase(typeName)) {
                    return type;
                }
            }
            return null;
        }
    }
}
