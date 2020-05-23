package com.haizhi.iap.search.repo;

import com.google.common.collect.Lists;
import com.haizhi.iap.search.model.ClusterDomain;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;

/**
 * Created by chenbo on 2017/8/28.
 */
@Slf4j
@Repository
public class ClusterDomainRepo {
    @Setter
    @Autowired
    JdbcTemplate template;

    private String TABLE_CLUSTER_DOMAIN = "cluster_domain";

    private final RowMapper<ClusterDomain> CLUSTER_DOMAIN_ROW_MAPPER = new BeanPropertyRowMapper<>(ClusterDomain.class);

    public ClusterDomain find(String domainName, String type){
        String sql = "select * from " +TABLE_CLUSTER_DOMAIN + " where domain_name = ? and `type` = ? ";
        try {
            return template.queryForObject(sql, CLUSTER_DOMAIN_ROW_MAPPER, domainName, type);
        }catch (DataAccessException ex){
        }
        return null;
    }

    public List<ClusterDomain> find(String domainName){
        String sql = "select * from " +TABLE_CLUSTER_DOMAIN + " where domain_name = ? ";
        try {
            return template.query(sql, CLUSTER_DOMAIN_ROW_MAPPER, domainName);
        }catch (DataAccessException ex){
        }
        return Collections.EMPTY_LIST;
    }

    public List<ClusterDomain> findByCondition(String domainName, String type, Integer offset, Integer count){
        StringBuffer buffer = new StringBuffer("select * from " +TABLE_CLUSTER_DOMAIN + " where 1=1 ");
        List<Object> args = Lists.newArrayList();

        if(domainName != null){
            buffer.append(" domain_name like ? ");
            args.add("%" + domainName + "%");
        }
        if(type != null){
            buffer.append(" type like ? ");
            args.add("%" + type + "%");
        }
        if(offset != null && count != null){
            buffer.append(" limit ?,? ");
            args.add(offset);
            args.add(count);
        }
        try{
            return template.query(buffer.toString(), CLUSTER_DOMAIN_ROW_MAPPER, args.toArray());
        }catch (DataAccessException ex){

        }
        return Collections.EMPTY_LIST;
    }

    public ClusterDomain delete(Long id){
        String sql = "delete from " +TABLE_CLUSTER_DOMAIN + " where id = ? ";
        try {
            return template.queryForObject(sql, CLUSTER_DOMAIN_ROW_MAPPER, id);
        }catch (DataAccessException ex){

        }
        return null;
    }

    public ClusterDomain create(String domainName, String type) {
        if (domainName == null && type == null) {
            return null;
        }
        GeneratedKeyHolder holder = new GeneratedKeyHolder();
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "insert ignore into " + TABLE_CLUSTER_DOMAIN + " " +
                    "(domain_name, `type`, create_time, update_time) " +
                    "values (?, ?, now(), now()) ", new String[]{"id"});
            ps.setString(1, domainName);
            ps.setString(2, type);

            return ps;
        }, holder);

        ClusterDomain domain = new ClusterDomain();
        domain.setDomainName(domainName);
        domain.setType(type);
        if (holder.getKey() != null) {
            domain.setId(holder.getKey().longValue());
        }else {
            domain.setId(null);
        }
        return domain;
    }
}
