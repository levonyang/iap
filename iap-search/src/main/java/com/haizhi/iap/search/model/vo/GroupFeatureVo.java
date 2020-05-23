package com.haizhi.iap.search.model.vo;

import lombok.Data;

/**
 * 集团特征
 * @author xuguoqin
 * @date 2018/11/5 11:33 AM
 */
@Data
public class GroupFeatureVo {

    /**
     * 行业类型 城市类型 或者 省份类型
     */
    private String content;

    /**
     * 该行业公司总数
     */
    private Integer count;

    /**
     * 该行业公司数与集团公司总数的占比
     */
    private String proportion;
}
