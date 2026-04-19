-- 更新绘本扩展字段数据
-- 使用UTF-8编码执行

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE picture_book_system;

-- 更新动物类绘本
UPDATE book SET
    age_range = CASE
        WHEN difficulty_level = 1 THEN '3-4岁'
        WHEN difficulty_level = 2 THEN '4-5岁'
        ELSE '5-6岁'
    END,
    knowledge_type = '科学',
    art_style = CASE
        WHEN id % 3 = 0 THEN '水彩'
        WHEN id % 3 = 1 THEN '卡通'
        ELSE '写实'
    END,
    theme = '自然探索,生命教育',
    keywords = '动物,自然,成长'
WHERE category = '动物';

-- 更新科普类绘本
UPDATE book SET
    age_range = CASE
        WHEN difficulty_level = 1 THEN '3-4岁'
        WHEN difficulty_level = 2 THEN '4-5岁'
        ELSE '5-6岁'
    END,
    knowledge_type = '科学',
    art_style = CASE
        WHEN id % 3 = 0 THEN '写实'
        WHEN id % 3 = 1 THEN '卡通'
        ELSE '水彩'
    END,
    theme = '科学启蒙,认知发展',
    keywords = '科普,科学,知识'
WHERE category = '科普';

-- 更新情感类绘本
UPDATE book SET
    age_range = CASE
        WHEN difficulty_level = 1 THEN '3-4岁'
        WHEN difficulty_level = 2 THEN '4-5岁'
        ELSE '5-6岁'
    END,
    knowledge_type = '社会',
    art_style = CASE
        WHEN id % 3 = 0 THEN '水彩'
        WHEN id % 3 = 1 THEN '卡通'
        ELSE '拼贴'
    END,
    theme = '情感培养,性格养成',
    keywords = '情感,成长,品格'
WHERE category = '情感';

-- 更新童话类绘本
UPDATE book SET
    age_range = CASE
        WHEN difficulty_level = 1 THEN '3-4岁'
        WHEN difficulty_level = 2 THEN '4-5岁'
        ELSE '5-6岁'
    END,
    knowledge_type = '语言',
    art_style = CASE
        WHEN id % 3 = 0 THEN '水彩'
        WHEN id % 3 = 1 THEN '卡通'
        ELSE '剪纸'
    END,
    theme = '想象力,价值观',
    keywords = '童话,故事,想象'
WHERE category = '童话';

-- 显示更新结果
SELECT '绘本扩展字段更新完成！' AS message;
SELECT category, COUNT(*) as count,
       GROUP_CONCAT(DISTINCT age_range) as age_ranges,
       GROUP_CONCAT(DISTINCT knowledge_type) as knowledge_types,
       GROUP_CONCAT(DISTINCT art_style) as art_styles
FROM book
GROUP BY category;
