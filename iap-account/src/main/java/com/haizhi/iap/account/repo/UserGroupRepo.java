package com.haizhi.iap.account.repo;

import com.haizhi.iap.account.model.UserGroup;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * Created by chenbo on 17/2/8.
 */
@Slf4j
@Repository
public class UserGroupRepo {
    @Setter
    @Autowired
    JdbcTemplate template;

    private String TABLE_USER_GROUP = "user_group";

    private final RowMapper<UserGroup> USER_GROUP_ROW_MAPPER = new BeanPropertyRowMapper<>(UserGroup.class);

    public UserGroup create(UserGroup group) {
        try {
            final String sql = "insert into " + TABLE_USER_GROUP +
                    "(`name`, create_time, update_time) " +
                    "select ?, now(), now() from DUAL where not exists(select id from " + TABLE_USER_GROUP + " where `name`=?)";
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            template.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, group.getName());
                ps.setString(2, group.getName());

                return ps;
            }, holder);
            if (holder.getKey() != null) {
                group.setId(holder.getKey().longValue());
            } else {
                log.warn(TABLE_USER_GROUP + ": name={} 已存在", group.getName());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return group;
    }

    public List<UserGroup> getAll() {
        try {
            String sql = "select * from " + TABLE_USER_GROUP;
            return template.query(sql, USER_GROUP_ROW_MAPPER);
        } catch (DataAccessException ex) {
            //ex.printStackTrace();
        }
        return null;
    }

    public void delete(Long id){
        try {
            String sql = "delete from " + TABLE_USER_GROUP + " WHERE id=?";
            template.update(sql, id);
        } catch (DataAccessException ex) {
            ex.printStackTrace();
        }
    }

    public UserGroup findByName(String name) {
        try {
            String sql = "select * from " + TABLE_USER_GROUP + " where name = ? ";
            return template.queryForObject(sql, USER_GROUP_ROW_MAPPER, name);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public UserGroup findById(Long id) {
        try {
            String sql = "select * from " + TABLE_USER_GROUP + " where id = ? ";
            return template.queryForObject(sql, USER_GROUP_ROW_MAPPER, id);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public void update(UserGroup group) {
        try {
            String sql = "update " + TABLE_USER_GROUP + " " +
                    "set name=?, update_time=now() " +
                    "where id=?";

            template.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, group.getName());
                ps.setLong(2, group.getId());

                return ps;
            });
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

}
