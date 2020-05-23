package com.haizhi.iap.search.service;

import com.alibaba.fastjson.JSON;
import com.haizhi.iap.search.constant.EntityType;
import com.haizhi.iap.search.model.qo.GroupMembersSearchQo;
import com.haizhi.iap.search.model.vo.GroupMembersVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:spring/applicationContext.xml",
        "classpath:spring/applicationContext-data.xml",
})
public class GroupServiceTest {

    @Autowired
    private GroupService groupService;
    @Test
    public void testFindGroupMembers(){
        GroupMembersSearchQo groupMembersSearchQo = new GroupMembersSearchQo();
        groupMembersSearchQo.setGroupType("market_updown_info");
        groupMembersSearchQo.setEntityType(EntityType.ALL);
        groupMembersSearchQo.setGroupName("upstream_group/-7825434093715585466");
        GroupMembersVo groupMembersVo =  groupService.findGroupMembers(groupMembersSearchQo);
        System.out.println(JSON.toJSONString(groupMembersVo,true));

    }
}
