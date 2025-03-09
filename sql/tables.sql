use scMoAnnoDB;

drop table if exists `feedback`;
drop table if exists `scMoAnnoFiles`;
drop table if exists `scMoAnnoTask`;
drop table if exists `scMoAnnoUser`;
drop table if exists `models`;

drop table if exists `models`;
CREATE TABLE `models` (
    `model_id` INT AUTO_INCREMENT PRIMARY KEY  				COMMENT '模型ID',
    `model_name` VARCHAR(255) NOT NULL         				COMMENT '模型名称',
    `model_type` VARCHAR(100) NOT NULL         				COMMENT '模型类型', -- single(单模态注释) -- multi(双模态注释) -- deno(降噪)
    `model_path` TEXT NOT NULL                 				COMMENT '模型存储路径',
    `predict_file_path` TEXT NOT NULL         				COMMENT '预测文件存储路径',
    `train_file_path` TEXT NOT NULL               			COMMENT '训练文件存储路径',
    `figure_path` TEXT               						COMMENT '模型图存储路径',
    `default_parameters` TEXT NOT NULL						COMMENT '默认参数' -- 格式： '参数1:值1,参数2:值2,...'
);

INSERT INTO models (model_name, model_type, model_path, predict_file_path, train_file_path, figure_path, default_parameters) VALUES 
    ('scLTH', 'multi', 'a', 'b', 'c', 'model.png', 'n_epochs:96,dropout:0.05,batch_size:128,patience:8,input_dim:512,num_layers:8,nhead:16,lr:5e-4,weight_decay:5e-3'),
    ('scMoAnno', 'single', 'a', 'b', 'c', 'model.png', 'n_epochs:96,dropout:0.05'),
    ('scTCHCN', 'multi', 'a', 'b', 'c', 'model.png', 'n_epochs:96,dropout:0.05');
    
-- 公司表：管理用户所属的公司
drop table if exists `company`;
CREATE TABLE `company` (
    `company_id` INT AUTO_INCREMENT PRIMARY KEY     NOT NULL COMMENT '公司ID',
    `company_name` VARCHAR(255) UNIQUE             NOT NULL COMMENT '公司名称',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

drop table if exists `scMoAnnoUser`;
create table `scMoAnnoUser`(
  `user_id` int AUTO_INCREMENT PRIMARY KEY  	NOT NULL	COMMENT '用户ID',
  `user_name` varchar(20) UNIQUE				NOT NULL	COMMENT '用户名',
  `psw` varchar(64)  							NOT NULL	COMMENT '用户密码',
  `email` varchar(32) UNIQUE 					NOT NULL	COMMENT '电子邮件',
  `is_admin` boolean DEFAULT false	 			NOT NULL	COMMENT '是否是管理员',
  `phone` varchar(32) UNIQUE				 	NOT NULL	COMMENT '电话号码',
  `avatar` LONGBLOB											COMMENT '用户头像',
  `company_id` INT 											COMMENT '所属公司ID',
  FOREIGN KEY (`company_id`) REFERENCES company(`company_id`)
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
  `type` VARCHAR(30) 										COMMENT '任务类型',       # not null -- 约定格式：(Annotation/Trainning/Denoising):(multi/single/deno)
  `parameters` TEXT 										COMMENT '任务参数',
  `model` VARCHAR(30) 										COMMENT '模型',           # not null
  `model_id` INT 											COMMENT '模型id',         # not null
  FOREIGN KEY (`uploader_id`) REFERENCES scMoAnnoUser(`user_id`),
  FOREIGN KEY (`model_id`) REFERENCES models(`model_id`)
);

drop table if exists `scMoAnnoFiles`;
create table `scMoAnnoFiles`(
  `file_id` int AUTO_INCREMENT PRIMARY KEY  	NOT NULL	COMMENT '文件ID',
  `scRNA_seq_file` varchar(100) 							COMMENT 'scRNA-seq文件名',
  `scATAC_seq_file` varchar(100) 							COMMENT 'scATAC-seq文件名',
  `Tag_file` varchar(100) 									COMMENT 'Tag文件名',
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
    `result_id` INT AUTO_INCREMENT PRIMARY KEY         NOT NULL   COMMENT '文件的唯一标识符',  
    `config_file` VARCHAR(255)                                    COMMENT 'config.js文件',
    `data_file` VARCHAR(255)                                      COMMENT 'data.js文件',
    `lable_file` VARCHAR(255)                                     COMMENT 'lable.js文件',
    `task_name` VARCHAR(255) UNIQUE                               COMMENT '对应任务',
    FOREIGN KEY (`task_name`) REFERENCES scMoAnnoTask(`task_name`)
);

drop table if exists `fileHashReference`;
CREATE TABLE `fileHashReference` (
  `hash` VARCHAR(64)                                 			 COMMENT '文件哈希值',
  `file_name` VARCHAR(255)  PRIMARY KEY              NOT NULL    COMMENT '文件名',
  `reference_count` INT DEFAULT 1                    NOT NULL    COMMENT '引用计数'
);

-- 分享表：记录用户分享任务的信息
drop table if exists `share`;
CREATE TABLE `share` (
    `share_id` INT AUTO_INCREMENT PRIMARY KEY        NOT NULL COMMENT '分享ID',
    `task_id` INT                                    NOT NULL COMMENT '任务ID',
    `sharer_id` INT                                  NOT NULL COMMENT '分享者ID',
    `receiver_id` INT                                		  COMMENT '接收者ID',		-- 同下↓↓↓
    `company_id` INT                                 		  COMMENT '接收公司ID',		-- 仅存在一个或0个值，若另一个值为空则此行有效，两个为空则所有人可查看
    `password` varchar(64)									  COMMENT '密码',
    `shared_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '分享时间',
    `due_time` DATETIME 									  COMMENT '到期时间',		-- 为空永久
    FOREIGN KEY (`task_id`) REFERENCES `scMoAnnoTask`(`task_id`),
    FOREIGN KEY (`sharer_id`) REFERENCES `scMoAnnoUser`(`user_id`),
    FOREIGN KEY (`company_id`) REFERENCES `company`(`company_id`),
    FOREIGN KEY (`receiver_id`) REFERENCES `scMoAnnoUser`(`user_id`)
);

-- 日志表：记录系统日志
drop table if exists `log`;
CREATE TABLE `log` (
    `log_id` INT AUTO_INCREMENT PRIMARY KEY        NOT NULL COMMENT '日志ID',
    `user_id` INT                                  NOT NULL COMMENT '用户ID',
    `action` VARCHAR(255)                          NOT NULL COMMENT '操作内容',
    `importance` INT                               NOT NULL COMMENT '操作内容',
    `timestamp` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '时间戳',
    FOREIGN KEY (`user_id`) REFERENCES `scMoAnnoUser`(`user_id`)
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