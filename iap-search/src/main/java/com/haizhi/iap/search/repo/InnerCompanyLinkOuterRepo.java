package com.haizhi.iap.search.repo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.haizhi.iap.search.model.CompanyRelate;

@Slf4j
@Repository
public class InnerCompanyLinkOuterRepo {

    private String TABLE_COMPANY_RELATE = "p_par_zstp_company_relate";

    @Setter
    @Autowired
    JdbcTemplate template;

    private final RowMapper<CompanyRelate> TAG_ROW_MAPPER = new BeanPropertyRowMapper<>(CompanyRelate.class);
    
    public List<CompanyRelate> findByCondition(Integer offset, Integer count) {
        StringBuffer buffer = new StringBuffer("select * from " + TABLE_COMPANY_RELATE);
        List<Object> args = Lists.newArrayList();
        if (offset != null && count != null) {	
            buffer.append(" limit ?,? ");
            args.add(offset);
            args.add(count);
        }
        try {
            return template.query(buffer.toString(), TAG_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
        }
        return Collections.emptyList();
    }
    
    public Map<String, Object> count() {
        StringBuffer buffer = new StringBuffer("select count(1) as num from " + TABLE_COMPANY_RELATE);
        try {
            return template.queryForMap(buffer.toString());
        } catch (DataAccessException ex) {
        	return new HashMap<String, Object>(){
        		{
        			put("num", 0);
        		}
        	};
        }
    }
    
}
