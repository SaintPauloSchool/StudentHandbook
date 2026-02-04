-- 创建class_log表
DROP TABLE IF EXISTS class_log;
CREATE TABLE `class_log` (
                             `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                             `studentClass` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                             `teacher` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                             `course` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                             `courseType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                             `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
                             `startDate` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                             `endDate` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                             `updateDate` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

-- 部门表
-- ----------------------------
drop table if exists sys_department;
CREATE TABLE `sys_department` (
                                  `id` bigint NOT NULL COMMENT '部门id',
                                  `parent_id` int DEFAULT '0' COMMENT '父亲部门id',
                                  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门名称',
                                  `type` int DEFAULT '0' COMMENT '部门类型：1-班级, 2-年级, 3-学段, 4-校区, 5-学校',
                                  `register_year` int DEFAULT NULL COMMENT '入学年份',
                                  `standard_grade` int DEFAULT NULL COMMENT '标准年级',
                                  `order_num` int DEFAULT '0' COMMENT '排序值',
                                  `is_graduated` tinyint(1) DEFAULT '0' COMMENT '是否毕业：1-是, 0-否',
                                  `open_group_chat` tinyint(1) DEFAULT '0' COMMENT '是否开启班级群：1-是, 0-否',
                                  `group_chat_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '班级群id',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表'

-- 家长学生关系表
-- ----------------------------
drop table if exists sys_parent_student_relation;
create table sys_parent_student_relation (
  id                bigint(20)      not null auto_increment    comment '主键ID',
  parent_user_id    varchar(64)     not null                   comment '家长用户ID',
  student_user_id   varchar(64)     not null                   comment '学生用户ID',
  student_name      varchar(100)    default null               comment '学生姓名',
  relation_desc     varchar(50)     default '家长'               comment '关系描述',
  mobile            varchar(20)     default null               comment '家长手机号',
  external_userid   varchar(64)     default null               comment '家长外部用户ID',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  primary key (id),
  unique key uk_parent_student (parent_user_id, student_user_id)
) engine=innodb auto_increment=1 comment = '家长学生关系表';

-- 部门家长绑定表
-- ----------------------------
drop table if exists sys_department_parent_binding;
create table sys_department_parent_binding (
  id                bigint(20)      not null auto_increment    comment '主键ID',
  department_id     bigint(20)      not null                   comment '部门ID',
  parent_user_id    varchar(64)     not null                   comment '家长用户ID',
  student_user_id   varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学生用户ID',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  primary key (id)
) engine=innodb auto_increment=1 comment = '部门家长绑定表';

-- Token表
-- ----------------------------
drop table if exists sys_token;
CREATE TABLE `sys_token` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `user_id` bigint NOT NULL COMMENT '用户ID',
                             `parent_user_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                             `token` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Token值',
                             `expire_time` datetime NOT NULL COMMENT '过期时间',
                             `create_time` datetime NOT NULL COMMENT '创建时间',
                             `update_time` datetime NOT NULL COMMENT '更新时间',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `token_value` (`token`),
                             KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=400 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Token表'

-- 課程班級表
-- ----------------------------
DROP TABLE IF EXISTS `class_section`;
CREATE TABLE `class_section` (
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `class_section_dsedj` varchar(8) NOT NULL,
                                 `class_section_sp` varchar(8) NOT NULL,
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=91 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='課程班級';
INSERT INTO `class_section` VALUES
                                (1,'I 1_A_家長','K1A'),(2,'I 1_B_家長','K1B'),(3,'I 1_C_家長','K1C'),(4,'I 1_D_家長','K1D'),(5,'I 1_E_家長','K1E'),(6,'I 1_F_家長','K1F'),
                                (7,'I 2_A_家長','K2A'),(8,'I 2_B_家長','K2B'),(9,'I 2_C_家長','K2C'),(10,'I 2_D_家長','K2D'),(11,'I 2_E_家長','K2E'),(12,'I 2_F_家長','K2F'),
                                (13,'I 3_A_家長','K3A'),(14,'I 3_B_家長','K3B'),(15,'I 3_C_家長','K3C'),(16,'I 3_D_家長','K3D'),(17,'I 3_E_家長','K3E'),(18,'I 3_F_家長','K3F'),
                                (19,'P1_A_家長','P1A'),(20,'P1_B_家長','P1B'),(21,'P1_C_家長','P1C'),(22,'P1_D_家長','P1D'),(23,'P1_E_家長','P1E'),(24,'P1_F_家長','P1F'),
                                (25,'P2_A_家長','P2A'),(26,'P2_B_家長','P2B'),(27,'P2_C_家長','P2C'),(28,'P2_D_家長','P2D'),(29,'P2_E_家長','P2E'),(30,'P2_F_家長','P2F'),
                                (31,'P3_A_家長','P3A'),(32,'P3_B_家長','P3B'),(33,'P3_C_家長','P3C'),(34,'P3_D_家長','P3D'),(35,'P3_E_家長','P3E'),(36,'P3_F_家長','P3F'),
                                (37,'P4_A_家長','P4A'),(38,'P4_B_家長','P4B'),(39,'P4_C_家長','P4C'),(40,'P4_D_家長','P4D'),(41,'P4_E_家長','P4E'),(42,'P4_F_家長','P4F'),
                                (43,'P5_A_家長','P5A'),(44,'P5_B_家長','P5B'),(45,'P5_C_家長','P5C'),(46,'P5_D_家長','P5D'),(47,'P5_E_家長','P5E'),(48,'P5_F_家長','P5F'),
                                (49,'P6_A_家長','P6A'),(50,'P6_B_家長','P6B'),(51,'P6_C_家長','P6C'),(52,'P6_D_家長','P6D'),(53,'P6_E_家長','P6E'),(54,'P6_F_家長','P6F'),
                                (55,'SG1_A_家長','F1A'),(56,'SG1_B_家長','F1B'),(57,'SG1_C_家長','F1C'),(58,'SG1_D_家長','F1D'),(59,'SG1_E_家長','F1E'),(60,'SG1_F_家長','F1F'),
                                (61,'SG2_A_家長','F2A'),(62,'SG2_B_家長','F2B'),(63,'SG2_C_家長','F2C'),(64,'SG2_D_家長','F2D'),(65,'SG2_E_家長','F2E'),(66,'SG2_F_家長','F2F'),
                                (67,'SG3_A_家長','F3A'),(68,'SG3_B_家長','F3B'),(69,'SG3_C_家長','F3C'),(70,'SG3_D_家長','F3D'),(71,'SG3_E_家長','F3E'),(72,'SG3_F_家長','F3F'),
                                (73,'SC1_A_家長','F4A'),(74,'SC1_B_家長','F4B'),(75,'SC1_C_家長','F4C'),(76,'SC1_D_家長','F4D'),(77,'SC1_E_家長','F4E'),(78,'SC1_F_家長','F4F'),
                                (79,'SC2_A_家長','F5A'),(80,'SC2_B_家長','F5B'),(81,'SC2_C_家長','F5C'),(82,'SC2_D_家長','F5D'),(83,'SC2_E_家長','F5E'),(84,'SC2_F_家長','F5F'),
                                (85,'SC3_A_家長','F6A'),(86,'SC3_B_家長','F6B'),(87,'SC3_C_家長','F6C'),(88,'SC3_D_家長','F6D'),(89,'SC3_E_家長','F6E'),(90,'SC3_F_家長','F6F');