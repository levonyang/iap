package com.haizhi.iap.mobile.repo;

import com.haizhi.iap.mobile.bean.FollowList;
import com.haizhi.iap.mobile.conf.SqlSchemaConstants;
import org.springframework.stereotype.Repository;

/**
 * Created by thomas on 18/4/13.
 */
@Repository
public class FollowListRepo extends BasicSqlRepo<FollowList>
{
    public FollowListRepo()
    {
        super(FollowList.class);
    }

    /**
     * 找出某个用户的默认关注列表
     *
     * @param userId
     * @return
     */
    public FollowList findDefault(Long userId)
    {
        String sql = "SELECT * FROM {{table}} WHERE user_id = ? AND name = ?".replace("{{table}}", SqlSchemaConstants.TABLE_FOLLOW_LIST);
        return findOne(sql, userId, FollowList.DEFAULT_NAME);
    }
}
