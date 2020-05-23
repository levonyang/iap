package com.haizhi.iap.search.repo;

import java.util.Collections;
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
import com.haizhi.iap.search.model.IndustryStatistics;

@Slf4j
@Repository
public class IndustryStatisticsRepo {

    private String TABLE = "industry_statistics";

    @Setter
    @Autowired
    JdbcTemplate template;

    private final RowMapper<IndustryStatistics> ROW_MAPPER = new BeanPropertyRowMapper<>(IndustryStatistics.class);
    
    public IndustryStatistics query(String industry,String yearMonth,String index) {
        StringBuffer buffer = new StringBuffer("select * from " + TABLE +" where `industry` = ? and year_month=? and `index` = ?");
        String [] args = {industry,yearMonth,index};
        try {
            return template.queryForObject(buffer.toString(), args, ROW_MAPPER);
        } catch (DataAccessException ex) {

        }
        return new IndustryStatistics();
    }
    
    public List<IndustryStatistics> findByCondition(String industry,String yearMonth,String index) {
        StringBuffer buffer = new StringBuffer("select * from " + TABLE + " where `industry` = ?");
        List<Object> args = Lists.newArrayList();
        args.add(industry);
        if(yearMonth!=null && index !=null && !yearMonth.isEmpty()){
            buffer.append(" and `year_month`= ? and `index`=? ");
            args.add(yearMonth);
            args.add(index);
        }
        buffer.append(" order by year desc");
        try {
            return template.query(buffer.toString(), ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
        	ex.printStackTrace();
        }
        return Collections.emptyList();
    }
    
    public List<Map<String, Object>> findCaibaoType(String industry) {
        StringBuffer buffer = new StringBuffer("select `year_month` from " + TABLE + " where `industry` = ? group by `year_month`");
        try {
            return template.queryForList(buffer.toString(), industry);
        } catch (DataAccessException ex) {
        	//ex.printStackTrace();
        }
        return Collections.emptyList();
    }
    
}
