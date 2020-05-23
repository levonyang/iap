package com.haizhi.iap.follow.repo;

import com.google.common.collect.Lists;
import com.haizhi.iap.follow.model.TypeQuery;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by chenbo on 17/1/16.
 */
@Repository
public class TypeQueryRepo {
    @Setter
    @Autowired
    @Qualifier(value = "followJdbcTemplate")
    JdbcTemplate template;

    public static final String TABLE_TYPE_QUERY = "type_query";

    private RowMapper<TypeQuery> TYPE_QUERY_ROW_MAPPER = new BeanPropertyRowMapper<>(TypeQuery.class);

    public TypeQuery findByCondition(Long userId, String typeName) {
        StringBuffer buffer = new StringBuffer("select * from " + TABLE_TYPE_QUERY + " where true ");
        List<Object> args = Lists.newArrayList();
        if (userId == null) {
            userId = 0l;
        }
        buffer.append("and user_id = ? ");
        args.add(userId);

        buffer.append("and type_name = ? ");
        args.add(typeName);
        try {
            return template.queryForObject(buffer.toString(), TYPE_QUERY_ROW_MAPPER, args.toArray());
        }catch (DataAccessException ex){
        }
        return null;
    }
}
