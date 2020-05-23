package com.haizhi.iap.follow.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.DefaultSecurityContext;
import com.haizhi.iap.common.auth.NoneAuthorization;
import com.haizhi.iap.common.utils.MapBuilder;
import com.haizhi.iap.follow.controller.model.CompanyEventSearchReq;
import com.haizhi.iap.follow.controller.model.NotificationVO;
import com.haizhi.iap.follow.enums.NotificationType;
import com.haizhi.iap.follow.exception.FollowException;
import com.haizhi.iap.follow.model.MonitorCard;
import com.haizhi.iap.follow.model.NotifyEventInfo;
import com.haizhi.iap.follow.model.ReqDelMsg;
import com.haizhi.iap.follow.model.ReqEditMsgs;
import com.haizhi.iap.follow.model.ReqGetMsgs;
import com.haizhi.iap.follow.model.User;
import com.haizhi.iap.follow.model.notification.Notification;
import com.haizhi.iap.follow.repo.FollowItemRepo;
import com.haizhi.iap.follow.repo.GroupDetailRepo;
import com.haizhi.iap.follow.repo.MongoPubNotifyRepo;
import com.haizhi.iap.follow.repo.NotificationRepo;
import com.haizhi.iap.follow.repo.UserRepo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 17/5/2.
 */
@Api(tags="【关注-消息模块】消息管理")
@RestController
@RequestMapping(value = "/notification")
public class NotifyController {
    @Setter
    @Autowired
    FollowItemRepo followItemRepo;

    @Setter
    @Autowired
    NotificationRepo notificationRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MongoPubNotifyRepo mongoPubNotifyRepo;

    @Autowired
    private GroupDetailRepo groupDetailRepo;

    /**
     * 获取风险信息和营销信息的消息数
     *
     * @return
     */
    @RequestMapping(value = "/msgs_num", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper msgsNum() {
        Long userId = DefaultSecurityContext.getUserId();
        Map<String, Object> data = Maps.newHashMap();
        Long riskTotalNum = notificationRepo.countRisk(userId);
        Long riskNotRead = notificationRepo.countRisk(userId, 0);
        Long marketingTotalNum = notificationRepo.countMarketing(userId);
        Long marketingNumNotRead = notificationRepo.countMarketing(userId, 0);

        data.put("risk_num", new MapBuilder("total", riskTotalNum).put("not_read", riskNotRead).build());
        data.put("marketing_num", new MapBuilder("total", marketingTotalNum).put("not_read", marketingNumNotRead).build());
        data.put("total_num", new MapBuilder("total", riskTotalNum + marketingTotalNum).put("not_read", riskNotRead + marketingNumNotRead).build());
        return Wrapper.OKBuilder.data(data).build();
    }

    /**
     * 获取通知消息列表
     *
     * @param read
     * @param type
     * @param collect
     * @param offset
     * @param count
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/msgs", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper msgs(@RequestParam(value = "read", required = false) String read,
                        @RequestParam(value = "collected", required = false) String collect,
                        @RequestParam(value = "type", required = false) String type,
                        @RequestParam(value = "sub_type", required = false) String subType,
                        @RequestParam(value = "offset", required = false) Integer offset,
                        @RequestParam(value = "count", required = false) Integer count) {
        Long userId = DefaultSecurityContext.getUserId();

        if (offset == null) {
            offset = 0;
        }
        if (count == null) {
            count = 10;
        }

        Integer collected = null;
        if (!Strings.isNullOrEmpty(collect)) {
            collected = Boolean.parseBoolean(collect) ? 1 : 0;
        }
        Integer readed = null;
        if (!Strings.isNullOrEmpty(read)) {
            readed = Boolean.parseBoolean(read) ? 1 : 0;
        }
        List<Notification> data;
        Long total;
        Long notRead;
        if (Strings.isNullOrEmpty(subType)) {
            if (Strings.isNullOrEmpty(type)) {
                data = notificationRepo.findByCondition(userId, null, readed, collected, offset, count);
                total = notificationRepo.countByCondition(userId, null, readed, collected);
                notRead = notificationRepo.countByCondition(userId, null, 0, null);
            } else if (type.equals("risk")) {
                data = notificationRepo.findRisk(userId, readed, collected, offset, count);
                total = notificationRepo.countReadOrCollected(userId,"RISK",readed,collected);
                notRead = notificationRepo.countRisk(userId, 0);
            } else if (type.equals("marketing")) {
                data = notificationRepo.findMarketing(userId, readed, collected, offset, count);
                total = notificationRepo.countReadOrCollected(userId,"MARKETING",readed,collected);
                notRead = notificationRepo.countMarketing(userId, 0);
            } else {
                return FollowException.WRONG_TYPE.get();
            }
        } else {
            NotificationType notificationType = NotificationType.get(subType);
            if (notificationType == null) {
                return FollowException.WRONG_SUB_TYPE.get();
            } else {
                data = notificationRepo.findByCondition(userId, notificationType.getCode(), readed, collected, offset, count);
                total = notificationRepo.countByCondition(userId, notificationType.getCode(), readed, collected);
                notRead = notificationRepo.countByCondition(userId, notificationType.getCode(), 0, collected);
            }
        }

        List<NotificationVO> dataOutput = new ArrayList<>(data.size());
        for (Notification notification : data) {
            NotificationVO notificationVO = new NotificationVO(notification);
            notificationVO.setDetail(null);
            dataOutput.add(notificationVO);
        }

        Map<String, Object> result = new MapBuilder()
                .put("data", dataOutput)
                .put("total_count", total)
                .put("not_read_count", notRead)
                .build();

        return Wrapper.OKBuilder.data(result).build();
    }

    @RequestMapping(value = "/getMsgs", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getMsgs(@RequestBody ReqGetMsgs reqGetMsgs) {
        Long userId = DefaultSecurityContext.getUserId();
        if(!StringUtils.isEmpty(reqGetMsgs.getMasterCompany())){
            reqGetMsgs.setIsClosely(true);
        }
        //version 2.5
        if (reqGetMsgs.isClosely() != null && reqGetMsgs.isClosely() == true && reqGetMsgs.getCompany() != null) {
            reqGetMsgs.setMasterCompany(reqGetMsgs.getCompany());
            reqGetMsgs.setCompany(null);
        }

        /*
        if(reqGetMsgs.isClosely() == null && reqGetMsgs.getCollected() == null){
            reqGetMsgs.setIsClosely(false);
        }
        */
        List<Notification> data = notificationRepo.queryByCondition(userId,reqGetMsgs);
        Integer total = notificationRepo.getCountByCondition(userId,reqGetMsgs);
        reqGetMsgs.setRead(false);
        Integer notRead = notificationRepo.getCountByCondition(userId,reqGetMsgs);

        for(Notification item : data){
            NotificationType notificationType = NotificationType.get(item.getType());
            if (notificationType != null) {
                item.setSubTypeCnName(notificationType.getCnName());
                item.setSubTypeEnName(notificationType.getEnName());
            }

            int type = item.getType();
            if (type < 200) {
                item.setTypeCnName(NotificationVO.marketing_cn_name);
                item.setTypeEnName(NotificationVO.marketing_en_name);
            } else {
                item.setTypeCnName(NotificationVO.risk_cn_name);
                item.setTypeEnName(NotificationVO.risk_en_name);
            }

            if (StringUtils.isEmpty(item.getTitle())) {
                item.setTitle("一条" + item.getRuleName());
            }

            if (type >= 201 && type <= 204 && !StringUtils.isEmpty(item.getTitle())) {
                if (item.getTitle().contains("原告")) {
                    item.setRole("原告");
                } else if (item.getTitle().contains("被告")) {
                    item.setRole("被告");
                }
            }
        }

        Map<String, Object> result = new MapBuilder()
                .put("data", data)
                .put("total_count", total)
                .put("not_read_count", notRead)
                .build();

        Boolean isOpen = isOpen(reqGetMsgs.getType(), reqGetMsgs.isClosely());
        if (isOpen != null) {
            result.put("open", isOpen);
        }

        return Wrapper.OKBuilder.data(result).build();
    }

    private Boolean isOpen(String type, Boolean closely) {
        if (type != null) {
            Long userID = DefaultSecurityContext.getUserId();
            return this.followItemRepo.isFollowOpen(userID, type, closely);
        }

        return null;
    }

    /**
     * 获取某用户的分类未阅读消息数量
     *
     * @param type risk or marketing
     * @return
     */
    @RequestMapping(value = "/not_read_nums", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper unreadMsgNum(@RequestParam("type") String type) {
        Long userId = DefaultSecurityContext.getUserId();
        if (!"marketing".equals(type) && !"risk".equals(type)) {
            return Wrapper.ERRORBuilder.msg("type must be risk / marketing").build();
        }
        List<Pair<Integer, Integer>> daoData = notificationRepo.countUnreadGroupByType(userId,
                "marketing".equals(type) ? 0 : 1);
        Map<String, Object> returnMap = Maps.newHashMap();
        if ("marketing".equals(type)) {
            for (NotificationType one : NotificationType.values()) {
                if (one.getCode() < 200) {
                    returnMap.put(one.getEnName(), 0);
                }
            }
        } else if ("risk".equals(type)) {
            for (NotificationType one : NotificationType.values()) {
                if (one.getCode() > 200) {
                    returnMap.put(one.getEnName(), 0);
                }
            }
        }
        for (Pair<Integer, Integer> one : daoData) {
            NotificationType notificationType = NotificationType.get(one.getKey());
            returnMap.put(notificationType == null ? "unknownType" : notificationType.getEnName(), one.getValue());
        }
        return Wrapper.OKBuilder.data(returnMap).build();
    }


    /**
     * 标记消息为已读
     *
     * @param data
     * @return
     */
    @RequestMapping(value = "/read_msgs", method = {RequestMethod.PUT, RequestMethod.PATCH},
            produces = MediaType.APPLICATION_JSON)
    public Wrapper readMsg(@RequestBody Map<String, List<Long>> data) {
        if (data == null) {
            return FollowException.MISS_BODY.get();
        } else if (data.get("ids") == null || (data.get("ids")).size() < 1) {
            return FollowException.MISS_IDS.get();
        }
        notificationRepo.read(data.get("ids"));
        return Wrapper.OK;
    }

    /**
     * 将所有消息标记为已读
     *
     * @return
     */
    @RequestMapping(value = "/read_all_msgs", method = {RequestMethod.PUT, RequestMethod.PATCH},
            produces = MediaType.APPLICATION_JSON)
    public Wrapper readAllMsg() {
        Long userId = DefaultSecurityContext.getUserId();
        notificationRepo.readAll(userId);
        return Wrapper.OK;
    }

    /**
     * 设置收藏
     *
     * @param data
     * @return
     */
    @RequestMapping(value = "/set_collect", method = {RequestMethod.PUT, RequestMethod.PATCH},
            produces = MediaType.APPLICATION_JSON)
    public Wrapper setCollectMsg(@RequestBody Map<String, Object> data) {
        Boolean collect = Boolean.parseBoolean(data.get("collect").toString());
        List<Long> idList = new ArrayList<>();
        for(Object item : (List)data.get("ids")){
            idList.add(Long.valueOf(item.toString()));
        }
        if (idList.size() < 1) {
            return FollowException.MISS_IDS.get();
        }
        if (collect) {
            notificationRepo.markCollected(idList, 1);
        } else {
            notificationRepo.markCollected(idList, 0);
        }
        return Wrapper.OK;
    }

    /**
     * 删除消息
     *
     * @param data
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/msgs", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper deleteMsgs(@RequestBody Map<String, List<Long>> data) {
        if (data == null) {
            return FollowException.MISS_BODY.get();
        } else if (data.get("ids") == null || (data.get("ids")).size() < 1) {
            return FollowException.MISS_IDS.get();
        }
        notificationRepo.delete(data.get("ids"));
        return Wrapper.OK;
    }

    @RequestMapping(value = "/delMsg", method = RequestMethod.DELETE,
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper delMsg(@RequestBody List<ReqDelMsg> list) {
        if (list == null) {
            return FollowException.MISS_BODY.get();
        } else if (list.isEmpty()) {
            return FollowException.MISS_IDS.get();
        }
        Long userId = DefaultSecurityContext.getUserId();
        notificationRepo.deleteMsgs(userId,list);
        return Wrapper.OK;
    }


    @RequestMapping(value = "/readMsgs", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper readMsgs(@RequestBody ReqEditMsgs reqEditMsgs) {
        try {
            Long userId = DefaultSecurityContext.getUserId();
            notificationRepo.read(userId,reqEditMsgs);
        }catch (Exception e){
            e.printStackTrace();
            return Wrapper.ERROR;
        }
        return Wrapper.OK;
    }


    /**
     * 获取消息详情
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/msg_detail", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper msgDetail(@RequestParam("id") Long id) {
        if (id == null) {
            return FollowException.MISS_ID.get();
        }
        Notification info = notificationRepo.findById(id);
        if (info.getType() >= 201 && info.getType() <= 204 && !StringUtils.isEmpty(info.getTitle())) {
            List<String> plaintiffList = (List) info.getDetail().get("plaintiff_list");
            List<String> defendantList = (List) info.getDetail().get("defendant_list");
            List<String> litigantList = (List) info.getDetail().get("litigant_list");

            if (info.getTitle().contains("原告")) {
                info.setRole("原告");
            } else if (info.getTitle().contains("被告")) {
                info.setRole("被告");
            } else {
                String company = info.getCompany();
                if (plaintiffList != null) {
                    if (plaintiffList.contains(company)) {
                        info.setRole("原告");
                    }
                }
                if (defendantList != null) {
                    if (defendantList.contains(company)) {
                        info.setRole("被告");
                    }
                }
                if (info.getRole() == null) {
                    info.setRole("其他关联方");
                }
            }
            //其他关联方
            if (litigantList != null) {
                if (plaintiffList != null) {
                    litigantList.removeAll(plaintiffList);
                }
                if (defendantList != null) {
                    litigantList.removeAll(defendantList);
                }
                info.setLitigantList(litigantList);
            }

        }

        return Wrapper.OKBuilder.data(new NotificationVO(info)).build();
    }

    /**
     * 获取监控卡片信息
     */
    @RequestMapping(value = "/getMonitorCardList", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getMonitorCardList(@RequestParam(value = "username", required = false) String username,
                                      @RequestParam("dateType") String dateType,
                                @RequestParam(value="limit",defaultValue = "9",required = false)int limit,
                                @RequestParam(value="offset",defaultValue = "0",required = false)int offset ) {
        Long userId = null;
        if(!StringUtils.isEmpty(username))
        {
            User user = userRepo.findByUsername(username);
            if(user == null) return FollowException.USER_NOT_EXISTS.get();
            userId = user.getId();
        }
        else userId = DefaultSecurityContext.getUserId();
        if (userId == null) {
            return FollowException.MISS_ID.get();
        }
        List<MonitorCard> result = null;
        int count = 0;
        try{
            result  = notificationRepo.queryMonitorCardList(userId,dateType,limit,offset);
            count = notificationRepo.countMonitorCardList(userId);
        }catch (Exception e){
            e.printStackTrace();
            return Wrapper.ERROR;
        }

        Map<String, Object> res = new MapBuilder()
                .put("data", result)
                .put("total_count", count)
                .build();
        return Wrapper.OKBuilder.data(res).build();
    }


    /**
    * @description 获取风险和机会事件信息
    * @param groupName
    * @param type
    * @param offset
    * @param count
    * @return com.haizhi.iap.common.Wrapper
    * @author LewisLouis
    * @date 2018/9/2
    */

    @ApiOperation(value="分页查询集团机会或风险事件信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupName", value = "集团名称", dataType = "String",paramType = "query"),
            @ApiImplicitParam(name = "ruleType", value = "规则类型", dataType = "Integer",paramType = "query"),
            @ApiImplicitParam(name = "offset", value = "数据偏移", dataType = "Integer",paramType = "query"),
            @ApiImplicitParam(name = "count", value = "单页数据量", dataType = "Integer",paramType = "query")
    })
    @RequestMapping(value = "/event_info", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper findNotifyInfo(@RequestParam(value = "groupName") String groupName,
                                  @RequestParam(value = "ruleType") Integer ruleType,
                                  @RequestParam(value = "offset", required = false) Integer offset,
                                  @RequestParam(value = "count", required = false) Integer count){

        if (StringUtils.isEmpty(groupName)){
            return FollowException.MISS_COMPANY_NAME.get();
        }

        if (null == ruleType){
            return FollowException.MISS_RULE_TYPE.get();
        }
        List<String> companies = groupDetailRepo.findEntityNamesByGroupName(groupName);
        if ((null == companies) || (companies.isEmpty())){
            return FollowException.NO_COMPANIES_TYPE.get();
        }

        List<NotifyEventInfo> infos = mongoPubNotifyRepo.findEventInfoByPage(companies,ruleType,offset,count);

        for(NotifyEventInfo item : infos){
            NotificationType notificationType = NotificationType.get(item.getType());
            if (notificationType != null) {
                item.setSubTypeCnName(notificationType.getCnName());
                item.setSubTypeEnName(notificationType.getEnName());
            }

            int type = item.getType();
            if (type < 200) {
                item.setTypeCnName(NotificationVO.marketing_cn_name);
                item.setTypeEnName(NotificationVO.marketing_en_name);
            } else {
                item.setTypeCnName(NotificationVO.risk_cn_name);
                item.setTypeEnName(NotificationVO.risk_en_name);
            }

//            if (StringUtils.isEmpty(item.getTitle())) {
//                item.setTitle("一条" + item.getRuleName());
//            }

//            if (type >= 201 && type <= 204 && !StringUtils.isEmpty(item.getTitle())) {
//                if (item.getTitle().contains("原告")) {
//                    item.setRole("原告");
//                } else if (item.getTitle().contains("被告")) {
//                    item.setRole("被告");
//                }
//            }
        }


        long totalCount = mongoPubNotifyRepo.findEventInfoCount(companies,ruleType);

        Map<String, Object> res = new MapBuilder()
                .put("eventDatas", infos)
                .put("totalCount", totalCount)
                .build();
        return Wrapper.ok(res);
    }


    /**
     * @description 根据公司名称列表获取风险和机会事件信息
     * @param companyEventSearchReq
     * @return com.haizhi.iap.common.Wrapper
     * @author LewisLouis
     * @date 2018/9/2
     */
    @ApiOperation(value="根据公司名称列表分页查询集团机会或风险事件信息")
    @ApiImplicitParams({

    })
    @RequestMapping(value = "/company_event_info", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper findCompanyNotifyInfo(@RequestBody @ApiParam(name="查询条件",value="传入json格式",required=true)
                                                     CompanyEventSearchReq companyEventSearchReq){

        if (null == companyEventSearchReq){
            return FollowException.MISS_BODY.get();
        }

        if ((null == companyEventSearchReq.getCompanies()) || (companyEventSearchReq.getCompanies().isEmpty()) ){
            return FollowException.MISS_COMPANIES.get();
        }

        if (null == companyEventSearchReq.getRuleType()){
            return FollowException.MISS_RULE_TYPE.get();
        }

        List<NotifyEventInfo> infos = mongoPubNotifyRepo.findEventInfoByPage(
                companyEventSearchReq.getCompanies(),
                companyEventSearchReq.getRuleType(),
                companyEventSearchReq.getOffset(),
                companyEventSearchReq.getCount());

        for(NotifyEventInfo item : infos){
            NotificationType notificationType = NotificationType.get(item.getType());
            if (notificationType != null) {
                item.setSubTypeCnName(notificationType.getCnName());
                item.setSubTypeEnName(notificationType.getEnName());
            }

            int type = item.getType();
            if (type < 200) {
                item.setTypeCnName(NotificationVO.marketing_cn_name);
                item.setTypeEnName(NotificationVO.marketing_en_name);
            } else {
                item.setTypeCnName(NotificationVO.risk_cn_name);
                item.setTypeEnName(NotificationVO.risk_en_name);
            }

        }


        long totalCount = mongoPubNotifyRepo.findEventInfoCount(
                companyEventSearchReq.getCompanies(),companyEventSearchReq.getRuleType());

        Map<String, Object> res = new MapBuilder()
                .put("eventDatas", infos)
                .put("totalCount", totalCount)
                .build();
        return Wrapper.ok(res);
    }
}
