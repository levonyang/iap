package com.haizhi.iap.search.repo;

import com.google.common.collect.Lists;
import com.haizhi.iap.search.model.BrowsingHistory;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * Created by chenbo on 17/2/21.
 */
@Slf4j
@Repository
public class BrowsingHistoryRepo {
    @Setter
    @Autowired
    JdbcTemplate template;

    private String TABLE_BROWSING_HISTORY = "browsing_history";

    private final RowMapper<BrowsingHistory> BROWSING_HISTORY_ROW_MAPPER = new BeanPropertyRowMapper<>(BrowsingHistory.class);

    public BrowsingHistory findByUserAndCompany(Long userId, String company) {
        try {
            String sql = "select * from " + TABLE_BROWSING_HISTORY + " where user_id = ? and company = ?";
            return template.queryForObject(sql, BROWSING_HISTORY_ROW_MAPPER, userId, company);
        } catch (DataAccessException ex) {
        }
        return null;
    }

    public int updateHistory(Long id) {
        try {
            String sql = "update " + TABLE_BROWSING_HISTORY + " set update_time = now() where id = ?";
            return template.update(sql, id);
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
        return 0;
    }

    public Long countByUser(Long userId) {
        try {
            String sql = "select count(1) from " + TABLE_BROWSING_HISTORY + " where user_id = ?";
            return template.queryForObject(sql, Long.class, userId);
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
        return 0l;
    }

    public List<BrowsingHistory> findByUser(Long userId, Integer offset, Integer count) {
        try {
            StringBuffer buffer = new StringBuffer("select * from " + TABLE_BROWSING_HISTORY + " where user_id = ? ");
            List<Object> args = Lists.newArrayList();
            args.add(userId);
            buffer.append(" order by update_time DESC ");

            if (offset != null && count != null) {
                buffer.append(" limit ?, ? ");
                args.add(offset);
                args.add(count);
            }
            return template.query(buffer.toString(), BROWSING_HISTORY_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public BrowsingHistory create(BrowsingHistory history) {
        try {
            final String sql = "insert ignore into " + TABLE_BROWSING_HISTORY +
                    " (user_id, company, create_time, update_time) " +
                    " values(?, ?, now(), now())";
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            template.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, history.getUserId());
                ps.setString(2, history.getCompany());

                return ps;
            }, holder);
            if (holder.getKey() != null) {
                history.setId(holder.getKey().longValue());
            } else {
                log.warn(TABLE_BROWSING_HISTORY + ": user_id={}, company={} 已存在", history.getUserId(), history.getCompany());
            }
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        return history;
    }

}
