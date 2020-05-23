package com.haizhi.iap.follow.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.follow.model.config.AbstractConfig;
import com.haizhi.iap.follow.utils.JsonUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/12/11.
 */
@Slf4j
@Repository
public abstract class AbstractConfigRepo<T extends AbstractConfig> {
    @Setter
    @Autowired
    @Qualifier(value = "followJdbcTemplate")
    JdbcTemplate template;

    @Setter
    @Autowired
    ObjectMapper objectMapper;

    private String TABLE_CONFIG = "user_config";

    public abstract T getConfigInstance(Integer type);

    public List<T> findRisk(Long userId) {
        return findByType(userId, 100, 200);
    }

    public List<T> findMarket(Long userId) {
        return findByType(userId, 200, 300);
    }

    public List<T> findMacro(Long userId) {
        return findByType(userId, 300, 400);
    }

    public T findMacroByType(Long userId, Integer type) {
        return findByType(userId, type);
    }

    public List<T> findConduct(Long userId) {
        return findByType(userId, 400, 500);
    }

    public T save(T config) {
        try {
            String sql = "insert ignore into " + TABLE_CONFIG +
                    " (user_id, `type`, `param`, `enable`, create_time, update_time)" +
                    " values(?, ?, ?, ?, now(), now())";
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            template.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, config.getUserId());
                ps.setInt(2, config.getType());
                if (config.getParam() == null) {
                    ps.setNull(3, Types.VARBINARY);
                } else {
                    ps.setString(3, JsonUtil.formatJSON(config.getParam()));
                }
                ps.setInt(4, config.getEnable());
                return ps;
            }, holder);
            if (holder.getKey() != null) {
                config.setId(holder.getKey().longValue());
            } else {
                log.warn(TABLE_CONFIG + ": user_id={}, type={} 已存在", config.getUserId(), config.getType());
            }
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
        return config;
    }

    public boolean update(AbstractConfig config) {
        if (config.getId() == null || config.getEnable() == null || config.getParam() == null) {
            return false;
        }

        try {
            String sql = "update " + TABLE_CONFIG + " set param=?, enable = ?, update_time = now() where id = ?";
            int updateNum = template.update(sql, JsonUtil.formatJSON(config.getParam()), config.getEnable(), config.getId());
            return updateNum > 0;
        } catch (DataAccessException ex) {
            log.error("{}", ex);
            return false;
        }
    }

    public List<T> findByType(Long userId, Integer typeBegin, Integer typeEnd) {
        try {
            StringBuffer buffer = new StringBuffer("select * from " + TABLE_CONFIG + " where 1=1 ");
            List<Object> args = Lists.newArrayList();

            if (userId != null) {
                buffer.append(" and user_id = ? ");
                args.add(userId);
            }
            if (typeBegin != null) {
                buffer.append(" and type > ? ");
                args.add(typeBegin);
            }

            if (typeEnd != null) {
                buffer.append(" and type < ? ");
                args.add(typeEnd);
            }
            return template.query(buffer.toString(), new ConfigRowMapper(), args.toArray());
        } catch (DataAccessException ex) {
            return Collections.emptyList();
        }
    }

    public T findByType(Long userId, Integer type) {
        try {
            StringBuffer buffer = new StringBuffer("select * from " + TABLE_CONFIG + " where 1=1 ");
            List<Object> args = Lists.newArrayList();

            if (userId != null) {
                buffer.append(" and user_id = ? ");
                args.add(userId);
            }
            if (type != null) {
                buffer.append(" and type = ? ");
                args.add(type);
            }
            buffer.append(" and enable = 1 ");
            return template.queryForObject(buffer.toString(), new ConfigRowMapper(), args.toArray());
        } catch (DataAccessException ex) {
            return null;
        }
    }

    private class ConfigRowMapper implements RowMapper<T> {
        @Override
        public T mapRow(ResultSet resultSet, int i) throws SQLException {
            T config = getConfigInstance(resultSet.getInt("type"));
            if (config == null) {
                log.error("存在脏数据，type {}" + resultSet.getInt("type"));
                return null;
            }
            config.setId(resultSet.getLong("id"));
            config.setUserId(resultSet.getLong("user_id"));
            config.setType(resultSet.getInt("type"));
            config.setEnable(resultSet.getInt("enable"));
            try {
                if (!StringUtils.isEmpty(resultSet.getString("param"))) {
                    config.setParam((Map<String, Object>) objectMapper.readValue(resultSet.getString("param"), HashMap.class));
                }
            } catch (Exception e) {
                log.error("字段detail属性解析错误, id: {}, {}", config.getId(), e);
                throw new ServiceAccessException(-1, String.format("字段param属性解析错误, id:%s", config.getId()));
            }

            return config;
        }
    }
}
