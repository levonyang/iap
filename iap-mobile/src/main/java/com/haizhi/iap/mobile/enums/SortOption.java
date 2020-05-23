package com.haizhi.iap.mobile.enums;

import com.haizhi.iap.mobile.bean.normal.Sort;
import com.haizhi.iap.mobile.conf.EsSchemaConstatns;
import lombok.Getter;

/**
 * Created by thomas on 18/4/11.
 *
 * 排序的选项
 */
public enum SortOption
{
    DEFAULT(null),
    /**
     * 最大注册资本排序
     */
    MAX_CAPITAL(new Sort(EsSchemaConstatns.FIELD_VAL_REGISTERED_CAPITAL, Direction.DESC)),

    /**
     * 最小注册资本排序
     */
    MIN_CAPITAL(new Sort(EsSchemaConstatns.FIELD_VAL_REGISTERED_CAPITAL, Direction.ASC)),

    /**
     * 最早成立时间排序
     */
    MIN_REGISTER_DATE(new Sort(EsSchemaConstatns.FIELD_VAL_REGISTERED_DATE, Direction.ASC)),

    /**
     * 最早成立时间排序
     */
    MAX_REGISTER_DATE(new Sort(EsSchemaConstatns.FIELD_VAL_REGISTERED_DATE, Direction.DESC));

    @Getter
    private Sort sort;

    SortOption(Sort sort)
    {
        this.sort = sort;
    }
}
