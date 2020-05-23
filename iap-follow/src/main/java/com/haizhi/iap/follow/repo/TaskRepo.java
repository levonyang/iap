package com.haizhi.iap.follow.repo;

import com.google.common.collect.Lists;
import com.haizhi.iap.common.auth.DefaultSecurityContext;
import com.haizhi.iap.follow.enums.TaskStatus;
import com.haizhi.iap.follow.model.Task;
import com.haizhi.iap.follow.service.PDFExportProcess;
import com.haizhi.iap.follow.utils.DateUtils;
import com.itextpdf.text.BadElementException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;

/**
 * Created by chenbo on 17/1/12.
 */
@Slf4j
@Repository
public class TaskRepo {

    @Setter
    @Autowired
    @Qualifier(value = "followJdbcTemplate")
    JdbcTemplate template;

    @Setter
    @Autowired
    PDFExportProcess pdfExportProcess;

    private static final String TABLE_TASK = "task";

    private RowMapper<Task> TASK_ROW_MAPPER = new BeanPropertyRowMapper<>(Task.class);

    public Task create(Task task) {
        try {
            final String sql = "insert into " + TABLE_TASK +
                    "(`name`, user_id, follow_list_id, data_type, mode, begin_date, end_date, create_time, expire_days, time_option, status, type, company_names) " +
                    "values (?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, ?, ?)";
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            template.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, task.getName());
                ps.setString(2, task.getUserId());
                if (task.getFollowListId() == null) {
                    ps.setNull(3, Types.INTEGER);
                } else {
                    ps.setLong(3, task.getFollowListId());
                }
                ps.setString(4, task.getDataType());
                ps.setInt(5, task.getMode());
                if (task.getBeginDate() == null) {
                    ps.setNull(6, Types.DATE);
                } else {
                    ps.setString(6, DateUtils.FORMAT.format(task.getBeginDate()));
                }
                if (task.getEndDate() == null) {
                    ps.setNull(7, Types.DATE);
                } else {
                    ps.setString(7, DateUtils.FORMAT.format(task.getEndDate()));
                }
                ps.setLong(8, task.getExpireDays());
                if (task.getTimeOption() == null) {
                    ps.setNull(9, Types.TINYINT);
                } else {
                    ps.setInt(9, task.getTimeOption());
                }
                ps.setInt(10, (task.getStatus() == null) ? TaskStatus.WAITING.getCode() : task.getStatus());
                ps.setString(11, task.getType());

                if (task.getCompanyNames() == null) {
                    ps.setNull(12, Types.VARCHAR);
                } else {
                    ps.setString(12, task.getCompanyNames());
                }
                return ps;
            }, holder);
            if (holder.getKey() != null) {
                task.setId(holder.getKey().longValue());
            }
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        return task;
    }

    public Task findById(Long taskId) {
        try {
            String sql = "select * from " + TABLE_TASK + " where id = ? and deleted = 0";
            return template.queryForObject(sql, TASK_ROW_MAPPER, taskId);
        } catch (DataAccessException ex) {
        }
        return null;
    }

    public Task findByName(String name) {
        try {
            String curUserId = DefaultSecurityContext.getUserId().toString();
            String sql = "select * from " + TABLE_TASK + " where name = ? and user_id = ? and deleted = 0";
            return template.queryForObject(sql, TASK_ROW_MAPPER, name, curUserId);
        } catch (DataAccessException ex) {
        }
        return null;
    }

    public List<Task> getAll() {
        try {
            String sql = "select * from " + TABLE_TASK + " where deleted = 0";
            return template.query(sql, TASK_ROW_MAPPER);
        } catch (DataAccessException ex) {
        }
        return Lists.newArrayList();
    }

    public List<Task> findByCondition(Long userId, Integer offset, Integer count, String type, String fuzzyKey) {
        try {
            StringBuffer buffer = new StringBuffer("select * from " + TABLE_TASK + "  where deleted = 0 ");
            List<Object> args = Lists.newArrayList();

            if (userId != null) {
                buffer.append(" and user_id = ? ");
                args.add(userId.toString());
            }

            if (type != null) {
                if (type.equals("pdf")) {
                    buffer.append(" and type = 'pdf' ");
                } else if (type.equals("excel")) {
                    buffer.append(" and (type is null or type = 'excel') ");
                } else {
                    buffer.append(" and type = 'pdfexcel' ");  // 什么都找不出
                }
            }
            if (fuzzyKey != null) {
                buffer.append(" and name like ? ");
                args.add("%" + fuzzyKey + "%");
            }
            buffer.append(" order by create_time DESC ");

            if (offset != null && count != null) {
                buffer.append(" limit ?, ?");
                args.add(offset);
                args.add(count);
            }
            return template.query(buffer.toString(), TASK_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
        }
        return Lists.newArrayList();
    }

    public void delete(Long taskId) {
        try {
            String sql = "update " + TABLE_TASK + " set deleted = 1 where id = ?";
            template.update(sql, taskId);
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

    public void updateStatus(Long id, Integer code) {
        try {
            String sql = "update " + TABLE_TASK + " set `status` = ? where id = ?";
            int update = template.update(sql, code, id);
            int count = 0;
            while (code.equals(TaskStatus.CANCELED.getCode())&& update <= 0 && count < 5) {
                update = template.update(sql, 3, id);
                log.error("cancel attempt {} times, status = {}, id = {}", count, code, id);
                count++;
            }
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

    public Long countAll(Long userId, String type, String fuzzyKey) {
        try {
            List<Object> args = Lists.newArrayList();
            StringBuffer buffer = new StringBuffer("select count(1) from " + TABLE_TASK + " where user_id = ? and deleted = 0 ");
            args.add(userId.toString());
            if (type != null) {
                if (type.equals("pdf")) {
                    buffer.append(" and type = 'pdf' ");
                } else if (type.equals("excel")) {
                    buffer.append(" and (type is null or type = 'excel') ");
                } else {
                    buffer.append(" and type = 'pdfexcel' ");  // 什么都找不出
                }
            }
            if (fuzzyKey != null) {
                buffer.append(" and name like ? ");
                args.add("%" + fuzzyKey + "%");
            }
            return (Long)template.queryForMap(buffer.toString(), args.toArray()).get("count(1)");
        } catch (DataAccessException ex) {
            //log.error("{}", ex);
        }
        return null;
    }

    public void update(Task task) {
        try {
            StringBuffer buffer = new StringBuffer("update " + TABLE_TASK + " set update_time = now()");
            List<Object> args = Lists.newArrayList();

//            if (task.getFinishTime() != null) {
//                buffer.append(" , finish_time = ? ");
//                args.add(DateUtils.FORMAT.format(task.getFinishTime()));
//            }

            if (task.getExportFile() != null) {
                buffer.append(" , export_file = ? ");
                args.add(task.getExportFile());
            }

            if (task.getExportFileLength() != null) {
                buffer.append(" , export_file_length = ? ");
                args.add(task.getExportFileLength());
            }

            if (task.getPercent() != null) {
                buffer.append(" , percent = ? ");
                args.add(task.getPercent());
            }

            buffer.append(" where id = ? ");
            args.add(task.getId());

            template.update(buffer.toString(), args.toArray());
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }

    public void finish(Long id, String exportFile, Long fileLength) {
        try {
            String sql = "update " + TABLE_TASK + " set percent = 100, finish_time = now(), status = 2," +
                    " export_file = ?, export_file_length = ? where id = ? ";
            template.update(sql, exportFile, fileLength, id);
        } catch (DataAccessException ex) {
            log.error("{}", ex);
        }
    }
    public String screenShot (String companyName) {
        String filename= null;
        try {
            filename = pdfExportProcess.screenShot(companyName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BadElementException e) {
            e.printStackTrace();
        }
        return filename;
    }
}
