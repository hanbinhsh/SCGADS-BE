use scMoAnnoDB;

drop table if exists `feedback`;
drop table if exists `scMoAnnoFiles`;
drop table if exists `scMoAnnoTask`;
drop table if exists `scMoAnnoUser`;

drop table if exists `scMoAnnoUser`;
create table `scMoAnnoUser`(
  `user_id` int AUTO_INCREMENT PRIMARY KEY  	NOT NULL	COMMENT '用户ID',
  `user_name` varchar(20) UNIQUE				NOT NULL	COMMENT '用户名',
  `psw` varchar(64)  							NOT NULL	COMMENT '用户密码',
  `email` varchar(32) UNIQUE 					NOT NULL	COMMENT '电子邮件',
  `is_admin` boolean DEFAULT false	 			NOT NULL	COMMENT '是否是管理员',
  `phone` varchar(32) UNIQUE				 	NOT NULL	COMMENT '电话号码',
  `avatar` LONGBLOB											COMMENT '用户头像'
);

drop table if exists `scMoAnnoTask`;
create table `scMoAnnoTask`(
  `task_id` int AUTO_INCREMENT PRIMARY KEY  	NOT NULL	COMMENT '任务ID',
  `task_name` varchar(20) UNIQUE				NOT NULL	COMMENT '任务名',
  `start_time` datetime				 			NOT NULL	COMMENT '开始时间',
  `end_time` datetime					 					COMMENT '结束时间',
  `status` tinyint DEFAULT 0					NOT NULL	COMMENT '标志位',
  `details` text											COMMENT '详情',
  `uploader_id` int 							NOT NULL	COMMENT '上传者ID',
  FOREIGN KEY (`uploader_id`) REFERENCES scMoAnnoUser(`user_id`)
);

drop table if exists `scMoAnnoFiles`;
create table `scMoAnnoFiles`(
  `file_id` int AUTO_INCREMENT PRIMARY KEY  	NOT NULL	COMMENT '文件ID',
  `scRNA_seq_file` varchar(100) UNIQUE						COMMENT 'scRNA-seq文件名',
  `scATAC_seq_file` varchar(100) UNIQUE						COMMENT 'scATAC-seq文件名',
  `Tag_file` varchar(100) UNIQUE							COMMENT 'Tag文件名',
  `task_name` varchar(20)                       NOT NULL    COMMENT '任务名',
  FOREIGN KEY (`task_name`) REFERENCES scMoAnnoTask(`task_name`)
);

drop table if exists `feedback`;
create table `feedback`(
  `feedback_id` int AUTO_INCREMENT PRIMARY KEY      NOT NULL	COMMENT '反馈ID',
  `user_id` int 								  	NOT NULL	COMMENT '用户ID',
  `subject` varchar(32)								NOT NULL	COMMENT '反馈主题',
  `message` text									NOT NULL	COMMENT '反馈信息',
  `created_time` datetime				 			NOT NULL	COMMENT '反馈时间',
  FOREIGN KEY (user_id) REFERENCES scMoAnnoUser(user_id)
);

drop table if exists `scMoAnnoResult`;
CREATE TABLE `scMoAnnoResult` (  
    `result_id` INT AUTO_INCREMENT PRIMARY KEY         NOT NULL    COMMENT '文件的唯一标识符',  
    `config_file` VARCHAR(255)                                     COMMENT 'config.js文件',
    `data_file` VARCHAR(255)                                     COMMENT 'data.js文件',
    `lable_file` VARCHAR(255)                                     COMMENT 'lable.js文件',
    `task_name` VARCHAR(255)                                      COMMENT '对应任务',
    FOREIGN KEY (`task_name`) REFERENCES scMoAnnoTask(`task_name`)
);

-- TRIGGER --
-- 用户密码加密
DELIMITER $$
DROP TRIGGER IF EXISTS before_user_insert $$
CREATE TRIGGER before_user_insert
BEFORE INSERT ON `scMoAnnoUser`
FOR EACH ROW
BEGIN
  SET NEW.psw = SHA2(NEW.psw, 256);
END;
$$
DELIMITER ;

DROP TRIGGER IF EXISTS before_user_delete;
DELIMITER //
CREATE TRIGGER before_user_delete
    BEFORE DELETE ON scMoAnnoUser
    FOR EACH ROW
BEGIN
    -- 删除即将被删除的用户ID的所有任务
    DELETE FROM scMoAnnoTask WHERE uploader_id = OLD.user_id;
    -- 删除即将被删除的用户ID的所有反馈
    DELETE FROM feedback WHERE user_id = OLD.user_id;
END //
DELIMITER ;

DROP TRIGGER IF EXISTS before_task_delete;
DELIMITER $$
CREATE TRIGGER `before_task_delete` BEFORE DELETE ON `scMoAnnoTask` FOR EACH ROW
BEGIN
    -- 删除scMoAnnoResult表中所有与即将被删除的任务名对应的记录
    DELETE FROM `scMoAnnoResult` WHERE `task_name` = OLD.task_name;
    DELETE FROM `scMoAnnoFiles` WHERE `task_name` = OLD.task_name;
    END$$
DELIMITER ;