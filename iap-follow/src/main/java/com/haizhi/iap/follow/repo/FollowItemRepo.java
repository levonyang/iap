package com.haizhi.iap.follow.repo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.common.auth.DefaultSecurityContext;
import com.haizhi.iap.follow.model.FollowItem;
import com.haizhi.iap.follow.utils.CompanyUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 17/1/10.
 */
@Slf4j
@Repository
public class FollowItemRepo {
    @Setter
    @Autowired
    @Qualifier(value = "followJdbcTemplate")
    JdbcTemplate template;

    @Setter
    @Autowired
    FollowListRepo followListRepo;

    public static final String TABLE_FOLLOW_ITEM = "follow_item";
    public static final Integer DEFAULT_COUNT = 10;
    private RowMapper<FollowItem> FOLLOW_ITEM_MAPPER = new BeanPropertyRowMapper<>(FollowItem.class);

    public List<FollowItem> findByName(Long userId, String companyName) {
        return findByCondition(userId, null, companyName, null, null, null,
                null, null,true);
    }

    public List<FollowItem> findByName(Long userId, String companyName,Boolean isFollow) {
        return findByCondition(userId, null, companyName, null, null, null,
                null, null,isFollow);
    }

    public FollowItem findById(Long id) {
        try {
            String sql = "select * from " + TABLE_FOLLOW_ITEM + " where id = ?";
            return template.queryForObject(sql, FOLLOW_ITEM_MAPPER, id);
        } catch (DataAccessException ex) {
            //log.error("{}", ex);
            return null;
        }
    }

    public FollowItem create(FollowItem item) {
        try {
            final String sql = "insert ignore into " + TABLE_FOLLOW_ITEM +
                    "(`user_id`, company_name, follow_list_id, is_exists_in, create_time, update_time) " +
                    "VALUES (?, ?, ?, ?, now(), now())";
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            int upNum = template.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, item.getUserId());
                ps.setString(2, CompanyUtil.ignoreENBrackets(item.getCompanyName()));
                ps.setLong(3, item.getFollowListId());
                ps.setInt(4, item.getIsExistsIn());

                return ps;
            }, holder);
            if (holder.getKey() != null) {
                item.setId(holder.getKey().longValue());
            } else {
                log.warn(TABLE_FOLLOW_ITEM + ": company_name={}, follow_list_id={} 已存在", item.getCompanyName(), item.getFollowListId());
            }
            if (upNum > 0) {
                followListRepo.incListCount(item.getFollowListId(), 1);
            }
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
        return item;
    }

    public List<FollowItem> findByList(Long followListId) {
        return findByCondition(null, followListId, null, null, null, null, null, null,null);
    }

    public List<FollowItem> findByUserAndList(Long userId, Long listId, Integer offset, Integer count) {
        return findByCondition(userId, listId, null, null, offset, count, null, null,null);
    }

    public List<FollowItem> findByCondition(Long userId, Long followListId, String companyName, Boolean isExistsIn,
                                            Integer offset, Integer count, Integer marketingNotify, Integer riskNotify,
                                            Boolean isFollow) {
        List<FollowItem> followItems = Lists.newArrayList();
        try {
            StringBuffer buffer = new StringBuffer("select * from " + TABLE_FOLLOW_ITEM + " where true ");
            List<Object> args = Lists.newArrayList();

            if (userId != null) {
                buffer.append(" and user_id = ? ");
                args.add(userId);
            }

            if (followListId != null) {
                buffer.append(" and follow_list_id = ? ");
                args.add(followListId);
            }

            if (companyName != null) {
                buffer.append(" and company_name = ? ");
                args.add(companyName);
            }

            if (isExistsIn != null) {
                buffer.append(" and is_exists_in = ? ");
                args.add(isExistsIn);
            }

            if (marketingNotify != null) {
                buffer.append(" and marketing_notify = ? ");
                args.add(marketingNotify);
            }

            if (riskNotify != null) {
                buffer.append(" and risk_notify = ? ");
                args.add(riskNotify);
            }

            if(isFollow != null){
                buffer.append(" and is_follow  = ? ");
                args.add(isFollow);
            }
            buffer.append(" order by create_time desc ");
            if (offset != null) {
                if (count == null) {
                    count = DEFAULT_COUNT;
                }
                buffer.append(" limit ?, ? ");
                args.add(offset);
                args.add(count);
            }

          
            
            return template.query(buffer.toString(), FOLLOW_ITEM_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
            //log.error(ex.getMessage());
        }
        return followItems;
    }

    public Long countByCondition(Long userId, Long followListId, String companyName, Boolean isExistsIn,
                                 Integer marketingNotify, Integer riskNotify,Boolean isFollow) {
        List<FollowItem> followItems = Lists.newArrayList();
        try {
            StringBuffer buffer = new StringBuffer("select count(1) from " + TABLE_FOLLOW_ITEM + " where true ");
            List<Object> args = Lists.newArrayList();

            if (userId != null) {
                buffer.append(" and user_id = ? ");
                args.add(userId);
            }

            if (followListId != null) {
                buffer.append(" and follow_list_id = ? ");
                args.add(followListId);
            }

            if (companyName != null) {
                buffer.append(" and company_name = ? ");
                args.add(companyName);
            }

            if (isExistsIn != null) {
                buffer.append(" and is_exists_in = ? ");
                args.add(isExistsIn);
            }

            if (marketingNotify != null) {
                buffer.append(" and marketing_notify = ? ");
                args.add(marketingNotify);
            }

            if (riskNotify != null) {
                buffer.append(" and risk_notify = ? ");
                args.add(riskNotify);
            }

            if(isFollow != null){
                buffer.append(" and is_follow  = ? ");
                args.add(isFollow);
            }

            return template.queryForObject(buffer.toString(), Long.class, args.toArray());
        } catch (DataAccessException ex) {
            //log.error(ex.getMessage());
        }
        return 0l;
    }

    public int[] batchInsert(List<FollowItem> items) {
        if (items != null && items.size() < 1) {
            return null;
        }
        int[] updateNum = null;
        try {
            final String sql = "insert into "+TABLE_FOLLOW_ITEM+
                    " (user_id, company_name, follow_list_id,is_exists_in,is_follow,create_time,update_time) values (" +
                    "?, ?, ?, ?, true,now(), now())" +
                    "   on duplicate key update\n" +
                    "        user_id = ?,company_name = ?,follow_list_id = ? ,is_exists_in = ?,is_follow = true,create_time=now()";

            updateNum = template.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Long userID = items.get(i).getUserId();
                    String company = CompanyUtil.ignoreENBrackets(items.get(i).getCompanyName());
                    Long followListID = items.get(i).getFollowListId();
                    int isExist = items.get(i).getIsExistsIn();

                    ps.setLong(1, userID);
                    ps.setString(2, company);
                    ps.setLong(3, followListID);
                    ps.setInt(4, isExist);

                    ps.setLong(5, userID);
                    ps.setString(6, company);
                    ps.setLong(7, followListID);
                    ps.setInt(8, isExist);
                }

                @Override
                public int getBatchSize() {
                    return items.size();
                }
            });
            Map<Long, Integer> countMap = Maps.newHashMap();
            for (int i = 0; i < updateNum.length; i++) {
                if (updateNum[i] > 0) {
                    Long listId = items.get(i).getFollowListId();
                    Integer cnt = countMap.get(listId);
                    if (cnt == null) {
                        countMap.put(listId, 1);
                    } else {
                        countMap.put(listId, cnt + 1);
                    }
                }
            }
            for (Map.Entry<Long, Integer> one : countMap.entrySet()) {
                followListRepo.incListCount(one.getKey(), one.getValue());
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return updateNum;
    }

    public void delete(Long itemId) {
        try {
            String sql = "select follow_list_id from " + TABLE_FOLLOW_ITEM + " where id = ?";
            Long followListId = template.queryForObject(sql, new Object[]{itemId}, Long.class);
            if (followListId != null) { //计数减一
                followListRepo.decListCount(followListId, 1);
            }
        } catch (Exception ex) {
            //log.error(ex.getMessage(), ex);
        }
        try {
            String sql = "update " + TABLE_FOLLOW_ITEM + " set is_follow = false where id = ?";
            template.update(sql, itemId);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    public void deleteByListAndName(Long listId, String companyName) {
        int delSuc = 0;
        try {
            String sql = "update " + TABLE_FOLLOW_ITEM +
                    " set is_follow = false where follow_list_id = ? and company_name = ? and user_id = ? and is_follow = true";
            delSuc = template.update(sql, listId, companyName,DefaultSecurityContext.getUserId());
            if (delSuc > 0) {
                followListRepo.decListCount(listId, 1);
            }
        } catch (DataAccessException ex) {
            log.error(ex.getMessage());
        }
    }

    public void deleteByList(Long listId) {
        try {
            String sql = "update " + TABLE_FOLLOW_ITEM + " set is_follow = false where follow_list_id = ? and user_id = ?";
            template.update(sql, listId, DefaultSecurityContext.getUserId());
            followListRepo.clearListCount(listId);
        } catch (DataAccessException ex) {
            log.error(ex.getMessage());
        }
    }

    public void setNotify(Long userId, String type, List<String> companies, Integer tag) {
        try {
            StringBuffer buffer = new StringBuffer("update " + TABLE_FOLLOW_ITEM + " ");
            if (type.equals("risk")) {
                buffer.append(" set risk_notify = ? ");
            } else if (type.equals("marketing")) {
                buffer.append(" set marketing_notify = ? ");
            } else {
                return;
            }
            buffer.append(" where user_id = ? and company_name = ?");
            template.batchUpdate(buffer.toString(), new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setInt(1, tag);
                    ps.setLong(2, userId);
                    ps.setString(3, companies.get(i).toString());
                }

                @Override
                public int getBatchSize() {
                    return companies.size();
                }
            });
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

    public boolean isMonitor(Long userID,String company){
        String sql = "select count(1) from "+TABLE_FOLLOW_ITEM +
                " where user_id = ? and company_name = ? and (risk_notify = true or marketing_notify = true or closely_risk_notify = true or closely_marketing_notify = true)";
        int count = template.queryForObject(sql,Integer.class,userID,company);
        if(count == 0){
            return false;
        }
        return true;
    }

    /**
     * 监控通知设置
     */
    public void setNotify(Long userID,Map<String,Object> data){
        try{
            StringBuilder sql = new StringBuilder("update " + TABLE_FOLLOW_ITEM + " set update_time = now()");
            Map<Integer,String> paramIndex = new HashMap<>();
            Integer index = 1;
            if(!StringUtils.isEmpty(data.get("risk_notify"))){
                sql.append(" ,risk_notify = ? ");
                paramIndex.put(index,"risk_notify");
                index++;
            }
            if(!StringUtils.isEmpty(data.get("marketing_notify"))){
                sql.append(" ,marketing_notify = ? ");
                paramIndex.put(index,"marketing_notify");
                index++;
            }
            if(!StringUtils.isEmpty(data.get("closely_risk_notify"))){
                sql.append(" ,closely_risk_notify = ? ");
                paramIndex.put(index,"closely_risk_notify");
                index++;
            }
            if(!StringUtils.isEmpty(data.get("closely_marketing_notify"))){
                sql.append(" ,closely_marketing_notify = ? ");
                paramIndex.put(index,"closely_marketing_notify");
                index++;
            }
            if(!StringUtils.isEmpty(data.get("follow"))){
                sql.append(" ,is_follow = ? ");
                paramIndex.put(index,"follow");
                index++;
            }
            if(data.get("closely_rule") != null){
                sql.append(" ,closely_rule = ? ");
                paramIndex.put(index,"closely_rule");
            }
            if(data.get("monitor_time") != null){
                sql.append(" ,monitor_time = now() ");
            }
            sql.append(" where company_name = ? and user_id = ? ");
            template.update(sql.toString(), new PreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps) throws SQLException {
                    Iterator<Integer> indexIte = paramIndex.keySet().iterator();
                    Integer psIndex = null;
                    int i = 1;
                    Object value = null;
                    while(indexIte.hasNext()){
                        psIndex = indexIte.next();
                        value = data.get(paramIndex.get(psIndex));
                        if(value instanceof Boolean){
                            ps.setBoolean(psIndex,(Boolean)value);
                        }else{
                            ps.setString(psIndex,(String)value);
                        }
                        i++;
                    }
                    ps.setString(i,(String)data.get("company"));
                    i++;
                    ps.setLong(i,userID);
                }
            });
        }catch (Exception e){
            log.error("监控通知设置错误! ",e);
        }
    }

    /**
     * ifnull and insert
     */
    public int ifNullAndAddNotify(Long userID,Map<String,Object> data){
        StringBuilder sql = new StringBuilder("insert into "+TABLE_FOLLOW_ITEM+"(");
        sql.append("user_id,company_name,risk_notify,marketing_notify,closely_risk_notify,closely_marketing_notify,is_follow,closely_rule,create_time,update_time ) (");
        sql.append("select ?,?,?,?,?,?,?,?,now(),now() from dual where not exists (");
        sql.append("select * from "+TABLE_FOLLOW_ITEM+" where user_id = ? and company_name = ? ) )");
        List<Object> args = Lists.newArrayList();
        args.add(userID);
        args.add(data.get("company"));

        addBooleanConditions(args,"risk_notify",data);
        addBooleanConditions(args,"marketing_notify",data);
        addBooleanConditions(args,"closely_risk_notify",data);
        addBooleanConditions(args,"closely_marketing_notify",data);
        addBooleanConditions(args,"follow",data);

        args.add(data.get("closely_rule"));
        args.add(userID);
        args.add(data.get("company"));

        return template.update(sql.toString(),args.toArray());
    }

    private void addBooleanConditions(List<Object> args,String field,Map<String,Object> data){
        Object value = data.get(field);
        if(StringUtils.isEmpty(value)){
            args.add(false);
        }else{
            args.add(value);
        }
    }

    /**
     * 开启监控时，判断数量。
     * 可能是取消监控后重新监控，
     * 可能是已监控，又修改了监控配置
     * 可能是第一次监控
     * @param userID
     * @param company
     * @return boolean
     * @author caochao
     * @Date 2018/9/5
     */
    public boolean checkMonitorLegality(Long userID, String company) {
        //已经监控
        if (isMonitor(userID, company)) {
            return countMonitorCompany(userID) <= 10;
        } else {
            return countMonitorCompany(userID) < 10;
        }
    }

    private Long countMonitorCompany(Long userID){
        //String sqlIsExist = "select count(1) from follow_item where user_id = ? and company_name = ?";
        //Boolean isExist = template.queryForObject(sqlIsExist,Integer.class,userID,company) != 0;

        String sql = "select count(1) from ( select distinct(company_name) from follow_item where user_id = ? and (risk_notify = true or marketing_notify = true or closely_risk_notify = true or closely_marketing_notify = true) ) a";
        //已监控的数量
        return template.queryForObject(sql,Long.class,userID);
    }

    /**
     * 判断该用户是否开启
     * @return
     */
    public boolean isFollowOpen(Long userID, String type, Boolean closely) {
        String field = null;
        if ("RISK".equalsIgnoreCase(type)) {
            if (closely != null && closely == true) {
                field = "closely_risk_notify";
            } else {
                field = "risk_notify";
            }
        } else {
            if (closely != null && closely == true) {
                field = "closely_marketing_notify";
            } else {
                field = "marketing_notify";
            }
        }
        String sql = "select id from follow_item where user_id = ? and " + field + " = true limit 1";
        List<Long> list = this.template.queryForList(sql, Long.TYPE, userID);
        if (list != null && !list.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean checkMonitor(Long userID) {
        String sql = "SELECT id FROM follow_item WHERE user_id = ? AND (risk_notify = TRUE OR marketing_notify = TRUE OR closely_risk_notify = TRUE OR closely_marketing_notify = TRUE)";

        List<Long> list = this.template.queryForList(sql, Long.TYPE, userID);
        if (list != null && !list.isEmpty()) {
            return true;
        }
        return false;
    }
}
