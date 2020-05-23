package com.haizhi.iap.search.repo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;

/**
 * Created by chenbo on 2017/8/28.
 */
@Slf4j
@Repository
public class CustomerTagsRepo {

    private String TABLE_CUSTOMER_TAGS = "p_par_zstp_cust_label";

    @Setter
    @Autowired
    JdbcTemplate template;

    
    public Map<String,Object> findByCondition(String company) {
        StringBuffer buffer = new StringBuffer("select * from " + TABLE_CUSTOMER_TAGS + " where name= ? limit 1");
        List<Object> args = Lists.newArrayList();
        args.add(company);
        try {
            return template.queryForMap(buffer.toString(),  args.toArray());
        } catch (DataAccessException ex) {
        }
        return Collections.emptyMap();
    }

}
