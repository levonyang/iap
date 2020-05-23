package com.haizhi.iap.configure.repo;

import com.google.common.collect.Lists;
import com.haizhi.iap.configure.model.Component;
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
 * @Date 2017/4/5 下午3:51.
 */
@Slf4j
@Repository
public class ComponentRepo {
    @Setter
    @Autowired
    JdbcTemplate template;

    public static final String TABLE_COMPONENT = "component";
    private RowMapper<Component> COMPONENT_ROW_MAPPER = new BeanPropertyRowMapper<>(Component.class);

    public Component getComponent(Long secondMenuId) {
        try {
            String sql = "select * from " + TABLE_COMPONENT + " where second_menu_id = ? and status = 0" ;
            return template.queryForObject(sql, COMPONENT_ROW_MAPPER, secondMenuId);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public Component getComponentById(Long id) {
        try {
            String sql = "select * from " + TABLE_COMPONENT + " where id = ? and status = 0" ;
            return template.queryForObject(sql, COMPONENT_ROW_MAPPER, id);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public List<Component> getAll() {
        try {
            String sql = "select * from " + TABLE_COMPONENT + " and status = 0";
            return template.query(sql, COMPONENT_ROW_MAPPER);
        } catch (DataAccessException ex) {
            return Collections.emptyList();
        }
    }

    public Component create(Component component) {
        try {
            final String sql = "insert into " + TABLE_COMPONENT +
                    "(`name`, first_menu_id, second_menu_id, `type`, datasource_ids, page_count, is_page, create_time, update_time) " +
                    "select ?, ?, ?, ?, ?, ?, ?, now(), now() from DUAL where not exists(select id from " + TABLE_COMPONENT +
                    " where second_menu_id = ? and status=0)";
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            template.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, component.getName());
                ps.setLong(2, component.getFirstMenuId());
                ps.setLong(3, component.getSecondMenuId());
                ps.setInt(4, component.getType());
                ps.setString(5, component.getDatasourceIds());
                ps.setObject(6, component.getPageCount() == null ? null : component.getPageCount());
                ps.setObject(7, component.getIsPage() == null ? 0 : component.getIsPage());
                ps.setLong(8, component.getSecondMenuId());

                return ps;
            }, holder);
            if (holder.getKey() != null) {
                component.setId(holder.getKey().longValue());
            } else {
                log.warn("{} 未插入", component);
            }
        } catch (DataAccessException ex) {
            log.error("{}", ex.getMessage());
        }
        return component;
    }

    public void batchDelete(List<Long> mcIds) {
        if(mcIds.size() == 0)
            return;

        try {
            String sql = "update " + TABLE_COMPONENT + " set status = 1 where id = ?";
            template.batchUpdate(sql, new BatchPreparedStatementSetter() {
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, mcIds.get(i));
                }

                public int getBatchSize() {
                    return mcIds.size();
                }
            });

        } catch (DataAccessException ex) {
            log.error(ex.getMessage());
        }
    }


    public void batchUpdate(List<Component> components) {
        if (components == null || components.size() == 0){
            return;
        }

        try {
            String sql = "update " + TABLE_COMPONENT + " " +
                    " set `name` = ?, datasource_ids = ?, page_count = ?, is_page = ?, update_time = now() " +
                    "where id = ?";
            template.batchUpdate(sql, new BatchPreparedStatementSetter() {

                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setString(1, components.get(i).getName());
                    ps.setString(2, components.get(i).getDatasourceIds());
                    ps.setObject(3, components.get(i).getPageCount() == null ? null : components.get(i).getPageCount());
                    ps.setObject(4, components.get(i).getIsPage() == null? 0 : components.get(i).getIsPage());
                    ps.setLong(5, components.get(i).getId());
                }

                public int getBatchSize() {
                    return components.size();
                }
            });

        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

    public List<Component> findByFirstMenu(Long fistMenuId) {
        try{
            String sql = "select * from " + TABLE_COMPONENT + " where first_menu_id = ? and status = 0";
            return template.query(sql, COMPONENT_ROW_MAPPER, fistMenuId);
        }catch (DataAccessException ex){
            return Collections.EMPTY_LIST;
        }
    }

    public List<Component> findBySourceConfigId(Long sourceConfigId) {
        try{
            String sql = "select * from " + TABLE_COMPONENT + " where datasource_ids = ? and status = 0";
            return template.query(sql, COMPONENT_ROW_MAPPER, sourceConfigId);
        }catch (DataAccessException ex){
            return Lists.newArrayList();
        }
    }
}
