# 营销事件推送设置
CREATE TABLE `market_event_setting` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(10) unsigned NOT NULL,
  `name` varchar(50) NOT NULL COMMENT '事件名',
  `types` varchar(255) NOT NULL COMMENT '以逗号分隔的事件子类型代号集合，对应notification表中的type',
  `description` varchar(255) DEFAULT NULL COMMENT '事件描述',
  `enable` int(1) DEFAULT NULL COMMENT '是否开启该事件推送',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`,`name`,`types`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8