-- =====================================================
-- 增量数据库扩展脚本 - 添加新字段
-- =====================================================

USE picture_book_system;

-- =====================================================
-- 1. 扩展行为分析表(behavior_analysis)字段
-- =====================================================
ALTER TABLE behavior_analysis ADD COLUMN replay_preference_score DECIMAL(5,2) DEFAULT 0 COMMENT '重复阅读偏好分数';
ALTER TABLE behavior_analysis ADD COLUMN interaction_score DECIMAL(5,2) DEFAULT 0 COMMENT '互动行为分数';
ALTER TABLE behavior_analysis ADD COLUMN emotion_stability_score DECIMAL(5,2) DEFAULT 0 COMMENT '情绪稳定性分数';
ALTER TABLE behavior_analysis ADD COLUMN cognitive_stage VARCHAR(50) COMMENT '认知发展阶段';
ALTER TABLE behavior_analysis ADD COLUMN reading_ability_score DECIMAL(5,2) DEFAULT 0 COMMENT '阅读能力评分';
ALTER TABLE behavior_analysis ADD COLUMN habit_formation_score DECIMAL(5,2) DEFAULT 0 COMMENT '阅读习惯养成度';
ALTER TABLE behavior_analysis ADD COLUMN growth_prediction_score DECIMAL(5,2) DEFAULT 0 COMMENT '成长预测分数';
ALTER TABLE behavior_analysis ADD COLUMN dimension_data TEXT COMMENT '分析维度数据JSON';

-- =====================================================
-- 2. 扩展成长报告表(growth_report)字段
-- =====================================================
ALTER TABLE growth_report ADD COLUMN cognitive_score DECIMAL(5,2) DEFAULT 0 COMMENT '认知发展评分';
ALTER TABLE growth_report ADD COLUMN social_score DECIMAL(5,2) DEFAULT 0 COMMENT '社交情感评分';
ALTER TABLE growth_report ADD COLUMN creativity_score DECIMAL(5,2) DEFAULT 0 COMMENT '创造力评分';
ALTER TABLE growth_report ADD COLUMN habit_score DECIMAL(5,2) DEFAULT 0 COMMENT '阅读习惯评分';
ALTER TABLE growth_report ADD COLUMN detailed_data TEXT COMMENT '详细维度数据JSON';
ALTER TABLE growth_report ADD COLUMN milestones TEXT COMMENT '成长里程碑JSON';

-- =====================================================
-- 3. 完成提示
-- =====================================================
SELECT '数据库增量扩展完成！' AS message;
