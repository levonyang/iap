package com.haizhi.iap.search.repo;

import java.util.Map;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Created by chenbo on 17/2/15.
 */
@Repository
public class IndustryAverageInfoRepo {

    private String TABLE = "t48_industry_average_info";

    @Setter
    @Autowired
    JdbcTemplate template;

    public Map<String, Object> query(String reptM,String industry) {
        StringBuffer buffer = new StringBuffer("select * from " + TABLE +" where `rept_ym` = ? and `indu_explain` = ?");
        try {
            return template.queryForMap(buffer.toString(),reptM,industry);
        } catch (DataAccessException ex) {
        	//ex.printStackTrace();
        }
        return null;
    }
}
