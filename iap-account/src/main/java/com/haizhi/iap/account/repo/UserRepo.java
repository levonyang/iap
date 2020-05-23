package com.haizhi.iap.account.repo;

import com.google.common.collect.Lists;
import com.haizhi.iap.account.model.User;
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
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

@Slf4j
@Repository
public class UserRepo {

    @Setter
    @Autowired
    JdbcTemplate template;

    private String TABLE_USERS = "bigdata_user";

    public static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final RowMapper<User> USER_ROW_MAPPER = new BeanPropertyRowMapper<>(User.class);

    public User findById(Long id) {
        try {
            String sql = "select * from " + TABLE_USERS + " where id = ? and is_deleted = 0";
            return template.queryForObject(sql, USER_ROW_MAPPER, id);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public User findByUsername(String username) {
        try {
            String sql = "select * from " + TABLE_USERS + " where username = ? and is_deleted = 0";
            return template.queryForObject(sql, USER_ROW_MAPPER, username);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public User findByUsernameIgnoreDel(String username) {
        try {
            String sql = "select * from " + TABLE_USERS + " where username = ?";
            return template.queryForObject(sql, USER_ROW_MAPPER, username);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public Long countAll() {
        try {
            String sql = "select count(1) from " + TABLE_USERS + " where is_deleted = 0";
            return template.queryForObject(sql, Long.class);
        } catch (DataAccessException ex) {
           // log.error("{}", ex);
        }
        return 0l;
    }

    public Long countByGroup(Long groupId) {
        try {
            String sql = "select count(1) from " + TABLE_USERS + " where is_deleted = 0 and group_id = ?";
            return template.queryForObject(sql, Long.class, groupId);
        } catch (DataAccessException ex) {
           // log.error("{}", ex);
        }
        return 0l;
    }

    public User create(User user) {
        try {
            final String sql = "insert into " + TABLE_USERS +
                    "(username, `password`, email, phone, register_time) " +
                    "select ?, ?, ?, ?, now() from DUAL where not exists(select id from " + TABLE_USERS + " where username = ?)";
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            template.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                if (user.getEmail() == null) {
                    ps.setNull(3, Types.VARCHAR);
                } else {
                    ps.setString(3, user.getEmail());
                }

                if (user.getPhone() == null) {
                    ps.setNull(4, Types.VARCHAR);
                } else {
                    ps.setString(4, user.getPhone());
                }
                ps.setString(5, user.getUsername());

                return ps;
            }, holder);
            if (holder.getKey() != null) {
                user.setId(holder.getKey().longValue());
            } else {
                log.warn(TABLE_USERS + ": username={} 已存在", user.getUsername());
            }
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        return user;
    }

    public void update(User user) {
        try {
            String sql = "update " + TABLE_USERS + " " +
                    "set password=?, email=?, phone=?, role_id=?, last_login_time=?, login_count=?, update_time=now(), group_id=?, activated=? " +
                    "where id=?";

            template.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, user.getPassword());
                if (user.getEmail() == null) {
                    ps.setString(2, "");
                } else {
                    ps.setString(2, user.getEmail());
                }

                if (user.getPhone() == null) {
                    ps.setString(3, "");
                } else {
                    ps.setString(3, user.getPhone());
                }

                if (user.getRoleId() == null) {
                    ps.setNull(4, Types.INTEGER);
                } else {
                    ps.setLong(4, user.getRoleId());
                }

                if (user.getLastLoginTime() == null) {
                    ps.setNull(5, Types.VARCHAR);
                } else {
                    ps.setString(5, FORMAT.format(user.getLastLoginTime()));
                }
                if (user.getLoginCount() == null) {
                    ps.setLong(6, 0l);
                } else {
                    ps.setLong(6, user.getLoginCount());
                }
                if (user.getGroupId() == null) {
                    ps.setNull(7, Types.INTEGER);
                } else {
                    ps.setLong(7, user.getGroupId());
                }

                if (user.getActivated() == null) {
                    ps.setNull(8, Types.INTEGER);
                } else {
                    ps.setLong(8, user.getActivated());
                }

                ps.setLong(9, user.getId());

                return ps;
            });
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

    public void updatePassword(User user) {
        try {
            String sql = "update " + TABLE_USERS + " set password=?, update_time=NOW() WHERE id=?";
            template.update(sql, user.getPassword(), user.getId());
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

    public void delete(Long id) {
        try {
            String sql = "update " + TABLE_USERS + " set is_deleted = 1, update_time=NOW() WHERE id=?";
            template.update(sql, id);
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

    public List<User> findByCondition(Long groupId, Integer offset, Integer count) {
        try {
            StringBuffer buffer = new StringBuffer("select * from " + TABLE_USERS + " where is_deleted = 0 ");
            List<Object> args = Lists.newArrayList();
            if (groupId != null) {
                buffer.append(" and group_id = ? ");
                args.add(groupId);
            }

            if (offset != null && count != null) {
                buffer.append("limit ?, ?");
                args.add(offset);
                args.add(count);
            }

            return template.query(buffer.toString(), USER_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
            //log.error("{}", ex);
            return Lists.newArrayList();
        }
    }

    public void removeFromGroup(Long userId) {
        try {
            String sql = "update " + TABLE_USERS + " set group_id = null, update_time=NOW() WHERE id=?";
            template.update(sql, userId);
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }
}
