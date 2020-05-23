package com.haizhi.iap.search.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 17/4/25.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Range<T extends Comparable> {
    T from;

    T to;

    private boolean includeLower;
    private boolean includeUpper;

    public Range(T from, T to)
    {
        this(from, to, true, true);
    }

    /**
     * 判定value是否落在该区间
     *
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean inRange(T value)
    {
        if(value == null) throw new IllegalArgumentException("null value is not allowed");
        boolean fromCompare = from == null || (includeLower ? value.compareTo(from) >= 0 : value.compareTo(from) > 0);
        boolean toCompare = to == null || (includeUpper ? value.compareTo(to) <= 0 : value.compareTo(to) < 0);
        return fromCompare && toCompare;
    }
}