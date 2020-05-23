
package com.haizhi.iap.follow.repo;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author mtl
 * @Description:
 * @date 2020/3/12 11:12
 */
@Slf4j
@Repository
public class CompanyDigResultRepo {

    private RowMapper MAP_ROW_MAPPER = new ColumnMapRowMapper();

    @Setter
    @Autowired
    @Qualifier(value = "atlasJdbcTemplate")
    private JdbcTemplate template;

    /**
     * 查询公司关系信息
     * @param companys
     * @return
     */
    public List<Map> findCompanyRelations(List<String> companys,String[] flags) {
        if(companys.size() == 0){
            return Collections.EMPTY_LIST;
        }
        String companysStr = "'"+String.join("','", companys)+"'";
        String flagsStr = "'"+String.join("','", flags)+"'";
        String sql = "select from_key,to_key,relship_info from w_bidding_info where from_key in ("+companysStr+") and to_key in ("+companysStr+") and relship_flag in ("+flagsStr+") ";
        if(log.isDebugEnabled()){
            log.debug(sql);
        }
        return this.template.query(sql,MAP_ROW_MAPPER);
    }

    /**
     * 查询公司关系信息
     * @param companys
     * @return
     */
    public List<Map> findAllCompanyRelations(List<String> companys) {
        if(companys.size() == 0){
            return Collections.EMPTY_LIST;
        }
        String companysStr = "'"+String.join("','", companys)+"'";
        String sql = "select from_key,to_key,relship_flag,relship_info from w_bidding_info where from_key in ("+companysStr+") and to_key in ("+companysStr+") ";
        if(log.isDebugEnabled()){
            log.debug(sql);
        }
        return this.template.query(sql,MAP_ROW_MAPPER);
    }

    /**
     * 查询公司详细信息
     * @param companys
     * @return
     */
    public List<Map> findCompanyInfos(List<String> companys) {
        if(companys.size() == 0){
            return Collections.EMPTY_LIST;
        }
        String companysStr = "'"+String.join("','", companys)+"'";
        String sql = "select entname,uniscid,legal_man,legal_id,legal_other_custname,name,actual_controller,altimate_beneficiary,conprop,subconam,rgtered_tel,rgtered_adress,rgtered_email from tv_Bidding_info where entname in ("+companysStr+")  ";
        if(log.isDebugEnabled()){
            log.debug(sql);
        }
        return this.template.query(sql,MAP_ROW_MAPPER);
    }

    /**
     * 查询公司详细信息
     * @param sql
     * @return
     */
    public List<Map> findBySql(String sql) {
        if(log.isDebugEnabled()){
            log.debug(sql);
        }
        return this.template.query(sql,MAP_ROW_MAPPER);
    }



}
