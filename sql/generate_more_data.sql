-- =====================================================
-- 生成更多测试数据
-- 用于 reading_stats, reading_bookmark, reading_annotation, operation_log, growth_report
-- =====================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE picture_book_system;

-- =====================================================
-- 1. 阅读统计表 (reading_stats) - 过去30天每天每个儿童的统计
-- =====================================================
INSERT INTO reading_stats (child_id, stat_date, total_duration, book_count, completed_count, avg_page_stay_time, avg_focus_score, page_turn_count)
SELECT
    c.id as child_id,
    DATE_SUB(CURDATE(), INTERVAL n DAY) as stat_date,
    FLOOR(300 + RAND() * 600) as total_duration,
    FLOOR(1 + RAND() * 3) as book_count,
    FLOOR(1 + RAND() * 2) as completed_count,
    ROUND(15 + RAND() * 20, 2) as avg_page_stay_time,
    ROUND(50 + RAND() * 40, 2) as avg_focus_score,
    FLOOR(15 + RAND() * 30) as page_turn_count
FROM child c
CROSS JOIN (
    SELECT 1 as n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
    UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
    UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25
    UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30
) days
WHERE NOT EXISTS (
    SELECT 1 FROM reading_stats rs
    WHERE rs.child_id = c.id AND rs.stat_date = DATE_SUB(CURDATE(), INTERVAL n DAY)
);

-- =====================================================
-- 2. 阅读收藏记录表 (reading_bookmark)
-- =====================================================
-- 先清空再插入
TRUNCATE TABLE reading_bookmark;

-- 为每个儿童随机收藏5-10本绘本
INSERT INTO reading_bookmark (child_id, book_id, bookmark_type, note, create_time)
SELECT
    c.id as child_id,
    b.id as book_id,
    FLOOR(1 + RAND() * 3) as bookmark_type,
    ELT(FLOOR(1 + RAND() * 6), '很喜欢这个故事', '画面很精美', '适合反复阅读', '孩子很喜欢', '值得推荐', '经典绘本') as note,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) as create_time
FROM child c
CROSS JOIN book b
WHERE RAND() < 0.08;

-- =====================================================
-- 3. 阅读批注记录表 (reading_annotation)
-- =====================================================
INSERT INTO reading_annotation (child_id, book_id, page_num, annotation_type, content, position_x, position_y, create_time)
SELECT
    FLOOR(1 + RAND() * 30) as child_id,
    FLOOR(1 + RAND() * 100) as book_id,
    FLOOR(1 + RAND() * 30) as page_num,
    FLOOR(1 + RAND() * 4) as annotation_type,
    ELT(FLOOR(1 + RAND() * 8),
        '这个地方很有趣',
        '小熊好可爱',
        '颜色真漂亮',
        '这是什么动物呢',
        '妈妈给我讲这里',
        '我喜欢这一页',
        '这个故事真好玩',
        '画得真好'
    ) as content,
    FLOOR(50 + RAND() * 300) as position_x,
    FLOOR(50 + RAND() * 400) as position_y,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) as create_time
FROM (
    SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
    UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
    UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25
    UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30
    UNION SELECT 31 UNION SELECT 32 UNION SELECT 33 UNION SELECT 34 UNION SELECT 35
    UNION SELECT 36 UNION SELECT 37 UNION SELECT 38 UNION SELECT 39 UNION SELECT 40
    UNION SELECT 41 UNION SELECT 42 UNION SELECT 43 UNION SELECT 44 UNION SELECT 45
    UNION SELECT 46 UNION SELECT 47 UNION SELECT 48 UNION SELECT 49 UNION SELECT 50
    UNION SELECT 51 UNION SELECT 52 UNION SELECT 53 UNION SELECT 54 UNION SELECT 55
    UNION SELECT 56 UNION SELECT 57 UNION SELECT 58 UNION SELECT 59 UNION SELECT 60
    UNION SELECT 61 UNION SELECT 62 UNION SELECT 63 UNION SELECT 64 UNION SELECT 65
    UNION SELECT 66 UNION SELECT 67 UNION SELECT 68 UNION SELECT 69 UNION SELECT 70
    UNION SELECT 71 UNION SELECT 72 UNION SELECT 73 UNION SELECT 74 UNION SELECT 75
    UNION SELECT 76 UNION SELECT 77 UNION SELECT 78 UNION SELECT 79 UNION SELECT 80
    UNION SELECT 81 UNION SELECT 82 UNION SELECT 83 UNION SELECT 84 UNION SELECT 85
    UNION SELECT 86 UNION SELECT 87 UNION SELECT 88 UNION SELECT 89 UNION SELECT 90
    UNION SELECT 91 UNION SELECT 92 UNION SELECT 93 UNION SELECT 94 UNION SELECT 95
    UNION SELECT 96 UNION SELECT 97 UNION SELECT 98 UNION SELECT 99 UNION SELECT 100
) temp;

-- =====================================================
-- 4. 操作日志表 (operation_log)
-- =====================================================
INSERT INTO operation_log (user_id, username, operation, method, params, ip, status, error_msg, duration, create_time)
SELECT
    u.id as user_id,
    u.username as username,
    ELT(FLOOR(1 + RAND() * 10),
        '用户登录',
        '查看绘本列表',
        '查看儿童信息',
        '查看阅读记录',
        '查看行为分析',
        '查看成长报告',
        '查看绘本推荐',
        '修改个人信息',
        '添加收藏',
        '添加批注'
    ) as operation,
    CONCAT('com.picturebook.controller.',
        ELT(FLOOR(1 + RAND() * 5), 'AuthController', 'BookController', 'ChildController', 'ReadingLogController', 'AnalysisController'),
        '.',
        ELT(FLOOR(1 + RAND() * 5), 'login', 'list', 'getById', 'save', 'update')
    ) as method,
    ELT(FLOOR(1 + RAND() * 3), '{"page":1,"size":10}', '{"id":1}', '{"keyword":"绘本"}') as params,
    CONCAT('192.168.', FLOOR(RAND() * 255), '.', FLOOR(RAND() * 255)) as ip,
    IF(RAND() > 0.05, 1, 0) as status,
    IF(RAND() > 0.05, NULL, '操作失败') as error_msg,
    FLOOR(10 + RAND() * 500) as duration,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) + INTERVAL FLOOR(RAND() * 24) HOUR + INTERVAL FLOOR(RAND() * 60) MINUTE as create_time
FROM sys_user u
CROSS JOIN (
    SELECT 1 as n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
    UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
) logs;

-- 添加更多管理员操作日志
INSERT INTO operation_log (user_id, username, operation, method, params, ip, status, duration, create_time)
SELECT
    1 as user_id,
    'admin' as username,
    ELT(FLOOR(1 + RAND() * 8),
        '用户登录',
        '添加绘本',
        '修改绘本',
        '删除绘本',
        '添加用户',
        '修改用户状态',
        '查看统计数据',
        '导出报表'
    ) as operation,
    'com.picturebook.controller.AdminController.handle' as method,
    '{}' as params,
    CONCAT('192.168.1.', FLOOR(1 + RAND() * 100)) as ip,
    1 as status,
    FLOOR(20 + RAND() * 300) as duration,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) + INTERVAL FLOOR(RAND() * 24) HOUR as create_time
FROM (
    SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
) temp;

-- =====================================================
-- 5. 成长报告表 (growth_report) - 为30个儿童生成多周报告
-- =====================================================
-- 清空现有报告
TRUNCATE TABLE growth_report;

-- 生成过去4周的周报
INSERT INTO growth_report (
    child_id, vocabulary_score, logic_score, reading_score, focus_score,
    cognitive_score, social_score, creativity_score, habit_score,
    total_score, report_type, report_date, start_date, end_date,
    book_count, total_duration, suggestion, detailed_data, milestones
)
SELECT
    c.id as child_id,
    ROUND(50 + RAND() * 40, 2) as vocabulary_score,
    ROUND(50 + RAND() * 40, 2) as logic_score,
    ROUND(50 + RAND() * 40, 2) as reading_score,
    ROUND(50 + RAND() * 40, 2) as focus_score,
    ROUND(50 + RAND() * 40, 2) as cognitive_score,
    ROUND(50 + RAND() * 40, 2) as social_score,
    ROUND(50 + RAND() * 40, 2) as creativity_score,
    ROUND(50 + RAND() * 40, 2) as habit_score,
    ROUND(55 + RAND() * 35, 2) as total_score,
    'weekly' as report_type,
    DATE_SUB(CURDATE(), INTERVAL w WEEK) as report_date,
    DATE_SUB(CURDATE(), INTERVAL w WEEK) - INTERVAL 6 DAY as start_date,
    DATE_SUB(CURDATE(), INTERVAL w WEEK) as end_date,
    FLOOR(3 + RAND() * 5) as book_count,
    FLOOR(1200 + RAND() * 1800) as total_duration,
    CASE
        WHEN c.reading_ability_score >= 70 THEN
            ELT(FLOOR(1 + RAND() * 3),
                '阅读能力优秀，建议尝试更高难度的绘本，可以开始引导独立阅读',
                '表现出色，可以增加阅读量，培养更广泛的阅读兴趣',
                '继续保持良好的阅读习惯，可以尝试复述故事内容'
            )
        WHEN c.reading_ability_score >= 50 THEN
            ELT(FLOOR(1 + RAND() * 3),
                '阅读能力良好，建议多读感兴趣的绘本，提高专注度',
                '进步明显，建议家长陪伴阅读，增加互动交流',
                '建议选择适合难度的绘本，培养每日阅读习惯'
            )
        ELSE
            ELT(FLOOR(1 + RAND() * 3),
                '需要加强阅读训练，建议从简单绘本开始培养兴趣',
                '建议家长多陪伴阅读，选择孩子感兴趣的主题',
                '保持耐心，从短时间阅读开始，逐步延长时间'
            )
    END as suggestion,
    JSON_OBJECT(
        'dailyAvgDuration', FLOOR(15 + RAND() * 20),
        'favoriteCategory', ELT(FLOOR(1 + RAND() * 4), '动物', '科普', '情感', '童话'),
        'improvementAreas', JSON_ARRAY('专注力', '阅读速度', '理解能力'),
        'strengths', JSON_ARRAY('兴趣广泛', '记忆力好', '喜欢互动')
    ) as detailed_data,
    JSON_ARRAY(
        JSON_OBJECT('date', DATE_SUB(CURDATE(), INTERVAL w WEEK), 'event', ELT(FLOOR(1 + RAND() * 5), '首次完整阅读一本绘本', '阅读时长突破10分钟', '学会翻页', '开始主动要求阅读', '能够识别绘本中的角色')),
        JSON_OBJECT('date', DATE_SUB(CURDATE(), INTERVAL w WEEK) - INTERVAL 2 DAY, 'event', ELT(FLOOR(1 + RAND() * 5), '阅读兴趣明显提升', '专注度有进步', '学会了新词汇', '能复述简单情节', '开始关注画面细节'))
    ) as milestones
FROM child c
CROSS JOIN (
    SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3
) weeks;

-- 生成月报
INSERT INTO growth_report (
    child_id, vocabulary_score, logic_score, reading_score, focus_score,
    cognitive_score, social_score, creativity_score, habit_score,
    total_score, report_type, report_date, start_date, end_date,
    book_count, total_duration, suggestion, detailed_data, milestones
)
SELECT
    c.id as child_id,
    ROUND(50 + RAND() * 40, 2) as vocabulary_score,
    ROUND(50 + RAND() * 40, 2) as logic_score,
    ROUND(50 + RAND() * 40, 2) as reading_score,
    ROUND(50 + RAND() * 40, 2) as focus_score,
    ROUND(50 + RAND() * 40, 2) as cognitive_score,
    ROUND(50 + RAND() * 40, 2) as social_score,
    ROUND(50 + RAND() * 40, 2) as creativity_score,
    ROUND(50 + RAND() * 40, 2) as habit_score,
    ROUND(55 + RAND() * 35, 2) as total_score,
    'monthly' as report_type,
    DATE_SUB(CURDATE(), INTERVAL m MONTH) as report_date,
    DATE_SUB(CURDATE(), INTERVAL m MONTH) - INTERVAL 29 DAY as start_date,
    DATE_SUB(CURDATE(), INTERVAL m MONTH) as end_date,
    FLOOR(10 + RAND() * 10) as book_count,
    FLOOR(5000 + RAND() * 5000) as total_duration,
    CASE
        WHEN c.reading_ability_score >= 70 THEN
            '本月阅读表现优秀，各方面能力稳步提升。建议继续保持阅读习惯，可以尝试挑战更高难度的绘本。'
        WHEN c.reading_ability_score >= 50 THEN
            '本月阅读能力有所进步，建议家长多陪伴阅读，选择孩子感兴趣的绘本主题，逐步提高阅读专注度。'
        ELSE
            '本月开始建立阅读习惯，建议从孩子感兴趣的简单绘本开始，保持每日短时间阅读，逐步培养阅读兴趣。'
    END as suggestion,
    JSON_OBJECT(
        'monthlyAvgDuration', FLOOR(20 + RAND() * 15),
        'favoriteCategory', ELT(FLOOR(1 + RAND() * 4), '动物', '科普', '情感', '童话'),
        'progressRate', ROUND(5 + RAND() * 15, 1),
        'totalReadingSessions', FLOOR(15 + RAND() * 15)
    ) as detailed_data,
    JSON_ARRAY(
        JSON_OBJECT('date', DATE_SUB(CURDATE(), INTERVAL m MONTH), 'event', '完成月度阅读目标'),
        JSON_OBJECT('date', DATE_SUB(CURDATE(), INTERVAL m MONTH) - INTERVAL 10 DAY, 'event', '阅读能力有明显进步'),
        JSON_OBJECT('date', DATE_SUB(CURDATE(), INTERVAL m MONTH) - INTERVAL 20 DAY, 'event', '养成良好的阅读习惯')
    ) as milestones
FROM child c
CROSS JOIN (
    SELECT 0 as m UNION SELECT 1 UNION SELECT 2
) months;

-- =====================================================
-- 6. 更新阅读日志的扩展字段
-- =====================================================
UPDATE reading_log SET
    replay_count = FLOOR(RAND() * 3),
    bookmark_count = IF(RAND() > 0.7, FLOOR(1 + RAND() * 2), 0),
    annotation_count = IF(RAND() > 0.8, FLOOR(1 + RAND() * 3), 0),
    voice_record_count = IF(RAND() > 0.9, 1, 0),
    emotion_data = JSON_OBJECT(
        'happy', FLOOR(20 + RAND() * 40),
        'curious', FLOOR(15 + RAND() * 30),
        'focused', FLOOR(20 + RAND() * 50),
        'confused', FLOOR(5 + RAND() * 15)
    )
WHERE replay_count IS NULL OR replay_count = 0;

-- =====================================================
-- 7. 更新行为分析表扩展字段
-- =====================================================
UPDATE behavior_analysis SET
    replay_preference_score = ROUND(30 + RAND() * 50, 2),
    interaction_score = ROUND(40 + RAND() * 40, 2),
    emotion_stability_score = ROUND(50 + RAND() * 40, 2),
    cognitive_stage = CASE
        WHEN focus_score >= 80 THEN '前运算阶段-发展良好'
        WHEN focus_score >= 60 THEN '前运算阶段-正常发展'
        ELSE '前运算阶段-需要关注'
    END,
    reading_ability_score = focus_score,
    habit_formation_score = ROUND(40 + RAND() * 40, 2),
    growth_prediction_score = ROUND(50 + RAND() * 35, 2),
    dimension_data = JSON_OBJECT(
        'durationScore', ROUND(50 + RAND() * 40, 2),
        'speedScore', ROUND(50 + RAND() * 40, 2),
        'completionScore', ROUND(60 + RAND() * 35, 2),
        'interactionScore', ROUND(40 + RAND() * 40, 2)
    )
WHERE cognitive_stage IS NULL OR cognitive_stage = '';

-- =====================================================
-- 8. 更新儿童表扩展字段
-- =====================================================
UPDATE child SET
    cognitive_stage = CASE
        WHEN reading_ability_score >= 70 THEN '前运算阶段-发展良好'
        WHEN reading_ability_score >= 50 THEN '前运算阶段-正常发展'
        ELSE '前运算阶段-需要关注'
    END,
    milestone = JSON_ARRAY(
        JSON_OBJECT('date', DATE_SUB(CURDATE(), INTERVAL FLOOR(RAND() * 30) DAY), 'event', '开始主动要求阅读'),
        JSON_OBJECT('date', DATE_SUB(CURDATE(), INTERVAL FLOOR(20 + RAND() * 20) DAY), 'event', '学会翻页'),
        JSON_OBJECT('date', DATE_SUB(CURDATE(), INTERVAL FLOOR(10 + RAND() * 15) DAY), 'event', '能识别绘本角色')
    )
WHERE cognitive_stage IS NULL OR cognitive_stage = '';

-- =====================================================
-- 显示结果统计
-- =====================================================
SELECT '=== 数据生成完成！===' AS message;
SELECT '阅读统计记录' AS '表名', COUNT(*) AS '记录数' FROM reading_stats
UNION ALL SELECT '收藏记录', COUNT(*) FROM reading_bookmark
UNION ALL SELECT '批注记录', COUNT(*) FROM reading_annotation
UNION ALL SELECT '操作日志', COUNT(*) FROM operation_log
UNION ALL SELECT '成长报告', COUNT(*) FROM growth_report
UNION ALL SELECT '阅读日志', COUNT(*) FROM reading_log
UNION ALL SELECT '行为分析', COUNT(*) FROM behavior_analysis
UNION ALL SELECT '儿童信息', COUNT(*) FROM child;
