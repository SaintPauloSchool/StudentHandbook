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
                                       receive_type        CHAR(1)         NOT NULL                   COMMENT '接收來源類型（1 WeCom家校通訊錄，2 自定義家校通訊錄）',
                                       receive_data        LONGTEXT                                   COMMENT '接收家長 parentUserId 列表（JSON 數組），如 ["userid1","userid2"]',
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
                                 cc_type             CHAR(1)         NOT NULL                   COMMENT '抄送來源類型（1 WeCom老師通訊錄，2 自定義老師通訊錄）',
                                 cc_data             TEXT            NOT NULL                   COMMENT '抄送成員 ID 列表（JSON 數組），如 [1,2,3]',
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
                                               user_id             VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用戶ID',
                                               user_type           CHAR(1)         NOT NULL                   COMMENT '用戶類型（1學生/家長 2教師/職工）',
                                               is_read             CHAR(1)         DEFAULT '0'                COMMENT '是否已讀（0未讀 1已讀）',
                                               read_time           DATETIME        DEFAULT NULL               COMMENT '閱讀時間',
                                               reply_status        CHAR(1)         DEFAULT '0'                COMMENT '回覆狀態（0未回覆 1已回覆）',
                                               reply_time          DATETIME        DEFAULT NULL               COMMENT '回覆時間',
                                               send_status         CHAR(1)         DEFAULT '0'                COMMENT '發送狀態（0發送失敗 1發送成功）',
                                               student_id          VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '關聯的學籍 student_id（student_profiles.student_info.student_id）',
                                               department_id       BIGINT(20)      DEFAULT NULL               COMMENT '發送時所屬部門ID',
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
                                              student_id          VARCHAR(64)     NOT NULL                   COMMENT '學籍 student_id（student_profiles.student_info.student_id）',
                                              parent_user_ids     TEXT            DEFAULT NULL               COMMENT '未回覆的家長用戶ID列表(JSON格式)',
                                              remind_send_time    DATETIME        DEFAULT NULL               COMMENT '提醒發送時間',
                                              remind_send_status  CHAR(1)         DEFAULT '0'                COMMENT '提醒發送狀態（0待發送 1發送成功 2發送失敗）',
                                              create_time         DATETIME                                   COMMENT '創建時間',
                                              PRIMARY KEY (reminder_id),
                                              KEY idx_notification (notification_id),
                                              KEY idx_send_record (send_record_id),
                                              KEY idx_student (student_id)
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
                                     student_id          VARCHAR(64)     NOT NULL                   COMMENT '學籍 student_id（student_profiles.student_info.student_id）',
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

-- 示例接收對象數據（按來源各存一行，receive_data 為 parentUserId JSON 數組）
INSERT INTO notification_receiver VALUES(1, 1, '1', '["parent_userid_1","parent_userid_2"]', NOW());
INSERT INTO notification_receiver VALUES(2, 1, '2', '["custom_parent_userid_1"]', NOW());

-- 示例抄送對象數據
INSERT INTO notification_cc VALUES(1, 1, '1', '[201,202]', NOW());
INSERT INTO notification_cc VALUES(2, 1, '2', '[301]', NOW());

-- 示例問題數據
-- 單選題：第一個問題
INSERT INTO notification_question VALUES(1, 1, NULL, '您是否支援舉辦運動會？', '1', '["支援","不支援"]', '1', 1, NULL, NULL, NULL, NULL, NOW());

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
-- 班級對照表
-- ----------------------------
DROP TABLE IF EXISTS class_section;
CREATE TABLE class_section (
                               id                  BIGINT          NOT NULL AUTO_INCREMENT,
                               class_section_dsedj VARCHAR(8)      NOT NULL,
                               class_section_sp    VARCHAR(8)      NOT NULL,
                               division            TINYINT         NOT NULL DEFAULT 0 COMMENT '學部（0幼稚園 1小學 2中學）',
                               PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='班級對照';

-- ----------------------------
-- 部門管理員表
-- ----------------------------
DROP TABLE IF EXISTS sys_department_admin;
CREATE TABLE sys_department_admin (
                                      id                  BIGINT          NOT NULL AUTO_INCREMENT    COMMENT '主鍵ID',
                                      department_id       BIGINT          NOT NULL                   COMMENT '部門ID',
                                      userid              VARCHAR(64)     NOT NULL                   COMMENT '部門管理員的userid',
                                      type                INT             DEFAULT NULL               COMMENT '部門管理員的類型：1-校區負責人, 2-年級負責人, 3-班主任, 4-任課老師, 5-學段負責人',
                                      subject             VARCHAR(100)    DEFAULT NULL               COMMENT '教師或班主任的科目',
                                      create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP  COMMENT '創建時間',
                                      update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
                                      PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COLLATE=utf8mb4_0900_ai_ci COMMENT='部門管理員表';
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
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主鍵ID',
                             `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用戶ID',
                             `display_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '顯示名稱',
                             `user_type` tinyint(1) DEFAULT NULL COMMENT '用戶類型 (1: parent, 0: student, 2: staff, 3: 考勤系統教職員)',
                             `token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Token值',
                             `expire_time` datetime NOT NULL COMMENT '過期時間',
                             `create_time` datetime NOT NULL COMMENT '創建時間',
                             `update_time` datetime NOT NULL COMMENT '更新時間',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `token_value` (`token`),
                             KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Token表';
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
                                              school_department_id BIGINT(20)     NOT NULL                   COMMENT '自定義部門節點 ID（sys_school_department.id）',
                                              userid              VARCHAR(100)    NOT NULL                   COMMENT '成員 UserID',
                                              name                VARCHAR(255)    DEFAULT NULL               COMMENT '成員名稱',
                                              department_id       BIGINT(20)      DEFAULT NULL               COMMENT '真實班級/部門 ID（選人時 sys_department.id）',
                                              open_userid         VARCHAR(100)    DEFAULT NULL               COMMENT '全局唯一 UserID',
                                              type                TINYINT(1)      DEFAULT 1                  COMMENT '類型：1-學校部門通訊錄，2-家校通訊錄',
                                              student_id          VARCHAR(64)     DEFAULT NULL               COMMENT '關聯學籍 student_id（student_profiles.student_info.student_id）',
                                              create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP  COMMENT '創建時間',
                                              update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
                                              PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系統學校部門成員表';
-- ----------------------------
-- 通知重發失敗記錄表
-- 用於追蹤每個用戶的重發失敗情況，失敗次數達到 3 次則放棄重發
DROP TABLE IF EXISTS notification_resend_fail_record;
CREATE TABLE `notification_resend_fail_record` (
                                                   `id`              bigint       NOT NULL AUTO_INCREMENT          COMMENT '主鍵ID',
                                                   `notification_id` bigint       NOT NULL                         COMMENT '通知ID',
                                                   `send_record_id`  bigint       NOT NULL                         COMMENT '發送記錄ID',
                                                   `user_id`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收用戶ID',
                                                   `student_id`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '關聯學生資訊表 student_id',
                                                   `fail_reason_1`   varchar(255)          DEFAULT NULL            COMMENT '第1次失敗原因',
                                                   `fail_message_1`  varchar(1000)         DEFAULT NULL            COMMENT '第1次失敗詳細資訊',
                                                   `fail_reason_2`   varchar(255)          DEFAULT NULL            COMMENT '第2次失敗原因',
                                                   `fail_message_2`  varchar(1000)         DEFAULT NULL            COMMENT '第2次失敗詳細資訊',
                                                   `fail_reason_3`   varchar(255)          DEFAULT NULL            COMMENT '第3次失敗原因',
                                                   `fail_message_3`  varchar(1000)         DEFAULT NULL            COMMENT '第3次失敗詳細資訊',
                                                   `fail_count`      int          NOT NULL DEFAULT 1               COMMENT '累計失敗次數（最大3次，達到後放棄重發）',
                                                   `status`          char(1)      NOT NULL DEFAULT '0'             COMMENT '狀態：0-待重發 1-已放棄',
                                                   `create_time`     datetime              DEFAULT NULL            COMMENT '首次失敗時間',
                                                   `update_time`     datetime              DEFAULT NULL            COMMENT '最近更新時間',
                                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知重發失敗記錄表';
-- ----------------------------
-- 系統管理員表（全局）
-- ----------------------------
DROP TABLE IF EXISTS sys_admin;
CREATE TABLE sys_admin (
                           id                  BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '主鍵ID',
                           user_id             VARCHAR(64)     NOT NULL                   COMMENT '用戶ID（關聯token表的user_id）',
                           admin_name          VARCHAR(100)    DEFAULT NULL               COMMENT '管理員姓名',
                           type                CHAR(1)         NOT NULL DEFAULT '1'       COMMENT '類型（0超級管理員 1管理員）',
                           status              CHAR(1)         DEFAULT '0'                COMMENT '狀態（0正常 1停用）',
                           create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP  COMMENT '創建時間',
                           update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
                           remark              VARCHAR(500)    DEFAULT NULL               COMMENT '備註',
                           PRIMARY KEY (id),
                           UNIQUE KEY uk_user_id (user_id),
                           KEY idx_status (status),
                           KEY idx_type (type)
) ENGINE=InnoDB AUTO_INCREMENT=1 COLLATE=utf8mb4_0900_ai_ci COMMENT='系統管理員表';

-- 插入示例管理員數據（需要根據實際user_id調整）
-- INSERT INTO sys_admin VALUES(1, 'admin_user_id', '系統管理員', '0', '0', NOW(), NOW(), '超級管理員');
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
                                `task_name` varchar(100) NOT NULL COMMENT '任務名稱 (例如: 每日學生手冊通知發送)',
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
-- ----------------------------
-- 定時任務配置表
-- ----------------------------
DROP TABLE IF EXISTS sys_scheduled_task;
CREATE TABLE `sys_scheduled_task` (
                                      `id`              bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                      `task_key`        varchar(64)  NOT NULL                COMMENT '任務唯一標識',
                                      `task_name`       varchar(100) NOT NULL                COMMENT '任務名稱',
                                      `task_bean`       varchar(100) NOT NULL                COMMENT 'Spring Task Bean 名稱（手動觸發用）',
                                      `method_name`     varchar(100) NOT NULL DEFAULT 'executeTask' COMMENT 'Task 方法名',
                                      `cron_expression` varchar(64)  NOT NULL                COMMENT 'Cron 表達式',
                                      `enabled`         char(1)      NOT NULL DEFAULT '0'    COMMENT '是否啟用（0停用 1啟用）',
                                      `sort_order`      int(11)      NOT NULL DEFAULT 0      COMMENT '排序',
                                      `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
                                      `update_time`     datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_task_key` (`task_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='定時任務配置表';

INSERT INTO sys_scheduled_task (task_key, task_name, task_bean, method_name, cron_expression, enabled, sort_order) VALUES
                                                                                                                       ('failed_task_notifier',       '檢查失敗任務通知',       'failedTaskNotifierTask',       'executeTask', '0 0 9 * * ?',         '0', 1),
                                                                                                                       ('department_sync',            '家校通訊錄部門數據同步', 'departmentSyncTask',           'executeTask', '0 0 0 * * ?',         '0', 2),
                                                                                                                       ('school_family_contact_sync', '家校通訊錄同步',         'schoolFamilyContactSyncTask',  'executeTask', '0 30 0 * * ?',        '0', 3),
                                                                                                                       ('student_match_sync',         '學生數據自動匹配',       'studentMatchSyncTask',         'executeTask', '0 0 1 * * ?',         '0', 4),
                                                                                                                       ('wecom_school_department',    '企業微信部門與成員同步', 'wecomSchoolDepartmentTask',    'executeTask', '0 30 1 * * ?',        '0', 5),
                                                                                                                       ('notification_reminder',      '定時提示家長回復通知',   'notificationReminderTask',     'executeTask', '0 30 9 * * ?',        '0', 6),
                                                                                                                       ('notification_resend',          '定時重新發送失敗通知',   'notificationResendTask',       'executeTask', '0 0 9-18 * * ?',      '0', 7),
                                                                                                                       ('school_notice',              '每日學生手冊通知發送',   'schoolNoticeTask',             'executeTask', '0 0 18 ? * MON-FRI',  '0', 8),
                                                                                                                       ('attendance_notify',          '考勤拍卡通知發送',       'attendanceNotifyTask',         'executeTask', '0 * * * * ?',         '1', 9);
-- ----------------------------
-- 學生數據匹配表
-- ----------------------------
DROP TABLE IF EXISTS sys_student_match;
CREATE TABLE IF NOT EXISTS sys_student_match (
                                                 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主鍵 ID',
                                                 student_id VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '學生 ID（關聯 student_profiles.student_info.student_id）',
    user_id VARCHAR(64) NOT NULL COMMENT '家校通訊錄家長 user_id（parent_user_id）',
    student_user_id VARCHAR(64) NOT NULL COMMENT '家校通訊錄學生 user_id（關聯 sys_school_family_contact.student_user_id）',
    match_status INT NOT NULL COMMENT '匹配狀態 (1: 自動匹配成功, 2: 手動匹配成功)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_contact (student_id, user_id, student_user_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='學生數據匹配表（學生與家長多對多）';
-- ----------------------------
-- 家校通訊錄表
-- ----------------------------
DROP TABLE IF EXISTS sys_school_family_contact;
CREATE TABLE `sys_school_family_contact` (
                                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主鍵 ID',
                                             `department_id` bigint NOT NULL COMMENT '部門 ID',
                                             `parent_user_id` varchar(64) NOT NULL COMMENT '家長用戶 ID',
                                             `student_user_id` varchar(64) NOT NULL COMMENT '學生用戶 ID',
                                             `student_name` varchar(100) DEFAULT NULL COMMENT '學生姓名',
                                             `relation_desc` varchar(50) DEFAULT '家長' COMMENT '關係描述',
                                             `mobile` varchar(20) DEFAULT NULL COMMENT '家長手機號',
                                             `external_userid` varchar(64) DEFAULT NULL COMMENT '家長外部用戶 ID',
                                             `create_time` datetime DEFAULT NULL COMMENT '創建時間',
                                             `update_time` datetime DEFAULT NULL COMMENT '更新時間',
                                             PRIMARY KEY (`id`),
                                             UNIQUE KEY `uk_parent_student_dept` (`parent_user_id`,`student_user_id`,`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='家校通訊錄表'
-- 系統公用配置表
-- ----------------------------
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
                            id              BIGINT          NOT NULL AUTO_INCREMENT    COMMENT '主鍵 ID',
                            config_key      VARCHAR(100)    NOT NULL                   COMMENT '配置鍵（唯一）',
                            config_name     VARCHAR(200)    NOT NULL                   COMMENT '配置名稱',
                            config_value    TEXT                                       COMMENT '配置值',
                            config_group    VARCHAR(50)     DEFAULT 'default'          COMMENT '配置分組',
                            value_type      VARCHAR(20)     DEFAULT 'string'           COMMENT '值類型：string/number/boolean/json',
                            remark          VARCHAR(500)    DEFAULT NULL               COMMENT '備註',
                            create_by       VARCHAR(64)     DEFAULT ''                 COMMENT '創建者',
                            create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
                            update_by       VARCHAR(64)     DEFAULT ''                 COMMENT '更新者',
                            update_time     DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系統公用配置表';
-- ----------------------------
-- 學生考勤記錄表
-- ----------------------------
DROP TABLE IF EXISTS attendance_record;
CREATE TABLE `attendance_record` (
                                     `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主鍵',
                                     `employee_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '學生 ID（關聯 student_profiles.student_info.student_id）',
                                     `access_datetime` datetime DEFAULT NULL COMMENT '進出日期和時間 (yyyy-MM-dd HH:mm:ss)',
                                     `access_date` date DEFAULT NULL COMMENT '進出日期 (yyyy-MM-dd)',
                                     `access_time` time DEFAULT NULL COMMENT '進出時間 (HH:mm:ss)',
                                     `access_result` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '存取結果: 0=失敗, 1=成功',
                                     `snapshot_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '驗證記錄抓拍圖',
                                     `access_mode` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '存取模式',
                                     `device_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '裝置名稱',
                                     `device_serial_number` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '裝置序列號',
                                     `resource_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '資源名稱',
                                     `card_reader_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '讀卡器名稱',
                                     `first_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '名字',
                                     `last_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '姓',
                                     `person_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '人員名稱',
                                     `person_group` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '人員群組',
                                     `card_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '卡號',
                                     `direction` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '方向: 0=進, 1=出',
                                     `is_notified` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0' COMMENT '是否已通知（0未通知 1已通知）',
                                     `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '記錄寫入時間',
                                     PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤記錄表'
