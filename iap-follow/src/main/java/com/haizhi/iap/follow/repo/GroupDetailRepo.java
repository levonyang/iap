package com.haizhi.iap.follow.repo;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* @description 族谱详细信息操作类
* @author LewisLouis
* @date 2018/9/2
*/
@Repository
public class GroupDetailRepo {

    /**
     * 族谱明细表
     */
    public static final String TABLE_GROUP_DETAIL = "group_detail";

    @Setter
    @Autowired
    @Qualifier(value = "followJdbcTemplate")
    JdbcTemplate template;



    /**
    * @description 根据族谱名称获取实体名称列表
    * @param groupName 族谱名称
    * @return java.util.List<java.lang.String>
    * @author LewisLouis
    * @date 2018/9/2
    */
    public  List<String> findEntityNamesByGroupName(String groupName){
        try {
            String sql = "select entity_name from " + TABLE_GROUP_DETAIL + " where group_name = ?";
            return template.queryForList(sql, String.class, groupName);
        } catch (DataAccessException ex) {
            return null;
        }
    }
 }
