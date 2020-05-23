package com.haizhi.iap.search.controller;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.model.qo.GroupMembersSearchQo;
import com.haizhi.iap.search.model.vo.GroupCreditEntityVo;
import com.haizhi.iap.search.model.vo.GroupFeatureVo;
import com.haizhi.iap.search.model.vo.GroupMembersVo;
import com.haizhi.iap.search.service.GroupFeatureService;
import com.haizhi.iap.search.service.GroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 集团相关
 *
 * @author xuguoqin
 * @date 2018/11/5 11:31 AM
 */
@Api(tags = "【搜索-族谱信息】查询族谱信息")
@RestController
@RequestMapping("/search/group")
public class GroupController {

    @Autowired
    private GroupFeatureService groupFeatureService;

    @Autowired
    private GroupService groupService;

    /**
     * 集团群体特征
     *
     * @param groupName
     * @param type
     * @return
     */
    @RequestMapping(value = "/feature", method = RequestMethod.GET)
    @ApiOperation(value = "查询集团群体特征")
    public Wrapper feature(@ApiParam(value = "族谱名称", required = true) @RequestParam("group_name") String groupName, String type) {

        List<GroupFeatureVo> groupFeatureVos = groupFeatureService.listGroupFeature(groupName, type);
        return Wrapper.ok(groupFeatureVos);
    }


    @ApiOperation(value = "查询集团内新注册企业")
    @RequestMapping(value = "/new_register_company", method = RequestMethod.GET)
    public Wrapper newRegisterCompany(@ApiParam(value = "族谱名称", required = true) @RequestParam("group_name") String groupName, Integer type) {
        DataItem dataItem = groupFeatureService.listNewRegisterGroupEnterprise(groupName, type);
        return Wrapper.ok(dataItem);
    }

    @ApiOperation(value = "条件查询集团内的成员(企业及自然人）信息")
    @RequestMapping(value = "/findGroupMembers", method = RequestMethod.POST)
    public Wrapper findGroupMembers(@ApiParam(value = "查询条件", required = true) @RequestBody @Valid GroupMembersSearchQo groupMembersSearchQo) {
        GroupMembersVo groupMembers = groupService.findGroupMembers(groupMembersSearchQo);
        return Wrapper.ok(groupMembers);
    }


    /**
     * @param groupName
     * @param type
     * @return com.haizhi.iap.common.Wrapper
     * @description 获取集团下授信超限实体(包含公司和自然人)
     * @author weimin
     * @date 2018-12-19
     */
    @ApiOperation(value = "获取集团下授信超限实体(包含公司和自然人)")
    @RequestMapping(value = "/groupCreditOverLimitEntities", method = RequestMethod.GET)
    public Wrapper findGroupCreditOverLimitEntities(@RequestParam("groupName") String groupName,
                                                    @RequestParam(value = "type") String type,
                                                    @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
                                                    @RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {
        List<GroupCreditEntityVo> data = groupFeatureService.findGroupCreditOverLimitEntities(groupName, type, offset, count);
        return Wrapper.ok(data);
    }
}
