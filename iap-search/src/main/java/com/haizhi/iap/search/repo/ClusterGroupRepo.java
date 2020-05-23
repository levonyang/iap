package com.haizhi.iap.search.repo;

import com.google.common.collect.Lists;
import com.haizhi.iap.search.controller.model.TypeCount;
import com.haizhi.iap.search.model.ClusterGroup;
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
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/8/28.
 */
@Slf4j
@Repository
public class ClusterGroupRepo {

    private String TABLE_CLUSTER_GROUP = "cluster_group";

    @Setter
    @Autowired
    JdbcTemplate template;

    private final RowMapper<ClusterGroup> CLUSTER_GROUP_ROW_MAPPER = new BeanPropertyRowMapper<>(ClusterGroup.class);
    private final RowMapper<TypeCount> TYPE_COUNT_ROW_MAPPER = new BeanPropertyRowMapper<>(TypeCount.class);

        public List<ClusterGroup> findByCondition(Long domainId, Integer offset, Integer count) {
        StringBuffer buffer = new StringBuffer("select * from " + TABLE_CLUSTER_GROUP + " where 1=1 ");
        List<Object> args = Lists.newArrayList();
        if (domainId != null) {
            buffer.append(" and domain_id = ? ");
            args.add(domainId);
        }

        buffer.append(" order by path_count DESC");

        if (offset != null && count != null) {
            buffer.append(" limit ?,? ");
            args.add(offset);
            args.add(count);
        }
        try {
            return template.query(buffer.toString(), CLUSTER_GROUP_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
        }
        return Collections.emptyList();
    }

    public List<TypeCount> getTypes(Long domainId) {
        try {
            String sql = "select `type`, count(1) as `count` from cluster_group where domain_id = ? group by `type`";
            return template.query(sql, TYPE_COUNT_ROW_MAPPER, domainId);
        } catch (DataAccessException ex) {

        }
        return Collections.EMPTY_LIST;
    }

    public List<ClusterGroup> getAll(Long domainId) {
        return findByCondition(domainId, null, null);
    }

    public List<ClusterGroup> getIgnorePaths(Long domainId, String groupType, Integer offset, Integer count) {
        StringBuffer buffer = new StringBuffer("select cluster_name, cluster_cid, path_count from " + TABLE_CLUSTER_GROUP + " where 1=1 ");
        List<Object> args = Lists.newArrayList();
        if (domainId != null) {
            buffer.append(" and domain_id = ? ");
            args.add(domainId);
        }

        if (groupType != null) {
            buffer.append(" and `type` = ? ");
            args.add(groupType);
        }

        buffer.append(" order by path_count DESC");

        if (offset != null && count != null) {
            buffer.append(" limit ?,? ");
            args.add(offset);
            args.add(count);
        }
        try {
            return template.query(buffer.toString(), CLUSTER_GROUP_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
        }
        return Collections.emptyList();
    }

    public int[] batchInsert(List<ClusterGroup> clusterGroupList) {
        if (clusterGroupList != null && clusterGroupList.size() < 1) {
            return null;
        }
        int[] updateNum = null;
        try {
            final String sql = "insert ignore into " + TABLE_CLUSTER_GROUP + " " +
                    "(cluster_name, cluster_cid, domain_id, paths, path_count, `type`, create_time, update_time) " +
                    "values (?, ?, ?, ?, ?, ?, now(), now()) ";
            updateNum = template.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setString(1, clusterGroupList.get(i).getClusterName());
                    ps.setString(2, clusterGroupList.get(i).getClusterCid());
                    ps.setLong(3, clusterGroupList.get(i).getDomainId());

                    if (clusterGroupList.get(i).getPaths() == null) {
                        ps.setNull(4, Types.VARCHAR);
                    } else {
                        ps.setString(4, clusterGroupList.get(i).getPaths());
                    }
                    if (clusterGroupList.get(i).getPathCount() == null) {
                        ps.setNull(5, Types.INTEGER);
                    } else {
                        ps.setInt(5, clusterGroupList.get(i).getPathCount());
                    }
                    if (clusterGroupList.get(i).getType() == null) {
                        ps.setNull(6, Types.VARCHAR);
                    } else {
                        ps.setString(6, clusterGroupList.get(i).getType());
                    }
                }

                @Override
                public int getBatchSize() {
                    return clusterGroupList.size();
                }
            });
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return updateNum;
    }

    public void delete(Long domainId) {
        String sql = "delete from " + TABLE_CLUSTER_GROUP + " where domain_id = ? ";
        template.update(sql, domainId);
    }

    public ClusterGroup findByCid(Long domainId, String cid) {
        try {
            String sql = "select * from " + TABLE_CLUSTER_GROUP + " where domain_id = ? and cluster_cid = ?";
            return template.queryForObject(sql, CLUSTER_GROUP_ROW_MAPPER, domainId, cid);
        } catch (DataAccessException ex) {

        }
        return null;
    }
    
    public ClusterGroup findByCname(Long domainId, String cname) {
        try {
            String sql = "select * from " + TABLE_CLUSTER_GROUP + " where domain_id = ? and cluster_name = ?";
            return template.queryForObject(sql, CLUSTER_GROUP_ROW_MAPPER, domainId, cname);
        } catch (DataAccessException ex) {

        }
        return null;
    }

    public Long countAll(Long domainId) {
        try {
            String sql = "select count(1) from " + TABLE_CLUSTER_GROUP + " where domain_id = ?";
            return template.queryForObject(sql, Long.class, domainId);
        } catch (DataAccessException ex) {

        }
        return 0L;
    }

    public Long count(Long domainId, String groupType) {
        try {
            String sql = "select count(1) from " + TABLE_CLUSTER_GROUP + " where domain_id = ? and `type` = ?";
            return template.queryForObject(sql, Long.class, domainId, groupType);
        } catch (DataAccessException ex) {

        }
        return 0L;
    }

    public List<ClusterGroup> findByName(Long domainId, String keyword, Integer offset, Integer count) {
        StringBuffer buffer = new StringBuffer("select cluster_name, cluster_cid, path_count from " + TABLE_CLUSTER_GROUP + " where 1=1 ");
        List<Object> args = Lists.newArrayList();
        if (domainId != null) {
            buffer.append(" and domain_id = ? ");
            args.add(domainId);
        }

        if (keyword != null) {
            buffer.append(" and cluster_name like ? ");
            args.add("%" + keyword + "%");
        }

        buffer.append(" order by path_count DESC");

        if (offset != null && count != null) {
            buffer.append(" limit ?,? ");
            args.add(offset);
            args.add(count);
        }
        try {
            return template.query(buffer.toString(), CLUSTER_GROUP_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
        }
        return Collections.emptyList();
    }

    public Long countByName(Long domainId, String keyword) {
        StringBuffer buffer = new StringBuffer("select count(1) from " + TABLE_CLUSTER_GROUP + " where 1=1 ");
        List<Object> args = Lists.newArrayList();
        if (domainId != null) {
            buffer.append(" and domain_id = ? ");
            args.add(domainId);
        }

        if (keyword != null) {
            buffer.append(" and cluster_name like ? ");
            args.add("%" + keyword + "%");
        }

        try {
            Long count = template.queryForObject(buffer.toString(), Long.class, args.toArray());
            if (count == null) {
                return 0L;
            } else {
                return count;
            }
        } catch (DataAccessException ex) {
        }
        return 0L;
    }
}
