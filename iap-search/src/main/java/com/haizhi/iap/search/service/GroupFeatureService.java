package com.haizhi.iap.search.service;

import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.model.vo.GroupFeatureVo;
import com.haizhi.iap.search.model.vo.GroupCreditEntityVo;

import java.util.List;

/**
 * 集团特征
 *
 * @author xuguoqin
 * @date 2018/11/5 2:24 PM
 */
public interface GroupFeatureService {

    /**
     * 得到集团特征列表
     *
     * @param groupName
     * @param type
     * @return
     */
    List<GroupFeatureVo> listGroupFeature(String groupName, String type);

    /**
     * 展示集团内新注册企业列表
     *
     * @param groupName 集团名
     * @param type      是否为行内客户 默认0
     * @return
     */
    DataItem listNewRegisterGroupEnterprise(String groupName, Integer type);

    /**
     * @param groupName
     * @param type
     * @param offset
     * @param count
     * @return com.haizhi.iap.search.controller.model.DataItem
     * @description
     * @author weimin
     * @date 2018-12-19
     */
    List<GroupCreditEntityVo> findGroupCreditOverLimitEntities(String groupName, String type, Integer offset, Integer count);

}
