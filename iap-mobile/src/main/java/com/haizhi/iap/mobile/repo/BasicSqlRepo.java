package com.haizhi.iap.mobile.repo;

import com.haizhi.iap.common.utils.SpringContextHolder;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by thomas on 18/4/13.
 */
@Data
public class BasicSqlRepo<T>
{
    protected static JdbcTemplate JDBC_TEMPLATE = SpringContextHolder.getBean(JdbcTemplate.class);
    protected static NamedParameterJdbcTemplate NAMED_PARAMETER_JDBC_TEMPLATE = new NamedParameterJdbcTemplate(JDBC_TEMPLATE);
    protected RowMapper<T> rowMapper;

    public BasicSqlRepo(Class<T> cls)
    {
        this(new BeanPropertyRowMapper<>(cls));
    }

    public BasicSqlRepo(RowMapper<T> rowMapper)
    {
        this.rowMapper = rowMapper;
    }

    public T findOne(String sql, Object... args)
    {
        if(ArrayUtils.isEmpty(args))
            return JDBC_TEMPLATE.query(sql, rowMapper).stream().findFirst().orElse(null);
        return JDBC_TEMPLATE.query(sql, rowMapper, args).stream().findFirst().orElse(null);
    }

    /**
     * 使用命名参数来查找
     *
     * @return
     */
    public T findOne(String sql, Map<String, Object> namedParams)
    {
        if(CollectionUtils.isEmpty(namedParams))
            return NAMED_PARAMETER_JDBC_TEMPLATE.query(sql, rowMapper).stream().findFirst().orElse(null);
        return NAMED_PARAMETER_JDBC_TEMPLATE.query(sql, namedParams, rowMapper).stream().findFirst().orElse(null);
    }

    public List<T> findAll(String sql, Object... args)
    {
        if(ArrayUtils.isEmpty(args))
            return JDBC_TEMPLATE.query(sql, rowMapper);
        return JDBC_TEMPLATE.query(sql, rowMapper, args);
    }

    /**
     * 使用命名参数来查找
     *
     * @return
     */
    public List<T> findAll(String sql, Map<String, Object> namedParams)
    {
        if(CollectionUtils.isEmpty(namedParams))
            return NAMED_PARAMETER_JDBC_TEMPLATE.query(sql, rowMapper);
        return NAMED_PARAMETER_JDBC_TEMPLATE.query(sql, namedParams, rowMapper);
    }

    public Long count(String sql, Object... args)
    {
        if(ArrayUtils.isEmpty(args))
            return JDBC_TEMPLATE.queryForObject(sql, Long.class);
        return JDBC_TEMPLATE.queryForObject(sql, Long.class, args);
    }

    /**
     * 使用命名参数来查找
     *
     * @return
     */
    public Long count(String sql, Map<String, Object> namedParams)
    {
        return NAMED_PARAMETER_JDBC_TEMPLATE.queryForObject(sql, namedParams, Long.class);
    }

    /**
     * 支持insert, update, delete操作
     *
     * @param sql
     * @param args
     * @return
     */
    public int update(String sql, Object... args)
    {
        if(ArrayUtils.isEmpty(args))
            return JDBC_TEMPLATE.update(sql);
        return JDBC_TEMPLATE.update(sql, args);
    }

    public Map<String, Object> rawQueryOne(String sql, Object... args)
    {
        if(ArrayUtils.isEmpty(args))
            return JDBC_TEMPLATE.queryForMap(sql);
        return JDBC_TEMPLATE.queryForMap(sql, args);
    }

    public List<Map<String, Object>> rawQueryAll(String sql, Object... args)
    {
        if(ArrayUtils.isEmpty(args))
            return JDBC_TEMPLATE.queryForList(sql);
        return JDBC_TEMPLATE.queryForList(sql, args);
    }
}
