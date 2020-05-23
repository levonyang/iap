package com.haizhi.iap.follow.repo;

import com.google.common.collect.Lists;
import com.haizhi.iap.follow.model.MacroNewsInfo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;

/**
 * @Author dmy
 * @Date 2017/12/22 下午1:10.
 */
@Slf4j
@Repository
public class MacroStoreRepo {

    @Setter
    @Autowired
    @Qualifier(value = "followJdbcTemplate")
    JdbcTemplate template;

    public static final String TABLE_MACRO_STORE = "macro_store";

    public boolean create(Long userId, Integer type, String macroId) {
        try {
            final String sql = "insert ignore into " + TABLE_MACRO_STORE +
                    "(macro_id, type, user_id, create_time) " +
                    "value(?, ?, ?, now())";
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            template.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, macroId);
                ps.setInt(2, type);
                ps.setLong(3, userId);

                return ps;
            }, holder);
            if (holder.getKey() != null) {
                return true;
            } else {
                log.warn(TABLE_MACRO_STORE + ": macro_id={}, user_id={}, type={} 已存在", macroId, userId, type);
            }
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
        return false;
    }

    public boolean delete(Long userId, Integer type, String macroId) {
        try {
            final String sql = "delete from " + TABLE_MACRO_STORE + " where macro_id = ? and user_id = ? and type = ? ";
            return template.update(sql, macroId, userId, type) < 1 ? false : true;
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
        return false;
    }

    public boolean isStore(Long userId, Integer type, String macroId) {
        try {
            String sql = "select count(*) from " + TABLE_MACRO_STORE + " where macro_id = ? and user_id = ? and type = ? ";
            return template.queryForObject(sql, Integer.class, macroId, userId, type) < 1 ? false : true;
        } catch (DataAccessException ex) {
            return false;
        }
    }

    public List<String> getStores(Long userId, Integer type, Integer offset, Integer count) {
        try {
            String sql = "select macro_id from " + TABLE_MACRO_STORE +
                    " where user_id = ? and type = ? order by create_time DESC limit ?, ?";
            List<MacroNewsInfo> macroNewsInfos =  template.query(sql, new BeanPropertyRowMapper<>(MacroNewsInfo.class), userId, type, offset, count);
            List<String> result = Lists.newArrayList();
            macroNewsInfos.stream().forEach(macroNewsInfo -> {
                result.add(macroNewsInfo.getMacroId());
            });
            return result;
        } catch (DataAccessException ex) {
           // log.error("{}", ex);
            return Collections.emptyList();
        }
    }

    public Integer getCount(Long userId, Integer type) {
        try {
            String sql = "select count(*) from " + TABLE_MACRO_STORE + " where user_id = ? and type = ? ";
            return template.queryForObject(sql, Integer.class, userId, type);
        } catch (DataAccessException ex) {
            return -1;
        }
    }
}
