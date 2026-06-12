SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `scaffolding_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `scaffolding_db`;

-- 文件信息表
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint(20) DEFAULT '0' COMMENT '文件大小（字节）',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `file_extension` varchar(20) DEFAULT NULL COMMENT '文件扩展名',
  `upload_user_id` bigint(20) DEFAULT NULL COMMENT '上传人ID',
  `upload_user_name` varchar(50) DEFAULT NULL COMMENT '上传人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_upload_user_id` (`upload_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';

-- 工作管理表
DROP TABLE IF EXISTS `work`;
CREATE TABLE `work` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_name` varchar(100) NOT NULL COMMENT '工作名称',
  `work_content` text COMMENT '工作内容',
  `work_status` varchar(20) DEFAULT 'pending' COMMENT '工作状态（pending-待处理，in_progress-进行中，completed-已完成，cancelled-已取消）',
  `work_time` datetime DEFAULT NULL COMMENT '工作时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `priority` varchar(20) DEFAULT 'normal' COMMENT '优先级（low-低，normal-普通，high-高，urgent-紧急）',
  `required_certs` varchar(500) DEFAULT NULL COMMENT '岗位要求的证件类型，逗号分隔（electrician-电工证，welder-焊工证，height_work-高处作业证，health-健康证）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_work_status` (`work_status`),
  KEY `idx_work_time` (`work_time`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作管理表';

-- 证件信息表
DROP TABLE IF EXISTS `certificate`;
CREATE TABLE `certificate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '持证人ID（关联user表）',
  `user_name` varchar(50) DEFAULT NULL COMMENT '持证人姓名',
  `cert_type` varchar(30) NOT NULL COMMENT '证件类型（electrician-电工证，welder-焊工证，height_work-高处作业证，health-健康证）',
  `cert_no` varchar(50) DEFAULT NULL COMMENT '证件编号',
  `cert_name` varchar(100) DEFAULT NULL COMMENT '证件名称',
  `file_id` bigint(20) DEFAULT NULL COMMENT '证件照片文件ID（关联file_info表）',
  `file_url` varchar(500) DEFAULT NULL COMMENT '证件照片访问地址',
  `expire_date` date DEFAULT NULL COMMENT '证件到期日期',
  `cert_status` varchar(20) DEFAULT 'valid' COMMENT '证件状态（valid-有效，expiring-即将过期，expired-已过期）',
  `audit_status` varchar(20) DEFAULT 'pending' COMMENT '审核状态（pending-待审核，approved-已通过，rejected-已驳回）',
  `auditor_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
  `auditor_name` varchar(50) DEFAULT NULL COMMENT '审核人姓名',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_cert_type` (`cert_type`),
  KEY `idx_cert_status` (`cert_status`),
  KEY `idx_expire_date` (`expire_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证件信息表';

-- 证件变更历史表（保留旧照片和审核人）
DROP TABLE IF EXISTS `certificate_history`;
CREATE TABLE `certificate_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `certificate_id` bigint(20) NOT NULL COMMENT '证件ID（关联certificate表）',
  `user_id` bigint(20) NOT NULL COMMENT '持证人ID',
  `user_name` varchar(50) DEFAULT NULL COMMENT '持证人姓名',
  `cert_type` varchar(30) NOT NULL COMMENT '证件类型',
  `old_cert_no` varchar(50) DEFAULT NULL COMMENT '变更前证件编号',
  `old_cert_name` varchar(100) DEFAULT NULL COMMENT '变更前证件名称',
  `old_file_id` bigint(20) DEFAULT NULL COMMENT '变更前照片文件ID',
  `old_file_url` varchar(500) DEFAULT NULL COMMENT '变更前照片访问地址',
  `old_expire_date` date DEFAULT NULL COMMENT '变更前到期日期',
  `old_auditor_id` bigint(20) DEFAULT NULL COMMENT '变更前审核人ID',
  `old_auditor_name` varchar(50) DEFAULT NULL COMMENT '变更前审核人姓名',
  `old_audit_time` datetime DEFAULT NULL COMMENT '变更前审核时间',
  `new_cert_no` varchar(50) DEFAULT NULL COMMENT '变更后证件编号',
  `new_cert_name` varchar(100) DEFAULT NULL COMMENT '变更后证件名称',
  `new_file_id` bigint(20) DEFAULT NULL COMMENT '变更后照片文件ID',
  `new_file_url` varchar(500) DEFAULT NULL COMMENT '变更后照片访问地址',
  `new_expire_date` date DEFAULT NULL COMMENT '变更后到期日期',
  `operator_id` bigint(20) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_certificate_id` (`certificate_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证件变更历史表';

-- 排班分配表
DROP TABLE IF EXISTS `work_assignment`;
CREATE TABLE `work_assignment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_id` bigint(20) NOT NULL COMMENT '工作/班次ID（关联work表）',
  `work_name` varchar(100) DEFAULT NULL COMMENT '工作/班次名称',
  `user_id` bigint(20) NOT NULL COMMENT '候选人ID（关联user表）',
  `user_name` varchar(50) DEFAULT NULL COMMENT '候选人姓名',
  `assign_status` varchar(20) DEFAULT 'assigned' COMMENT '排班状态（assigned-已排班，checked_in-已到岗，completed-已完成，cancelled-已取消）',
  `cert_check_passed` tinyint(1) DEFAULT '0' COMMENT '证件检查是否通过（0-未通过，1-通过）',
  `cert_check_detail` text COMMENT '证件检查详情（JSON格式，记录每项证件检查结果）',
  `cert_warnings` varchar(1000) DEFAULT NULL COMMENT '证件警告信息（即将过期的证件提示）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_work_id` (`work_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_assign_status` (`assign_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班分配表';

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名（账号）',
  `password` varchar(100) NOT NULL COMMENT '密码（不加密）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认admin账号
INSERT INTO `user` (`username`, `password`, `nickname`) VALUES ('admin', '123456', '管理员');

SET FOREIGN_KEY_CHECKS = 1;
