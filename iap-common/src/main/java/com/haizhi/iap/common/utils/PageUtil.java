package com.haizhi.iap.common.utils;

import java.util.Collections;
import java.util.List;

/**
 * Created by chenbo on 2017/11/9.
 */
public class PageUtil {

    public static <T> List<T> pageList(List<T> list, Integer offset, Integer count) {
        if (list == null) {
            return Collections.emptyList();
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
