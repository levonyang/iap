package com.haizhi.iap.configure.repo;

import com.google.common.base.Strings;
import com.haizhi.iap.configure.model.Param;
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
 * @Date 2017/5/8 下午7:01.
 */
@Slf4j
@Repository
public class ParamRepo {

    @Setter
    @Autowired
    JdbcTemplate template;

    public static final String TABLE_PARAM = "param";
    private RowMapper<Param> PARAM_ROW_MAPPER = new BeanPropertyRowMapper<>(Param.class);

    public Param getByComponentId(Long componentId) {
        try {
            String sql = "select * from " + TABLE_PARAM + " where component_id = ? and status = 0";
            return template.queryForObject(sql, PARAM_ROW_MAPPER, componentId);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public Param getParamByDataAndComponent(Long datasourceid, Long componentId) {
        try {
            String sql = "select * from " + TABLE_PARAM + " where datasource_id = ? and component_id = ? and status = 0" ;
            return template.queryForObject(sql, PARAM_ROW_MAPPER, datasourceid, componentId);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public List<Param> findByFirstMenu(Long firstMenuId) {
        try {
            String sql = "select * from " + TABLE_PARAM + " where first_menu_id = ? and status = 0";
            return template.query(sql, PARAM_ROW_MAPPER, firstMenuId);
        } catch (DataAccessException ex) {
            return Collections.EMPTY_LIST;
        }
    }

    public Param create(Param param) {
        try {
            final String sql = "insert into " + TABLE_PARAM +
                    "(datasource_id, component_id, order_key, is_desc, is_order, first_menu_id, status, create_time, update_time) " +
                    "select ?, ?, ?, ?, ?, ?, 0, now(), now() from DUAL where not exists(select id from " + TABLE_PARAM +
                    " where datasource_id = ? and component_id = ? and status=0)";
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            template.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, param.getDatasourceId());
                ps.setLong(2, param.getComponentId());
                ps.setObject(3, param.getOrderKey());
                ps.setObject(4, param.getIsDesc() == null ? 1 : param.getIsDesc());
                ps.setObject(5, param.getIsOrder() == null ? 0 : param.getIsOrder());
                ps.setLong(6, param.getFirstMenuId());
                ps.setLong(7, param.getDatasourceId());
                ps.setLong(8, param.getComponentId());

                return ps;
            }, holder);
            if (holder.getKey() != null) {
                param.setId(holder.getKey().longValue());
            } else {
                log.warn("{} 未插入", param);
            }
        } catch (DataAccessException ex) {
            log.error("{}", ex.getMessage());
        }
        return param;
    }

    public void batchDelete(List<Long> paramIds) {
        if(paramIds.size() == 0)
            return;

        try {
            String sql = "update " + TABLE_PARAM + " set status = 1 where id = ?";
            template.batchUpdate(sql, new BatchPreparedStatementSetter() {
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, paramIds.get(i));
                }

                public int getBatchSize() {
                    return paramIds.size();
                }
            });

        } catch (DataAccessException ex) {
            log.error(ex.getMessage());
        }
    }


    public void batchUpdate(List<Param> params) {
        if (params == null || params.size() == 0){
            return;
        }

        try {
            String sql = "update " + TABLE_PARAM + " " +
                    " set datasource_id = ?, order_key = ?, is_desc = ?, is_order = ?, update_time = now() " +
                    "where id = ?";
            template.batchUpdate(sql, new BatchPreparedStatementSetter() {

                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, params.get(i).getDatasourceId());
                    ps.setObject(2, params.get(i).getOrderKey());
                    ps.setObject(3, params.get(i).getIsDesc() == null ? 1 : params.get(i).getIsDesc());
                    ps.setObject(4, params.get(i).getIsOrder() == null ? 0 : params.get(i).getIsOrder());
                    ps.setLong(5, params.get(i).getId());
                }

                public int getBatchSize() {
                    return params.size();
                }
            });

        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

}
