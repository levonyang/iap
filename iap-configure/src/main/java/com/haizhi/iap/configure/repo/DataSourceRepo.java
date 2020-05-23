package com.haizhi.iap.configure.repo;

import com.google.common.collect.Lists;
import com.haizhi.iap.configure.model.DataSourceConfig;
import com.haizhi.iap.configure.model.SourceFieldMap;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;

/**
 * @Author dmy
 * @Date 2017/4/13 下午7:36.
 */
@Slf4j
@Repository
public class DataSourceRepo {

    @Setter
    @Autowired
    JdbcTemplate template;

    private static final String TABLE_DATASOURCE_CONFIG = "datasource_config";
    private static final String TABLE_SOURCE_FIELD_MAP = "source_field_map";
    private RowMapper<DataSourceConfig> DATASOURCE_ROW_MAPPER = new BeanPropertyRowMapper<>(DataSourceConfig.class);
    private RowMapper<SourceFieldMap> FIELD_ROW_MAPPER = new BeanPropertyRowMapper<>(SourceFieldMap.class);

    public List<DataSourceConfig> findConfigByCondition(String dataType, String sourceType,
                                                        Integer importStatus, Integer offset, Integer count) {
        try {
            StringBuffer buffer = new StringBuffer("select * from " + TABLE_DATASOURCE_CONFIG + " where 1 = 1 ");
            List<Object> args = Lists.newArrayList();
            if (dataType != null) {
                buffer.append(" and data_type = ? ");
                args.add(dataType);
            }

            if (sourceType != null) {
                buffer.append(" and source_type = ? ");
                args.add(sourceType);
            }

            if (importStatus != null) {
                buffer.append(" and import_status = ? ");
                args.add(importStatus);
            }

            buffer.append(" order by create_time desc");

            if (offset != null && count != null) {
                buffer.append(" limit ?,? ");
                args.add(offset);
                args.add(count);
            }

            return template.query(buffer.toString(), DATASOURCE_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
            return Collections.emptyList();
        }
    }

    public Long countConfigByCondition(String dataType, String sourceType, Integer importStatus) {
        try {
            StringBuffer buffer = new StringBuffer("select count(1) from " + TABLE_DATASOURCE_CONFIG + " where 1 = 1 ");
            List<Object> args = Lists.newArrayList();
            if (dataType != null) {
                buffer.append(" and data_type = ? ");
                args.add(dataType);
            }

            if (sourceType != null) {
                buffer.append(" and source_type = ? ");
                args.add(sourceType);
            }

            if (importStatus != null) {
                buffer.append(" and import_status = ? ");
                args.add(importStatus);
            }

            Long count = template.queryForObject(buffer.toString(), Long.class, args.toArray());
            if (count == null) {
                count = 0l;
            }
            return count;
        } catch (DataAccessException ex) {
            return 0l;
        }
    }

    public DataSourceConfig findConfigById(Long id) {
        try {
            String sql = "select * from " + TABLE_DATASOURCE_CONFIG + " where id = ?";
            return template.queryForObject(sql, DATASOURCE_ROW_MAPPER, id);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public DataSourceConfig findConfigByName(String name) {
        try {
            String sql = "select * from " + TABLE_DATASOURCE_CONFIG + " where name = ?";
            return template.queryForObject(sql, DATASOURCE_ROW_MAPPER, name);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public List<SourceFieldMap> getFieldsBySourceId(Long datasourceId) {
        try {
            String sql = "select * from " + TABLE_SOURCE_FIELD_MAP + " where source_config_id = ? ";
            return template.query(sql, FIELD_ROW_MAPPER, datasourceId);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public SourceFieldMap getFieldByFieldId(Long sourceFieldId) {
        try {
            String sql = "select * from " + TABLE_SOURCE_FIELD_MAP + " where id = ?";
            return template.queryForObject(sql, FIELD_ROW_MAPPER, sourceFieldId);
        } catch (DataAccessException ex) {

        }
        return null;
    }

    public DataSourceConfig createConfig(DataSourceConfig config) {
        String sql = "insert ignore into  " + TABLE_DATASOURCE_CONFIG + " " +
                "(`name`, data_type, source_type, host, port, username, password, `database`, " +
                "source_table, target_table, comment, update_mode, import_status, create_time, update_time) " +
                "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, now(), now())";

        GeneratedKeyHolder holder = new GeneratedKeyHolder();
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            if (config.getName() == null) {
                ps.setNull(1, Types.VARCHAR);
            } else {
                ps.setString(1, config.getName());
            }
            if (config.getDataType() == null) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, config.getDataType());
            }
            if (config.getSourceType() == null) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, config.getSourceType());
            }

            if (config.getHost() == null) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, config.getHost());
            }
            if (config.getPort() == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, config.getPort());
            }
            if (config.getUsername() == null) {
                ps.setNull(6, Types.VARCHAR);
            } else {
                ps.setString(6, config.getUsername());
            }
            if (config.getPassword() == null) {
                ps.setNull(7, Types.VARCHAR);
            } else {
                ps.setString(7, config.getPassword());
            }
            if (config.getDatabase() == null) {
                ps.setNull(8, Types.VARCHAR);
            } else {
                ps.setString(8, config.getDatabase());
            }
            if (config.getSourceTable() == null) {
                ps.setNull(9, Types.VARCHAR);
            } else {
                ps.setString(9, config.getSourceTable());
            }
            if (config.getTargetTable() == null) {
                ps.setNull(10, Types.VARCHAR);
            } else {
                ps.setString(10, config.getTargetTable());
            }
            if (config.getComment() == null) {
                ps.setString(11, "");
            } else {
                ps.setString(11, config.getComment());
            }
            if (config.getUpdateMode() == null) {
                ps.setInt(12, 1);
            } else {
                ps.setInt(12, config.getUpdateMode());
            }

            return ps;
        }, holder);
        if (holder.getKey() != null) {
            config.setId(holder.getKey().longValue());
        } else {
            log.warn(TABLE_DATASOURCE_CONFIG + ": name={} 已存在", config.getName());
        }
        return config;
    }

    public boolean createFieldMap(Long sourceConfigId, List<SourceFieldMap> fieldMapList) {
        if (sourceConfigId == null || fieldMapList == null || fieldMapList.size() < 1) {
            return false;
        }
        final String sql = "insert ignore into " + TABLE_SOURCE_FIELD_MAP + " " +
                "(source_config_id, source_field, is_key, `name`, create_time, update_time) " +
                "values (?, ?, ?, ?, now(), now()) ";
        template.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, sourceConfigId);
                if (fieldMapList.get(i).getSourceField() == null) {
                    ps.setNull(2, Types.VARCHAR);
                } else {
                    ps.setString(2, fieldMapList.get(i).getSourceField());
                }

                ps.setInt(3, fieldMapList.get(i).getIsKey());

                if (fieldMapList.get(i).getName() == null) {
                    ps.setNull(4, Types.VARCHAR);
                } else {
                    ps.setString(4, fieldMapList.get(i).getName());
                }
            }

            @Override
            public int getBatchSize() {
                return fieldMapList.size();
            }
        });
        return true;
    }

    public boolean deleteConfig(Long sourceConfigId) {
        if (sourceConfigId != null) {
            String sql = "delete from " + TABLE_DATASOURCE_CONFIG + " where id = ?";
            template.update(sql, sourceConfigId);
        }
        return true;
    }

    public boolean deleteFieldMap(Long sourceConfigId) {
        if (sourceConfigId != null) {
            String sql = "delete from " + TABLE_SOURCE_FIELD_MAP + " where source_config_id = ?";
            template.update(sql, sourceConfigId);
        }
        return true;
    }

    public void updateConfig(Long id, Integer status, Double percent) {
        if (status == null || percent == null) {
            return;
        }
        String sql = "update " + TABLE_DATASOURCE_CONFIG + " set import_status = ?, percent = ?, update_time = now() where id = ?";
        template.update(sql, status, percent, id);
    }

    public void updateConfig(Long id, Integer status, Double percent, Long actualNum) {
        if (status == null || percent == null || actualNum == null) {
            return;
        }
        String sql = "update " + TABLE_DATASOURCE_CONFIG + " set import_status = ?, percent = ?, actual_num= ?, " +
                "update_time = now() where id = ?";
        template.update(sql, status, percent, actualNum, id);
    }

    public void updateConfig(Long id, Integer status) {
        if (id == null || status == null) {
            return;
        }
        String sql = "update " + TABLE_DATASOURCE_CONFIG + " set import_status = ?, update_time = now() where id = ?";
        template.update(sql, status, id);
    }

    public void updateConfig(Long id, Integer status, Long actualNum) {
        if (id == null || status == null || actualNum == null) {
            return;
        }
        String sql = "update " + TABLE_DATASOURCE_CONFIG + " set import_status = ?, actual_num = ?, update_time = now() where id = ?";
        template.update(sql, status, actualNum, id);
    }

    public void updateConfig(Long id, Double percent) {
        if (id == null || percent == null) {
            return;
        }
        String sql = "update " + TABLE_DATASOURCE_CONFIG + " set percent = ?, update_time = now() where id = ?";
        template.update(sql, percent, id);
    }

    public void updateOriginNum(Long id, Long originNum) {
        if (id == null || originNum == null) {
            return;
        }
        String sql = "update " + TABLE_DATASOURCE_CONFIG + " set origin_num = ?, update_time = now() where id = ?";
        template.update(sql, originNum, id);
    }

    public void updateActualNum(Long id, Long actualNum) {
        if (id == null || actualNum == null) {
            return;
        }
        String sql = "update " + TABLE_DATASOURCE_CONFIG + " set actual_num = ?, update_time = now() where id = ?";
        template.update(sql, actualNum, id);
    }

    public void updateMode(Long id, Integer updateMode) {
        if (id == null || updateMode == null) {
            return;
        }
        String sql = "update " + TABLE_DATASOURCE_CONFIG + " set update_mode = ?, update_time = now() where id = ?";
        template.update(sql, updateMode, id);
    }

}
