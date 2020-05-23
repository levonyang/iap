
package com.haizhi.iap.search.repo;

import com.google.common.collect.Lists;
import com.haizhi.iap.search.controller.model.GraphListReq;
import com.haizhi.iap.search.model.CompanyGroup;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author dmy
 * @Date 2018/2/7 下午9:25.
 */
@Slf4j
@Repository
public class CompanyGroupRepo {

    private String TABLE_COMPANY_GROUP = "company_group";
    private String TABLE_GROUP_DETAIL = "group_detail";

    private RowMapper<CompanyGroup> GROUP_NAME_LIST_ROW_MAPPER = new BeanPropertyRowMapper<>(CompanyGroup.class);

    private  int num = 30;
    @Setter
    @Autowired
    JdbcTemplate template;


    public String findByGroupName(String group_name) {
        try {
            String sql = "select paths from " + TABLE_COMPANY_GROUP + " where group_name = ? ";
            return template.queryForObject(sql, String.class, group_name);
        } catch (EmptyResultDataAccessException ex) {
            log.info("select group_name fail! reason: emptyresult, group_name = " + group_name );
        } catch (IncorrectResultSizeDataAccessException e) {
            log.info("select group_name fail! reason: has more than 1 recoed, group_name = " + group_name);
        } catch (DataAccessException e) {
            log.error("dataAccessException: {}", e.getMessage());
        }
        return null;
    }

    /**
    * @description 根据族谱名称和类型获取族谱信息
    * @param group_name
    * @param groupType
    * @return java.lang.String
    * @author liulu
    * @date 2018/12/25
    */
    public String findByGroupName(String group_name,String groupType) {
        try {
            String sql = "select paths from " + TABLE_COMPANY_GROUP + " where group_name = ? and type = ? ";
            return template.queryForObject(sql, String.class, group_name,groupType);
        } catch (EmptyResultDataAccessException ex) {
            log.info("select group_name fail! reason: emptyresult, group_name = " + group_name + " type = "  + groupType);
        } catch (IncorrectResultSizeDataAccessException e) {
            log.info("select group_name fail! reason: has more than 1 recoed, group_name = " + group_name+ " type = "  + groupType);
        } catch (DataAccessException e) {
            log.error("dataAccessException: {}", e.getMessage());
        }
        return null;
    }

    /**
    * @description 从数据库获取单个族谱信息
    * @param group_name 族谱名称
    * @param type 族谱类型
    * @return com.haizhi.iap.search.model.CompanyGroup
    * @author LewisLouis
    * @date 2018/8/20
    */
    public CompanyGroup findOneGroup(String group_name, String type) {
        try {
            String sql = "select * from " + TABLE_COMPANY_GROUP + " where group_name = ? and type = ?";
            List<CompanyGroup> groups = template.query(sql, GROUP_NAME_LIST_ROW_MAPPER, group_name,type);
            if (!CollectionUtils.isEmpty(groups)){
                return groups.get(0);
            }
        } catch (DataAccessException ex) {
        }
        return null;
    }

    public List<CompanyGroup> findGroupNameByType(String type,Integer offset, Integer count ) {
        try {
            String sql = "select distinct(group_name),entity_count from " + TABLE_COMPANY_GROUP + " where type = ? limit ?, ?";
            return template.query(sql, GROUP_NAME_LIST_ROW_MAPPER, type,offset,count);
        } catch (DataAccessException ex) {
            //log.error("{}", ex);
            return Collections.emptyList();
        }
    }

    private Map orderBy(GraphListReq req){
        Map map = new HashMap();
        map.put("field",req.getField());
        map.put("sort",req.getSort());
        return map;
    }

    /**
    * @description 通过集团类型并查询包含该公司的所有集团
    * @param req
    * @return java.util.List<java.util.Map<String,Object>>
    * @author yuding
    * @date 2018/8/23
    */
    public List<Map<String,Object>> getGroupListByEntityAndType(GraphListReq req){
        try {
            StringBuilder sql = new StringBuilder();
             sql.append("select company_group.id, company_group.group_name, company_group.type," +
                     " company_group.entity_count, company_group.belong_inner, company_group.inner_entity_count " +
                     "from (select group_name from ");
             sql.append(TABLE_GROUP_DETAIL);
             sql.append(" where entity_name = ? ) as a left join company_group on ");
             sql.append("a.group_name = company_group.group_name where company_group.type = ? ");

            Map map = orderBy(req);
            if(map != null){
                sql.append("order by ");
                sql.append(map.get("field")).append(" ");
                sql.append(map.get("sort"));
            }
            sql.append(" limit ?, ? ");
            return template.queryForList(sql.toString(), req.getEntityName(), req.getType(),req.getOffset(), req.getPageSize());
        }catch (Exception ex){
            log.error(ex.getMessage(), ex);
        }
        return Lists.newArrayList();
    }

    /**
    * @description 通过集团类型获取集团列表
    * @param req
    * @return java.util.List<java.util.Map<String,Object>>
    * @author yuding
    * @date 2018/8/23
    */
    public List<Map<String,Object>>getGroupListByType(GraphListReq req){
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select company_group.id, company_group.group_name, company_group.type, " +
                    "company_group.entity_count, company_group.belong_inner,company_group.inner_entity_count " +
                    "from ");
            sql.append(TABLE_COMPANY_GROUP);
            sql.append(" where company_group.type = ? ");
            Map map = orderBy(req);
            if(map != null){
                sql.append(" order by ");
                sql.append(map.get("field")).append(" ");
                sql.append(map.get("sort"));
            }
            sql.append(" limit ?, ? ");
            return template.queryForList(sql.toString(), req.getType(), req.getOffset(), req.getPageSize());
        }catch (Exception ex){
            log.error(ex.getMessage(), ex);
        }
        return Lists.newArrayList();
    }

    /**
    * @description 通过公司ID获取公司详细图谱信息
    * @param groupId
    * @return java.util.Map<java.lang.String,java.lang.Object>
    * @author yuding
    * @date 2018/8/23
    */
    public Map<String, Object> getGroupDetail(Long groupId){
        String sql = "select * from " + TABLE_COMPANY_GROUP +" where id = ? ";
        Map<String, Object> maps = template.queryForMap(sql, groupId);
        return maps;

    }

    /**
    * @description 统计四个类型的族谱
    * @param
    * @return java.util.List<java.util.Map<String,Object>>
    * @author yuding
    * @date 2018/8/23
    */
    public List<Map<String, Object>> countByType(){

        String sql = "select type, count(*) as countByType from " + TABLE_COMPANY_GROUP +" group by type ";
        List<Map<String, Object>> maps = template.queryForList(sql);
        return maps;
    }

    /**
    * @description 每个类型的详细统计
    * @param Type
    * @return java.lang.Integer
    * @author yuding
    * @date 2018/8/23
    */
    public Integer countTotal(String Type){
        String sql = "select sum(entity_count) from " + TABLE_COMPANY_GROUP + " where type = ?";
        return template.queryForObject(sql,Integer.class,Type);

    }
    /**
    * @description 统计某类型的集团数
    * @param Type
    * @return java.lang.Integer
    * @author yuding
    * @date 2018/8/23
    */
    public Integer countTotalGroup(String Type){
        String sql = "select count(*) from " + TABLE_COMPANY_GROUP + " where type = ?";
        return template.queryForObject(sql,Integer.class,Type);

    }
    /**
    * @description 统计包括某公司的集团数
    * @param req
    * @return java.lang.Integer
    * @author yuding
    * @date 2018/8/23
    */
    public Integer countByTypeAndEntity(GraphListReq req){
        Integer total=0;
        try {
            String sql = "select count(*) from (select group_name from " + TABLE_GROUP_DETAIL +
                    " where entity_name = ? ) as a left join company_group on " +
                    " a.group_name = company_group.group_name WHERE company_group.type = ? ";
            total= template.queryForObject(sql, Integer.class, req.getEntityName(),req.getType());
            return total;
        }catch (Exception ex){
            log.error(ex.getMessage(), ex);
        }
        return total;
    }

    public  Integer countEntityOverNum(String Type){
        String sql = "select count(*) from" + TABLE_COMPANY_GROUP + "where type=? and count>" +num;
        return  template.queryForObject(sql,Integer.class,Type);
    }
    public Integer getCountByType(String type) {
        try {
            String sql = "select count(0) from " + TABLE_COMPANY_GROUP + " where type = ?";
            return template.queryForObject(sql, Integer.class, type);
        } catch (DataAccessException ex) {
            log.info("select company_group total fail!");
        }
        return 0;
    }

    public void delete(String type) {
        String sql = "delete from " + TABLE_COMPANY_GROUP + " where type = ? ";
        template.update(sql, type);
    }

    //批量插入企业族谱实体信息
    public int[] batchInsert(List<CompanyGroup> groups) {
        if (groups != null && groups.size() < 1) {
            return null;
        }

        int[] updateNum = null;
        try {
            final String sql = "insert ignore into " + TABLE_COMPANY_GROUP + " " +
                    "(group_name, paths, type, entity_count, " +
                    "belong_inner,inner_entity_count,sub_type,create_time,update_time)" +
                    "values (?, ?, ?, ?, ?, ?,?,now(3),now(3)) ";
            updateNum = template.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    if( groups.get(i).getGroupName()==null){
                        ps.setNull(1,Types.VARCHAR);
                    }else{
                        ps.setString(1, groups.get(i).getGroupName());
                    }

                    if(groups.get(i).getPaths()==null){
                        ps.setNull(2,Types.VARCHAR);
                    }else{
                        ps.setString(2, groups.get(i).getPaths());
                    }

                    if(groups.get(i).getType()==null){
                        ps.setNull(3,Types.VARCHAR);
                    }else{
                        ps.setString(3, groups.get(i).getType());
                    }

                    if(groups.get(i).getEntityCount()==null){
                        ps.setNull(4,Types.INTEGER);
                    }else{
                        ps.setInt(4,groups.get(i).getEntityCount());
                    }

                    if(groups.get(i).getBelongInner()==null){
                        ps.setNull(5,Types.BOOLEAN);
                    }else{
                        ps.setBoolean(5,groups.get(i).getBelongInner());
                    }

                    if(groups.get(i).getInnerEntityCount()==null){
                        ps.setNull(6,Types.INTEGER);
                    }else {
                        ps.setInt(6, groups.get(i).getInnerEntityCount());
                    }

                    if(groups.get(i).getSubType()==null){
                        ps.setNull(7,Types.VARCHAR);
                    }else{
                        ps.setString(7, groups.get(i).getSubType());
                    }

                }

                @Override
                public int getBatchSize() {
                    return groups.size();
                }
            });
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return updateNum;
    }


    /**
     * @description 获取族谱信息列表
     * @param type 族谱类型
     *（如profile_enterprise_info 关联集团
     * market_updown_info  上下游
     * risk_propagation  风险传导
     * risk_guarantee_info 关联担保
     * risk_black_info  黑名单）
    * @param subType 族谱子类型（如circle）
    * @param offset 开始行数
    * @param count 返回数量
     * @return java.util.List<com.haizhi.iap.search.model.CompanyGroup>
     * @author LewisLouis
     * @date 2018/8/20
     */
    public List<CompanyGroup> findGroupsByTypeWithOutPaths(String type, String subType, Integer offset, Integer count) {
        StringBuffer buffer = new StringBuffer("select group_name,type,sub_type, entity_count,belong_inner, inner_entity_count from "
                + TABLE_COMPANY_GROUP + " where 1=1 ");
        List<Object> args = Lists.newArrayList();
        if (!Strings.isNullOrEmpty(type)){
            buffer.append(" and type = ? ");
            args.add(type);
        }
        if (!Strings.isNullOrEmpty(subType)){
            buffer.append(" and sub_type = ? ");
            args.add(subType);
        }

        buffer.append(" order by entity_count DESC");

        if (offset != null && count != null) {
            buffer.append(" limit ?,? ");
            args.add(offset);
            args.add(count);
        }
        try {
            return template.query(buffer.toString(), GROUP_NAME_LIST_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
        }
        return Collections.emptyList();
    }

    /**
    * @description 获取族谱信息列表
     * @param type 族谱类型
     *（如profile_enterprise_info 关联集团
     * market_updown_info  上下游
     * risk_propagation  风险传导
     * risk_guarantee_info 关联担保
     * risk_black_info  黑名单）
    * @param subType 族谱子类型（如circle）
    * @param offset 开始行数
    * @param count 返回数量
    * @return java.util.List<com.haizhi.iap.search.model.CompanyGroup>
    * @author LewisLouis
    * @date 2018/8/20
    */
    public List<CompanyGroup> findGroupsByType(String type, String subType, Integer offset, Integer count) {
        StringBuffer buffer = new StringBuffer("select group_name,type,sub_type, paths, entity_count,belong_inner, inner_entity_count from "
                + TABLE_COMPANY_GROUP + " where 1=1 ");
        List<Object> args = Lists.newArrayList();
        if (!Strings.isNullOrEmpty(type)){
            buffer.append(" and type = ? ");
            args.add(type);
        }
        if (!Strings.isNullOrEmpty(subType)){
            buffer.append(" and sub_type = ? ");
            args.add(subType);
        }

        buffer.append(" order by entity_count DESC");

        if (offset != null && count != null) {
            buffer.append(" limit ?,? ");
            args.add(offset);
            args.add(count);
        }
        try {
            return template.query(buffer.toString(), GROUP_NAME_LIST_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
        }
        return Collections.emptyList();
    }

    /**
    * @description 根据类型获取族谱的统计数量
     * @param type 族谱类型
     *（如profile_enterprise_info 关联集团
     * market_updown_info  上下游
     * risk_propagation  风险传导
     * risk_guarantee_info 关联担保
     * risk_black_info  黑名单）
    * @param subType 族谱子类型（如circle）
    * @return java.lang.Integer 指定类型的族谱的数量
    * @author LewisLouis
    * @date 2018/8/20
    */
    public Long findGroupsCountByType(String type, String subType) {
        try {
            StringBuffer sqlBuffer = new StringBuffer("select count(*) from " + TABLE_COMPANY_GROUP + " where 1 = 1");
            List<Object> args = Lists.newArrayList();
            if (!Strings.isNullOrEmpty(type)){
                sqlBuffer.append(" and type = ? ");
                args.add(type);
            }
            if (!Strings.isNullOrEmpty(subType)){
                sqlBuffer.append(" and sub_type = ? ");
                args.add(subType);
            }

            return template.queryForObject(sqlBuffer.toString(), Long.class, args.toArray());
        } catch (DataAccessException ex) {

        }
        return 0L;
    }

    /**
     * @description 统计指定族谱类型的边类型数量
     * @param type 族谱类型
     * @return java.util.Map<java.lang.String,java.lang.Integer> <变类型名称，对应的族谱数量>
     * @author LewisLouis
     * @date 2018/8/20
     */
    public Map<String, Long> findSubTypes(String type){
        try {
            String sql = "select `sub_type`, count(1) as `count` from " + TABLE_COMPANY_GROUP + " where type = ? group by sub_type";
            List<Map<String,Object>> subTypes = template.queryForList(sql,type);
            Map<String,Long> resMap = new HashMap<>();
            for (Map<String,Object> map:subTypes) {
                resMap.put(map.get("sub_type").toString(),(Long)map.get("count"));
            }
            return resMap;
        } catch (DataAccessException ex) {

        }
        return Collections.EMPTY_MAP;
    }


}

