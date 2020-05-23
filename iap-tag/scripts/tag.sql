CREATE TABLE `tag_collection` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL DEFAULT '' comment '中文名字',
  `es_name` varchar(128) NOT NULL DEFAULT '' comment '对应es的名字',
  `comment` text NOT NULL DEFAULT '' comment '备注',
  `is_deleted` int not null default 0 comment '是否删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  unique KEY idx_es_name (`es_name`),
  unique KEY idx_name (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `tag_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `parent_id` int not null comment '父标签id',
  `collection_id` int not null comment 'tag_collection的id',
  `name` varchar(128) NOT NULL DEFAULT '' comment '中文名字',
  `field_name` varchar(128) NOT NULL DEFAULT '' comment 'es中field的名字',
  `tag_type` int not null default 0 comment '类型。0：无，1：枚举，2：整型，3：长整型',
  `comment` text NOT NULL DEFAULT '' comment '备注',
  `is_deleted` int not null default 0 comment '是否删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY(`field_name`),
  KEY(`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;