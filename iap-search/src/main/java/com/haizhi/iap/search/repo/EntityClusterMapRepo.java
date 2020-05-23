package com.haizhi.iap.search.repo;

import com.haizhi.iap.search.model.EntityClusterMap;
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
 * Created by chenbo on 2017/8/28.
 */
@Slf4j
@Repository
public class EntityClusterMapRepo {
    private String TABLE_ENTITY_CLUSTER_MAP = "entity_cluster_map";

    @Setter
    @Autowired
    JdbcTemplate template;

    private final RowMapper<EntityClusterMap> ENTITY_CLUSTER_MAP_ROW_MAPPER = new BeanPropertyRowMapper<>(EntityClusterMap.class);

    public EntityClusterMap find(Long domainId, String entityId){
        try {
            String sql = "select * from " + TABLE_ENTITY_CLUSTER_MAP + " where domain_id = ? and entity_id = ?";
            return template.queryForObject(sql, ENTITY_CLUSTER_MAP_ROW_MAPPER, domainId, entityId);
        }catch (DataAccessException ex){
            log.error("{}", ex);
        }
        return null;
    }

    public void delete(Long domainId) {
        String sql = "delete from " + TABLE_ENTITY_CLUSTER_MAP + " where domain_id = ? ";
        template.update(sql, domainId);
    }

    public int[] batchInsert(List<EntityClusterMap> entityClusterMapList) {
        if (entityClusterMapList != null && entityClusterMapList.size() < 1) {
            return null;
        }
        int[] updateNum = null;
        try {
            final String sql = "insert ignore into " + TABLE_ENTITY_CLUSTER_MAP + " " +
                    "(domain_id, entity_id, cluster_cid, create_time, update_time) " +
                    "values (?, ?, ?, now(), now()) ";
            updateNum = template.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, entityClusterMapList.get(i).getDomainId());
                    ps.setString(2, entityClusterMapList.get(i).getEntityId());
                    ps.setString(3, entityClusterMapList.get(i).getClusterCid());
                }

                @Override
                public int getBatchSize() {
                    return entityClusterMapList.size();
                }
            });
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return updateNum;
    }

}
