-- ----------------------------
-- 家校通知系统数据库表结构
-- ----------------------------
-- 创建 class_log 表
-- ----------------------------
DROP TABLE IF EXISTS class_log;
CREATE TABLE `class_log` (
                             `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                             `student_class` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                             `teacher` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                             `course` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                             `course_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                             `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
                             `start_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                             `end_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                             `update_date` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 1. 为最常用的查询条件添加联合索引
-- 优化按班级、日期、课程类型的组合查询
ALTER TABLE class_log ADD INDEX idx_class_date_type (student_class, start_date, course_type);

-- 2. 为 id 字段添加普通索引（加速按 id 的查询和更新）
ALTER TABLE class_log ADD INDEX idx_id (id);

-- ----------------------------
-- 通知主表
-- ----------------------------
DROP TABLE IF EXISTS notification;
CREATE TABLE notification (
                              notification_id     BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '通知 ID',
                              title               VARCHAR(255)    NOT NULL                   COMMENT '通知标题',
                              content             TEXT            DEFAULT NULL               COMMENT '通知正文',
                              sender_id           BIGINT(20)      NOT NULL                   COMMENT '发送人 ID',
                              sender_name         VARCHAR(100)    NOT NULL                   COMMENT '发送人姓名',
                              jump_url            VARCHAR(500)    DEFAULT NULL               COMMENT '跳转链接',
                              attachment_urls     TEXT            DEFAULT NULL               COMMENT '附件/图片 URL 列表 (JSON 格式)',
                              status              CHAR(1)         DEFAULT '0'                COMMENT '状态（0 草稿 1 已发布 2 已撤回）',
                              reply_deadline      DATETIME        DEFAULT NULL               COMMENT '回复截止时间',
                              create_by           VARCHAR(64)     DEFAULT ''                 COMMENT '创建者',
                              create_time         DATETIME                                   COMMENT '创建时间',
                              update_by           VARCHAR(64)     DEFAULT ''                 COMMENT '更新者',
                              update_time         DATETIME                                   COMMENT '更新时间',
                              remark              VARCHAR(500)    DEFAULT NULL               COMMENT '备注',
                              PRIMARY KEY (notification_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT = '通知主表';

-- ----------------------------
-- 通知接收对象表
-- ----------------------------
DROP TABLE IF EXISTS notification_receiver;
CREATE TABLE notification_receiver (
                                       receiver_id         BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '接收关系 ID',
                                       notification_id     BIGINT(20)      NOT NULL                   COMMENT '通知 ID',
                                       receive_type        CHAR(1)         NOT NULL                   COMMENT '接收类型（1 班级 2 学生/家长）',
                                       receive_data        TEXT                                       COMMENT 'receive_ids 接收对象 ID 列表 (JSON 格式)， receive_names 接收对象名称列表 (JSON 格式)， type = 1 是wecom的數據， type = 2 是自定義的數據',
                                       create_time         DATETIME                                   COMMENT '创建时间',
                                       PRIMARY KEY (receiver_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT = '通知接收对象表';

-- ----------------------------
-- 通知抄送对象表
-- ----------------------------
DROP TABLE IF EXISTS notification_cc;
CREATE TABLE notification_cc (
                                 cc_id               BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '抄送关系 ID',
                                 notification_id     BIGINT(20)      NOT NULL                   COMMENT '通知 ID',
                                 cc_type             CHAR(1)         NOT NULL                   COMMENT '抄送类型（1 教职员工 2 学校通讯录）',
                                 cc_data             TEXT            NOT NULL                   COMMENT '抄送数据 (JSON 格式)，格式：[{"cc_ids": [1,2], "type": 1, "cc_names": ["聖保祿學校-054"]}, {"cc_ids": [1,2], "type": 2, "cc_names": ["聖保祿學校"]}]，其中 type 1代表wecom的，type 2代表自定义的',
                                 create_time         DATETIME                                   COMMENT '创建时间',
                                 PRIMARY KEY (cc_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT = '通知抄送对象表';

-- ----------------------------
-- 问题表
-- ----------------------------
DROP TABLE IF EXISTS notification_question;
CREATE TABLE notification_question (
       question_id         BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '问题 ID',
       notification_id     BIGINT(20)      NOT NULL                   COMMENT '通知 ID',
       parent_question_id  BIGINT(20)      DEFAULT NULL               COMMENT '父问题 ID（用于分支问题，记录上一题的选项继续后指向此题）',
       question_title      VARCHAR(500)    NOT NULL                   COMMENT '问题标题',
       question_type       CHAR(1)         NOT NULL                   COMMENT '问题类型（1 单选 2 多选 3 填空 4 附件上传 5 逻辑表单）',
       options             TEXT            DEFAULT NULL               COMMENT '选项列表 (JSON 格式)
                                                - 单选/多选：["选项 1","选项 2",...]
                                                - 逻辑表单：存储在 content 字段中，包含完整的问卷结构和子问题列表',
       is_required         CHAR(1)         DEFAULT '0'                COMMENT '是否必答（0 否 1 是）',
       sort_order          INT(4)          DEFAULT 0                  COMMENT '排序',
       logic_rules         TEXT            DEFAULT NULL               COMMENT '跳转逻辑规则 (JSON 格式)',
       fill_blanks         TEXT            DEFAULT NULL               COMMENT '填空题的填空列表 (JSON 格式)',
       correct_answers     TEXT            DEFAULT NULL               COMMENT '填空题的正确答案 (JSON 格式)',
       content             TEXT            DEFAULT NULL               COMMENT '题目内容，根据题型存储不同格式数据：①逻辑表单(type=5)存储JSON格式{"questionnaire":{"title":"问卷标题","description":"问卷描述"},"questions":[{"id":1,"type":"1/2/3/4","title":"子问题标题","description":"子问题描述","required":true/false,"options":["选项1","选项2"],"placeholder":"占位符文本","defaultValue":"默认值","validation":[],"minLength":0,"maxLength":200,"randomOrder":false,"logicRuleList":[],"minOptions":1,"maxOptions":null,"uploadNote":"上传说明","fillBlanks":[],"correctAnswers":[]}]}；②填空题(type=3)存储带占位符的纯文本如"这是{{fillblank-1}}一个{{fillblank-2}}填空题"；③其他题型可存储富文本/HTML内容或题目描述',
       create_time         DATETIME                                   COMMENT '创建时间',
       PRIMARY KEY (question_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT = '通知问题表';

-- ----------------------------
-- 发送通知记录主表
-- ----------------------------
DROP TABLE IF EXISTS notification_send_record;
CREATE TABLE notification_send_record (
      send_record_id      BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '发送记录ID',
      notification_id     BIGINT(20)      NOT NULL                   COMMENT '通知ID',
      sender_id           BIGINT(20)      NOT NULL                   COMMENT '发送人ID',
      sender_name         VARCHAR(100)    DEFAULT NULL               COMMENT '发送人姓名',
      send_time           DATETIME        DEFAULT NULL               COMMENT '发送时间',
      send_status         CHAR(1)         DEFAULT '0'                COMMENT '发送状态（0待发送 1发送中 2发送成功 3发送失败 4部分成功）',
      total_count         INT(11)         DEFAULT 0                  COMMENT '应发送总人数',
      success_count       INT(11)         DEFAULT 0                  COMMENT '发送成功人数',
      fail_count          INT(11)         DEFAULT 0                  COMMENT '发送失败人数',
      create_by           VARCHAR(64)     DEFAULT ''                 COMMENT '创建者',
      create_time         DATETIME                                   COMMENT '创建时间',
      update_by           VARCHAR(64)     DEFAULT ''                 COMMENT '更新者',
      update_time         DATETIME                                   COMMENT '更新时间',
      remark              VARCHAR(500)    DEFAULT NULL               COMMENT '备注',
      PRIMARY KEY (send_record_id),
      KEY idx_notification (notification_id),
      KEY idx_sender (sender_id),
      KEY idx_send_time (send_time),
      KEY idx_send_status (send_status)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT = '发送通知记录表';

-- ----------------------------
-- 用户通知阅读状态表
-- ----------------------------
DROP TABLE IF EXISTS notification_user_read_record;
CREATE TABLE notification_user_read_record (
       read_id             BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '閱讀記錄ID',
       send_record_id      BIGINT(20)      NOT NULL                   COMMENT '發送記錄ID',
       user_id             VARCHAR(64)     NOT NULL                   COMMENT '用戶ID',
       user_type           CHAR(1)         NOT NULL                   COMMENT '用戶類型（1學生 2家長 3教師）',
       is_read             CHAR(1)         DEFAULT '0'                COMMENT '是否已讀（0未讀 1已讀）',
       read_time           DATETIME        DEFAULT NULL               COMMENT '閱讀時間',
       reply_status        CHAR(1)         DEFAULT '0'                COMMENT '回覆狀態（0未回覆 1已回覆）',
       reply_time          DATETIME        DEFAULT NULL               COMMENT '回覆時間',
       send_status         CHAR(1)         DEFAULT '0'                COMMENT '發送狀態（0發送失敗 1發送成功）',
       create_time         DATETIME                                   COMMENT '創建時間',
       PRIMARY KEY (read_id),
       KEY idx_send_record (send_record_id),
       KEY idx_user (user_id),
       KEY idx_read_status (is_read),
       KEY idx_reply_status (reply_status)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT = '通知用戶閱讀記錄表';

-- ----------------------------
-- 回复答案表
-- ----------------------------
DROP TABLE IF EXISTS notification_answer;
CREATE TABLE notification_answer (
         answer_id           BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '答案 ID',
         notification_id     BIGINT(20)      NOT NULL                   COMMENT '通知 ID',
         question_id         BIGINT(20)      NOT NULL                   COMMENT '問題 ID',
         user_id             VARCHAR(64)     NOT NULL                   COMMENT '用戶 ID（parentUserId）',
         student_user_id     VARCHAR(64)     NOT NULL                   COMMENT '學生用戶 ID',
         user_type           CHAR(1)         NOT NULL                   COMMENT '用戶類型（1 學生 2 家長 3 教師）',
         answer_data         JSON            DEFAULT NULL               COMMENT '答案數據（JSON格式，包含nodeId、nodeTitle、nodeType、answerContent、attachmentUrls）',
         create_time         DATETIME                                   COMMENT '創建時間',
         PRIMARY KEY (answer_id),
         KEY idx_notification_user (notification_id, user_id),
         KEY idx_question (question_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT = '通知回答表';

-- ----------------------------
-- 初始化数据
-- ----------------------------
-- 示例通知数据
INSERT INTO notification VALUES(
       1,
       '关于春季运动会的通知',
       '各位家长同学，我校将于下周五举办春季运动会，请大家准时参加。',
       1,
       '张老师',
       NULL,
       NULL,
       '1',
       '2026-03-15 23:59:59',
       'admin',
       NOW(),
       '',
       NULL,
       '重要通知'
   );

-- 示例接收对象数据
INSERT INTO notification_receiver VALUES(1, 1, '1', '[1,2,3]', '["一年级 1 班","一年级 2 班","二年级 1 班"]', NOW());
INSERT INTO notification_receiver VALUES(2, 1, '2', '[101,102,103]', '["张小明家长","李小红家长","王小华家长"]', NOW());

-- 示例抄送对象数据
INSERT INTO notification_cc VALUES(1, 1, '1', '[{"cc_ids": [201,202], "type": 1, "cc_names": ["李主任","王副校长"]}]', NOW());
INSERT INTO notification_cc VALUES(2, 1, '2', '[{"cc_ids": [301], "type": 2, "cc_names": ["校长办公室"]}]', NOW());

-- 示例问题数据
-- 单选题：第一个问题
INSERT INTO notification_question VALUES(1, 1, NULL, '您是否支持举办运动会？', '1', '["支持","不支持"]', '1', 1, NULL, NULL, NULL, NULL, NOW());

-- 第二个问题：多选题
INSERT INTO notification_question VALUES(2, 1, NULL, '请选择您想参加的项目（可多选）', '2', '["跑步","跳远","投掷","其他"]', '0', 2, NULL, NULL, NULL, NULL, NOW());

-- 第三个问题：填空题
INSERT INTO notification_question VALUES(3, 1, NULL, '请留下您的联系方式', '3', NULL, '1', 3, NULL, NULL, NULL, NULL, NOW());

-- ----------------------------
-- 部门表
-- ----------------------------
DROP TABLE IF EXISTS sys_department;
CREATE TABLE sys_department (
    id                  BIGINT          NOT NULL                        COMMENT '部门 id',
    parent_id           INT             DEFAULT '0'                     COMMENT '父亲部门 id',
    name                VARCHAR(255)    COLLATE utf8mb4_unicode_ci      NOT NULL COMMENT '部门名称',
    type                INT             DEFAULT '0'                     COMMENT '部门类型：1-班级，2-年级，3-学段，4-校区，5-学校',
    register_year       INT             DEFAULT NULL                    COMMENT '入学年份',
    standard_grade      INT             DEFAULT NULL                    COMMENT '标准年级',
    order_num           INT             DEFAULT '0'                     COMMENT '排序值',
    is_graduated        TINYINT(1)      DEFAULT '0'                     COMMENT '是否毕业：1-是，0-否',
    open_group_chat     TINYINT(1)      DEFAULT '0'                     COMMENT '是否开启班级群：1-是，0-否',
    group_chat_id       VARCHAR(255)    COLLATE utf8mb4_unicode_ci      DEFAULT NULL COMMENT '班级群 id',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- ----------------------------
-- 家长学生关系表
-- ----------------------------
DROP TABLE IF EXISTS sys_parent_student_relation;
CREATE TABLE sys_parent_student_relation (
     id                  BIGINT          NOT NULL AUTO_INCREMENT    COMMENT '主键 ID',
     parent_user_id      VARCHAR(64)     NOT NULL                   COMMENT '家长用户 ID',
     student_user_id     VARCHAR(64)     NOT NULL                   COMMENT '学生用户 ID',
     student_name        VARCHAR(100)    DEFAULT NULL               COMMENT '学生姓名',
     relation_desc       VARCHAR(50)     DEFAULT '家长'             COMMENT '关系描述',
     mobile              VARCHAR(20)     DEFAULT NULL               COMMENT '家长手机号',
     external_userid     VARCHAR(64)     DEFAULT NULL               COMMENT '家长外部用户 ID',
     create_time         DATETIME        DEFAULT NULL               COMMENT '创建时间',
     update_time         DATETIME        DEFAULT NULL               COMMENT '更新时间',
     PRIMARY KEY (id),
     UNIQUE KEY uk_parent_student (parent_user_id, student_user_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='家长学生关系表';

-- ----------------------------
-- 部门家长绑定表
-- ----------------------------
DROP TABLE IF EXISTS sys_department_parent_binding;
CREATE TABLE sys_department_parent_binding (
       id                  BIGINT          NOT NULL AUTO_INCREMENT    COMMENT '主键 ID',
       department_id       BIGINT          NOT NULL                   COMMENT '部门 ID',
       parent_user_id      VARCHAR(64)     NOT NULL                   COMMENT '家长用户 ID',
       student_user_id     VARCHAR(64)     DEFAULT NULL               COMMENT '学生用户 ID',
       create_time         DATETIME        DEFAULT NULL               COMMENT '创建时间',
       update_time         DATETIME        DEFAULT NULL               COMMENT '更新时间',
       PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='部门家长绑定表';

-- ----------------------------
-- 課程班級表
-- ----------------------------
DROP TABLE IF EXISTS class_section;
CREATE TABLE class_section (
       id                  BIGINT          NOT NULL AUTO_INCREMENT,
       class_section_dsedj VARCHAR(8)      NOT NULL,
       class_section_sp    VARCHAR(8)      NOT NULL,
       PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=91 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='課程班級';

INSERT INTO class_section VALUES
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

-- ----------------------------
-- 微信学校部门表
-- ----------------------------
DROP TABLE IF EXISTS wecom_school_department;
CREATE TABLE wecom_school_department (
                                         id                  BIGINT(20)      NOT NULL                        COMMENT '部门 id',
                                         parent_id           INT(11)         DEFAULT NULL                    COMMENT '父部门 id',
                                         name                VARCHAR(255)    DEFAULT NULL                    COMMENT '部门名称',
                                         name_en             VARCHAR(255)    DEFAULT NULL                    COMMENT '部门英文名称',
                                         order_num           INT(11)         DEFAULT NULL                    COMMENT '在父部门中的次序值',
                                         department_leader   TEXT            DEFAULT NULL                    COMMENT '部门负责人的 UserID（JSON 数组字符串）',
                                         create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP       COMMENT '创建时间',
                                         update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企業微信学校部门表';

-- ----------------------------
-- 微信学校部门成员表
-- ----------------------------
DROP TABLE IF EXISTS wecom_school_department_member;
CREATE TABLE wecom_school_department_member (
                                                id                  BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '主键 ID',
                                                userid              VARCHAR(100)    NOT NULL                   COMMENT '成员 UserID',
                                                name                VARCHAR(255)    DEFAULT NULL               COMMENT '成员名称',
                                                department_id       BIGINT(20)      NOT NULL                   COMMENT '部门 ID',
                                                open_userid         VARCHAR(100)    DEFAULT NULL               COMMENT '全局唯一 UserID',
                                                create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
                                                update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企業微信学校部门成员表';
-- ----------------------------
-- token表
-- ----------------------------
CREATE TABLE `sys_token` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `user_id` bigint NOT NULL COMMENT '用户ID',
                             `parent_user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                             `token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Token值',
                             `expire_time` datetime NOT NULL COMMENT '过期时间',
                             `create_time` datetime NOT NULL COMMENT '创建时间',
                             `update_time` datetime NOT NULL COMMENT '更新时间',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `token_value` (`token`),
                             KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=934 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Token表'
-- ----------------------------
-- 系統学校部门表
-- ----------------------------
DROP TABLE IF EXISTS sys_school_department;
CREATE TABLE sys_school_department (
                                       id                  BIGINT(20)      NOT NULL AUTO_INCREMENT         COMMENT '部门 id',
                                       parent_id           INT(11)         DEFAULT NULL                    COMMENT '父部门 id',
                                       name                VARCHAR(255)    DEFAULT NULL                    COMMENT '部门名称',
                                       name_en             VARCHAR(255)    DEFAULT NULL                    COMMENT '部门英文名称',
                                       order_num           INT(11)         DEFAULT NULL                    COMMENT '在父部门中的次序值',
                                       department_leader   TEXT            DEFAULT NULL                    COMMENT '部门负责人的 UserID（JSON 数组字符串）',
                                       type                TINYINT(1)      DEFAULT 1                       COMMENT '类型：1-学校部门通讯录，2-家校通讯录',
                                       create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP       COMMENT '创建时间',
                                       update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系統学校部门表';

-- ----------------------------
-- 系統学校部门成员表
-- ----------------------------

DROP TABLE IF EXISTS sys_school_department_member;
CREATE TABLE sys_school_department_member (
                                              id                  BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '主键 ID',
                                              userid              VARCHAR(100)    NOT NULL                   COMMENT '成员 UserID',
                                              name                VARCHAR(255)    DEFAULT NULL               COMMENT '成员名称',
                                              department_id       BIGINT(20)      NOT NULL                   COMMENT '部门 ID',
                                              open_userid         VARCHAR(100)    DEFAULT NULL               COMMENT '全局唯一 UserID',
                                              type                TINYINT(1)      DEFAULT 1                  COMMENT '类型：1-学校部门通讯录，2-家校通讯录',
                                              create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
                                              update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                              PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系統学校部门成员表';
-- ----------------------------
