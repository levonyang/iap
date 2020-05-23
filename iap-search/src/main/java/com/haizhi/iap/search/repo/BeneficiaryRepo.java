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
import com.haizhi.iap.search.model.Beneficiary;

@Slf4j
@Repository
public class BeneficiaryRepo {

    private String TABLE_NEW_COMPANY = "beneficiary";

    @Setter
    @Autowired
    JdbcTemplate template;

    private final RowMapper<Beneficiary> ROW_MAPPER = new BeanPropertyRowMapper<>(Beneficiary.class);
    
    public List<Beneficiary> findByCondition(Integer offset, Integer count) {
        StringBuffer buffer = new StringBuffer("SELECT b.*,c.CUST_ID as cust_id,c.CERT_NO as cert_no,cd.cd_type as cert_type FROM `beneficiary` as b left join p_par_zstp_cust_info as c on b.company=c.CUST_NAME left JOIN cert_type_cd as cd on c.CERT_TYPE_CD=cd.cd_val");
        List<Object> args = Lists.newArrayList();
        if (offset != null && count != null) {	
            buffer.append(" limit ?,? ");
            args.add(offset);
            args.add(count);
        }
        try {
            return template.query(buffer.toString(), ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
        	ex.printStackTrace();
        }
        return Collections.emptyList();
    }
    
    public Map<String, Object> count() {
        StringBuffer buffer = new StringBuffer("select count(*) as num from " + TABLE_NEW_COMPANY);
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
