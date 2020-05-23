package com.haizhi.iap.proxy.repo;

import com.haizhi.iap.proxy.model.User;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Created by chenbo on 17/2/17.
 */
@Repository
public class UserRepo {

    @Setter
    @Autowired
    JdbcTemplate template;

    private String TABLE_USERS = "bigdata_user";

    private final RowMapper<User> USER_ROW_MAPPER = new BeanPropertyRowMapper<>(User.class);

    public User findById(Long id) {
        try {
            String sql = "select * from " + TABLE_USERS + " where id = ?";
            return template.queryForObject(sql, USER_ROW_MAPPER, id);
        } catch (DataAccessException ex) {
            return null;
        }
    }
}
