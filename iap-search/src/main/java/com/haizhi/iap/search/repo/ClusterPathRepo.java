package com.haizhi.iap.search.repo;

import com.google.common.collect.Lists;
import com.haizhi.iap.search.model.ClusterPath;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/8/29.
 */
@Slf4j
@Repository
public class ClusterPathRepo {
    private String TABLE_CLUSTER_PATH = "cluster_path";

    @Setter
    @Autowired
    JdbcTemplate template;

    private final RowMapper<ClusterPath> CLUSTER_PATH_ROW_MAPPER = new BeanPropertyRowMapper<>(ClusterPath.class);

    public List<ClusterPath> group(Long domainId) {
        try {
            String sql = "select `type`, count(1) as `count` from cluster_path where domain_id = ? group by `type`";
            return template.query(sql, CLUSTER_PATH_ROW_MAPPER, domainId);
        } catch (DataAccessException ex) {

        }
        return Collections.EMPTY_LIST;
    }

    public int[] batchInsert(List<ClusterPath> clusterPathList) {
        if (clusterPathList != null && clusterPathList.size() < 1) {
            return null;
        }
        int[] updateNum = null;
        try {
            final String sql = "insert ignore into " + TABLE_CLUSTER_PATH + " " +
                    "(domain_id, path_id, `type`, paths, create_time, update_time) " +
                    "values (?, ?, ?, ?, now(), now()) ";
            updateNum = template.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, clusterPathList.get(i).getDomainId());
                    ps.setString(2, clusterPathList.get(i).getPathId());
                    ps.setString(3, clusterPathList.get(i).getType());
                    ps.setString(4, clusterPathList.get(i).getPaths());
                }

                @Override
                public int getBatchSize() {
                    return clusterPathList.size();
                }
            });
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return updateNum;
    }

    public void delete(Long domainId) {
        String sql = "delete from " + TABLE_CLUSTER_PATH + " where domain_id = ? ";
        template.update(sql, domainId);
    }

    public ClusterPath findById(Long id) {
        try {
            String sql = "select * from " + TABLE_CLUSTER_PATH + " where id = ?";
            return template.queryForObject(sql, CLUSTER_PATH_ROW_MAPPER, id);
        } catch (DataAccessException ex) {

        }
        return null;
    }

    public ClusterPath findByPathId(String pathId) {
        try {
            String sql = "select * from " + TABLE_CLUSTER_PATH + " where path_id = ?";
            return template.queryForObject(sql, CLUSTER_PATH_ROW_MAPPER, pathId);
        } catch (DataAccessException ex) {

        }
        return null;
    }

    public List<ClusterPath> findByCondition(Long domainId, String pathType, Integer offset, Integer count) {
        try {
            StringBuffer buffer = new StringBuffer("select * from " + TABLE_CLUSTER_PATH + " where 1=1 ");
            List<Object> args = Lists.newArrayList();
            if(domainId != null){
                buffer.append(" and domain_id = ? ");
                args.add(domainId);
            }

            if(pathType != null){
                buffer.append(" and `type` = ? ");
                args.add(pathType);
            }

            if(offset != null && count != null){
                buffer.append(" limit ?,? ");
                args.add(offset);
                args.add(count);
            }
            return template.query(buffer.toString(), CLUSTER_PATH_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {

        }
        return Collections.EMPTY_LIST;
    }

    public Long countByCondition(Long domainId, String pathType) {
        try {
            StringBuffer buffer = new StringBuffer("select count(1) from " + TABLE_CLUSTER_PATH + " where 1=1 ");
            List<Object> args = Lists.newArrayList();
            if(domainId != null){
                buffer.append(" and domain_id = ? ");
                args.add(domainId);
            }

            if(pathType != null){
                buffer.append(" and `type` = ? ");
                args.add(pathType);
            }

            Long count = template.queryForObject(buffer.toString(), Long.class, args.toArray());
            if(count != null){
                return count;
            }
        } catch (DataAccessException ex) {

        }
        return 0L;
    }
}
