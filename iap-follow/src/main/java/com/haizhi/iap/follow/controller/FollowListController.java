package com.haizhi.iap.follow.controller;


import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.DefaultSecurityContext;
import com.haizhi.iap.common.utils.MapBuilder;
import com.haizhi.iap.follow.enums.LimitConfig;
import com.haizhi.iap.follow.exception.FollowException;
import com.haizhi.iap.follow.model.FollowItem;
import com.haizhi.iap.follow.model.FollowList;
import com.haizhi.iap.follow.model.User;
import com.haizhi.iap.follow.repo.FollowItemRepo;
import com.haizhi.iap.follow.repo.FollowListRepo;
import com.haizhi.iap.follow.repo.NotificationRepo;
import com.haizhi.iap.follow.repo.UserRepo;
import io.swagger.annotations.Api;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by chenbo on 17/1/12.
 */
@Api(tags="【关注-我的关注模块】关注列表管理")
@Slf4j
@RestController
@RequestMapping(value = "/follow")
public class FollowListController {

    @Setter
    @Autowired
    FollowListRepo followListRepo;

    @Setter
    @Autowired
    NotificationRepo notificationRepo;

    @Setter
    @Autowired
    FollowItemRepo itemRepo;

    @Autowired
    private UserRepo userRepo;

    private List<String> typeList = Lists.newArrayList("risk", "marketing");
    private List<String> opList = Lists.newArrayList("add", "cancel");

    @POST
    @Path("/follow_list")
    @RequestMapping(value = "/follow_list", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper addFollowList(@RequestBody FollowList list) {
        if (Strings.isNullOrEmpty(list.getName())) {
            return FollowException.NO_NAME.get();
        }
        if (list.getName() != null && list.getName().length() > 16) {
            return FollowException.NAME_LIMIT.get();
        }
        Long userId = DefaultSecurityContext.getUserId();
        //分组数限制
        if (followListRepo.countByUserId(userId) > LimitConfig.LIST_NUM_PER_USER) {
            return FollowException.OVER_LIMIT_LIST_NUM_PER_USER.get();
        }
        FollowList taken = followListRepo.findByName(userId, list.getName());
        if (taken != null && taken.getId() != null) {
            return FollowException.NAME_ALREADY_USED.get();
        } else {
            list.setUserId(userId);
            followListRepo.create(list);
        }
        return Wrapper.OK;
    }

    @RequestMapping(value = "/follow_list", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper modifyFollowList(@RequestBody FollowList list) {
        if (list.getId() == null || list.getId().equals(0l)) {
            return FollowException.NO_FOLLOW_LIST_ID.get();
        }
        if (Strings.isNullOrEmpty(list.getName())) {
            return FollowException.NO_NAME.get();
        }
        if (list.getName() != null && list.getName().length() > 16) {
            return FollowException.NAME_LIMIT.get();
        }

        Long userId = DefaultSecurityContext.getUserId();

        FollowList taken = followListRepo.findByName(userId, list.getName());
        if (taken != null && !taken.getId().equals(list.getId())) {
            return FollowException.NAME_ALREADY_USED.get();
        } else {
            followListRepo.update(list);
        }
        return Wrapper.OK;
    }

    @RequestMapping(value = "/follow_list/{follow_list_id}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper deleteFollowList(@PathVariable("follow_list_id") Long listId) {
        if (listId == null || listId.equals(0l)) {
            return FollowException.NO_FOLLOW_LIST_ID.get();
        }

        //删除分组
        followListRepo.delete(listId);
        //删除item对应的数据
        itemRepo.deleteByList(listId);

        return Wrapper.OK;
    }

    @RequestMapping(value = "/follow_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper followLists(@RequestParam(value = "company_name", required = false) String companyName) {
        Long userId = DefaultSecurityContext.getUserId();
        List<FollowList> followLists = followListRepo.findByUserId(userId);

        if (Strings.isNullOrEmpty(companyName)) {
            return Wrapper.OKBuilder.data(followLists).build();
        } else {
            List<FollowItem> items = itemRepo.findByName(userId, companyName);
            List<Long> existIds = Lists.newArrayList();
            existIds.addAll(items.stream().map(FollowItem::getFollowListId).collect(Collectors.toList()));

            for (FollowList list : followLists) {
                if (existIds.contains(list.getId())) {
                    list.setCompanyIn(true);
                } else {
                    list.setCompanyIn(false);
                }
            }
            return Wrapper.OKBuilder.data(followLists).build();
        }
    }

    @RequestMapping(value = "/follow_list/items/{follow_list_id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getItemsByPage(@PathVariable("follow_list_id") Long listId,
                                  @RequestParam(name = "count", required = false) Integer count,
                                  @RequestParam(name = "offset", required = false) Integer offset,
                                  @RequestParam(name = "msg_type", required = false) String type) {
        if (count == null) {
            count = 10;
        }
        if (offset == null) {
            offset = 0;
        }

        List<FollowItem> items;
        Map<String, Object> map = Maps.newHashMap();
        Long userId = DefaultSecurityContext.getUserId();
        if (Strings.isNullOrEmpty(type) || !typeList.contains(type)) {
            items = itemRepo.findByCondition(userId, listId,null,null,
                    offset, count,null,null,true);
            map.put("total_count", followListRepo.findById(listId).getListCount());
        } else {
            if (typeList.get(0).equals(type)) {
                items = itemRepo.findByCondition(userId, listId, null, null,
                        offset, count, null, 1,true);
                map.put("total_count", itemRepo.countByCondition(userId, listId, null,
                        null, null, 1,true));
            } else {
                items = itemRepo.findByCondition(userId, listId, null, null,
                        offset, count, 1, null,true);
                map.put("total_count", itemRepo.countByCondition(userId, listId, null,
                        null, 1, null,true));
            }
        }

        map.put("data", items);
        return Wrapper.OKBuilder.data(map).build();
    }

    @RequestMapping(value = "/follow_list/items", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper deleteFromList(@RequestParam(value = "item_id", required = false) Long itemId,
                                  @RequestParam(value = "follow_list_id", required = false) Long listId,
                                  @RequestParam(value = "company_name", required = false) String companyName) {
        if (itemId == null && (Strings.isNullOrEmpty(companyName) && listId == null)) {
            return FollowException.MISS_PARAM.get();
        }
        Long userId = DefaultSecurityContext.getUserId();
        if (itemId != null) {
            itemRepo.delete(itemId);
        } else {
            if (!listId.equals(0l)) {
                //从指定组中删除
                itemRepo.deleteByListAndName(listId, companyName);
            } else {
                //在公司详情页取消关注,就没有指定组
                List<FollowItem> items = itemRepo.findByName(userId, companyName);
                for (FollowItem item : items) {
                    itemRepo.deleteByListAndName(item.getFollowListId(), companyName);
                }
            }
        }
        return Wrapper.OK;
    }

    @RequestMapping(value = "/follow_list/items", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper addItemToList(@RequestBody FollowItem item) {
        if (item.getFollowListId() == null || item.getFollowListId().equals(0l)) {
            return FollowException.NO_FOLLOW_LIST_ID.get();
        }
        if (item.getCompanyName() == null) {
            return FollowException.MISS_COMPANY_NAME.get();
        }
        Long userId = DefaultSecurityContext.getUserId();
        //每个分组中的企业数限制
        if (followListRepo.findById(item.getFollowListId()).getListCount() >= LimitConfig.ITEM_NUM_PER_LIST) {
            return FollowException.OVER_LIMIT_ITEM_NUM_PER_LIST.get();
        } else if (followListRepo.sumItemCount(userId) >= LimitConfig.ITEM_SUM_PER_USER) {
            //单个用户企业数限制
            return FollowException.OVER_LIMIT_ITEM_SUM_PER_USER.get();
        }
        item.setUserId(DefaultSecurityContext.getUserId());
        item.setIsExistsIn(1);

        itemRepo.create(item);
        //TODO 启动抓取

        return Wrapper.OK;
    }

    /**
     * 复制到其他分组
     *
     * @param companyName
     * @param listIds
     * @return
     */
    @RequestMapping(value = "/batch_modify_follow", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper batchModify(@RequestParam("company_name") String companyName,
                               @RequestBody List<Long> listIds) {
        List<FollowItem> origin = itemRepo.findByName(DefaultSecurityContext.getUserId(), companyName);
        List<Long> originListIds = Lists.newArrayList();
        if (origin != null && origin.size() > 0) {
            originListIds.addAll(origin.stream().map(FollowItem::getFollowListId).collect(Collectors.toList()));
        }
        List<Long> retainList = (List<Long>) ((ArrayList) originListIds).clone();

        //得到交集
        retainList.retainAll(listIds);

        //得到该删除的部分
        originListIds.removeAll(retainList);

        //得到该新增的部分
        listIds.removeAll(retainList);

        Long userId = DefaultSecurityContext.getUserId();
        List<FollowItem> updateList = Lists.newArrayList();
        for (Long listId : listIds) {
            FollowItem item = new FollowItem();
            item.setUserId(userId);
            item.setCompanyName(companyName);
            item.setFollowListId(listId);
            //每个分组中的企业数限制
            if (followListRepo.findById(listId).getListCount() >= LimitConfig.ITEM_NUM_PER_LIST) {
                return FollowException.OVER_LIMIT_ITEM_NUM_PER_LIST.get();
            } else if (followListRepo.sumItemCount(userId) >= LimitConfig.ITEM_SUM_PER_USER) {
                //单个用户企业数限制
                return FollowException.OVER_LIMIT_ITEM_SUM_PER_USER.get();
            }
            item.setIsExistsIn(1);
            updateList.add(item);
        }

        for (Long listId : originListIds) {
            itemRepo.deleteByListAndName(listId, companyName);
        }

        itemRepo.batchInsert(updateList);
        return Wrapper.OK;
    }

    @Deprecated
    @RequestMapping(value = "/set_notification", method = {RequestMethod.PUT, RequestMethod.PATCH},
            produces = MediaType.APPLICATION_JSON)
    public Wrapper set(@RequestBody Map<String, Object> data) {
        if (data.get("companies") == null) {
            return FollowException.MISS_COMPANIES.get();
        } else if (!(data.get("companies") instanceof List)) {
            return FollowException.WRONG_COMPANIES.get();
        }
        if (data.get("type") == null || Strings.isNullOrEmpty((String) data.get("type"))) {
            return FollowException.MISS_TYPE.get();
        } else if (!typeList.contains(data.get("type"))) {
            return FollowException.WRONG_TYPE.get();
        }

        if (data.get("operation") == null || Strings.isNullOrEmpty((String) data.get("operation"))) {
            return FollowException.MISS_OPERATION.get();
        } else if (!opList.contains(data.get("operation"))) {
            return FollowException.WRONG_OPERATION.get();
        }
        List<String> companies = Lists.newArrayList();
        for(Object obj : (List)data.get("companies")){
            if(obj != null){
                companies.add(obj.toString());
            }
        }
        Long userId = DefaultSecurityContext.getUserId();
        Integer tag = data.get("operation").equals("add") ? 1 : 0;
        itemRepo.setNotify(userId, (String) data.get("type"), companies, tag);
        return Wrapper.OK;
    }

    /**
     *   监控设置
     *  替代set_notification
     */
    @RequestMapping(value = "/setMonitorConfig", method = {RequestMethod.PUT, RequestMethod.PATCH},
            produces = MediaType.APPLICATION_JSON)
    public Wrapper setMonitorConfig(@RequestBody Map<String, Object> data) {
        Object company = data.get("company");
        if (company == null) {
            return FollowException.MISS_COMPANIES.get();
        }
        Long userID = DefaultSecurityContext.getUserId();
        if(data.containsKey("username"))
        {
            User user = userRepo.findByUsername(data.get("username").toString());
            if(user == null) return FollowException.USER_NOT_EXISTS.get();
            userID = user.getId();
        }

        //关注或监控时，判断已关注或监控企业数量
        if(!cancelMonitor(data)){
            if(!itemRepo.checkMonitorLegality(userID,company.toString())){
                return Wrapper.ERRORBuilder.msg("监控企业数量不允许大于10个！").build();
            }
        }
        int isNull = 0;
        try {
            isNull = itemRepo.ifNullAndAddNotify(userID, data);
        }catch (Exception e){
            log.error("监控设置记录不存在，插入时报错！     userID ："+userID +"      company : "
                    +data.get("company"),e);
            e.printStackTrace();
            return Wrapper.ERROR;
        }
        if(isNull != 1){
            if(!itemRepo.isMonitor(userID,data.get("company").toString())){
                data.put("monitor_time","true");
            }
            itemRepo.setNotify(userID,data);
        }

        //version2.5 取消监控 删除消息
        if (cancelMonitor(data)) {
            this.notificationRepo.deleteAllByMonitor(userID, company.toString());
        }
        return Wrapper.OK;
    }

    private boolean cancelMonitor(Map<String, Object> data){
        return !StringUtils.isEmpty(data.get("company"))
                && data.get("risk_notify") != null
                && data.get("marketing_notify") != null
                && data.get("closely_risk_notify") != null
                && data.get("closely_marketing_notify") != null

                && data.get("risk_notify").toString().equals("false")
                && data.get("marketing_notify").toString().equals("false")
                && data.get("closely_risk_notify").toString().equals("false")
                && data.get("closely_marketing_notify").toString().equals("false");
    }

    @RequestMapping(value = "/get_switch", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getNotificationSetting(@RequestParam("company_name") String company) {
        Long userId = DefaultSecurityContext.getUserId();

        if (Strings.isNullOrEmpty(company)) {
            return FollowException.MISS_COMPANY_NAME.get();
        }
        List<FollowItem> items = itemRepo.findByName(userId, company,null);
        boolean follow = false;
        boolean risk_notify = false;
        boolean marketing_notify = false;
        boolean closelyRiskNotify = false;
        boolean closelyMarketingNotify = false;
        String closelyRule = null;
        if (items != null && items.size() > 0) {
            risk_notify = items.get(0).getRiskNotify() != 0;
            marketing_notify = items.get(0).getMarketingNotify() != 0;

            closelyRiskNotify = items.get(0).getCloselyRiskNotify() != 0;
            closelyMarketingNotify = items.get(0).getCloselyMarketingNotify() != 0;
            closelyRule = items.get(0).getCloselyRule();
            follow = items.get(0).getIsFollow() != 0;
        }
        Map<String, Object> data = new MapBuilder()
                .put("follow", follow)
                .put("risk_notify", risk_notify)
                .put("marketing_notify", marketing_notify)
                .put("closely_risk_notify", closelyRiskNotify)
                .put("closely_marketing_notify", closelyMarketingNotify)
                .put("closely_rule",closelyRule)
                .build();
        return Wrapper.OKBuilder.data(data).build();
    }

    @RequestMapping(value = "/check_monitor", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper checkMonitor() {
        Long userID = DefaultSecurityContext.getUserId();
        boolean flag = this.itemRepo.checkMonitor(userID);

        Map map = new HashMap();
        map.put("exist_monitor", flag);
        return Wrapper.OKBuilder.data(map).build();
    }

}
