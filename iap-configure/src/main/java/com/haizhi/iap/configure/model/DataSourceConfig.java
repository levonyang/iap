package com.haizhi.iap.configure.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @Author dmy
 * @Date 2017/4/13 下午4:57.
 * 本地源表对象
 */
@Data
@NoArgsConstructor
public class DataSourceConfig {
    Long id;

    String name;

    //数据类型
    @JsonProperty("data_type")
    String dataType;

    @JsonProperty("update_mode")
    Integer updateMode;

    //数据源类型
    @JsonProperty("source_type")
    String sourceType;

    //数据源host	host
    String host;

    //数据源端口	port
    Integer port;

    //数据源用户名	username
    String username;

    //数据源密码	password
    String password;

    //数据源库		database
    String database;

    //数据源表名	source_table
    @JsonProperty("source_table")
    String sourceTable;

    //生成的mongo数据表名 target_table
    @JsonProperty("target_table")
    String targetTable;

    //0 未导入 1 导入中 2 导入失败 3 已导入
    @JsonProperty("import_status")
    Integer importStatus;

    String comment;

    @JsonProperty("origin_num")
    Long originNum;

    @JsonProperty("actual_num")
    Long actualNum;

    @JsonProperty("create_time")
    Date createTime;

    @JsonProperty("update_time")
    Date updateTime;

    @JsonProperty("field_map_list")
    List<SourceFieldMap> fieldMapList;

    public String generateTargetTable(){
        return this.sourceTable + "_" + System.currentTimeMillis();
    }
}
