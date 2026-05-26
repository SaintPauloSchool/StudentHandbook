-- ----------------------------
-- 家校通知系統數據庫表結構
-- ----------------------------
-- 創建 class_log 表
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

-- 1. 為最常用的查詢條件添加聯合索引
-- 優化按班級、日期、課程類型的組合查詢
ALTER TABLE class_log ADD INDEX idx_class_date_type (student_class, start_date, course_type);

-- 2. 為 id 字段添加普通索引（加速按 id 的查詢和更新）
ALTER TABLE class_log ADD INDEX idx_id (id);

-- ----------------------------
-- 通知主表
-- ----------------------------
DROP TABLE IF EXISTS notification;
CREATE TABLE notification (
                              notification_id     BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '通知 ID',
                              title               VARCHAR(255)    NOT NULL                   COMMENT '通知標題',
                              content             TEXT            DEFAULT NULL               COMMENT '通知正文',
                              sender_id           BIGINT(20)      NOT NULL                   COMMENT '發送人 ID',
                              sender_name         VARCHAR(100)    NOT NULL                   COMMENT '發送人姓名',
                              jump_url            VARCHAR(500)    DEFAULT NULL               COMMENT '跳轉鏈接',
                              attachment_urls     TEXT            DEFAULT NULL               COMMENT '附件/圖片 URL 列表 (JSON 格式)',
                              status              CHAR(1)         DEFAULT '0'                COMMENT '狀態（0 草稿 1 已發佈 2 已撤回）',
                              reply_deadline      DATETIME        DEFAULT NULL               COMMENT '回覆截止時間',
                              reminder_time       DATETIME        DEFAULT NULL               COMMENT '提示回覆時間（只到日期）',
                              create_by           VARCHAR(64)     DEFAULT ''                 COMMENT '創建者',
                              create_time         DATETIME                                   COMMENT '創建時間',
                              update_by           VARCHAR(64)     DEFAULT ''                 COMMENT '更新者',
                              update_time         DATETIME                                   COMMENT '更新時間',
                              remark              VARCHAR(500)    DEFAULT NULL               COMMENT '備註',
                              PRIMARY KEY (notification_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COLLATE=utf8mb4_0900_ai_ci COMMENT = '通知主表';

-- ----------------------------
-- 通知接收對象表
-- ----------------------------
DROP TABLE IF EXISTS notification_receiver;
CREATE TABLE notification_receiver (
                                       receiver_id         BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '接收關係 ID',
                                       notification_id     BIGINT(20)      NOT NULL                   COMMENT '通知 ID',
                                       receive_type        CHAR(1)         NOT NULL                   COMMENT '接收類型（1 班級 2 學生/家長）',
                                       receive_data        TEXT                                       COMMENT 'receive_ids 接收對象 ID 列表 (JSON 格式)， receive_names 接收對象名稱列表 (JSON 格式)， type = 1 是wecom的數據， type = 2 是自定義的數據',
                                       create_time         DATETIME                                   COMMENT '創建時間',
                                       PRIMARY KEY (receiver_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COLLATE=utf8mb4_0900_ai_ci COMMENT = '通知接收對象表';

-- ----------------------------
-- 通知抄送對象表
-- ----------------------------
DROP TABLE IF EXISTS notification_cc;
CREATE TABLE notification_cc (
                                 cc_id               BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '抄送關係 ID',
                                 notification_id     BIGINT(20)      NOT NULL                   COMMENT '通知 ID',
                                 cc_type             CHAR(1)         NOT NULL                   COMMENT '抄送類型（1 教職員工 2 學校通訊錄）',
                                 cc_data             TEXT            NOT NULL                   COMMENT '抄送數據 (JSON 格式)，格式：[{"cc_ids": [1,2], "type": 1, "cc_names": ["聖保祿學校-054"]}, {"cc_ids": [1,2], "type": 2, "cc_names": ["聖保祿學校"]}]，其中 type 1代表wecom的，type 2代表自定義的',
                                 create_time         DATETIME                                   COMMENT '創建時間',
                                 PRIMARY KEY (cc_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COLLATE=utf8mb4_0900_ai_ci COMMENT = '通知抄送對象表';

-- ----------------------------
-- 問題表
-- ----------------------------
DROP TABLE IF EXISTS notification_question;
CREATE TABLE notification_question (
                                       question_id         BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '問題 ID',
                                       notification_id     BIGINT(20)      NOT NULL                   COMMENT '通知 ID',
                                       parent_question_id  BIGINT(20)      DEFAULT NULL               COMMENT '父問題 ID（用於分支問題，記錄上一題的選項繼續後指向此題）',
                                       question_title      VARCHAR(500)    NOT NULL                   COMMENT '問題標題',
                                       question_type       CHAR(1)         NOT NULL                   COMMENT '問題類型（1 單選 2 多選 3 填空 4 附件上傳 5 邏輯表單）',
                                       options             TEXT            DEFAULT NULL               COMMENT '選項列表 (JSON 格式)
                                                                                - 單選/多選：["選項 1","選項 2",...]
                                                                                - 邏輯表單：存儲在 content 字段中，包含完整的問卷結構和子問題列表',
                                       is_required         CHAR(1)         DEFAULT '0'                COMMENT '是否必答（0 否 1 是）',
                                       sort_order          INT(4)          DEFAULT 0                  COMMENT '排序',
                                       logic_rules         TEXT            DEFAULT NULL               COMMENT '跳轉邏輯規則 (JSON 格式)',
                                       fill_blanks         TEXT            DEFAULT NULL               COMMENT '填空題的填空列表 (JSON 格式)',
                                       correct_answers     TEXT            DEFAULT NULL               COMMENT '填空題的正確答案 (JSON 格式)',
                                       content             TEXT            DEFAULT NULL               COMMENT '題目內容，根據題型存儲不同格式數據：①邏輯表單(type=5)存儲JSON格式{"questionnaire":{"title":"問卷標題","description":"問卷描述"},"questions":[{"id":1,"type":"1/2/3/4","title":"子問題標題","description":"子問題描述","required":true/false,"options":["選項1","選項2"],"placeholder":"佔位符文本","defaultValue":"默認值","validation":[],"minLength":0,"maxLength":200,"randomOrder":false,"logicRuleList":[],"minOptions":1,"maxOptions":null,"uploadNote":"上傳說明","fillBlanks":[],"correctAnswers":[]}]}；②填空題(type=3)存儲帶佔位符的純文本如"這是{{fillblank-1}}一個{{fillblank-2}}填空題"；③其他題型可存儲富文本/HTML內容或題目描述',
                                       create_time         DATETIME                                   COMMENT '創建時間',
                                       PRIMARY KEY (question_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COLLATE=utf8mb4_0900_ai_ci COMMENT = '通知問題表';

-- ----------------------------
-- 發送通知記錄主表
-- ----------------------------
DROP TABLE IF EXISTS notification_send_record;
CREATE TABLE notification_send_record (
                                          send_record_id      BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '發送記錄ID',
                                          notification_id     BIGINT(20)      NOT NULL                   COMMENT '通知ID',
                                          sender_id           BIGINT(20)      NOT NULL                   COMMENT '發送人ID',
                                          sender_name         VARCHAR(100)    DEFAULT NULL               COMMENT '發送人姓名',
                                          send_time           DATETIME        DEFAULT NULL               COMMENT '發送時間',
                                          send_status         CHAR(1)         DEFAULT '0'                COMMENT '發送狀態（0待發送 1發送中 2發送成功 3發送失敗 4部分成功）',
                                          total_count         INT(11)         DEFAULT 0                  COMMENT '應發送總人數',
                                          success_count       INT(11)         DEFAULT 0                  COMMENT '發送成功人數',
                                          fail_count          INT(11)         DEFAULT 0                  COMMENT '發送失敗人數',
                                          create_by           VARCHAR(64)     DEFAULT ''                 COMMENT '創建者',
                                          create_time         DATETIME                                   COMMENT '創建時間',
                                          update_by           VARCHAR(64)     DEFAULT ''                 COMMENT '更新者',
                                          update_time         DATETIME                                   COMMENT '更新時間',
                                          remark              VARCHAR(500)    DEFAULT NULL               COMMENT '備註',
                                          PRIMARY KEY (send_record_id),
                                          KEY idx_notification (notification_id),
                                          KEY idx_sender (sender_id),
                                          KEY idx_send_time (send_time),
                                          KEY idx_send_status (send_status)
) ENGINE=InnoDB AUTO_INCREMENT=1 COLLATE=utf8mb4_0900_ai_ci COMMENT = '發送通知記錄表';

-- ----------------------------
-- 用戶通知閱讀狀態表
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
                                               student_user_id     VARCHAR(64)     DEFAULT NULL               COMMENT '關聯的學生ID（當接收者是家長時記錄，若發送給學生本身則與userId相同）',
                                               create_time         DATETIME                                   COMMENT '創建時間',
                                               PRIMARY KEY (read_id),
                                               KEY idx_send_record (send_record_id),
                                               KEY idx_user (user_id),
                                               KEY idx_read_status (is_read),
                                               KEY idx_reply_status (reply_status)
) ENGINE=InnoDB AUTO_INCREMENT=1 COLLATE=utf8mb4_0900_ai_ci COMMENT = '通知用戶閱讀記錄表';

-- ----------------------------
-- 通知提醒記錄表（用於收集提示家長回覆的記錄）
-- ----------------------------
DROP TABLE IF EXISTS notification_reminder_record;
CREATE TABLE notification_reminder_record (
                                              reminder_id         BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '提醒記錄ID',
                                              notification_id     BIGINT(20)      NOT NULL                   COMMENT '原通知ID',
                                              send_record_id      BIGINT(20)      NOT NULL                   COMMENT '原發送記錄ID',
                                              student_user_id     VARCHAR(64)     NOT NULL                   COMMENT '學生用戶ID',
                                              parent_user_ids     TEXT            DEFAULT NULL               COMMENT '未回覆的家長用戶ID列表(JSON格式)',
                                              remind_send_time    DATETIME        DEFAULT NULL               COMMENT '提醒發送時間',
                                              remind_send_status  CHAR(1)         DEFAULT '0'                COMMENT '提醒發送狀態（0待發送 1發送成功 2發送失敗）',
                                              create_time         DATETIME                                   COMMENT '創建時間',
                                              PRIMARY KEY (reminder_id),
                                              KEY idx_notification (notification_id),
                                              KEY idx_send_record (send_record_id),
                                              KEY idx_student (student_user_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COLLATE=utf8mb4_0900_ai_ci COMMENT = '通知提醒記錄表';

-- ----------------------------
-- 回覆答案表
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
) ENGINE=InnoDB AUTO_INCREMENT=1 COLLATE=utf8mb4_0900_ai_ci COMMENT = '通知回答表';

-- ----------------------------
-- 初始化數據
-- ----------------------------
-- 示例通知數據
INSERT INTO notification VALUES(
                                   1,
                                   '關於春季運動會的通知',
                                   '各位家長同學，我校將於下週五舉辦春季運動會，請大家準時參加。',
                                   1,
                                   '張老師',
                                   NULL,
                                   NULL,
                                   '1',
                                   '2026-03-15 23:59:59',
                                   NULL,
                                   'admin',
                                   NOW(),
                                   '',
                                   NULL,
                                   '重要通知'
                               );

-- 示例接收對象數據
INSERT INTO notification_receiver VALUES(1, 1, '1', '[1,2,3]', '["一年級 1 班","一年級 2 班","二年級 1 班"]', NOW());
INSERT INTO notification_receiver VALUES(2, 1, '2', '[101,102,103]', '["張小明家長","李小紅家長","王小華家長"]', NOW());

-- 示例抄送對象數據
INSERT INTO notification_cc VALUES(1, 1, '1', '[{"cc_ids": [201,202], "type": 1, "cc_names": ["李主任","王副校長"]}]', NOW());
INSERT INTO notification_cc VALUES(2, 1, '2', '[{"cc_ids": [301], "type": 2, "cc_names": ["校長辦公室"]}]', NOW());

-- 示例問題數據
-- 單選題：第一個問題
INSERT INTO notification_question VALUES(1, 1, NULL, '您是否支持舉辦運動會？', '1', '["支持","不支持"]', '1', 1, NULL, NULL, NULL, NULL, NOW());

-- 第二個問題：多選題
INSERT INTO notification_question VALUES(2, 1, NULL, '請選擇您想參加的項目（可多選）', '2', '["跑步","跳遠","投擲","其他"]', '0', 2, NULL, NULL, NULL, NULL, NOW());

-- 第三個問題：填空題
INSERT INTO notification_question VALUES(3, 1, NULL, '請留下您的聯繫方式', '3', NULL, '1', 3, NULL, NULL, NULL, NULL, NOW());

-- ----------------------------
-- 部門表
-- ----------------------------
DROP TABLE IF EXISTS sys_department;
CREATE TABLE sys_department (
                                id                  BIGINT          NOT NULL                        COMMENT '部門 id',
                                parent_id           INT             DEFAULT '0'                     COMMENT '父親部門 id',
                                name                VARCHAR(255)    NOT NULL                        COMMENT '部門名稱',
                                type                INT             DEFAULT '0'                     COMMENT '部門類型：1-班級，2-年級，3-學段，4-校區，5-學校',
                                register_year       INT             DEFAULT NULL                    COMMENT '入學年份',
                                standard_grade      INT             DEFAULT NULL                    COMMENT '標準年級',
                                order_num           INT             DEFAULT '0'                     COMMENT '排序值',
                                is_graduated        TINYINT(1)      DEFAULT '0'                     COMMENT '是否畢業：1-是，0-否',
                                open_group_chat     TINYINT(1)      DEFAULT '0'                     COMMENT '是否開啟班級群：1-是，0-否',
                                group_chat_id       VARCHAR(255)    DEFAULT NULL                    COMMENT '班級群 id',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部門表';

-- ----------------------------
-- 家長學生關係表
-- ----------------------------
DROP TABLE IF EXISTS sys_parent_student_relation;
CREATE TABLE sys_parent_student_relation (
                                             id                  BIGINT          NOT NULL AUTO_INCREMENT    COMMENT '主鍵 ID',
                                             parent_user_id      VARCHAR(64)     NOT NULL                   COMMENT '家長用戶 ID',
                                             student_user_id     VARCHAR(64)     NOT NULL                   COMMENT '學生用戶 ID',
                                             student_name        VARCHAR(100)    DEFAULT NULL               COMMENT '學生姓名',
                                             relation_desc       VARCHAR(50)     DEFAULT '家長'             COMMENT '關係描述',
                                             mobile              VARCHAR(20)     DEFAULT NULL               COMMENT '家長手機號',
                                             external_userid     VARCHAR(64)     DEFAULT NULL               COMMENT '家長外部用戶 ID',
                                             create_time         DATETIME        DEFAULT NULL               COMMENT '創建時間',
                                             update_time         DATETIME        DEFAULT NULL               COMMENT '更新時間',
                                             PRIMARY KEY (id),
                                             UNIQUE KEY uk_parent_student (parent_user_id, student_user_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COLLATE=utf8mb4_0900_ai_ci COMMENT='家長學生關係表';

-- ----------------------------
-- 部門家長綁定表
-- ----------------------------
DROP TABLE IF EXISTS sys_department_parent_binding;
CREATE TABLE sys_department_parent_binding (
                                               id                  BIGINT          NOT NULL AUTO_INCREMENT    COMMENT '主鍵 ID',
                                               department_id       BIGINT          NOT NULL                   COMMENT '部門 ID',
                                               parent_user_id      VARCHAR(64)     NOT NULL                   COMMENT '家長用戶 ID',
                                               student_user_id     VARCHAR(64)     DEFAULT NULL               COMMENT '學生用戶 ID',
                                               create_time         DATETIME        DEFAULT NULL               COMMENT '創建時間',
                                               update_time         DATETIME        DEFAULT NULL               COMMENT '更新時間',
                                               PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COLLATE=utf8mb4_0900_ai_ci COMMENT='部門家長綁定表';

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
-- 部门管理员表
-- ----------------------------
DROP TABLE IF EXISTS sys_department_admin;
CREATE TABLE sys_department_admin (
                                      id                  BIGINT          NOT NULL AUTO_INCREMENT    COMMENT '主键ID',
                                      department_id       BIGINT          NOT NULL                   COMMENT '部门ID',
                                      userid              VARCHAR(64)     NOT NULL                   COMMENT '部门管理员的userid',
                                      type                INT             DEFAULT NULL               COMMENT '部门管理员的类型：1-校区负责人, 2-年级负责人, 3-班主任, 4-任课老师, 5-学段负责人',
                                      subject             VARCHAR(100)    DEFAULT NULL               COMMENT '教师或班主任的科目',
                                      create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
                                      update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门管理员表';
-- ----------------------------
-- 微信學校部門表
-- ----------------------------
DROP TABLE IF EXISTS wecom_school_department;
CREATE TABLE wecom_school_department (
                                         id                  BIGINT(20)      NOT NULL                        COMMENT '部門 id',
                                         parent_id           INT(11)         DEFAULT NULL                    COMMENT '父部門 id',
                                         name                VARCHAR(255)    DEFAULT NULL                    COMMENT '部門名稱',
                                         name_en             VARCHAR(255)    DEFAULT NULL                    COMMENT '部門英文名稱',
                                         order_num           INT(11)         DEFAULT NULL                    COMMENT '在父部門中的次序值',
                                         department_leader   TEXT            DEFAULT NULL                    COMMENT '部門負責人的 UserID（JSON 數組字符串）',
                                         create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP       COMMENT '創建時間',
                                         update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
                                         PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企業微信學校部門表';

-- ----------------------------
-- 微信學校部門成員表
-- ----------------------------
DROP TABLE IF EXISTS wecom_school_department_member;
CREATE TABLE wecom_school_department_member (
                                                id                  BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '主鍵 ID',
                                                userid              VARCHAR(100)    NOT NULL                   COMMENT '成員 UserID',
                                                name                VARCHAR(255)    DEFAULT NULL               COMMENT '成員名稱',
                                                department_id       BIGINT(20)      NOT NULL                   COMMENT '部門 ID',
                                                open_userid         VARCHAR(100)    DEFAULT NULL               COMMENT '全局唯一 UserID',
                                                create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP  COMMENT '創建時間',
                                                update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
                                                PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企業微信學校部門成員表';
-- ----------------------------
-- token表
-- ----------------------------
DROP TABLE IF EXISTS sys_token;
CREATE TABLE `sys_token` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
                             `user_type` tinyint(1) DEFAULT NULL COMMENT '用户类型 (1: parent, 0: student, 2: staff)',
                             `token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Token值',
                             `expire_time` datetime NOT NULL COMMENT '过期时间',
                             `create_time` datetime NOT NULL COMMENT '创建时间',
                             `update_time` datetime NOT NULL COMMENT '更新时间',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `token_value` (`token`),
                             KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=934 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Token表';
-- ----------------------------
-- 系統學校部門表
-- ----------------------------
DROP TABLE IF EXISTS sys_school_department;
CREATE TABLE sys_school_department (
                                       id                  BIGINT(20)      NOT NULL AUTO_INCREMENT         COMMENT '部門 id',
                                       parent_id           INT(11)         DEFAULT NULL                    COMMENT '父部門 id',
                                       name                VARCHAR(255)    DEFAULT NULL                    COMMENT '部門名稱',
                                       name_en             VARCHAR(255)    DEFAULT NULL                    COMMENT '部門英文名稱',
                                       order_num           INT(11)         DEFAULT NULL                    COMMENT '在父部門中的次序值',
                                       department_leader   TEXT            DEFAULT NULL                    COMMENT '部門負責人的 UserID（JSON 數組字符串）',
                                       type                TINYINT(1)      DEFAULT 1                       COMMENT '類型：1-學校部門通訊錄，2-家校通訊錄',
                                       create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP       COMMENT '創建時間',
                                       update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
                                       PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系統學校部門表';

-- ----------------------------
-- 系統學校部門成員表
-- ----------------------------

DROP TABLE IF EXISTS sys_school_department_member;
CREATE TABLE sys_school_department_member (
                                              id                  BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '主鍵 ID',
                                              userid              VARCHAR(100)    NOT NULL                   COMMENT '成員 UserID',
                                              name                VARCHAR(255)    DEFAULT NULL               COMMENT '成員名稱',
                                              department_id       BIGINT(20)      NOT NULL                   COMMENT '部門 ID',
                                              open_userid         VARCHAR(100)    DEFAULT NULL               COMMENT '全局唯一 UserID',
                                              type                TINYINT(1)      DEFAULT 1                  COMMENT '類型：1-學校部門通訊錄，2-家校通訊錄',
                                              create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP  COMMENT '創建時間',
                                              update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
                                              PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系統學校部門成員表';
-- ----------------------------
-- 通知重发失败记录表
-- 用于追踪每个用户的重发失败情况，失败次数达到 3 次则放弃重发
DROP TABLE IF EXISTS notification_resend_fail_record;
CREATE TABLE `notification_resend_fail_record` (
                                                   `id`              bigint       NOT NULL AUTO_INCREMENT          COMMENT '主键ID',
                                                   `notification_id` bigint       NOT NULL                         COMMENT '通知ID',
                                                   `send_record_id`  bigint       NOT NULL                         COMMENT '发送记录ID',
                                                   `user_id`         varchar(64)  NOT NULL                         COMMENT '接收用户ID（家长或学生）',
                                                   `user_type`       char(1)      NOT NULL DEFAULT '2'             COMMENT '用户类型（1学生 2家长）',
                                                   `student_user_id` varchar(64)           DEFAULT NULL            COMMENT '关联学生ID',
                                                   `fail_reason_1`   varchar(255)          DEFAULT NULL            COMMENT '第1次失败原因',
                                                   `fail_message_1`  varchar(1000)         DEFAULT NULL            COMMENT '第1次失败详细信息',
                                                   `fail_reason_2`   varchar(255)          DEFAULT NULL            COMMENT '第2次失败原因',
                                                   `fail_message_2`  varchar(1000)         DEFAULT NULL            COMMENT '第2次失败详细信息',
                                                   `fail_reason_3`   varchar(255)          DEFAULT NULL            COMMENT '第3次失败原因',
                                                   `fail_message_3`  varchar(1000)         DEFAULT NULL            COMMENT '第3次失败详细信息',
                                                   `fail_count`      int          NOT NULL DEFAULT 1               COMMENT '累计失败次数（最大3次，达到后放弃重发）',
                                                   `status`          char(1)      NOT NULL DEFAULT '0'             COMMENT '状态：0-待重发 1-已放弃',
                                                   `create_time`     datetime              DEFAULT NULL            COMMENT '首次失败时间',
                                                   `update_time`     datetime              DEFAULT NULL            COMMENT '最近更新时间',
                                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知重发失败记录表';
-- ----------------------------
-- 系统管理员表（全局）
-- ----------------------------
DROP TABLE IF EXISTS sys_admin;
CREATE TABLE sys_admin (
                           id                  BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '主键ID',
                           user_id             VARCHAR(64)     NOT NULL                   COMMENT '用户ID（关联token表的user_id）',
                           admin_name          VARCHAR(100)    DEFAULT NULL               COMMENT '管理员姓名',
                           status              CHAR(1)         DEFAULT '0'                COMMENT '状态（0正常 1停用）',
                           create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
                           update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           remark              VARCHAR(500)    DEFAULT NULL               COMMENT '备注',
                           PRIMARY KEY (id),
                           UNIQUE KEY uk_user_id (user_id),
                           KEY idx_status (status)
) ENGINE=InnoDB AUTO_INCREMENT=1 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统管理员表';

-- 插入示例管理员数据（需要根据实际user_id调整）
-- INSERT INTO sys_admin VALUES(1, 'admin_user_id', '系统管理员', '0', NOW(), NOW(), '超级管理员');
-- ----------------------------
-- 行事曆事件表
-- ----------------------------
DROP TABLE IF EXISTS calendar_event;
CREATE TABLE IF NOT EXISTS `calendar_event` (
                                                `event_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '事件ID',
    `event_date` date NOT NULL COMMENT '事件日期',
    `title` varchar(255) NOT NULL COMMENT '事件標題',
    `target_type` int(11) NOT NULL DEFAULT '0' COMMENT '對象類型（0: 全校, 1: 幼稚園, 2: 小學, 3: 中學）',
    `create_by` varchar(64) DEFAULT '' COMMENT '創建者',
    `create_time` datetime DEFAULT NULL COMMENT '創建時間',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新時間',
    `remark` varchar(500) DEFAULT NULL COMMENT '備註',
    PRIMARY KEY (`event_id`),
    KEY `idx_event_date` (`event_date`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='行事曆事件表';
-- ----------------------------
-- 定時任務執行日誌表
-- ----------------------------
DROP TABLE IF EXISTS sys_task_log;
CREATE TABLE `sys_task_log` (
                                `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日誌主鍵',
                                `task_name` varchar(100) NOT NULL COMMENT '任務名稱 (例如: 每日學校通知發送)',
                                `bean_name` varchar(100) NOT NULL COMMENT 'Spring Bean 名稱 (例如: notificationPublishHandler)',
                                `method_name` varchar(255) DEFAULT NULL COMMENT '方法名稱',
                                `status` char(1) DEFAULT '0' COMMENT '執行狀態(0-成功, 1-失敗, 2-部分失敗)',
                                `is_processed` char(1) DEFAULT '0' COMMENT '是否已處理(0-未處理, 1-已處理)',
                                `fail_reason` text COMMENT '失敗原因',
                                `success_count` int(11) DEFAULT 0 COMMENT '成功數量',
                                `fail_count` int(11) DEFAULT 0 COMMENT '失敗數量',
                                `execution_time` datetime DEFAULT NULL COMMENT '執行時間',
                                `duration` bigint(20) NOT NULL COMMENT '執行耗時 (毫秒)',
                                `create_time` datetime DEFAULT NULL COMMENT '創建時間',
                                PRIMARY KEY (`log_id`),
                                KEY `idx_execution_time` (`execution_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='定時任務執行日誌表';
