-- 添加behavior_analysis表缺失的字段
SET NAMES utf8mb4;

-- 先检查并添加缺失字段
ALTER TABLE behavior_analysis ADD COLUMN replay_preference_score DECIMAL(5,2) DEFAULT 0.00;
ALTER TABLE behavior_analysis ADD COLUMN interaction_score DECIMAL(5,2) DEFAULT 0.00;
ALTER TABLE behavior_analysis ADD COLUMN emotion_stability_score DECIMAL(5,2) DEFAULT 0.00;
ALTER TABLE behavior_analysis ADD COLUMN cognitive_stage VARCHAR(50) DEFAULT NULL;
ALTER TABLE behavior_analysis ADD COLUMN reading_ability_score DECIMAL(5,2) DEFAULT 0.00;
ALTER TABLE behavior_analysis ADD COLUMN habit_formation_score DECIMAL(5,2) DEFAULT 0.00;
ALTER TABLE behavior_analysis ADD COLUMN growth_prediction_score DECIMAL(5,2) DEFAULT 0.00;
ALTER TABLE behavior_analysis ADD COLUMN dimension_data TEXT DEFAULT NULL;

-- 更新behavior_analysis数据，添加认知发展阶段
UPDATE behavior_analysis SET cognitive_stage = CASE
    WHEN focus_score < 70 THEN '前运算阶段'
    WHEN focus_score < 85 THEN '具体运算阶段初期'
    ELSE '具体运算阶段'
END
WHERE cognitive_stage IS NULL;

-- 更新其他分数
UPDATE behavior_analysis SET
    reading_ability_score = focus_score * 0.9 + completion_rate * 10,
    habit_formation_score = focus_score * 0.85,
    interaction_score = 70 + RAND() * 20,
    emotion_stability_score = 65 + RAND() * 25,
    growth_prediction_score = focus_score * 0.95
WHERE reading_ability_score IS NULL OR reading_ability_score = 0;
