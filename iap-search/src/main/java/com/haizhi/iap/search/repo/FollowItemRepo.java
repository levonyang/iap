package com.haizhi.iap.search.repo;

import com.haizhi.iap.common.auth.DefaultSecurityContext;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Created by chenbo on 2017/11/8.
 */
@Slf4j
@Repository
public class FollowItemRepo {
    @Setter
    @Autowired
    JdbcTemplate template;

    private static String TABLE_FOLLOW_ITEM = "follow_item";

    public boolean checkCompanyFollowed(String companyName) {
        Long userId = DefaultSecurityContext.getUserId();
        if (userId == null) {
            return false;
        } else {
            try {
                String sql = "select count(1) from " + TABLE_FOLLOW_ITEM +
                        " where user_id = ? and company_name = ? and is_follow = true";
                Long count = template.queryForObject(sql, Long.class, userId, companyName);
                return count != null && !count.equals(0l);
            } catch (DataAccessException ex) {
                log.error(ex.getMessage());
            }
        }
        return false;
    }

}
