package com.haizhi.iap.mobile.service;

import com.haizhi.iap.mobile.bean.MarketEventSetting;
import com.haizhi.iap.mobile.bean.Notification;
import com.haizhi.iap.mobile.bean.User;
import com.haizhi.iap.mobile.bean.param.MarketEventSearchParam;
import com.haizhi.iap.mobile.bean.param.MarketEventSettingParam;
import com.haizhi.iap.mobile.bean.result.HasMoreResult;
import com.haizhi.iap.mobile.conf.SqlSchemaConstants;
import com.haizhi.iap.mobile.enums.MarketEventType;
import com.haizhi.iap.mobile.repo.BasicSqlRepo;
import com.haizhi.iap.mobile.repo.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by thomas on 18/4/19.
 */
@Service
@Slf4j
public class MarketEventService
{
    @Autowired
    private BasicSqlRepo<Notification> notificationRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private BasicSqlRepo<MarketEventSetting> marketEventSettingRepo;

    /**
     * 查找营销事件
     *
     * @param searchParam
     * @return
     */
    public HasMoreResult marketEvent(MarketEventSearchParam searchParam)
    {
        User user = userRepo.findOneByName(searchParam.getUsername());
        StringBuilder sqlBuilder = new StringBuilder("SELECT {{select}} FROM ").append(SqlSchemaConstants.TABLE_NOTIFICATION).append(" WHERE user_id = :userId AND `delete` = 0");
        Map<String, Object> namedParams = new HashMap<>();
        namedParams.put("userId", user.getId());
        if(!CollectionUtils.isEmpty(searchParam.getCompanyNames()))
        {
            sqlBuilder.append(" AND company IN (:companyNames)");
            namedParams.put("companyNames", searchParam.getCompanyNames());
        }
        if(StringUtils.isNotBlank(searchParam.getKeyword()))
        {
            sqlBuilder.append(" AND detail LIKE :keyword");
            namedParams.put("keyword", String.format("%%%s%%", searchParam.getKeyword()));
        }

        //哪些事件类型被允许推送
        Map<String, MarketEventSetting> enabledEventSettingMap = new HashMap<>();
        HasMoreResult<List<MarketEventSetting>> result = findMarketEventSetting(searchParam.getUsername());
        if(result != null && !CollectionUtils.isEmpty(result.getResults()))
            result.getResults().stream().filter(MarketEventSetting::isEnable).forEach(eventSetting -> {
                enabledEventSettingMap.put(eventSetting.getName(), eventSetting);
            });
        Set<Integer> types = null;

        if(!CollectionUtils.isEmpty(enabledEventSettingMap))
        {
            if(searchParam.getFilter() != null && !CollectionUtils.isEmpty(searchParam.getFilter().getEventTypes()))
            {
                types = searchParam.getFilter().getEventTypes().stream().filter(eventType -> enabledEventSettingMap.keySet().contains(eventType.name())).flatMap(eventType -> eventType.getTypes().stream()).collect(Collectors.toSet());
            }
            else
            {
                types = enabledEventSettingMap.values().stream().flatMap(eventSetting -> eventSetting.getTypesAsList().stream()).collect(Collectors.toSet());
            }
        }
        if(!CollectionUtils.isEmpty(types))
        {
            sqlBuilder.append(" AND type IN (:types)");
            namedParams.put("types", types);
        }

        String countSql = sqlBuilder.toString().replace("{{select}}", "count(*)");
        Long cnt = notificationRepo.count(countSql, namedParams);
        //sort
        if(!CollectionUtils.isEmpty(searchParam.getSorts()))
        {
            List<String> orderSqls = searchParam.getSorts().stream().map(sort -> String.format("%s %s", sort.getField(), sort.getDirection().name())).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(orderSqls))
                sqlBuilder.append(" ORDER BY ").append(StringUtils.join(orderSqls, ", "));
        }
        //limit
        if(searchParam.getOffset() != null && searchParam.getOffset() >= 0 && searchParam.getSize() != null && searchParam.getSize() > 0)
        {
            sqlBuilder.append(" LIMIT :offset, :size");
            namedParams.put("offset", searchParam.getOffset());
            namedParams.put("size", searchParam.getSize());
        }

        /*System.out.println(sqlBuilder.toString());
        for (String s : namedParams.keySet()) {
            System.out.println(namedParams.get(s));
        }*/

        List<Notification> notifications = notificationRepo.findAll(sqlBuilder.toString().replace("{{select}}", "*"), namedParams);
        return new HasMoreResult<>(cnt, cnt > notifications.size(), notifications);
    }

    /**
     * 获取营销事件设置
     *
     * @return
     */
    public HasMoreResult<List<MarketEventSetting>> findMarketEventSetting(String username)
    {
        User user = userRepo.findOneByName(username);
        List<MarketEventSetting> marketEventSettings = marketEventSettingRepo.findAll("SELECT * FROM " + SqlSchemaConstants.TABLE_MARKET_EVENT_SETTING + " WHERE user_id = ?", user.getId());
        //setting表中没有出现的事件类型，默认为enable
        Map<String, MarketEventSetting> settingMap = new HashMap<>();
        marketEventSettings.forEach(marketEventSetting -> settingMap.put(marketEventSetting.getName(), marketEventSetting));
        Arrays.stream(MarketEventType.values()).filter(marketEventType -> !settingMap.containsKey(marketEventType.name())).map(marketEventType -> {
            return new MarketEventSetting(null, user.getId(), marketEventType, true);
        }).forEach(marketEventSettings::add);
        return new HasMoreResult<>(marketEventSettings.size(), false, marketEventSettings);
    }

    /**
     * 更新营销事件设置
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMarketEventSetting(MarketEventSettingParam settingParam)
    {
        if(CollectionUtils.isEmpty(settingParam.getSettings())) return;
        User user = userRepo.findOneByName(settingParam.getUsername());
        String sql = null;

        Map<String, Boolean> eventSettingMap = new HashMap<>();
        //previous settings
        HasMoreResult<List<MarketEventSetting>> result = findMarketEventSetting(settingParam.getUsername());
        if(result != null && !CollectionUtils.isEmpty(result.getResults()))
        {
            result.getResults().forEach(eventSetting -> eventSettingMap.put(eventSetting.getName(), eventSetting.isEnable()));
            sql = "DELETE FROM " + SqlSchemaConstants.TABLE_MARKET_EVENT_SETTING + " WHERE user_id = ?";
            marketEventSettingRepo.update(sql, user.getId());
        }
        eventSettingMap.putAll(settingParam.getSettings());

        sql = "INSERT IGNORE INTO " + SqlSchemaConstants.TABLE_MARKET_EVENT_SETTING + "(user_id, name, types, description, enable) VALUES {{values}}";
        List<String> exprList = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        eventSettingMap.forEach((name, enable) -> {
            MarketEventType marketEventType = MarketEventType.getMarketEventType(name);
            exprList.add("(?, ?, ?, ?, ?)");
            args.addAll(Arrays.asList(user.getId(), name, marketEventType.getTypesAsString(), marketEventType.getDescription(), enable ? 1 : 0));
        });
        String valueExpr = StringUtils.join(exprList, ", ");
        marketEventSettingRepo.update(sql.replace("{{values}}", valueExpr), args.toArray());
    }
}
