package com.haizhi.iap.search.repo;

import java.util.Map;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;

@Slf4j
@Repository
public class CustInfoRepo {

    private String TABLE = "p_par_zstp_cust_info";

    @Setter
    @Autowired
    JdbcTemplate template;
    
    public Map<String, Object> findByCondition(String custName) {
        StringBuffer buffer = new StringBuffer("select * from " + TABLE + " where CUST_NAME = ? limit 1");
        try {
            return template.queryForMap(buffer.toString(), custName);
        } catch (DataAccessException ex) {
        }
        return Maps.newHashMap();
    }
    
}
