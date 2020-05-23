package com.haizhi.iap.follow.repo;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author mtl
 * @Description:
 * @date 2020/3/10 14:18
 */
@Slf4j
@Repository
public class CustuploadRepo {

    @Setter
    @Autowired
    @Qualifier(value = "followJdbcTemplate")
    private JdbcTemplate template;

    private RowMapper MAPROWMAPPER = new ColumnMapRowMapper();

    public void batchInsert(List<Map> list,String batchid,String userid) {
        try {
            final String sql = "insert into cust_upload (batchid,serial,company,authperson,idnumber,phone,applydate,userid,status) values (?,?,?,?,?,?,?,?,?) ";
//            template.batchUpdate(connection -> {
//                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
//                ps.setString(1, userid);
//                ps.setInt(2, 0);
//                return ps;
//            });
            template.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setString(1,batchid);
                    Map map = list.get(i);
//                    String company = (String) map.get("company");
                    String serial = getFromMap(map,"serial",null);
                    String company = getFromMap(map,"company",null);
                    String authperson = getFromMap(map,"authperson",null);
                    String idnumber = getFromMap(map,"idnumber",null);
                    String phone = getFromMap(map,"phone",null);
                    String applydate = getFromMap(map,"applydate",null);
                    ps.setString(2,serial);
                    ps.setString(3,company);
                    ps.setString(4,authperson);
                    ps.setString(5,idnumber);
                    ps.setString(6,phone);
                    ps.setString(7,applydate);
                    ps.setString(8,userid);
                    ps.setInt(9,1);
                }

                @Override
                public int getBatchSize() {
                    return list.size();
                }
            });

        } catch (Exception ex) {
            log.error("{}", ex);
            throw ex;
        }
    }

    private String getFromMap(Map map,String key,String rpl){
        Object obj = map.get(key);
        if(null == obj){
            return rpl;
        }
        return obj.toString();
    }

    public List<Map> findByBatchid(String batchid) {
        String sql = "select * from cust_upload where batchid = ? ";
        return template.query(sql,new Object[]{batchid},MAPROWMAPPER);
    }

    /**
     * 通过batchid删除
     * @param userid
     */
    public void updateStatus(int status,String userid) {
        try {
            final String sql = "update cust_upload set status = ? where userid = ? ";
            template.update(sql, new PreparedStatementSetter(){
                @Override
                public void setValues(PreparedStatement preparedStatement) throws SQLException {
                    preparedStatement.setInt(1,status);
                    preparedStatement.setString(2,userid);
                }
            });
        } catch (Exception ex) {
            log.error("{}", ex);
            throw ex;
        }
    }

    /**
     * 查询用户最新的上传清单
     * @param userid
     * @param status
     * @return
     */
    public List<Map> findByUserid(String userid,int status) {
        String sql = "select * from cust_upload where userid = ? and status = ? "; //查询用户最新的上传清单
        return template.query(sql,new Object[]{userid,status},MAPROWMAPPER);
    }
}
