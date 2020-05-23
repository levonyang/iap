package com.haizhi.iap.configure.service;

import com.haizhi.iap.configure.model.Param;

/**
 * @Author dmy
 * @Date 2017/5/8 下午6:59.
 */
public interface ParamService {
    /**
     * 通过组件id获取参数
     * @param componentId
     * @return
     */
    Param getByComponentId(Long componentId);

    /**
     * 通过数据源id和组件id获取参数
     * @param datasourceid
     * @param componentId
     * @return
     */
    Param getParamByDataAndComponent(Long datasourceid, Long componentId);
}
