package com.haizhi.iap.search.service;


import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model.Graph;
import com.haizhi.iap.search.model.CompanyGroup;

import java.util.List;
import java.util.Map;

/**
* @description 族谱服务接口
* @author LewisLouis
* @date 2018/8/20
*/
public interface GraphGroupService {

    /**
     * @description 获取指定族谱的边
     * @param type 族谱类型
     *（如profile_enterprise_info 关联集团
     * market_updown_info  上下游
     * risk_propagation  风险传导
     * risk_guarantee_info 关联担保
     * risk_black_info  黑名单）
    * @param subType 族谱子类型（如circle）
    * @param offset 开始行数
    * @param count 返回数量
     * @return DataItem
     * @author LewisLouis
     * @date 2018/8/20
     */
    DataItem findGroupsByTypeWithOutPaths(String type, String subType, Integer offset, Integer count);

    /**
     * @description 获取指定族谱的簇子图信息
     * @param groupName 族谱名称
     * @param type 族谱类型
     * @return com.haizhi.iap.search.controller.model.Graph
     * @author LewisLouis
     * @date 2018/8/20
     */
    Graph findOneGroupGraph(String groupName, String type);

    /**
    * @description 获取指定族谱信息
    * @param groupName 族谱名称
    * @param type 族谱类型
    * @return com.haizhi.iap.search.model.CompanyGroup
    * @author LewisLouis
    * @date 2018/8/20
    */
    CompanyGroup findOneGroup(String groupName, String type);

    /**
    * @description 根据实体Id获取所属的族谱名称
    * @param entityId 实体Id
    * @param type 族谱类型
    * @return 族谱名称
    * @author LewisLouis
    * @date 2018/8/20
    */
    String findGroupNameByEntity(String entityId, String type);

    /**
    * @description 统计指定族谱类型的边类型数量
    * @param type 族谱类型
    * @return java.util.Map<java.lang.String,java.lang.Long> <变类型名称，对应的族谱数量>
    * @author LewisLouis
    * @date 2018/8/20
    */
    Map<String, Long> findSubTypes(String type);

    /**
    * @description 根据族谱类型获取边列表
    * @param type 族谱类型
    * @param subType 族谱边类型
    * @param offset
    * @param count
    * @return com.haizhi.iap.search.controller.model.DataItem
    * @author LewisLouis
    * @date 2018/8/20
    */
    DataItem findGroupPaths(String type, String subType, Integer offset, Integer count);

    /**
    * @description 获取实体相关的边信息
    * @param type 族谱类型
    * @param entityId 实体Id
    * @return java.util.List<com.haizhi.iap.search.model.CompanyGroup>
    * @author LewisLouis
    * @date 2018/8/20
    */
    List<CompanyGroup> findEntityPaths(String type, String entityId);

}
