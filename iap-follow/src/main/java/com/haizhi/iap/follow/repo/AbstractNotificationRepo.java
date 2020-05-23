package com.haizhi.iap.follow.repo;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.follow.enums.DataType;
import com.haizhi.iap.follow.model.MonitorCard;
import com.haizhi.iap.follow.model.ReqDelMsg;
import com.haizhi.iap.follow.model.ReqEditMsgs;
import com.haizhi.iap.follow.model.ReqGetMsgs;
import com.haizhi.iap.follow.model.notification.Notification;
import com.haizhi.iap.follow.utils.DateUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 17/5/3.
 */
@Slf4j
public abstract class AbstractNotificationRepo<T extends Notification> {

    @Setter
    @Autowired
    @Qualifier(value = "followJdbcTemplate")
    JdbcTemplate template;

    @Setter
    @Autowired
    ObjectMapper objectMapper;

    private String TABLE_NOTIFICATION = "notification";

    public List<T> findRisk(Long userId, Integer read, Integer collected, Integer offset, Integer count) {
        try {
            StringBuffer buffer = new StringBuffer("select * from " + TABLE_NOTIFICATION + " where `delete` = 0 and type > 200 ");
            List<Object> args = Lists.newArrayList();

            if (userId != null) {
                buffer.append(" and user_id = ? ");
                args.add(userId);
            }

            buffer.append(getConditionByLTOneMonth());

            if (read != null) {
                buffer.append(" and `read` = ? ");
                args.add(read);
            }

            if (collected != null) {
                buffer.append(" and collected = ? ");
                args.add(collected);
            }

            buffer.append(" order by push_time desc ");

            if (offset != null && count != null) {
                buffer.append(" limit ?, ? ");
                args.add(offset);
                args.add(count);
            }
            return template.query(buffer.toString(), new NotificationRowMapper(), args.toArray());
        } catch (DataAccessException ex) {

        }
        return Collections.EMPTY_LIST;
    }

    public List<T> findMarketing(Long userId, Integer read, Integer collected, Integer offset, Integer count) {
        try {
            StringBuffer buffer = new StringBuffer("select * from " + TABLE_NOTIFICATION + " where `delete` = 0 and type < 200 ");
            List<Object> args = Lists.newArrayList();

            if (userId != null) {
                buffer.append(" and user_id = ? ");
                args.add(userId);
            }

            buffer.append(getConditionByLTOneMonth());

            if (read != null) {
                buffer.append(" and `read` = ? ");
                args.add(read);
            }

            if (collected != null) {
                buffer.append(" and collected = ? ");
                args.add(collected);
            }

            buffer.append(" order by push_time desc ");

            if (offset != null && count != null) {
                buffer.append(" limit ?, ? ");
                args.add(offset);
                args.add(count);
            }
            return template.query(buffer.toString(), new NotificationRowMapper(), args.toArray());
        } catch (DataAccessException ex) {

        }
        return Collections.EMPTY_LIST;
    }

    public List<Pair<Integer, Integer>> countUnreadGroupByType(Long userId, Integer filterType) {
        StringBuffer buffer = new StringBuffer("select `type`, count(1) as cnt from " + TABLE_NOTIFICATION + " where `delete` = 0 and user_id = ? and `read` = 0");
        buffer.append(getConditionByLTOneMonth());
        if (filterType != null && filterType >= 0) {
            if (filterType == 0) {
                buffer.append(" and `type` < 200");
            } else if (filterType == 1) {
                buffer.append(" and `type` > 200");
            }
        }
        buffer.append(" group by `type`");
        return template.query(buffer.toString(), (rs, rowNum) -> {
            int type = rs.getInt("type");
            int cnt = rs.getInt("cnt");
            return Pair.of(type, cnt);
        }, userId);
    }

    public Long countRisk(Long userId) {
        try {
            StringBuffer sql = new StringBuffer("select count(1) from " + TABLE_NOTIFICATION + " where `delete` = 0 and user_id = ? and type > 200");
            /**
             *  取最近一个月数据 start
             */
            sql.append(getConditionByLTOneMonth());

            return template.queryForObject(sql.toString(), Long.class, userId);
        } catch (DataAccessException ex) {

        }
        return 0l;
    }

    public Long countRisk(Long userId, Integer read) {
        if (userId == null || read != null && read != 0 && read != 1) {
            return 0l;
        }
        try {
            StringBuffer buffer = new StringBuffer("select count(1) from " + TABLE_NOTIFICATION + " where `delete` = 0 and user_id = ? and `type` > 200 ");
            buffer.append(getConditionByLTOneMonth());
            List<Object> args = Lists.newArrayList();
            args.add(userId);
            if (read != null) {
                buffer.append(" and `read` = ? ");
                args.add(read);
            }
            return template.queryForObject(buffer.toString(), Long.class, args.toArray());
        } catch (DataAccessException ex) {
            //log.error("{}", ex);
        }
        return 0l;
    }

    /**
     * statusRead or statusCollected:
     * 1 : 已读或 已收藏
     * 0 ：未读 或 未收藏
     * -1 ：忽略
     *
     * @author zhangjunfei
     */
    public Long countReadOrCollected(Long userId, String ruleType, Integer statusRead, Integer statusCollected) {
        if (userId == null) {
            return 0l;
        }
        StringBuffer sql = new StringBuffer("select count(1) from " + TABLE_NOTIFICATION +
                " where `delete` = 0 and user_id = ?");

        try {
            if ("RISK".equals(ruleType)) {
                sql.append(" and `type` > 200 ");
            } else if ("MARKETING".equals(ruleType)) {
                sql.append(" and `type` < 200 ");
            }
            sql.append(getConditionByLTOneMonth());
            List<Object> args = Lists.newArrayList();
            args.add(userId);
            if (statusRead != null && statusRead != -1) {
                sql.append(" and `read` = ? ");
                args.add(statusRead);
            }
            if (statusCollected != null && statusCollected != -1) {
                sql.append(" and collected = ? ");
                args.add(statusCollected);
            }
            return template.queryForObject(sql.toString(), Long.class, args.toArray());
        } catch (DataAccessException ex) {
            //log.error("{}", ex);
        }
        return 0l;
    }


    public Long countMarketing(Long userId) {
        try {
            StringBuffer sql = new StringBuffer("select count(1) from " + TABLE_NOTIFICATION + " where `delete` = 0 and user_id = ? and `type` < 200 and `type` > 0");

            /**
             *  取最近一个月数据 start
             */
            sql.append(getConditionByLTOneMonth());

            return template.queryForObject(sql.toString(), Long.class, userId);
        } catch (DataAccessException ex) {

        }
        return 0l;
    }

    public Long countMarketing(Long userId, Integer read) {
        if (userId == null || read != null && read != 0 && read != 1) {
            return 0l;
        }
        try {
            StringBuffer buffer = new StringBuffer("select count(1) from " + TABLE_NOTIFICATION + " where `delete` = 0 and user_id = ? and `type` < 200 and `type` > 0");
            List<Object> args = Lists.newArrayList();
            args.add(userId);
            buffer.append(getConditionByLTOneMonth());
            if (read != null) {
                buffer.append(" and `read` = ? ");
                args.add(read);
            }
            return template.queryForObject(buffer.toString(), Long.class, args.toArray());
        } catch (DataAccessException ex) {
            //log.error("{}", ex);
        }
        return 0l;
    }

    public T findById(Long id) {
        try {
            String sql = "select * from " + TABLE_NOTIFICATION + " where id = ?";
            return template.queryForObject(sql, new NotificationRowMapper(), id);
        } catch (DataAccessException ex) {

        }
        return null;
    }

    public List<T> findByCondition(Long userId, Integer type, Integer read, Integer collected,
                                   Integer offset, Integer count) {
        try {
            StringBuffer buffer = new StringBuffer("select * from " + TABLE_NOTIFICATION + " where `delete` = 0  ");
            List<Object> args = Lists.newArrayList();

            if (userId != null) {
                buffer.append(" and user_id = ? ");
                args.add(userId);
            }

            buffer.append(getConditionByLTOneMonth());

            if (type != null) {
                buffer.append(" and type = ? ");
                args.add(type);
            }

            if (read != null) {
                buffer.append(" and `read` = ? ");
                args.add(read);
            }

            if (collected != null) {
                buffer.append(" and collected = ? ");
                args.add(collected);
            }

            buffer.append(" order by push_time desc ");

            if (offset != null && count != null) {
                buffer.append(" limit ?, ? ");
                args.add(offset);
                args.add(count);
            }

            return template.query(buffer.toString(), new NotificationRowMapper(), args.toArray());
        } catch (DataAccessException ex) {
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * for closelyNotification
     *
     * @param userId
     * @param reqGetMsgs
     * @return
     */
    public List<T> queryByCondition(Long userId, ReqGetMsgs reqGetMsgs) {
        try {
            StringBuilder sql = new StringBuilder(
                    "select id,user_id,title,rule_name, `level`,`desc`, company,master_company,type,`read`,collected,push_time,detail,closely from "
                            + TABLE_NOTIFICATION + " where `delete` = 0   ");
            List<Object> args = Lists.newArrayList();

            if (userId != null) {
                sql.append(" and user_id = ? ");
                args.add(userId);
            }
            addConditions(sql, args, reqGetMsgs);

            sql.append(" order by push_time desc ");
            sql.append(" limit ?, ? ");
            args.add(reqGetMsgs.getOffset());
            args.add(reqGetMsgs.getCount());

            return template.query(sql.toString(), new NotificationRowMapper(), args.toArray());
        } catch (DataAccessException ex) {
            //log.error("queryByCondition 时报错！", ex);
        }
        return Collections.EMPTY_LIST;
    }

    private void addConditions(StringBuilder sql, List<Object> args, ReqGetMsgs reqGetMsgs) {
        //for version2.5 start
        if (reqGetMsgs.getCollected() == null || !reqGetMsgs.getCollected()) {
            sql.append(" and `delete` = false ");
        }
        //for version2.5 end

        if (DataType.MONTH.equals(reqGetMsgs.getDataType())) {
            sql.append(getConditionByLTOneMonth());
        }
        if (reqGetMsgs.isClosely() == null && reqGetMsgs.getCompany() != null) {
            sql.append(" and type > 0 ");//for version2.5
            sql.append(" and (master_company = ? or company = ?) ");
            args.add(reqGetMsgs.getCompany());
            args.add(reqGetMsgs.getCompany());
        } else if (reqGetMsgs.isClosely() == null && reqGetMsgs.getCompany() == null) {
            sql.append(" and type > 0 ");//for version2.5
        } else {
            if (!StringUtils.isEmpty(reqGetMsgs.getMasterCompany())) {
                sql.append(" and master_company = ? ");
                args.add(reqGetMsgs.getMasterCompany());
            }

            if (!StringUtils.isEmpty(reqGetMsgs.getCompany())) {
                sql.append(" and company like ? ");
                args.add("%" + reqGetMsgs.getCompany() + "%");
            }
        }

        if ("RISK".equalsIgnoreCase(reqGetMsgs.getType())) {
            sql.append(" and type > 200 ");
        } else if ("MARKETING".equalsIgnoreCase(reqGetMsgs.getType())) {
            sql.append(" and type < 200 and type > 0 ");
        }
        if (reqGetMsgs.getSubType().size() > 1) {
            sql.append(" and type in (");
            for (Integer subType : reqGetMsgs.getSubType()) {
                sql.append("?,");
                args.add(subType);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        } else if (reqGetMsgs.getSubType().size() == 1) {
            sql.append(" and type = ? ");
            args.add(reqGetMsgs.getSubType().get(0));
        }

        if (reqGetMsgs.getRead() != null) {
            sql.append(" and `read` = ? ");
            args.add(reqGetMsgs.getRead());
        }

        if (reqGetMsgs.getCollected() != null) {
            sql.append(" and collected = ? ");
            args.add(reqGetMsgs.getCollected());
        }

        if (reqGetMsgs.isClosely() != null) {
            sql.append(" and closely = ? ");
            args.add(reqGetMsgs.isClosely());
        }

        if (DataType.ALL.equals(reqGetMsgs.getDataType()) && !StringUtils.isEmpty(reqGetMsgs.getPushTime())) {
            sql.append(" and push_time >= ?");
            args.add(reqGetMsgs.getPushTime());
        } else if (!StringUtils.isEmpty(reqGetMsgs.getPushTime())) {
            sql.append(" and push_time >= ? and push_time <= ?");
            String timeStr = reqGetMsgs.getPushTime().split(" ")[0];
            args.add(timeStr + " 00:00:00");
            args.add(timeStr + " 23:59:59");
        }
    }

    public Integer getCountByCondition(Long userId, ReqGetMsgs reqGetMsgs) {
        try {
            StringBuilder sql = new StringBuilder("select count(1) from " + TABLE_NOTIFICATION + " where `delete` = 0   ");
            List<Object> args = Lists.newArrayList();

            if (userId != null) {
                sql.append(" and user_id = ? ");
                args.add(userId);
            }
            addConditions(sql, args, reqGetMsgs);

            Integer count = template.queryForObject(sql.toString(), Integer.class, args.toArray());
            if (count != null) {
                return count;
            }
        } catch (Exception e) {
            //log.error("getCountByCondition 时报错！", e);
        }
        return 0;
    }

    public Long countByCondition(Long userId, Integer type, Integer read, Integer collected) {
        try {
            StringBuffer buffer = new StringBuffer("select count(1) from " + TABLE_NOTIFICATION + " where `delete` = 0  ");
            List<Object> args = Lists.newArrayList();

            if (userId != null) {
                buffer.append(" and user_id = ? ");
                args.add(userId);
            }

            buffer.append(getConditionByLTOneMonth());

            if (type != null) {
                buffer.append(" and type = ? ");
                args.add(type);
            }

            if (read != null) {
                buffer.append(" and `read` = ? ");
                args.add(read);
            }

            if (collected != null) {
                buffer.append(" and collected = ? ");
                args.add(collected);
            }

            Long count = template.queryForObject(buffer.toString(), Long.class, args.toArray());
            if (count != null) {
                return count;
            }
        } catch (DataAccessException ex) {
        }
        return 0l;
    }

    public void create(List<T> notifications) {
        if (notifications == null || notifications.size() < 1) {
            return;
        }

        String sql = "insert into " + TABLE_NOTIFICATION + "(user_id, title, company, `type`, `read`, collected, detail, push_time) " +
                " values(?, ?, ?, ?, 0, 0, ?, ?)";
        template.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                T notify = notifications.get(i);
                ps.setLong(1, notify.getUserId());
                if (notify.getTitle() != null) {
                    ps.setString(2, notify.getTitle());
                } else {
                    ps.setString(2, "");
                }
                ps.setString(3, notify.getCompany());
                ps.setInt(4, notify.getType());
                if (notify.getDetail() != null) {
                    try {
                        ps.setString(5, objectMapper.writeValueAsString(notify.getDetail()));
                    } catch (JsonProcessingException e) {
                        log.error("{}", e);
                    }
                } else {
                    ps.setString(5, "");
                }
                if (notify.getPushTime() != null) {
                    ps.setDate(6, new java.sql.Date(notify.getPushTime().getTime()));
                } else {
                    ps.setDate(6, new java.sql.Date(System.currentTimeMillis()));
                }
            }

            @Override
            public int getBatchSize() {
                return notifications.size();
            }
        });
    }

    public void read(List<Long> ids) {
        if (ids == null || ids.size() < 1) {
            return;
        }
        try {
            String sql = "update " + TABLE_NOTIFICATION + " set `read` = 1 where id = ? ";
            template.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, ids.get(i));
                }

                @Override
                public int getBatchSize() {
                    return ids.size();
                }
            });
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

    public void markCollected(List<Long> ids, Integer status) {
        if (ids == null || ids.size() < 1) {
            return;
        }
        try {
            String sql = "update " + TABLE_NOTIFICATION + " set collected = ? where id = ? ";
            template.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setInt(1, status);
                    ps.setLong(2, ids.get(i));
                }

                @Override
                public int getBatchSize() {
                    return ids.size();
                }
            });
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

    public void delete(List<Long> ids) {
        if (ids == null || ids.size() < 1) {
            return;
        }
        try {
            String sql = "delete from " + TABLE_NOTIFICATION + " where id = ? ";
            template.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, ids.get(i));
                }

                @Override
                public int getBatchSize() {
                    return ids.size();
                }
            });
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

    public void deleteMsgs(Long userId, List<ReqDelMsg> list) {
        try {
            List<Map> allMergePushTime = new ArrayList<>();
            List<Map> allCloselyPushTime = new ArrayList<>();

            for (ReqDelMsg req : list) {
                if (req.getSubType() != null && req.getSubType() == -1) {
                    Map item = new HashMap();
                    item.put("pushTime", req.getPushTime().split(" ")[0]);
                    item.put("company", req.getCompany());
                    allMergePushTime.add(item);
                } else if (req.isClosely() != null && req.isClosely()) {
                    Map item = new HashMap();
                    item.put("pushTime", req.getPushTime().split(" ")[0]);
                    item.put("company", req.getCompany());
                    allCloselyPushTime.add(item);
                }
            }

            String sql = "delete from " + TABLE_NOTIFICATION + " where id = ? ";
            template.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, list.get(i).getId());
                }

                @Override
                public int getBatchSize() {
                    return list.size();
                }
            });

            if (!allMergePushTime.isEmpty()) {
                String allMergeSql = "delete from " + TABLE_NOTIFICATION +
                        " where user_id = ? and closely = true and push_time > ? and push_time < ? and master_company = ?";
                template.batchUpdate(allMergeSql, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, userId);
                        String dateStr = allMergePushTime.get(i).get("pushTime").toString();
                        ps.setString(2, dateStr + " 00:00:00");
                        ps.setString(3, dateStr + " 23:59:59");
                        ps.setString(4, allMergePushTime.get(i).get("company").toString());
                    }

                    @Override
                    public int getBatchSize() {
                        return allMergePushTime.size();
                    }
                });
            }

            if (!allCloselyPushTime.isEmpty()) {
                String closelyCountSql = "select count(1) from " + TABLE_NOTIFICATION +
                        " where `delete` = 0 and user_id = ? and closely = true and push_time > ? and push_time < ? and master_company = ?";

                String deleteMergeSql = "delete from " + TABLE_NOTIFICATION +
                        " where user_id = ? and type = -1 and push_time > ? and push_time < ? and company = ?";

                String updateMergeSql = "update " + TABLE_NOTIFICATION + " set title = ?" +
                        " where user_id = ? and type = -1 and push_time > ? and push_time < ? and company = ?";

                String selectTitleSql = "select title from " + TABLE_NOTIFICATION +
                        " where `delete` = 0 and user_id = ? and closely = true and push_time > ? and push_time < ? and master_company = ? order by push_time desc limit 1";

                for (Map item : allCloselyPushTime) {
                    int count = template.queryForObject(closelyCountSql, Integer.class,
                            userId, item.get("pushTime") + " 00:00:00",
                            item.get("pushTime") + " 23:59:59", item.get("company"));
                    if (count == 0) {
                        template.update(deleteMergeSql, userId, item.get("pushTime") + " 00:00:00",
                                item.get("pushTime") + " 23:59:59", item.get("company"));
                    } else if (count > 0) {
                        String title = template.queryForObject(selectTitleSql, String.class,
                                userId, item.get("pushTime") + " 00:00:00",
                                item.get("pushTime") + " 23:59:59", item.get("company"));
                        JSONObject titleJSON = new JSONObject();
                        titleJSON.put("count", count);
                        titleJSON.put("title", title);

                        template.update(updateMergeSql,
                                titleJSON.toString(), userId, item.get("pushTime") + " 00:00:00",
                                item.get("pushTime") + " 23:59:59", item.get("company"));
                    }
                }
            }
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }

    }

    private void doDeleteOrUpdate(String sql, ReqEditMsgs reqEditMsgs) {
        try {
            template.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, reqEditMsgs.getIdList().get(i));
                }

                @Override
                public int getBatchSize() {
                    return reqEditMsgs.getIdList().size();
                }
            });
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

    public void read(Long userId, ReqEditMsgs reqEditMsgs) {
        StringBuilder sql = new StringBuilder("update " + TABLE_NOTIFICATION + " set `read` = true where 1 = 1");
        if (reqEditMsgs.getIdList().isEmpty() ||
                (reqEditMsgs.isAllEdit() != null && reqEditMsgs.isAllEdit())) {
            sql.append(" and user_id = ? ");
            readAll(sql, userId, reqEditMsgs);
            return;
        }
        sql.append(" and id = ? ");
        doDeleteOrUpdate(sql.toString(), reqEditMsgs);
    }

    private void readAll(StringBuilder sql, Long userId, ReqEditMsgs reqEditMsgs) {
        List<Object> args = Lists.newArrayList();
        args.add(userId);
        addCondition(sql, args, reqEditMsgs);
        template.update(sql.toString(), args.toArray());
    }

    private void addCondition(StringBuilder sql, List<Object> args, ReqEditMsgs reqEditMsgs) {
        if (reqEditMsgs.getCollected() != null) {
            sql.append(" and collected = ?");
            args.add(reqEditMsgs.getCollected());
        }
        if (reqEditMsgs.isClosely() != null) {
            sql.append(" and closely = ?");
            args.add(reqEditMsgs.isClosely());
        }
        if ("risk".equalsIgnoreCase(reqEditMsgs.getType())) {
            sql.append(" and type > 200");
        } else if ("marketing".equalsIgnoreCase(reqEditMsgs.getType())) {
            sql.append(" and type < 200 and type > 0");
        }
        if (!reqEditMsgs.getSubType().isEmpty()) {
            sql.append(" and type in (");
            for (Integer subType : reqEditMsgs.getSubType()) {
                sql.append(subType + ",");
                args.add(subType);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(")");
        }
    }

    public void readAll(Long userId) {
        try {
            String sql = "update " + TABLE_NOTIFICATION + " set `read` = 1 where user_id = ?";
            template.update(sql, userId);
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

    public List<MonitorCard> queryMonitorCardList(Long userId, String dateType, int limit, int offset) {
        String sqlCompany = "select * from( select company_name company, max(monitor_time) time, risk_notify, marketing_notify, closely_risk_notify, closely_marketing_notify from follow_item " +
                " where  user_id = ? and (risk_notify = true or marketing_notify = true or closely_risk_notify = true or closely_marketing_notify = true) " +
                " group by company_name,risk_notify, marketing_notify, closely_risk_notify, closely_marketing_notify ) a order by a.time desc";

        List<MonitorCard> monitorCardList = this.template.query(sqlCompany.toString(), new RowMapper<MonitorCard>() {
            @Override
            public MonitorCard mapRow(ResultSet rs, int rowNum) throws SQLException {
                MonitorCard monitorCard = new MonitorCard();
                monitorCard.setCompany(rs.getString("company"));
                monitorCard.setTime(rs.getString("time"));
                monitorCard.setRiskNotify(rs.getInt("risk_notify") == 1);
                monitorCard.setMarketingNotify(rs.getInt("marketing_notify") == 1);
                monitorCard.setCloselyRiskNotify(rs.getInt("closely_risk_notify") == 1);
                monitorCard.setCloselyMarketingNotify(rs.getInt("closely_marketing_notify") == 1);
                return monitorCard;
            }
        }, userId);

        if (monitorCardList != null && monitorCardList.size() > 0) {
            List<Object> sqlCountArgs = Lists.newArrayList();

            String sqlCount = buildSQLByMonitorCount(monitorCardList, sqlCountArgs, userId, dateType);

            this.template.query(sqlCount, new RowMapper<MonitorCard>() {
                @Override
                public MonitorCard mapRow(ResultSet rs, int rowNum) throws SQLException {
                    MonitorCard monitorCard = monitorCardList.get(rowNum);
                    monitorCard.setRisk(
                            monitorCard.isRiskNotify() ? rs.getInt("risk") : 0);
                    monitorCard.setMarketing(
                            monitorCard.isMarketingNotify() ? rs.getInt("marketing") : 0);
                    monitorCard.setCloselyRisk(
                            monitorCard.isCloselyRiskNotify() ? rs.getInt("closelyRisk") : 0);
                    monitorCard.setCloselyMarketing(
                            monitorCard.isCloselyMarketingNotify() ? rs.getInt("closelyMarketing") : 0);
                    return monitorCard;
                }
            }, sqlCountArgs.toArray());
        }

        if (monitorCardList != null && !monitorCardList.isEmpty()) {
            int toIndex = limit + offset;
            if (toIndex > monitorCardList.size()) {
                toIndex = monitorCardList.size();
            }

            return monitorCardList.subList(offset, toIndex);
        }
        return null;
    }

    public int countMonitorCardList(Long userId) {
        String sqlCompany =
                "select count(1) from ( select distinct(company_name) from follow_item where user_id = ? and (risk_notify = true or marketing_notify = true or closely_risk_notify = true or closely_marketing_notify = true) ) a";
        int count = 0;
        try {
            count = template.queryForObject(sqlCompany.toString(), Integer.class, userId);
        } catch (Exception e) {
            //log.error("", e);
            count = 0;
        }
        return count;
    }

    private String buildSQLByMonitorCount(List<MonitorCard> monitorCardList, List<Object> args,
                                          Long userId, String dateType) {
        String sqlNotification = "select count(1) from " + TABLE_NOTIFICATION + " where `delete` = 0 and user_id =? and company = ? and closely = false";
        String sqlNotificationClosely = "select count(1) from " + TABLE_NOTIFICATION + " where `delete` = 0 and user_id =? and master_company = ?";
        StringBuilder sqlCount = new StringBuilder();

        for (MonitorCard item : monitorCardList) {
            sqlCount.append(" select ? company, (")
                    .append(sqlNotification + " and type > 200 ");
            args.add(item.getCompany());
            args.add(userId);
            args.add(item.getCompany());
            if (DataType.MONTH.equals(dateType)) {
                sqlCount.append(getConditionByLTOneMonth());
            } else {
                sqlCount.append(" and push_time >= ?");
                args.add(item.getTime());
            }
            sqlCount.append(" ) as risk ,(" + sqlNotification + " and type > 0 and type < 200 ");
            args.add(userId);
            args.add(item.getCompany());
            if (DataType.MONTH.equals(dateType)) {
                sqlCount.append(getConditionByLTOneMonth());
            } else {
                sqlCount.append(" and push_time >= ?");
                args.add(item.getTime());
            }
            sqlCount.append(" ) as marketing , (" + sqlNotificationClosely + " and closely = true and type > 200 ");
            args.add(userId);
            args.add(item.getCompany());
            if (DataType.MONTH.equals(dateType)) {
                sqlCount.append(getConditionByLTOneMonth());
            } else {
                sqlCount.append(" and push_time >= ?");
                args.add(item.getTime());
            }
            sqlCount.append(" ) as closelyRisk , (" + sqlNotificationClosely + " and closely = true and type > 0 and type < 200 ");
            args.add(userId);
            args.add(item.getCompany());
            if (DataType.MONTH.equals(dateType)) {
                sqlCount.append(getConditionByLTOneMonth());
            } else {
                sqlCount.append(" and push_time >= ?");
                args.add(item.getTime());
            }
            sqlCount.append(" ) as closelyMarketing  union all");
        }
        if (sqlCount.length() > 0) {
            sqlCount.delete(sqlCount.length() - 10, sqlCount.length());
        }
        return sqlCount.toString();
    }

    private class NotificationRowMapper implements RowMapper<T> {
        @Override
        public T mapRow(ResultSet resultSet, int i) throws SQLException {
            T notification = getNotificationInstance(resultSet.getInt("type"));
            if (notification == null) {
                throw new ServiceAccessException(-1, "不支持的type: " + resultSet.getInt("type"));
            }
            notification.setId(resultSet.getLong("id"));
            notification.setUserId(resultSet.getLong("user_id"));
            notification.setCompany(resultSet.getString("company"));
            notification.setMasterCompany(resultSet.getString("master_company"));
            notification.setLevel(resultSet.getString("level"));
            notification.setDesc(resultSet.getString("desc"));
            notification.setRuleName(resultSet.getString("rule_name"));
            notification.setType(resultSet.getInt("type"));
            notification.setRead(resultSet.getInt("read"));
            notification.setCollected(resultSet.getInt("collected"));
            notification.setPushTime(new Date(resultSet.getDate("push_time").getTime()));
            notification.setTitle(resultSet.getString("title"));
            notification.setIsClosely(resultSet.getBoolean("closely"));
            try {
                if (!StringUtils.isEmpty(resultSet.getString("detail"))) {
                    notification.setDetail((Map<String, Object>) objectMapper.readValue(resultSet.getString("detail"), HashMap.class));
                }
            } catch (Exception e) {
                log.error("字段detail属性解析错误, id: {}, {}", notification.getId(), e);
                throw new ServiceAccessException(-1, String.format("字段detail属性解析错误, id:%s", notification.getId()));
            }

            return notification;
        }
    }

    public abstract T getNotificationInstance(Integer type);

    /**
     * @author zhangjunfei
     */
    private String getConditionByLTOneMonth() {
        return String.format(" and push_time > '%s'", DateUtils.getOneMonthBefore());
    }
}
