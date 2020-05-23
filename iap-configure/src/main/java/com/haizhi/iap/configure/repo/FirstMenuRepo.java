package com.haizhi.iap.configure.repo;

import com.haizhi.iap.configure.model.FirstMenu;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * @Author dmy
 * @Date 2017/4/6 下午5:53.
 */
@Slf4j
@Repository
public class FirstMenuRepo {

    @Setter
    @Autowired
    JdbcTemplate template;

    public static final String TABLE_FIRST_MENU = "first_menu";
    private RowMapper<FirstMenu> FIRST_MENU_ROW_MAPPER = new BeanPropertyRowMapper<>(FirstMenu.class);

    public List<FirstMenu> getAll() {
        try {
            String sql = "select * from " + TABLE_FIRST_MENU + " where status = 0";
            return template.query(sql, FIRST_MENU_ROW_MAPPER);
        } catch (DataAccessException ex) {
            return Collections.emptyList();
        }
    }

    public FirstMenu findByName(String name) {
        try {
            String sql = "select * from " + TABLE_FIRST_MENU + " where `name` = ? and status = 0";
            return template.queryForObject(sql, FIRST_MENU_ROW_MAPPER, name);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public FirstMenu create(FirstMenu firstMenu) {
        try {
            final String sql = "insert into " + TABLE_FIRST_MENU +
                    "(`name`, `order`, create_time, update_time) " +
                    "select ?, ?, now(), now() from DUAL where not exists(select id from " + TABLE_FIRST_MENU +
                    " where `name` = ? and status = 0)";
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            template.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, firstMenu.getName());
                ps.setInt(2, firstMenu.getOrder());
                ps.setString(3, firstMenu.getName());

                return ps;
            }, holder);
            if (holder.getKey() != null) {
                firstMenu.setId(holder.getKey().longValue());
            } else {
                log.warn("{}", firstMenu);
            }
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
        return firstMenu;
    }


    public void batchDelete(List<Long> fmIds) {
        if (fmIds.size() == 0)
            return;

        try {
            String sql = "update " + TABLE_FIRST_MENU + " set status = 1 where id = ?";
            template.batchUpdate(sql, new BatchPreparedStatementSetter() {
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, fmIds.get(i));
                }

                public int getBatchSize() {
                    return fmIds.size();
                }
            });

        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }

    }


    public void batchUpdate(List<FirstMenu> firstMenus) {
        if (firstMenus == null || firstMenus.size() == 0) {
            return;
        }

        try {
            String sql = "update " + TABLE_FIRST_MENU + " " +
                    "set `name` = ?, `order` = ?, update_time = now() " +
                    "where id = ?";
            template.batchUpdate(sql, new BatchPreparedStatementSetter() {

                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setString(1, firstMenus.get(i).getName());
                    ps.setInt(2, firstMenus.get(i).getOrder());
                    ps.setLong(3, firstMenus.get(i).getId());
                }

                public int getBatchSize() {
                    return firstMenus.size();
                }
            });

        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

    public FirstMenu getById(Long id) {
        try {
            String sql = "select * from " + TABLE_FIRST_MENU + " where id = ? and status = 0";
            return template.queryForObject(sql, FIRST_MENU_ROW_MAPPER, id);
        } catch (DataAccessException ex) {
            return null;
        }
    }
}
