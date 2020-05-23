package com.haizhi.iap.follow.repo;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.haizhi.iap.follow.model.FollowList;

/**
 * Created by chenbo on 17/1/11.
 */
@Slf4j
@Repository
public class FollowListRepo {

    @Setter
    @Autowired
    @Qualifier(value = "followJdbcTemplate")
    JdbcTemplate template;

    public static final String TABLE_FOLLOW_LIST = "follow_list";
    private RowMapper<FollowList> FOLLOW_LIST_ROW_MAPPER = new BeanPropertyRowMapper<>(FollowList.class);

    public FollowList findById(Long id) {
        try {
            String sql = "select * from " + TABLE_FOLLOW_LIST + " where id = ?";
            return template.queryForObject(sql, FOLLOW_LIST_ROW_MAPPER, id);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public List<FollowList> findByUserId(Long userId) {
        try {
            String sql = "select * from " + TABLE_FOLLOW_LIST + " where user_id = ? order by create_time desc";
            return template.query(sql, FOLLOW_LIST_ROW_MAPPER, userId);
        } catch (DataAccessException ex) {
            //log.error("{}", ex);
            return Collections.emptyList();
        }
    }

    public Integer sumItemCount(Long userId){
        try {
            String sql = "select sum(list_count) from " + TABLE_FOLLOW_LIST + " where user_id = ?";
            Integer count = template.queryForObject(sql, Integer.class, userId);
            return count == null ? 0 : count;
        } catch (DataAccessException ex) {
            //log.error("{}", ex);
            return 0;
        }
    }

    public Integer countByUserId(Long userId){
        try {
            String sql = "select count(1) from " + TABLE_FOLLOW_LIST + " where user_id = ?";
            Integer count = template.queryForObject(sql, Integer.class, userId);
            return count == null ? 0 : count;
        } catch (DataAccessException ex) {
            //log.error("{}", ex);
            return 0;
        }
    }

    public FollowList findByName(Long userId, String name) {
        try {
            StringBuilder sql = new StringBuilder("select * from " + TABLE_FOLLOW_LIST + " where user_id = ? and `name` = ? ");
            List<Object> args = Lists.newArrayList();

            args.add(userId);
            args.add(name);

            FollowList list = template.queryForObject(sql.toString(), FOLLOW_LIST_ROW_MAPPER, args.toArray());
            return list;
        } catch (DataAccessException ex) {
            return null;
        }
    }


    public FollowList create(FollowList list) {
        try {
            final String sql = "insert into " + TABLE_FOLLOW_LIST +
                    "(`name`, user_id, create_time, update_time) " +
                    "select ?, ?, now(), now() from DUAL " +
                    "   where not exists(select id from " + TABLE_FOLLOW_LIST + " where `name`=? and user_id=?)";
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            template.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, list.getName());
                ps.setLong(2, list.getUserId());
                ps.setString(3, list.getName());
                ps.setLong(4, list.getUserId());

                return ps;
            }, holder);
            if (holder.getKey() != null) {
                list.setId(holder.getKey().longValue());
            } else {
                log.warn(TABLE_FOLLOW_LIST + ": name={}, user_id={} 已存在", list.getName(), list.getUserId());
            }
        } catch (DataAccessException ex) {
            //log.error("{}", ex);
        }
        return list;
    }

    public void update(FollowList list) {
        try {
            final String sql = "update " + TABLE_FOLLOW_LIST + " " +
                    "set `name` = ?, update_time = now() " +
                    "where id = ?";
            template.update(sql, list.getName(), list.getId());
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

    public void delete(Long listId) {
        try {
            final String sql = "delete from " + TABLE_FOLLOW_LIST + " where id = ?";
            template.update(sql, listId);
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

    public int incListCount(Long followListId, Integer count) {
        final String sql = "update " + TABLE_FOLLOW_LIST + " set list_count = list_count + ? where id = ?";
        return template.update(sql, count, followListId);
    }

    public int decListCount(Long followListId, Integer count) {
        final String sql = "update " + TABLE_FOLLOW_LIST + " set list_count = list_count - ? where id = ?";
        return template.update(sql, count, followListId);
    }

    public int clearListCount(Long followListId) {
        final String sql = "update " + TABLE_FOLLOW_LIST + " set list_count = 0 where id = ?";
        return template.update(sql, followListId);
    }
}
