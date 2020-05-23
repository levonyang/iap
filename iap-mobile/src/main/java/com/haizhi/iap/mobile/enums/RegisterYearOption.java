package com.haizhi.iap.mobile.enums;

import com.haizhi.iap.mobile.bean.normal.Range;
import lombok.Getter;

/**
 * Created by thomas on 18/4/11.
 *
 * 注册年限的筛选项
 */
public enum RegisterYearOption
{
    /**
     * 1年内
     */
    ONE(new Range<>(0, 1, true, false)),

    /**
     * 1-2年
     */
    TWO(new Range<>(1, 2, true, false)),

    /**
     * 2-3年
     */
    THREE(new Range<>(2, 3, true, false)),

    FIVE(new Range<>(3, 5, true, false)),

    TEN(new Range<>(5, 10, true, false)),

    MORE(new Range<>(10, null, true, true));

    @Getter
    private Range<Integer> range;

    RegisterYearOption(Range<Integer> range)
    {
        this.range = range;
    }
}
