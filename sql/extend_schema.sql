-- =====================================================
-- 儿童绘本阅读行为分析与成长跟踪系统 数据库扩展脚本
-- 用于完善系统功能，添加新字段和新表
-- =====================================================

USE picture_book_system;

-- =====================================================
-- 1. 扩展绘本表(book)字段
-- =====================================================
ALTER TABLE book ADD COLUMN age_range VARCHAR(50) COMMENT '适合年龄段' AFTER page_count;
ALTER TABLE book ADD COLUMN knowledge_type VARCHAR(50) COMMENT '知识点类型' AFTER age_range;
ALTER TABLE book ADD COLUMN art_style VARCHAR(50) COMMENT '画风风格' AFTER knowledge_type;
ALTER TABLE book ADD COLUMN theme VARCHAR(100) COMMENT '绘本主题' AFTER art_style;
ALTER TABLE book ADD COLUMN keywords VARCHAR(255) COMMENT '关键词标签' AFTER theme;

-- =====================================================
-- 2. 扩展阅读日志表(reading_log)字段
-- =====================================================
ALTER TABLE reading_log ADD COLUMN replay_count INT DEFAULT 0 COMMENT '重复阅读次数' AFTER focus_score;
ALTER TABLE reading_log ADD COLUMN bookmark_count INT DEFAULT 0 COMMENT '收藏次数' AFTER replay_count;
ALTER TABLE reading_log ADD COLUMN annotation_count INT DEFAULT 0 COMMENT '批注次数' AFTER bookmark_count;
ALTER TABLE reading_log ADD COLUMN voice_record_count INT DEFAULT 0 COMMENT '录音次数' AFTER annotation_count;
ALTER TABLE reading_log ADD COLUMN emotion_data TEXT COMMENT '情绪数据JSON' AFTER voice_record_count;

-- =====================================================
-- 3. 新建收藏记录表(reading_bookmark)
-- =====================================================
DROP TABLE IF EXISTS reading_bookmark;
CREATE TABLE reading_bookmark (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    child_id BIGINT NOT NULL COMMENT '儿童ID',
    book_id BIGINT NOT NULL COMMENT '绘本ID',
    bookmark_type TINYINT DEFAULT 1 COMMENT '收藏类型',
    note VARCHAR(500) COMMENT '收藏备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (id),
    KEY idx_child_id (child_id),
    KEY idx_book_id (book_id),
    UNIQUE KEY uk_child_book (child_id, book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阅读收藏记录表';

-- =====================================================
-- 4. 新建批注记录表(reading_annotation)
-- =====================================================
DROP TABLE IF EXISTS reading_annotation;
CREATE TABLE reading_annotation (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '批注ID',
    child_id BIGINT NOT NULL COMMENT '儿童ID',
    book_id BIGINT NOT NULL COMMENT '绘本ID',
    page_num INT NOT NULL COMMENT '页码',
    annotation_type TINYINT DEFAULT 1 COMMENT '批注类型',
    content TEXT COMMENT '批注内容',
    position_x INT COMMENT 'X坐标位置',
    position_y INT COMMENT 'Y坐标位置',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_child_id (child_id),
    KEY idx_book_id (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阅读批注记录表';

-- =====================================================
-- 5. 更新儿童表，添加成长阶段字段
-- =====================================================
ALTER TABLE child ADD COLUMN cognitive_stage VARCHAR(50) COMMENT '认知发展阶段' AFTER reading_type;
ALTER TABLE child ADD COLUMN reading_ability_score DECIMAL(5,2) DEFAULT 0 COMMENT '阅读能力综合评分' AFTER cognitive_stage;
ALTER TABLE child ADD COLUMN milestone VARCHAR(500) COMMENT '成长里程碑' AFTER reading_ability_score;

-- =====================================================
-- 6. 更新现有绘本数据，添加扩展属性
-- =====================================================
UPDATE book SET
    age_range = CASE
        WHEN difficulty_level = 1 THEN '3-4岁'
        WHEN difficulty_level = 2 THEN '4-5岁'
        WHEN difficulty_level >= 3 THEN '5-6岁'
        ELSE '3-6岁'
    END,
    knowledge_type = CASE category
        WHEN '动物' THEN '科学'
        WHEN '科普' THEN '科学'
        WHEN '情感' THEN '社会'
        WHEN '童话' THEN '语言'
        ELSE '语言'
    END,
    art_style = CASE
        WHEN id % 3 = 1 THEN '水彩'
        WHEN id % 3 = 2 THEN '卡通'
        ELSE '写实'
    END,
    theme = CASE category
        WHEN '动物' THEN '自然探索,生命教育'
        WHEN '科普' THEN '科学启蒙,认知发展'
        WHEN '情感' THEN '情感培养,性格养成'
        WHEN '童话' THEN '想象力,价值观'
        ELSE '综合发展'
    END;

-- =====================================================
-- 7. 扩展行为分析表(behavior_analysis)字段
-- =====================================================
ALTER TABLE behavior_analysis ADD COLUMN replay_preference_score DECIMAL(5,2) DEFAULT 0 COMMENT '重复阅读偏好分数' AFTER total_reading_count;
ALTER TABLE behavior_analysis ADD COLUMN interaction_score DECIMAL(5,2) DEFAULT 0 COMMENT '互动行为分数' AFTER replay_preference_score;
ALTER TABLE behavior_analysis ADD COLUMN emotion_stability_score DECIMAL(5,2) DEFAULT 0 COMMENT '情绪稳定性分数' AFTER interaction_score;
ALTER TABLE behavior_analysis ADD COLUMN cognitive_stage VARCHAR(50) COMMENT '认知发展阶段' AFTER emotion_stability_score;
ALTER TABLE behavior_analysis ADD COLUMN reading_ability_score DECIMAL(5,2) DEFAULT 0 COMMENT '阅读能力评分' AFTER cognitive_stage;
ALTER TABLE behavior_analysis ADD COLUMN habit_formation_score DECIMAL(5,2) DEFAULT 0 COMMENT '阅读习惯养成度' AFTER reading_ability_score;
ALTER TABLE behavior_analysis ADD COLUMN growth_prediction_score DECIMAL(5,2) DEFAULT 0 COMMENT '成长预测分数' AFTER habit_formation_score;
ALTER TABLE behavior_analysis ADD COLUMN dimension_data TEXT COMMENT '分析维度数据JSON' AFTER growth_prediction_score;

-- =====================================================
-- 8. 扩展成长报告表(growth_report)字段
-- =====================================================
ALTER TABLE growth_report ADD COLUMN cognitive_score DECIMAL(5,2) DEFAULT 0 COMMENT '认知发展评分' AFTER suggestion;
ALTER TABLE growth_report ADD COLUMN social_score DECIMAL(5,2) DEFAULT 0 COMMENT '社交情感评分' AFTER cognitive_score;
ALTER TABLE growth_report ADD COLUMN creativity_score DECIMAL(5,2) DEFAULT 0 COMMENT '创造力评分' AFTER social_score;
ALTER TABLE growth_report ADD COLUMN habit_score DECIMAL(5,2) DEFAULT 0 COMMENT '阅读习惯评分' AFTER creativity_score;
ALTER TABLE growth_report ADD COLUMN detailed_data TEXT COMMENT '详细维度数据JSON' AFTER habit_score;
ALTER TABLE growth_report ADD COLUMN milestones TEXT COMMENT '成长里程碑JSON' AFTER detailed_data;

-- =====================================================
-- 9. 完成提示
-- =====================================================
SELECT '数据库扩展完成！' AS message;
