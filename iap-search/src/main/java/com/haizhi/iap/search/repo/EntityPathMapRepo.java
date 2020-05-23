package com.haizhi.iap.search.repo;

import com.haizhi.iap.search.model.EntityPathMap;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by chenbo on 2017/8/29.
 */
@Slf4j
@Repository
public class EntityPathMapRepo {
    private String TABLE_ENTITY_PATH_MAP = "entity_path_map";

    @Setter
    @Autowired
    JdbcTemplate template;

    private final RowMapper<EntityPathMap> ENTITY_CLUSTER_MAP_ROW_MAPPER = new BeanPropertyRowMapper<>(EntityPathMap.class);

    public List<EntityPathMap> find(Long domainId, String entity){
        try {
            String sql = "select * from " + TABLE_ENTITY_PATH_MAP + " where domain_id = ? and entity_id = ?";
            return template.query(sql, ENTITY_CLUSTER_MAP_ROW_MAPPER, domainId, entity);
        }catch (DataAccessException ex){
        }
        return null;
    }

    public void delete(Long domainId) {
        String sql = "delete from " + TABLE_ENTITY_PATH_MAP + " where domain_id = ? ";
        template.update(sql, domainId);
    }

    public int[] batchInsert(List<EntityPathMap> entityPathMapList) {
        if (entityPathMapList != null && entityPathMapList.size() < 1) {
            return null;
        }
        int[] updateNum = null;
        try {
            final String sql = "insert ignore into " + TABLE_ENTITY_PATH_MAP + " " +
                    "(domain_id, entity_id, path_id, create_time, update_time) " +
                    "values (?, ?, ?, now(), now()) ";
            updateNum = template.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, entityPathMapList.get(i).getDomainId());
                    ps.setString(2, entityPathMapList.get(i).getEntityId());
                    ps.setString(3, entityPathMapList.get(i).getPathId());
                }

                @Override
                public int getBatchSize() {
                    return entityPathMapList.size();
                }
            });
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return updateNum;
    }

}
