-- =====================================================
-- 儿童绘本阅读行为分析与成长跟踪系统 数据库脚本
-- 数据库: picture_book_system
-- 字符集: utf8mb4
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS picture_book_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE picture_book_system;

-- =====================================================
-- 1. 角色表 (sys_role)
-- =====================================================
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(50) NOT NULL COMMENT '角色标识(ADMIN/TEACHER/PARENT)',
    description VARCHAR(255) COMMENT '角色描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- =====================================================
-- 2. 用户表 (sys_user)
-- =====================================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    status TINYINT DEFAULT 1 COMMENT '状态(0禁用 1启用)',
    avatar VARCHAR(255) COMMENT '头像URL',
    login_fail_count INT DEFAULT 0 COMMENT '登录失败次数',
    last_login_time DATETIME COMMENT '最后登录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =====================================================
-- 3. 班级表 (class_info)
-- =====================================================
DROP TABLE IF EXISTS class_info;
CREATE TABLE class_info (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '班级ID',
    class_name VARCHAR(100) NOT NULL COMMENT '班级名称',
    teacher_id BIGINT COMMENT '教师ID',
    institution_name VARCHAR(200) COMMENT '机构名称',
    student_count INT DEFAULT 0 COMMENT '学生人数',
    description VARCHAR(500) COMMENT '班级描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_teacher_id (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- =====================================================
-- 4. 儿童表 (child)
-- =====================================================
DROP TABLE IF EXISTS child;
CREATE TABLE child (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '儿童ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    birth_date DATE COMMENT '出生日期',
    gender TINYINT DEFAULT 1 COMMENT '性别(0女 1男)',
    parent_id BIGINT COMMENT '家长ID',
    class_id BIGINT COMMENT '班级ID',
    avatar VARCHAR(255) COMMENT '头像URL',
    reading_type VARCHAR(50) COMMENT '阅读类型(专注型/跳跃型/兴趣导向型)',
    interest_tags VARCHAR(255) COMMENT '兴趣标签(逗号分隔)',
    focus_score DECIMAL(5,2) DEFAULT 0 COMMENT '专注度评分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id),
    KEY idx_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='儿童表';

-- =====================================================
-- 5. 绘本表 (book)
-- =====================================================
DROP TABLE IF EXISTS book;
CREATE TABLE book (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '绘本ID',
    title VARCHAR(200) NOT NULL COMMENT '绘本名称',
    author VARCHAR(100) COMMENT '作者',
    publisher VARCHAR(100) COMMENT '出版社',
    category VARCHAR(50) NOT NULL COMMENT '主类别(动物/科普/情感/童话)',
    sub_category VARCHAR(50) COMMENT '子类别',
    difficulty_level INT DEFAULT 1 COMMENT '难度级别(1-5)',
    page_count INT DEFAULT 1 COMMENT '页数',
    cover_url VARCHAR(255) COMMENT '封面URL',
    description TEXT COMMENT '简介',
    vocabulary_count INT DEFAULT 0 COMMENT '词汇量',
    read_count INT DEFAULT 0 COMMENT '阅读次数',
    status TINYINT DEFAULT 1 COMMENT '状态(0下架 1上架)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_category (category),
    KEY idx_difficulty (difficulty_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绘本表';

-- =====================================================
-- 6. 阅读日志表 (reading_log)
-- =====================================================
DROP TABLE IF EXISTS reading_log;
CREATE TABLE reading_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    child_id BIGINT NOT NULL COMMENT '儿童ID',
    book_id BIGINT NOT NULL COMMENT '绘本ID',
    start_time DATETIME NOT NULL COMMENT '阅读开始时间',
    end_time DATETIME COMMENT '阅读结束时间',
    duration INT DEFAULT 0 COMMENT '阅读时长(秒)',
    page_stay_times TEXT COMMENT '每页停留时长(JSON数组)',
    page_turn_count INT DEFAULT 0 COMMENT '翻页次数',
    is_completed TINYINT DEFAULT 0 COMMENT '是否完成(0否 1是)',
    completion_rate DECIMAL(5,2) DEFAULT 0 COMMENT '完成率',
    avg_page_stay_time DECIMAL(10,2) DEFAULT 0 COMMENT '平均每页停留时间(秒)',
    focus_score DECIMAL(5,2) DEFAULT 0 COMMENT '本次阅读专注度',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_child_id (child_id),
    KEY idx_book_id (book_id),
    KEY idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阅读日志表';

-- =====================================================
-- 7. 阅读统计表 (reading_stats)
-- =====================================================
DROP TABLE IF EXISTS reading_stats;
CREATE TABLE reading_stats (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    child_id BIGINT NOT NULL COMMENT '儿童ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    total_duration INT DEFAULT 0 COMMENT '总阅读时长(秒)',
    book_count INT DEFAULT 0 COMMENT '阅读绘本数',
    completed_count INT DEFAULT 0 COMMENT '完成绘本数',
    avg_page_stay_time DECIMAL(10,2) DEFAULT 0 COMMENT '平均每页停留时间',
    avg_focus_score DECIMAL(5,2) DEFAULT 0 COMMENT '平均专注度',
    page_turn_count INT DEFAULT 0 COMMENT '总翻页次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_child_date (child_id, stat_date),
    KEY idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阅读统计表';

-- =====================================================
-- 8. 行为分析结果表 (behavior_analysis)
-- =====================================================
DROP TABLE IF EXISTS behavior_analysis;
CREATE TABLE behavior_analysis (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分析ID',
    child_id BIGINT NOT NULL COMMENT '儿童ID',
    reading_type VARCHAR(50) COMMENT '阅读类型(专注型/跳跃型/兴趣导向型)',
    interest_tags VARCHAR(255) COMMENT '兴趣标签(逗号分隔)',
    focus_score DECIMAL(5,2) DEFAULT 0 COMMENT '专注度评分',
    avg_reading_duration DECIMAL(10,2) DEFAULT 0 COMMENT '平均阅读时长(秒)',
    avg_turn_speed DECIMAL(10,2) DEFAULT 0 COMMENT '平均翻页速度(页/分钟)',
    completion_rate DECIMAL(5,2) DEFAULT 0 COMMENT '平均完成率',
    total_reading_count INT DEFAULT 0 COMMENT '总阅读次数',
    analysis_date DATE COMMENT '分析日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_child_id (child_id),
    KEY idx_analysis_date (analysis_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行为分析结果表';

-- =====================================================
-- 9. 成长报告表 (growth_report)
-- =====================================================
DROP TABLE IF EXISTS growth_report;
CREATE TABLE growth_report (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '报告ID',
    child_id BIGINT NOT NULL COMMENT '儿童ID',
    vocabulary_score DECIMAL(5,2) DEFAULT 0 COMMENT '词汇量得分',
    logic_score DECIMAL(5,2) DEFAULT 0 COMMENT '逻辑理解力得分',
    reading_score DECIMAL(5,2) DEFAULT 0 COMMENT '阅读量得分',
    focus_score DECIMAL(5,2) DEFAULT 0 COMMENT '专注度得分',
    total_score DECIMAL(5,2) DEFAULT 0 COMMENT '综合得分',
    report_type VARCHAR(20) DEFAULT 'weekly' COMMENT '报告类型(weekly/monthly)',
    report_date DATE NOT NULL COMMENT '报告日期',
    start_date DATE COMMENT '统计开始日期',
    end_date DATE COMMENT '统计结束日期',
    book_count INT DEFAULT 0 COMMENT '阅读绘本数',
    total_duration INT DEFAULT 0 COMMENT '总阅读时长(秒)',
    suggestion TEXT COMMENT '阅读建议',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_child_id (child_id),
    KEY idx_report_date (report_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成长报告表';

-- =====================================================
-- 10. 推荐记录表 (recommendation)
-- =====================================================
DROP TABLE IF EXISTS recommendation;
CREATE TABLE recommendation (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '推荐ID',
    child_id BIGINT NOT NULL COMMENT '儿童ID',
    book_id BIGINT NOT NULL COMMENT '绘本ID',
    recommend_type VARCHAR(50) NOT NULL COMMENT '推荐类型(content_based/collaborative/hot)',
    recommend_score DECIMAL(5,2) DEFAULT 0 COMMENT '推荐得分',
    reason VARCHAR(255) COMMENT '推荐理由',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读(0否 1是)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_child_id (child_id),
    KEY idx_book_id (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐记录表';

-- =====================================================
-- 11. 操作日志表 (operation_log)
-- =====================================================
DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    operation VARCHAR(100) COMMENT '操作内容',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    status TINYINT DEFAULT 1 COMMENT '状态(0失败 1成功)',
    error_msg TEXT COMMENT '错误信息',
    duration BIGINT COMMENT '执行时长(毫秒)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- =====================================================
-- 初始数据插入
-- =====================================================

-- 插入角色数据
INSERT INTO sys_role (id, role_name, role_key, description) VALUES
(1, '系统管理员', 'ADMIN', '拥有系统所有权限，可管理用户、绘本、系统配置'),
(2, '教师/园长', 'TEACHER', '教育机构用户，可查看班级统计数据、班级儿童能力分布'),
(3, '家长', 'PARENT', '家长用户，可绑定孩子、查看孩子阅读报告与成长曲线');

-- 插入用户数据 (密码都是123456，BCrypt加密后的值)
INSERT INTO sys_user (id, username, password, real_name, phone, email, role_id, status) VALUES
(1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', '13800138001', 'admin@picturebook.com', 1, 1),
(2, 'teacher01', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '王老师', '13800138002', 'teacher01@picturebook.com', 2, 1),
(3, 'parent01', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '李妈妈', '13800138003', 'parent01@picturebook.com', 3, 1);

-- 插入班级数据
INSERT INTO class_info (id, class_name, teacher_id, institution_name, student_count, description) VALUES
(1, '阳光小班', 2, '阳光幼儿园', 3, '3-4岁儿童班级'),
(2, '星星中班', 2, '阳光幼儿园', 2, '4-5岁儿童班级');

-- 插入儿童数据
INSERT INTO child (id, name, birth_date, gender, parent_id, class_id, reading_type, focus_score) VALUES
(1, '小明', '2021-03-15', 1, 3, 1, '专注型', 85.50),
(2, '小红', '2021-05-20', 0, 3, 1, '兴趣导向型', 72.30),
(3, '小刚', '2020-08-10', 1, 3, 1, '跳跃型', 65.80),
(4, '小美', '2020-01-25', 0, 3, 2, '专注型', 88.20),
(5, '小强', '2020-11-08', 1, 3, 2, '兴趣导向型', 70.60);

-- 插入绘本数据
INSERT INTO book (id, title, author, publisher, category, sub_category, difficulty_level, page_count, vocabulary_count, description, read_count, status) VALUES
(1, '小熊的冒险', '张小明', '儿童出版社', '动物', '陆地动物', 1, 20, 150, '讲述小熊在森林中的冒险故事，培养孩子的勇气和探索精神。', 45, 1),
(2, '海底世界', '李华', '海洋出版社', '动物', '海洋动物', 2, 25, 200, '探索神秘的海底世界，认识各种海洋生物。', 38, 1),
(3, '小鸟学飞', '王芳', '天空出版社', '动物', '飞行动物', 1, 18, 120, '小鸟努力学习飞翔的故事，教会孩子坚持不懈。', 52, 1),
(4, '神奇的植物', '陈博士', '科学出版社', '科普', '自然科学', 3, 30, 280, '介绍各种神奇植物的生长过程和特点。', 28, 1),
(5, '安全小卫士', '刘安全', '教育出版社', '科普', '安全知识', 2, 22, 180, '教导孩子日常生活中的安全知识。', 65, 1),
(6, '我的家', '赵温馨', '家庭出版社', '情感', '亲情', 1, 16, 100, '温馨的家庭故事，培养孩子对家的热爱。', 42, 1),
(7, '好朋友', '孙友谊', '友谊出版社', '情感', '友情', 1, 18, 110, '讲述朋友之间的友谊故事。', 55, 1),
(8, '情绪小怪兽', '周心理', '心理出版社', '情感', '情绪管理', 2, 24, 160, '帮助孩子认识和管理自己的情绪。', 48, 1),
(9, '白雪公主', '格林', '童话出版社', '童话', '经典童话', 2, 28, 220, '经典童话故事白雪公主。', 72, 1),
(10, '小马过河', '中国民间', '民间出版社', '童话', '寓言故事', 1, 20, 140, '教会孩子勇于尝试的道理。', 58, 1);

-- 插入模拟阅读日志数据 (50条)
INSERT INTO reading_log (id, child_id, book_id, start_time, end_time, duration, page_stay_times, page_turn_count, is_completed, completion_rate, avg_page_stay_time, focus_score) VALUES
(1, 1, 1, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY) + INTERVAL 480 SECOND, 480, '[25,30,28,22,35,40,38,30,25,20,28,32,35,22,18,25,30,28,22,20]', 20, 1, 100.00, 27.50, 82.50),
(2, 1, 2, DATE_SUB(NOW(), INTERVAL 29 DAY), DATE_SUB(NOW(), INTERVAL 29 DAY) + INTERVAL 600 SECOND, 600, '[30,35,28,32,40,45,38,35,30,28,35,40,38,32,28,25,30,35,28,25,22,28,30,25,20]', 25, 1, 100.00, 32.00, 78.30),
(3, 1, 3, DATE_SUB(NOW(), INTERVAL 28 DAY), DATE_SUB(NOW(), INTERVAL 28 DAY) + INTERVAL 360 SECOND, 360, '[20,22,18,25,30,28,22,20,18,22,25,20,18,15,20,22,18,20]', 18, 1, 100.00, 20.00, 85.60),
(4, 2, 4, DATE_SUB(NOW(), INTERVAL 27 DAY), DATE_SUB(NOW(), INTERVAL 27 DAY) + INTERVAL 450 SECOND, 450, '[15,18,20,22,25,28,30,25,22,20,18,15,20,22,25,20,18,20,22,18,15,20,18,15,12,18,20,15,12,10]', 20, 0, 66.67, 18.50, 65.20),
(5, 2, 5, DATE_SUB(NOW(), INTERVAL 26 DAY), DATE_SUB(NOW(), INTERVAL 26 DAY) + INTERVAL 330 SECOND, 330, '[15,18,20,15,18,20,22,18,15,20,18,15,18,20,15,18,20,22,18,15,12,18]', 22, 1, 100.00, 15.00, 58.80),
(6, 3, 6, DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY) + INTERVAL 240 SECOND, 240, '[15,18,12,15,18,20,15,12,15,18,12,15,10,12,15,12]', 16, 1, 100.00, 15.00, 52.30),
(7, 3, 7, DATE_SUB(NOW(), INTERVAL 24 DAY), DATE_SUB(NOW(), INTERVAL 24 DAY) + INTERVAL 180 SECOND, 180, '[10,12,8,10,12,15,10,8,10,12,8,10,8,10,12,10,8,10]', 18, 1, 100.00, 10.00, 45.60),
(8, 4, 8, DATE_SUB(NOW(), INTERVAL 23 DAY), DATE_SUB(NOW(), INTERVAL 23 DAY) + INTERVAL 580 SECOND, 580, '[25,28,30,35,40,38,35,30,28,25,30,35,28,30,35,40,35,30,28,30,25,28,30,25]', 24, 1, 100.00, 28.50, 88.20),
(9, 4, 9, DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY) + INTERVAL 700 SECOND, 700, '[28,30,35,40,45,42,38,35,30,28,35,40,38,35,30,28,30,35,40,38,35,30,28,30,35,30,28,25]', 28, 1, 100.00, 32.50, 85.80),
(10, 5, 10, DATE_SUB(NOW(), INTERVAL 21 DAY), DATE_SUB(NOW(), INTERVAL 21 DAY) + INTERVAL 400 SECOND, 400, '[20,22,25,28,30,25,22,20,18,22,25,20,18,20,22,25,20,18,20,22]', 20, 1, 100.00, 22.00, 72.50),
(11, 1, 4, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY) + INTERVAL 520 SECOND, 520, '[18,20,22,25,28,30,35,30,28,25,22,20,25,28,30,25,22,20,25,22,18,20,22,18,15,20,22,18,20,22]', 30, 1, 100.00, 22.00, 75.60),
(12, 2, 1, DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY) + INTERVAL 380 SECOND, 380, '[18,20,22,18,20,22,25,20,18,22,20,18,20,22,18,20,22,18,20,18]', 20, 1, 100.00, 19.00, 62.80),
(13, 3, 2, DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY) + INTERVAL 280 SECOND, 280, '[12,15,10,12,15,18,12,10,12,15,10,12,10,12,15,12,10,12,10,8,10,12,10,8,10]', 25, 1, 100.00, 11.20, 48.50),
(14, 4, 1, DATE_SUB(NOW(), INTERVAL 17 DAY), DATE_SUB(NOW(), INTERVAL 17 DAY) + INTERVAL 450 SECOND, 450, '[22,25,28,30,25,22,20,25,28,22,20,22,25,20,18,22,20,18,20,22]', 20, 1, 100.00, 22.50, 82.30),
(15, 5, 2, DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY) + INTERVAL 500 SECOND, 500, '[20,22,25,28,30,28,25,22,20,22,25,28,25,22,20,22,25,20,18,22,20,18,20,22,18]', 25, 1, 100.00, 20.00, 70.80),
(16, 1, 5, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY) + INTERVAL 420 SECOND, 420, '[20,22,18,20,22,25,22,20,18,22,20,18,20,22,18,20,22,18,20,22,18,20]', 22, 1, 100.00, 19.09, 80.50),
(17, 2, 3, DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY) + INTERVAL 300 SECOND, 300, '[15,18,20,15,18,15,12,15,18,15,12,15,12,10,12,15,12,10]', 18, 1, 100.00, 16.67, 55.20),
(18, 3, 8, DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY) + INTERVAL 350 SECOND, 350, '[15,18,12,15,18,15,12,15,18,15,12,15,12,10,15,18,15,12,15,12,10,12,15,12]', 24, 1, 100.00, 14.58, 50.80),
(19, 4, 3, DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY) + INTERVAL 380 SECOND, 380, '[22,25,20,22,25,28,22,20,22,25,20,18,22,20,18,20,22,18]', 18, 1, 100.00, 21.11, 86.50),
(20, 5, 6, DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY) + INTERVAL 280 SECOND, 280, '[18,20,15,18,20,18,15,18,20,15,12,15,18,15,12,15]', 16, 1, 100.00, 17.50, 68.20),
(21, 1, 6, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 320 SECOND, 320, '[20,22,18,20,22,20,18,20,22,18,15,18,20,18,15,18]', 16, 1, 100.00, 20.00, 83.20),
(22, 2, 7, DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY) + INTERVAL 250 SECOND, 250, '[12,15,18,12,15,12,10,12,15,12,10,12,10,8,10,12,10,8]', 18, 1, 100.00, 13.89, 52.50),
(23, 3, 9, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY) + INTERVAL 400 SECOND, 400, '[15,18,12,15,18,20,15,12,15,18,12,15,12,10,12,15,12,10,12,15,12,10,12,10,8,10,12,10]', 28, 1, 100.00, 14.29, 48.20),
(24, 4, 5, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 480 SECOND, 480, '[22,25,28,30,25,22,25,28,22,20,22,25,22,20,22,25,20,18,22,20,18,20]', 22, 1, 100.00, 21.82, 85.60),
(25, 5, 8, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 550 SECOND, 550, '[22,25,28,30,35,30,28,25,22,25,28,30,25,22,25,28,25,22,20,25,22,20,22,25]', 24, 1, 100.00, 22.92, 75.80),
(26, 1, 7, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 360 SECOND, 360, '[20,22,18,20,22,20,18,20,22,18,15,18,20,18,15,18,20,18]', 18, 1, 100.00, 20.00, 84.50),
(27, 2, 9, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 520 SECOND, 520, '[18,20,22,25,28,25,22,20,22,25,20,18,22,20,18,20,22,18,20,22,18,20,22,18,20,18,20,22]', 28, 1, 100.00, 18.57, 68.50),
(28, 3, 10, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 320 SECOND, 320, '[15,18,12,15,18,15,12,15,18,12,10,12,15,12,10,12,10,8,10,12]', 20, 1, 100.00, 16.00, 50.20),
(29, 4, 10, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 420 SECOND, 420, '[22,25,20,22,25,28,22,20,22,25,20,18,22,20,18,20,22,18,20,22]', 20, 1, 100.00, 21.00, 88.80),
(30, 5, 4, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 480 SECOND, 480, '[18,20,22,18,20,22,25,20,18,22,20,18,20,22,18,20,22,18,20,22,18,20,22,18,15,18,20,18,15,18]', 30, 1, 100.00, 16.00, 72.30),
(31, 1, 8, DATE_SUB(NOW(), INTERVAL 30 DAY) + INTERVAL 1 HOUR, DATE_SUB(NOW(), INTERVAL 30 DAY) + INTERVAL 1 HOUR + INTERVAL 540 SECOND, 540, '[25,28,30,35,30,28,25,28,30,25,22,25,28,25,22,25,28,22,20,25,22,20,22,25]', 24, 1, 100.00, 22.50, 85.20),
(32, 2, 10, DATE_SUB(NOW(), INTERVAL 29 DAY) + INTERVAL 2 HOUR, DATE_SUB(NOW(), INTERVAL 29 DAY) + INTERVAL 2 HOUR + INTERVAL 380 SECOND, 380, '[18,20,22,18,20,18,15,18,20,18,15,18,20,15,12,15,18,15,12,15]', 20, 1, 100.00, 19.00, 60.80),
(33, 3, 5, DATE_SUB(NOW(), INTERVAL 28 DAY) + INTERVAL 3 HOUR, DATE_SUB(NOW(), INTERVAL 28 DAY) + INTERVAL 3 HOUR + INTERVAL 280 SECOND, 280, '[12,15,10,12,15,12,10,12,15,10,8,10,12,10,8,10,12,10,8,10,12,10]', 22, 1, 100.00, 12.73, 45.50),
(34, 4, 2, DATE_SUB(NOW(), INTERVAL 27 DAY) + INTERVAL 4 HOUR, DATE_SUB(NOW(), INTERVAL 27 DAY) + INTERVAL 4 HOUR + INTERVAL 620 SECOND, 620, '[28,30,35,38,40,35,30,28,32,35,30,28,30,35,30,28,30,35,28,25,28,30,28,25,28]', 25, 1, 100.00, 24.80, 86.50),
(35, 5, 1, DATE_SUB(NOW(), INTERVAL 26 DAY) + INTERVAL 5 HOUR, DATE_SUB(NOW(), INTERVAL 26 DAY) + INTERVAL 5 HOUR + INTERVAL 400 SECOND, 400, '[20,22,25,20,22,25,28,22,20,22,25,20,18,22,20,18,20,22,18,20]', 20, 1, 100.00, 20.00, 71.50),
(36, 1, 9, DATE_SUB(NOW(), INTERVAL 25 DAY) + INTERVAL 6 HOUR, DATE_SUB(NOW(), INTERVAL 25 DAY) + INTERVAL 6 HOUR + INTERVAL 680 SECOND, 680, '[28,30,35,40,42,38,35,30,32,35,30,28,32,35,30,28,30,35,30,28,30,35,28,25,28,30,28,25]', 28, 1, 100.00, 24.29, 82.80),
(37, 2, 6, DATE_SUB(NOW(), INTERVAL 24 DAY) + INTERVAL 7 HOUR, DATE_SUB(NOW(), INTERVAL 24 DAY) + INTERVAL 7 HOUR + INTERVAL 260 SECOND, 260, '[15,18,15,12,15,18,15,12,15,18,12,10,12,15,12,10]', 16, 1, 100.00, 16.25, 55.80),
(38, 3, 1, DATE_SUB(NOW(), INTERVAL 23 DAY) + INTERVAL 8 HOUR, DATE_SUB(NOW(), INTERVAL 23 DAY) + INTERVAL 8 HOUR + INTERVAL 220 SECOND, 220, '[10,12,8,10,12,10,8,10,12,8,10,8,10,12,8,10,8,10,12,10]', 20, 1, 100.00, 11.00, 42.50),
(39, 4, 6, DATE_SUB(NOW(), INTERVAL 22 DAY) + INTERVAL 9 HOUR, DATE_SUB(NOW(), INTERVAL 22 DAY) + INTERVAL 9 HOUR + INTERVAL 340 SECOND, 340, '[22,25,20,22,20,18,22,25,20,18,22,20,18,20,22,18]', 16, 1, 100.00, 21.25, 85.20),
(40, 5, 3, DATE_SUB(NOW(), INTERVAL 21 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 21 DAY) + INTERVAL 10 HOUR + INTERVAL 350 SECOND, 350, '[18,20,22,25,20,18,20,22,18,15,18,20,15,12,15,18,15,12]', 18, 1, 100.00, 19.44, 70.50),
(41, 1, 10, DATE_SUB(NOW(), INTERVAL 20 DAY) + INTERVAL 11 HOUR, DATE_SUB(NOW(), INTERVAL 20 DAY) + INTERVAL 11 HOUR + INTERVAL 440 SECOND, 440, '[22,25,20,22,25,28,22,20,22,25,20,18,22,20,18,20,22,18,20,22]', 20, 1, 100.00, 22.00, 84.80),
(42, 2, 8, DATE_SUB(NOW(), INTERVAL 19 DAY) + INTERVAL 12 HOUR, DATE_SUB(NOW(), INTERVAL 19 DAY) + INTERVAL 12 HOUR + INTERVAL 480 SECOND, 480, '[20,22,25,28,25,22,20,22,25,20,18,22,20,18,20,22,18,20,22,18,20,22,18,20]', 24, 1, 100.00, 20.00, 65.80),
(43, 3, 4, DATE_SUB(NOW(), INTERVAL 18 DAY) + INTERVAL 13 HOUR, DATE_SUB(NOW(), INTERVAL 18 DAY) + INTERVAL 13 HOUR + INTERVAL 320 SECOND, 320, '[12,15,10,12,15,12,10,12,10,8,10,12,10,8,10,12,8,10,8,10]', 20, 0, 66.67, 10.67, 45.20),
(44, 4, 7, DATE_SUB(NOW(), INTERVAL 17 DAY) + INTERVAL 14 HOUR, DATE_SUB(NOW(), INTERVAL 17 DAY) + INTERVAL 14 HOUR + INTERVAL 380 SECOND, 380, '[22,25,20,22,25,22,20,22,25,20,18,22,20,18,20,22,18,20]', 18, 1, 100.00, 21.11, 87.50),
(45, 5, 9, DATE_SUB(NOW(), INTERVAL 16 DAY) + INTERVAL 15 HOUR, DATE_SUB(NOW(), INTERVAL 16 DAY) + INTERVAL 15 HOUR + INTERVAL 560 SECOND, 560, '[20,22,25,28,30,28,25,22,25,28,25,22,20,25,22,20,22,25,20,18,22,20,18,22,25,20,18,20]', 28, 1, 100.00, 20.00, 74.50),
(46, 1, 3, DATE_SUB(NOW(), INTERVAL 15 DAY) + INTERVAL 16 HOUR, DATE_SUB(NOW(), INTERVAL 15 DAY) + INTERVAL 16 HOUR + INTERVAL 380 SECOND, 380, '[22,25,20,22,20,18,22,25,20,18,22,20,18,20,22,18,20,22]', 18, 1, 100.00, 21.11, 85.20),
(47, 2, 4, DATE_SUB(NOW(), INTERVAL 14 DAY) + INTERVAL 17 HOUR, DATE_SUB(NOW(), INTERVAL 14 DAY) + INTERVAL 17 HOUR + INTERVAL 420 SECOND, 420, '[15,18,20,22,18,15,18,20,15,12,15,18,15,12,15,18,12,10,12,15,12,10,12,10,8,10,12,10,8,10]', 30, 1, 100.00, 14.00, 58.50),
(48, 3, 7, DATE_SUB(NOW(), INTERVAL 13 DAY) + INTERVAL 18 HOUR, DATE_SUB(NOW(), INTERVAL 13 DAY) + INTERVAL 18 HOUR + INTERVAL 200 SECOND, 200, '[10,12,8,10,8,10,12,8,10,8,10,12,8,10,8,10,12,10]', 18, 1, 100.00, 11.11, 40.50),
(49, 4, 4, DATE_SUB(NOW(), INTERVAL 12 DAY) + INTERVAL 19 HOUR, DATE_SUB(NOW(), INTERVAL 12 DAY) + INTERVAL 19 HOUR + INTERVAL 580 SECOND, 580, '[22,25,28,30,32,28,25,28,30,25,22,25,28,25,22,25,28,22,20,25,22,20,22,25,22,20,22,25,20,18]', 30, 1, 100.00, 19.33, 86.80),
(50, 5, 7, DATE_SUB(NOW(), INTERVAL 11 DAY) + INTERVAL 20 HOUR, DATE_SUB(NOW(), INTERVAL 11 DAY) + INTERVAL 20 HOUR + INTERVAL 320 SECOND, 320, '[18,20,15,18,20,18,15,18,20,15,12,15,18,15,12,15,18,15]', 18, 1, 100.00, 17.78, 69.50);

-- 更新儿童表的兴趣标签
UPDATE child SET interest_tags = '动物,童话' WHERE id = 1;
UPDATE child SET interest_tags = '科普,情感' WHERE id = 2;
UPDATE child SET interest_tags = '情感,童话' WHERE id = 3;
UPDATE child SET interest_tags = '动物,科普,童话' WHERE id = 4;
UPDATE child SET interest_tags = '科普,情感,动物' WHERE id = 5;

-- 更新绘本阅读次数统计
UPDATE book b SET read_count = (SELECT COUNT(*) FROM reading_log rl WHERE rl.book_id = b.id);

-- 插入行为分析结果数据
INSERT INTO behavior_analysis (child_id, reading_type, interest_tags, focus_score, avg_reading_duration, avg_turn_speed, completion_rate, total_reading_count, analysis_date) VALUES
(1, '专注型', '动物,童话', 84.35, 478.00, 2.52, 100.00, 8, CURDATE()),
(2, '兴趣导向型', '科普,情感', 62.58, 398.00, 3.02, 98.00, 8, CURDATE()),
(3, '跳跃型', '情感,童话', 46.38, 272.00, 4.12, 96.00, 8, CURDATE()),
(4, '专注型', '动物,科普,童话', 86.52, 496.00, 2.45, 100.00, 8, CURDATE()),
(5, '兴趣导向型', '科普,情感,动物', 71.12, 438.00, 2.78, 100.00, 8, CURDATE());

-- 插入成长报告数据
INSERT INTO growth_report (child_id, vocabulary_score, logic_score, reading_score, focus_score, total_score, report_type, report_date, start_date, end_date, book_count, total_duration, suggestion) VALUES
(1, 82.50, 85.00, 88.00, 84.35, 84.96, 'weekly', CURDATE(), DATE_SUB(CURDATE(), INTERVAL 7 DAY), CURDATE(), 8, 3824, '小明阅读表现优秀，建议继续保持阅读习惯，可以尝试更高难度的绘本。'),
(2, 68.00, 72.00, 75.00, 62.58, 69.40, 'weekly', CURDATE(), DATE_SUB(CURDATE(), INTERVAL 7 DAY), CURDATE(), 8, 3184, '小红对科普类绘本兴趣浓厚，建议多提供此类绘本，同时培养阅读专注力。'),
(3, 55.00, 58.00, 62.00, 46.38, 55.35, 'weekly', CURDATE(), DATE_SUB(CURDATE(), INTERVAL 7 DAY), CURDATE(), 8, 2176, '小刚阅读速度较快但专注度有待提高，建议选择他感兴趣的绘本慢慢引导。'),
(4, 88.00, 90.00, 92.00, 86.52, 89.13, 'weekly', CURDATE(), DATE_SUB(CURDATE(), INTERVAL 7 DAY), CURDATE(), 8, 3968, '小美阅读能力出色，各方面发展均衡，建议挑战更高难度的绘本。'),
(5, 72.00, 75.00, 78.00, 71.12, 74.03, 'weekly', CURDATE(), DATE_SUB(CURDATE(), INTERVAL 7 DAY), CURDATE(), 8, 3504, '小强阅读兴趣广泛，建议根据兴趣标签推荐相关绘本，保持阅读热情。');

-- 插入推荐记录数据
INSERT INTO recommendation (child_id, book_id, recommend_type, recommend_score, reason) VALUES
(1, 2, 'content_based', 85.50, '根据您对动物类绘本的兴趣推荐'),
(1, 9, 'collaborative', 82.30, '相似儿童也喜欢这本绘本'),
(1, 4, 'hot', 78.00, '同龄人热门绘本'),
(2, 5, 'content_based', 80.20, '根据您对科普类绘本的兴趣推荐'),
(2, 8, 'content_based', 75.80, '根据您对情感类绘本的兴趣推荐'),
(2, 1, 'hot', 72.50, '同龄人热门绘本'),
(3, 6, 'content_based', 70.50, '根据您对情感类绘本的兴趣推荐'),
(3, 7, 'content_based', 68.20, '根据您对情感类绘本的兴趣推荐'),
(3, 10, 'hot', 65.00, '同龄人热门绘本'),
(4, 2, 'content_based', 88.00, '根据您对动物类绘本的兴趣推荐'),
(4, 4, 'content_based', 85.50, '根据您对科普类绘本的兴趣推荐'),
(4, 9, 'collaborative', 82.00, '相似儿童也喜欢这本绘本'),
(5, 4, 'content_based', 78.50, '根据您对科普类绘本的兴趣推荐'),
(5, 1, 'content_based', 76.00, '根据您对动物类绘本的兴趣推荐'),
(5, 8, 'collaborative', 74.20, '相似儿童也喜欢这本绘本');

-- 完成提示
SELECT '数据库初始化完成！' AS message;
