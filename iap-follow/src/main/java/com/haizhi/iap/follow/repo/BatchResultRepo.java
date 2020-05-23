package com.haizhi.iap.follow.repo;

import com.haizhi.iap.follow.enums.TaskStatus;
import com.haizhi.iap.follow.model.Task;
import com.haizhi.iap.follow.utils.DateUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * @author mtl
 * @Description:
 * @date 2020/3/12 11:12
 */
@Slf4j
@Repository
public class BatchResultRepo {

    private RowMapper MAP_ROW_MAPPER = new ColumnMapRowMapper();

    @Setter
    @Autowired
    @Qualifier(value = "followJdbcTemplate")
    private JdbcTemplate template;


    /**
     * 保存批次查询结果内容
     * @param batchid
     * @param content
     * @return
     */
    public int insert(String batchid,String content,String expireTime) {
        try {
            final String sql = "insert into batch_result (batchid,content,expiretime) values (?, ?, ?)";
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            template.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1,batchid);
                ps.setString(2,content);
                ps.setString(3,expireTime);
                return ps;
            }, holder);
            if (holder.getKey() != null) {
                return holder.getKey().intValue();
            }
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        return -1;
    }

    /**
     * 通过batchid查询图谱数据
     * @param batchid
     * @return
     */
    public List<Map> findByBatchid(String batchid){
        String sql = "select * from batch_result where batchid = ? ";
        return template.query(sql, MAP_ROW_MAPPER, batchid);
    }

    /**
     * 通过batchid查询图谱数据(未超时的)
     * @param batchid
     * @return
     */
    public List<Map> findUnExpireByBatchid(String batchid,String time){
        String sql = "select * from batch_result where batchid = ? and expiretime >= ? ";
        return template.query(sql, MAP_ROW_MAPPER, batchid,time);
    }

    public void deleteByBatchid(String batchid) {
        try {
            String sql = "delete from batch_result where batchid = ? ";
            template.update(sql, new PreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps) throws SQLException {
                    ps.setString(1,batchid);
                }
            });
        } catch (Exception ex) {
            log.error("{}", ex);
        }
    }

}
