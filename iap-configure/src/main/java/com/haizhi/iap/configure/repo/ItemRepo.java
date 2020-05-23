package com.haizhi.iap.configure.repo;

import com.haizhi.iap.configure.model.Item;
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
 * @Date 2017/4/11 下午2:38.
 */
@Slf4j
@Repository
public class ItemRepo {

    @Setter
    @Autowired
    JdbcTemplate template;

    public static final String TABLE_ITEM = "item";
    private RowMapper<Item> ITEM_ROW_MAPPER = new BeanPropertyRowMapper<>(Item.class);

    public List<Item> getItems(Long componentId) {
        try {
            String sql = "select * from " + TABLE_ITEM + " where component_id = ? and status = 0";
            return template.query(sql, ITEM_ROW_MAPPER, componentId);
        } catch (DataAccessException ex) {
            return Collections.emptyList();
        }
    }

    public List<Item> getAll() {
        try {
            String sql = "select * from " + TABLE_ITEM + " and status = 0";
            return template.query(sql, ITEM_ROW_MAPPER);
        } catch (DataAccessException ex) {
            return Collections.emptyList();
        }
    }


    public Item create(Item item) {
        try {
            final String sql = "insert into " + TABLE_ITEM +
                    "(`name`, first_menu_id, component_id, source_field_id, x, y, col_space, row_space, type, ele_type, count_form, create_time, update_time)" +
                    " select ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), now() from DUAL where not exists(select id from " + TABLE_ITEM +
                    " where `name` = ? and component_id = ? and type = ? and status = 0)";
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            template.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, item.getName());
                ps.setLong(2, item.getFirstMenuId());
                ps.setLong(3, item.getComponentId());
                ps.setLong(4, item.getSourceFieldId());
                ps.setInt(5, item.getX());
                ps.setInt(6, item.getY());
                ps.setInt(7, item.getColSpace());
                ps.setInt(8, item.getRowSpace());
                ps.setInt(9, item.getType());
                ps.setInt(10, item.getEleType());
                ps.setObject(11, item.getCountForm() == null ? null : item.getCountForm());
                ps.setString(12, item.getName());
                ps.setLong(13, item.getComponentId());
                ps.setInt(14, item.getType());

                return ps;
            }, holder);
            if (holder.getKey() != null) {
                item.setId(holder.getKey().longValue());
            } else {
                log.error("{} 未插入", item);
            }
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
        return item;
    }


    public void batchDelete(List<Long> itemIds) {
        if (itemIds.size() == 0)
            return;

        try {
            String sql = "update " + TABLE_ITEM + " set status = 1 where id = ?";
            template.batchUpdate(sql, new BatchPreparedStatementSetter() {

                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, itemIds.get(i));
                }

                public int getBatchSize() {
                    return itemIds.size();
                }
            });

        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }


    public void batchUpdate(List<Item> items) {
        if (items == null || items.size() == 0) {
            return;
        }

        try {
            String sql = "update " + TABLE_ITEM + " " +
                    "set `name` = ?, source_field_id = ?, x = ?, y = ?, col_space = ?, row_space = ?, update_time = now(), type = ?, ele_type = ?, count_form = ? " +
                    "where id = ?";
            template.batchUpdate(sql, new BatchPreparedStatementSetter() {

                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setString(1, items.get(i).getName());
                    ps.setLong(2, items.get(i).getSourceFieldId());
                    ps.setInt(3, items.get(i).getX());
                    ps.setInt(4, items.get(i).getY());
                    ps.setInt(5, items.get(i).getColSpace());
                    ps.setInt(6, items.get(i).getRowSpace());
                    ps.setInt(7, items.get(i).getType());
                    ps.setInt(8, items.get(i).getEleType());
                    ps.setObject(9, items.get(i).getCountForm() == null ? null : items.get(i).getCountForm());
                    ps.setObject(10, items.get(i).getId());
                }

                public int getBatchSize() {
                    return items.size();
                }
            });

        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

    public List<Item> findByFirstMenu(Long firstMenuId) {
        try {
            String sql = "select * from " + TABLE_ITEM + " where first_menu_id = ? and status = 0";
            return template.query(sql, ITEM_ROW_MAPPER, firstMenuId);
        } catch (DataAccessException ex) {
            return Collections.EMPTY_LIST;
        }
    }

    public Item findByNameAndComponent(String name, Long componentId) {
        try {
            String sql = "select * from " + TABLE_ITEM + " where `name` = ? and component_id = ? and status = 0";
            return template.queryForObject(sql, ITEM_ROW_MAPPER, name, componentId);
        } catch (DataAccessException ex) {

        }
        return null;
    }
}
