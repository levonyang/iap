package com.haizhi.iap.configure.repo;

import com.haizhi.iap.configure.model.SecondMenu;
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
 * @Date 2017/4/7 下午5:05.
 */
@Slf4j
@Repository
public class SecondMenuRepo{

    @Setter
    @Autowired
    JdbcTemplate template;

    public static final String TABLE_SECOND_MENU = "second_menu";
    private RowMapper<SecondMenu> SECOND_MENU_ROW_MAPPER = new BeanPropertyRowMapper<>(SecondMenu.class);

    public List<SecondMenu> getSecondMenus(Long firstMenuId) {
        try {
            String sql = "select * from " + TABLE_SECOND_MENU + " where first_menu_id = ? and status = 0" ;
            return template.query(sql, SECOND_MENU_ROW_MAPPER, firstMenuId);
        } catch (DataAccessException ex) {
            return Collections.emptyList();
        }
    }


    public List<SecondMenu> getAll() {
        try {
            String sql = "select * from " + TABLE_SECOND_MENU + " where status = 0";
            return template.query(sql, SECOND_MENU_ROW_MAPPER);
        } catch (DataAccessException ex) {
            return Collections.emptyList();
        }
    }

    public SecondMenu getById(Long secondMenuId){
        try {
            String sql = "select * from " + TABLE_SECOND_MENU + " where id = ? and status = 0";
            return template.queryForObject(sql, SECOND_MENU_ROW_MAPPER, secondMenuId);
        } catch (DataAccessException ex) {
            return null;
        }
    }


    public SecondMenu create(SecondMenu secondMenu) {
        try {
            final String sql = "insert into " + TABLE_SECOND_MENU +
                    "(`name`, first_menu_id, `order`, create_time, update_time) " +
                    "select ?, ?, ?, now(), now() from DUAL where not exists(select id from " + TABLE_SECOND_MENU +
                    " where `name` = ? and first_menu_id = ? and status = 0)";
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            template.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, secondMenu.getName());
                ps.setLong(2, secondMenu.getFirstMenuId());
                ps.setInt(3, secondMenu.getOrder());
                ps.setString(4, secondMenu.getName());
                ps.setLong(5, secondMenu.getFirstMenuId());

                return ps;
            }, holder);
            if (holder.getKey() != null) {
                secondMenu.setId(holder.getKey().longValue());
            } else {
                log.warn("{} 未插入", secondMenu);
            }
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
        return secondMenu;
    }


    public void batchDelete(List<Long> smIds) {
        if(smIds.size() == 0)
            return;

        try {
            String sql = "update " + TABLE_SECOND_MENU + " set status =1 where id = ?";
            template.batchUpdate(sql, new BatchPreparedStatementSetter() {
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, smIds.get(i));
                }

                public int getBatchSize() {
                    return smIds.size();
                }
            });

        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }


    public void batchUpdate(List<SecondMenu> secondMenus) {
        if(secondMenus == null || secondMenus.size() == 0){
            return;
        }

        try {
            String sql = "update " + TABLE_SECOND_MENU + " " +
                    "set `name` = ?, `order` = ?, update_time = now() " +
                    "where id = ?";
            template.batchUpdate(sql, new BatchPreparedStatementSetter() {

                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setString(1, secondMenus.get(i).getName());
                    ps.setInt(2, secondMenus.get(i).getOrder());
                    ps.setLong(3, secondMenus.get(i).getId());
                }

                public int getBatchSize() {
                    return secondMenus.size();
                }
            });

        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

    public SecondMenu findByNameAndFirst(String name, Long firstMenuId) {
        try{
            String sql = "select * from " + TABLE_SECOND_MENU + " where `name` = ? and first_menu_id = ? and status = 0";
            return template.queryForObject(sql, SECOND_MENU_ROW_MAPPER, name, firstMenuId);
        }catch (DataAccessException ex){

        }
        return null;
    }
}
