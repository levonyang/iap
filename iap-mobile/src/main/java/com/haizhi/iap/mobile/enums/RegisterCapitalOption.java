package com.haizhi.iap.mobile.enums;

import com.haizhi.iap.mobile.bean.normal.Range;
import lombok.Getter;

/**
 * Created by thomas on 18/4/11.
 *
 * 注册资本的筛选项
 */
public enum RegisterCapitalOption
{
    /**
     * 100万
     */
    ONE(new Range<>(0, 1000000, true, false)),

    /**
     * 200万
     */
    TWO(new Range<>(1000000, 2000000, true, false)),

    /**
     * 500万
     */
    FIVE(new Range<>(2000000, 5000000, true, false)),

    TEN(new Range<>(5000000, 10000000, true, false)),

    MORE(new Range<>(10000000, null, true, true));

    @Getter
    private Range<Integer> range;

    RegisterCapitalOption(Range<Integer> range)
    {
        this.range = range;
    }
}
