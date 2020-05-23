package com.haizhi.iap.mobile.service;

import com.haizhi.iap.mobile.bean.CustomerBelong;
import com.haizhi.iap.mobile.bean.FollowItem;
import com.haizhi.iap.mobile.bean.FollowList;
import com.haizhi.iap.mobile.bean.User;
import com.haizhi.iap.mobile.bean.param.FollowParam;
import com.haizhi.iap.mobile.bean.param.SearchParam;
import com.haizhi.iap.mobile.bean.result.HasMoreResult;
import com.haizhi.iap.mobile.conf.SqlSchemaConstants;
import com.haizhi.iap.mobile.exception.ExceptionStatus;
import com.haizhi.iap.mobile.repo.BasicSqlRepo;
import com.haizhi.iap.mobile.repo.FollowListRepo;
import com.haizhi.iap.mobile.repo.UserRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by thomas on 18/4/13.
 */
@Repository
public class CustomerService
{
    @Resource(name = "followItemRepo")
    private BasicSqlRepo<FollowItem> followItemRepo;

    @Autowired
    private FollowListRepo followListRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EnterpriseService enterpriseService;

    @Autowired
    private BasicSqlRepo<CustomerBelong> customerBelongRepo;

    /**
     * 返回我的客户
     *
     * @param searchParam
     * @return
     */
    public HasMoreResult myCustomers(SearchParam searchParam)
    {
        StringBuilder sqlBuilder = new StringBuilder("SELECT {{select}} FROM ").append(SqlSchemaConstants.TABLE_CUSTOMER_BELONG).append(" WHERE MGMT_CUSTM_ID = ?");
        List<Object> args = new ArrayList<>();
        args.add(searchParam.getUsername());
        if(StringUtils.isNotBlank(searchParam.getKeyword()))
        {
            sqlBuilder.append(" AND CUST_NAME LIKE ?");
            args.add(String.format("%%%s%%", searchParam.getKeyword()));
        }
        String countSql = sqlBuilder.toString().replace("{{select}}", "count(*)");
        Long cnt = customerBelongRepo.count(countSql, args.toArray());

        if(searchParam.getOffset() != null && searchParam.getOffset() >= 0 && searchParam.getSize() != null && searchParam.getSize() > 0)
        {
            sqlBuilder.append(" LIMIT ?, ?");
            args.add(searchParam.getOffset());
            args.add(searchParam.getSize());
        }
        String sql = sqlBuilder.toString().replace("{{select}}", "*");

        List<CustomerBelong> customerBelongs = customerBelongRepo.findAll(sql, args.toArray());
        Set<String> customerNames = customerBelongs.stream().map(CustomerBelong::getCustomerName).collect(Collectors.toSet());
        List<Map<String, Object>> basicInfos = enterpriseService.getBasicInfo(customerNames);

        return new HasMoreResult<>(cnt, cnt > customerNames.size(), basicInfos);
    }

    /**
     * 获取该用户所关注的企业
     *
     * @return
     */
    public HasMoreResult getFollowees(SearchParam searchParam)
    {
        User user = userRepo.findOneByName(searchParam.getUsername());
        List<Object> args = new ArrayList<>();
        List<Object> countArgs = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT {{select}} FROM ").append(SqlSchemaConstants.TABLE_FOLLOW_ITEM).append(" WHERE user_id = ? AND is_follow = 1 ");
        args.add(user.getId());
        countArgs.add(user.getId());
        if(StringUtils.isNotBlank(searchParam.getKeyword()))
        {
            sqlBuilder.append("AND company_name like ? ");
            args.add("%" + searchParam.getKeyword() + "%");
            countArgs.add("%" + searchParam.getKeyword() + "%");
        }
        if(searchParam.getOffset() != null && searchParam.getSize() != null)
        {
            sqlBuilder.append("LIMIT ?, ?");
            args.add(searchParam.getOffset());
            args.add(searchParam.getSize());
        }
        String sql = sqlBuilder.toString();
        List<FollowItem> followItems = followItemRepo.findAll(sql.replace("{{select}}", "*"), args.toArray());
        String countSql = StringUtils.substringBefore(sql, "LIMIT").replace("{{select}}", "count(*)");
        Long cnt = followItemRepo.count(countSql, countArgs.toArray());
        List<Map<String, Object>> results = Collections.emptyList();
        if(!CollectionUtils.isEmpty(followItems))
        {
            Set<String> companys = followItems.stream().map(FollowItem::getCompanyName).collect(Collectors.toSet());
            results = enterpriseService.getBasicInfo(companys);
        }
        return new HasMoreResult<>(cnt, cnt > results.size(), results);
    }

    /**
     * 关注/取关 某个企业
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int follow(FollowParam followParam)
    {
        User user = userRepo.findOneByName(followParam.getUsername());
        //找默认关注列表。若无，则创建之
        FollowList defaultFollowList = followListRepo.findDefault(user.getId());
        if(defaultFollowList == null)
        {
            followListRepo.update("INSERT IGNORE INTO {{table}}(user_id, name, list_count, create_time, update_time) VALUES(?, ?, ?, now(), now())".replace("{{table}}", SqlSchemaConstants.TABLE_FOLLOW_LIST),
                    user.getId(), FollowList.DEFAULT_NAME, 0);
            defaultFollowList = followListRepo.findOne("SELECT * FROM {{table}} WHERE user_id = ? AND name = ?".replace("{{table}}", SqlSchemaConstants.TABLE_FOLLOW_LIST), user.getId(), FollowList.DEFAULT_NAME);
        }
        if(defaultFollowList != null)
        {
            Long followListId = defaultFollowList.getId();
            //将当前公司加入关注列表
            if(followParam.getFollow())
            {
                int update1 = followItemRepo.update("INSERT IGNORE INTO {{table}}(user_id, company_name, follow_list_id, is_follow, create_time, update_time) VALUES(?, ?, ?, 1, now(), now())".replace("{{table}}", SqlSchemaConstants.TABLE_FOLLOW_ITEM),
                        user.getId(), followParam.getCompanyName(), followListId);
                int update2 = followListRepo.update("UPDATE {{table}} SET list_count = list_count + ? WHERE id = ?".replace("{{table}}", SqlSchemaConstants.TABLE_FOLLOW_LIST), 1, followListId);
                return update1 + update2;
            }
            //取消关注，将当前公司从默认关注列表中移除
            else
            {
                int update1 = followItemRepo.update("DELETE FROM {{table}} WHERE user_id = ? AND company_name = ? AND follow_list_id = ?".replace("{{table}}", SqlSchemaConstants.TABLE_FOLLOW_ITEM),
                        user.getId(), followParam.getCompanyName(), followListId);
                int update2 = followListRepo.update("UPDATE {{table}} SET list_count = list_count - ? WHERE id = ?".replace("{{table}}", SqlSchemaConstants.TABLE_FOLLOW_LIST), 1, followListId);
                return update1 + update2;
            }
        }
        return ExceptionStatus.FAIL_TO_MONITOR.getCode();
    }
}
