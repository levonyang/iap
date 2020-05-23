### 1.6.4.GP 升级说明升级说明
- 执行以下数据库升级脚本
```
USE iap;

-- 增加索引,加快查询速度
ALTER TABLE `iap`.`group_detail` ADD INDEX `entity_name_index`(`entity_name`) USING BTREE;

-- 添加唯一索引
alter table group_detail add unique ientity_unique_index(group_name,type,entity_id);

-- 删除索引（可能没有，则不用删除）
alter table group_detail drop index entity_name_type ;
```

- 部署后，需清空redis

- 修改配置,修改***all_env***所有的mysql连接字符串，追加配置参数：
 ```
 &autoReconnection=true&useServerPrepStmts=false&rewriteBatchedStatements=true
 ```
  > 以开发环境为例，修改内容如下
  > - 修改前配置
```
##mysql
#bigdata_monitor
iap.mysql.url=jdbc:mysql://192.168.1.146:3306/iap?Unicode=true&characterEncoding=utf8

#cube
cube.mysql.url=jdbc:mysql://192.168.1.146:3306/cube?Unicode=true&characterEncoding=utf8

#tag
tag.mysql.url=jdbc:mysql://192.168.1.146:3306/tag?Unicode=true&characterEncoding=utf8

#消息推送
np.mysql.url=jdbc:mysql://192.168.1.146:3306/notification_producer_self?Unicode=true&characterEncoding=utf8
  ```
  > - 修改后配置
  ```
  #bigdata_monitor
  iap.mysql.url=jdbc:mysql://192.168.1.146:3306/iap?Unicode=true&characterEncoding=utf8&autoReconnection=true&useServerPrepStmts=false&rewriteBatchedStatements=true
  
  #cube
  cube.mysql.url=jdbc:mysql://192.168.1.146:3306/cube?Unicode=true&characterEncoding=utf8&autoReconnection=true&useServerPrepStmts=false&rewriteBatchedStatements=true
  
  #tag
  tag.mysql.url=jdbc:mysql://192.168.1.146:3306/tag?Unicode=true&characterEncoding=utf8&autoReconnection=true&useServerPrepStmts=false&rewriteBatchedStatements=true
  
  #消息推送
  np.mysql.url=jdbc:mysql://192.168.1.146:3306/notification_producer_self?Unicode=true&characterEncoding=utf8&autoReconnection=true&useServerPrepStmts=false&rewriteBatchedStatements=true
  ```
- 注意：配置相关的脚本文件create_config.sh不用修改。