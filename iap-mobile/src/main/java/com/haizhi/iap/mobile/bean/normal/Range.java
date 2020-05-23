package com.haizhi.iap.mobile.bean.normal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by thomas on 18/4/11.
 *
 * 表示一个区间
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Range<T>
{
    /**
     * 下限
     */
    private T from;
    /**
     * 上限
     */
    private T to;

    /**
     * 是否包含下限
     */
    private boolean includeLower;
    /**
     * 是否包含上限
     */
    private boolean includeUpper;

    public Range(T from, T to)
    {
        this(from, to, true, true);
    }
}
