package com.haizhi.iap.search.repo;

import java.util.Collections;
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.haizhi.iap.search.model.Notification;

@Slf4j
@Repository
public class NotificationRepo {

    private String TABLE_NOTIFICATION = "notification";

    @Setter
    @Autowired
    JdbcTemplate template;

    private final RowMapper<Notification> TAG_ROW_MAPPER = new BeanPropertyRowMapper<>(Notification.class);
    
    public List<Notification> findByCondition(Integer offset, Integer count,String company,String type) {
        StringBuffer buffer = new StringBuffer("select id,user_id,rule_name,`level`,`desc`,type,company,bussiness_starttime as push_time,detail,title from " + TABLE_NOTIFICATION + " where now()<bussiness_endtime and user_id=0 and trim(company) = ?");
    	if(type.equals("marketing")){
    		buffer.append(" and type < 200");
    	}else if (type.equals("risk")) {
			buffer.append(" and type > 200");
		}
        List<Object> args = Lists.newArrayList();
        args.add(company);
        if (offset != null && count != null) {	
            buffer.append(" limit ?,? ");
            args.add(offset);
            args.add(count);
        }
        buffer.append(" order by push_time desc");
        try {
            return template.query(buffer.toString(), TAG_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
        }
        return Collections.emptyList();
    }

}
