package com.haizhi.iap.search.service;

import com.haizhi.iap.search.model.qo.GroupMembersSearchQo;
import com.haizhi.iap.search.model.vo.GroupMembersVo;

import java.util.List;
import java.util.Map;

/**
* @description 族谱操作接口
* @author liulu
* @date 2018/12/19
*/
public interface GroupService {

    /**
    * @description 条件查询指定族谱的成员信息
    * @param groupMembersSearchQo
    * @return com.haizhi.iap.search.model.vo.GroupCompanyVo
    * @author liulu
    * @date 2018/12/19
    */
    GroupMembersVo findGroupMembers(GroupMembersSearchQo groupMembersSearchQo);


    /**
     * @description  完善公司顶点信息
     * @param vertices
    * @param needCompleteBelongInner 是否需要完善行内（即授信）、行外tag
     * @return void
     * @author liulu
     * @date 2018/12/25
     */
    void completeVertices(List<Map<String, Object>> vertices, Boolean needCompleteBelongInner);

}
