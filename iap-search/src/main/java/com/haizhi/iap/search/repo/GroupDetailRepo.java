package com.haizhi.iap.search.repo;

import com.haizhi.iap.search.constant.EntityType;
import com.haizhi.iap.search.model.GroupDetail;
import com.haizhi.iap.search.model.qo.GroupMembersSearchQo;
import joptsimple.internal.Strings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @Author dmy
 * @Date 2018/2/8 下午2:20.
 */
@Slf4j
@Repository
public class GroupDetailRepo {
    private String TABLE_GROUP_DETAIL = "group_detail";
    private RowMapper<GroupDetail> GROUP_DETAIL_LIST_ROW_MAPPER = new BeanPropertyRowMapper<>(GroupDetail.class);

    @Setter
    @Autowired
    JdbcTemplate template;

    /**
     * @description 根据实体id获取所属族谱的名称
     * @param entityId 实体Id
    * @param type 族谱类型
    * @return java.util.List<java.lang.String> 族谱名称列表
    * @author LewisLouis
    * @date 2018/8/20
    */
    public List<String> findGroupNameByEntity(String entityId, String type){
        try {
            String sql = "select group_name from " + TABLE_GROUP_DETAIL + " where entity_id = ? and type = ?";
            List<Map<String,Object>> groupDatas = template.queryForList(sql, entityId, type);
            List<String> groupNames = new ArrayList<>();
            for (Map<String,Object> oneGroup:groupDatas) {
                groupNames.add(oneGroup.get("group_name").toString());
            }
            return groupNames;
        } catch (DataAccessException ex) {
            //log.info("select company_group fail!company = " + companyName );
        }
        return null;
    }

    public List<GroupDetail> findCompanys(String keyWords, String type) {
        try {
            String sql = "select distinct(entity_name) from " + TABLE_GROUP_DETAIL + " where entity_name like ? and type = ?";
            return template.query(sql, GROUP_DETAIL_LIST_ROW_MAPPER, "%" + keyWords + "%", type);
        } catch (DataAccessException ex) {
            log.error("{}", ex);
            return Collections.emptyList();
        }
    }


    public void delete(String type) {
        String sql = "delete from " + TABLE_GROUP_DETAIL + " where type = ? ";
        template.update(sql, type);
    }

    //批量插入企业族谱实体信息
    public int[] batchInsert(List<GroupDetail> groupDetails) {
        if (groupDetails != null && groupDetails.size() < 1) {
            return null;
        }

        int[] updateNum = null;
        try {
            final String sql = "insert ignore into " + TABLE_GROUP_DETAIL + " " +
                    "(entity_name, type, group_name, entity_id,belong_inner,create_time, update_time) " +
                    "values (?, ?, ?, ?,?,now(3),now(3)) ";
            updateNum = template.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    if(groupDetails.get(i).getEntityName() == null ){
                        ps.setNull(1,Types.VARCHAR);
                    }else{
                        ps.setString(1, groupDetails.get(i).getEntityName());
                    }

                    if(groupDetails.get(i).getType() == null){
                        ps.setNull(2,Types.VARCHAR);
                    }else{
                        ps.setString(2, groupDetails.get(i).getType());
                    }

                    if (groupDetails.get(i).getGroupName() == null) {
                        ps.setNull(3, Types.VARCHAR);
                    } else {
                        ps.setString(3, groupDetails.get(i).getGroupName());
                    }
                    if (groupDetails.get(i).getEntityId() == null) {
                        ps.setNull(4, Types.VARCHAR);
                    } else {
                        ps.setString(4, groupDetails.get(i).getEntityId());
                    }

                    if (groupDetails.get(i).getBelongInner() == null) {
                        ps.setNull(5, Types.BOOLEAN);
                    } else {
                        ps.setBoolean(5, groupDetails.get(i).getBelongInner());
                    }
                }

                @Override
                public int getBatchSize() {
                    return groupDetails.size();
                }
            });
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return updateNum;
    }

    /**
    * @description 查询指定族谱下的成员信息
    * @param groupMembersSearchQo
    * @return java.util.List<java.lang.String>
    * @author liulu
    * @date 2018/12/19
    */
    public List<GroupDetail> findGroupDetails(GroupMembersSearchQo groupMembersSearchQo){
        try {
            String sql = "select * from " + TABLE_GROUP_DETAIL + " where 1 = 1 ";
            sql = sql +  buildFindGroupDetailCondition(groupMembersSearchQo);
            return template.query(sql,GROUP_DETAIL_LIST_ROW_MAPPER);
        } catch (DataAccessException ex) {
            log.error("{}", ex);
            return Collections.emptyList();
        }
    }

    /**
    * @description
    * @param groupMembersSearchQo
    * @return java.lang.String
    * @author liulu
    * @date 2018/12/19
    */
    private String buildFindGroupDetailCondition(GroupMembersSearchQo groupMembersSearchQo){
        StringBuilder conditions = new StringBuilder(Strings.EMPTY);

        if (null == groupMembersSearchQo){
            return conditions.toString();
        }

        if (!StringUtils.isBlank(groupMembersSearchQo.getGroupName())){
            conditions.append(String.format(" and group_name = '%s'",groupMembersSearchQo.getGroupName()));
        }

        if (!StringUtils.isBlank(groupMembersSearchQo.getGroupType())){
            conditions.append(String.format(" and type = '%s'",groupMembersSearchQo.getGroupType()));
        }


        if ((null != groupMembersSearchQo.getBelongInner())
         && (!groupMembersSearchQo.getBelongInner().equals(-1))){
            conditions.append(String.format(" and belong_inner = %d",
                    groupMembersSearchQo.getBelongInner()));
        }

        if ((null != groupMembersSearchQo.getEntityType())
          && (!EntityType.ALL.equals(groupMembersSearchQo.getEntityType()))){
            conditions.append(String.format(" and entity_id like '%s%%'",
                    groupMembersSearchQo.getEntityType().getValue()));
        }

        return conditions.toString();

    }


    /**
    * @description 获取指定族谱的所有的客户
    * @param groupName
    * @param type
    * @return java.util.List<java.lang.String>
    * @author liulu
    * @date 2018/12/25
    */
    public List<String> findEntitiesNameByGNameTye(String groupName, String type) {
        try {
            String sql = "select `entity_name` from " + TABLE_GROUP_DETAIL + " where group_name = ? and `type` = ? and belong_inner = 1";
            return template.queryForList(sql, String.class, groupName, type);
        } catch (DataAccessException ex) {
            log.error("{}", ex);
            return Collections.emptyList();
        }
    }

    /**
    * @description 获取所有的行内/行外客户
    * @param belongInner
    * @return java.util.List<java.lang.String>
    * @author liulu
    * @date 2018/12/25
    */
    public List<String> findBelongInnderEntitiesName(Integer belongInner){
        try {
            String sql = "select `entity_name` from " + TABLE_GROUP_DETAIL + " where  belong_inner = ?";
            return template.queryForList(sql, String.class, belongInner);
        } catch (DataAccessException ex) {
            log.error("{}", ex);
            return Collections.emptyList();
        }

    }


}
