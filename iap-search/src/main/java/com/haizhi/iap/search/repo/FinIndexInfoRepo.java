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
public class FinIndexInfoRepo {

    private String TABLE = "c_par_corp_fin_index_info";

    @Setter
    @Autowired
    JdbcTemplate template;
    
    public Map<String, Object> findByCondition(String custId,String index,String year) {
        StringBuffer buffer = new StringBuffer("select * from " + TABLE + " where cust_id = ? and rept_item_cd = ? and rept_year = ? limit 1");
        try {
            return template.queryForMap(buffer.toString(), custId,index,year);
        } catch (DataAccessException ex) {
        }
        return Maps.newHashMap();
    }
    
}
