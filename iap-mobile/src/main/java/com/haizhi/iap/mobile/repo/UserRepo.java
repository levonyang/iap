package com.haizhi.iap.mobile.repo;

import com.haizhi.iap.mobile.bean.User;
import com.haizhi.iap.mobile.conf.SqlSchemaConstants;
import org.springframework.stereotype.Repository;

/**
 * Created by thomas on 18/4/13.
 */
@Repository
public class UserRepo extends BasicSqlRepo<User>
{
    public UserRepo()
    {
        super(User.class);
    }

    public User findOneByName(String username)
    {
        String sql = "SELECT * FROM {{table}} WHERE username = ?".replace("{{table}}", SqlSchemaConstants.TABLE_USER);
        return findOne(sql, username);
    }
}
