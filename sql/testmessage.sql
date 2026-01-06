use scMoAnnoDB;

INSERT INTO `scmoannotask` (`task_id`,`task_name`,`start_time`,`end_time`,`status`,`details`,`uploader_id`,`type`,`parameters`,`model_id`,`re_pretrain`) VALUES (41,'训练任务之预训练','2025-05-29 00:16:52','2025-05-30 17:40:52',2,'123',1,'training:multi','n_epochs:10,dropout:0.05,batch_size:128,patience:8,input_dim:512,num_layers:8,nhead:16,lr:0.0005,weight_decay:0.005,seed:1224455,pretrain_patience:32,pretrain_epochs:10',1,1);
INSERT INTO `scmoannotask` (`task_id`,`task_name`,`start_time`,`end_time`,`status`,`details`,`uploader_id`,`type`,`parameters`,`model_id`,`re_pretrain`) VALUES (42,'训练任务不进行预训练','2025-05-29 00:55:00','2025-05-29 15:25:52',2,'',1,'training:multi','n_epochs:10,dropout:0.05,batch_size:128,patience:8,input_dim:512,num_layers:8,nhead:16,lr:0.0005,weight_decay:0.005,seed:1224455,pretrain_patience:32,pretrain_epochs:10',1,0);
INSERT INTO `scmoannotask` (`task_id`,`task_name`,`start_time`,`end_time`,`status`,`details`,`uploader_id`,`type`,`parameters`,`model_id`,`re_pretrain`) VALUES (43,'内置模型预测','2025-05-29 01:02:03','2025-05-29 15:45:10',2,'',1,'annotation:multi','n_epochs:96,dropout:0.05,batch_size:128,patience:8,input_dim:512,num_layers:8,nhead:16,lr:0.0005,weight_decay:0.005,seed:1224455,pretrain_patience:32,pretrain_epochs:96',1,0);
INSERT INTO `scmoannotask` (`task_id`,`task_name`,`start_time`,`end_time`,`status`,`details`,`uploader_id`,`type`,`parameters`,`model_id`,`re_pretrain`) VALUES (44,'训练任务之预训练预测','2025-05-29 01:03:07','2025-05-29 15:25:23',2,'',1,'annotation:multi','n_epochs:10,dropout:0.05,batch_size:128,patience:8,input_dim:512,num_layers:8,nhead:16,lr:0.0005,weight_decay:0.005,seed:1224455,pretrain_patience:32,pretrain_epochs:10',1,0);

INSERT INTO `scmoannofiles` (`file_id`,`scRNA_seq_file`,`scATAC_seq_file`,`Tag_file`,`task_name`) VALUES (29,'6f0260d8-5d6d-4fea-91b8-cb13751912ab','84292c0b-9a48-4cba-a0c4-8ebae321480a','3cc5aff5-c2be-466d-8137-6d0042b5f4bf','训练任务之预训练');
INSERT INTO `scmoannofiles` (`file_id`,`scRNA_seq_file`,`scATAC_seq_file`,`Tag_file`,`task_name`) VALUES (30,'6384d0d2-2870-484b-85da-23db22187a10','5fa5c997-5583-4e56-93bf-c3a55a0a1fdd','a34745e6-ac07-491a-906b-f7af25ec06c8','训练任务不进行预训练');
INSERT INTO `scmoannofiles` (`file_id`,`scRNA_seq_file`,`scATAC_seq_file`,`Tag_file`,`task_name`) VALUES (31,'5689e04a-fc41-4891-bd8e-f635d0b1aac2','50422f66-71a5-4ed2-89ed-1065c451090d',NULL,'内置模型预测');
INSERT INTO `scmoannofiles` (`file_id`,`scRNA_seq_file`,`scATAC_seq_file`,`Tag_file`,`task_name`) VALUES (32,'91267758-88bf-4e7b-adec-4ac1d27950ef','795f2fc9-2fa7-49af-9e8a-6fffa9c221eb',NULL,'训练任务之预训练预测');

INSERT INTO `company` (`company_id`,`company_name`,`created_time`) VALUES (1,'A','2025-04-19 23:30:24');
INSERT INTO `company` (`company_id`,`company_name`,`created_time`) VALUES (2,'B','2025-05-29 13:44:17');

INSERT INTO `share` (`share_id`,`task_id`,`sharer_id`,`receiver_id`,`company_id`,`shared_time`,`due_time`,`password`) VALUES (13,41,1,NULL,NULL,'2025-05-29 22:47:36','2025-06-07 00:00:00','123');
INSERT INTO `share` (`share_id`,`task_id`,`sharer_id`,`receiver_id`,`company_id`,`shared_time`,`due_time`,`password`) VALUES (14,41,1,2,NULL,'2025-05-29 22:52:41','2025-06-13 00:00:00','');
INSERT INTO `share` (`share_id`,`task_id`,`sharer_id`,`receiver_id`,`company_id`,`shared_time`,`due_time`,`password`) VALUES (15,41,1,NULL,1,'2025-05-29 22:53:02',NULL,'');
INSERT INTO `share` (`share_id`,`task_id`,`sharer_id`,`receiver_id`,`company_id`,`shared_time`,`due_time`,`password`) VALUES (18,44,1,2,NULL,'2025-05-30 20:54:10','2025-05-31 00:27:00','123');
