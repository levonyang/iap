USE iap;

-- 增加索引,加快查询速度
ALTER TABLE `iap`.`group_detail`
ADD INDEX `entity_name_index`(`entity_name`) USING BTREE;

-- 添加唯一索引
alter table group_detail add unique ientity_unique_index(group_name,type,entity_id);

-- 删除索引（可能没有，则不用删除）
alter table group_detail drop index entity_name ;