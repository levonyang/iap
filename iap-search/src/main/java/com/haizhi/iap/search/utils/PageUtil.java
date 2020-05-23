package com.haizhi.iap.search.utils;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by chenbo on 2017/11/9.
 */
public class PageUtil {

    public static List pageList(List list, Integer offset, Integer count) {
        if (list == null) {
            return new ArrayList<>();
        }
        if (list.size() >= (offset + count)) {
            Integer toIndex = offset + count;
            if (toIndex > list.size()) {
                toIndex = list.size();
            }
            return list.subList(offset, toIndex);
        } else if (list.size() > offset && list.size() < (offset + count)) {
            return list.subList(offset, list.size());
        } else {
            //越界
            return Collections.emptyList();
        }
    }
}
