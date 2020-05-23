package com.haizhi.iap.follow.repo;

import com.haizhi.iap.follow.model.User;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepo {

    @Setter
    @Autowired
    @Qualifier(value = "followJdbcTemplate")
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

    public User findByUsername(String username) {
        try {
            String sql = "select * from " + TABLE_USERS + " where username = ?";
            return template.queryForObject(sql, USER_ROW_MAPPER, username);
        } catch (DataAccessException ex) {
            return null;
        }
    }

}
