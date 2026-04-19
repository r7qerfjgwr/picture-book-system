package com.picturebook.service;

import com.picturebook.entity.BehaviorAnalysis;
import java.util.List;
import java.util.Map;

public interface AnalysisService {

    void analyzeAllChildren();

    BehaviorAnalysis analyzeChild(Long childId);

    BehaviorAnalysis getLatestAnalysis(Long childId);

    List<BehaviorAnalysis> getAnalysisList(Long classId);

    Map<String, Object> getClassAnalysisStats(Long classId);

    String classifyReadingType(Double avgDuration, Double turnSpeed, Double completionRate);

    Double calculateFocusScore(Long childId);

    List<String> generateInterestTags(Long childId);

    // 新增方法：计算重复阅读偏好分数
    Double calculateReplayPreferenceScore(Long childId);

    // 新增方法：计算互动行为分数
    Double calculateInteractionScore(Long childId);

    // 新增方法：分析情绪稳定性
    Double calculateEmotionStabilityScore(Long childId);

    // 新增方法：评估认知发展阶段
    String assessCognitiveStage(Long childId);

    // 新增方法：计算阅读能力评分
    Double calculateReadingAbilityScore(Long childId);

    // 新增方法：计算阅读习惯养成度
    Double calculateHabitFormationScore(Long childId);

    // 新增方法：预测成长趋势
    Double predictGrowthTrend(Long childId);

    // 新增方法：获取多维度分析数据
    Map<String, Object> getMultiDimensionAnalysis(Long childId);

    // 新增方法：生成阅读时长分布
    Map<String, Object> getReadingDurationDistribution(Long childId);

    // 新增方法：生成翻页速率分析
    Map<String, Object> getTurnSpeedAnalysis(Long childId);

    // 新增方法：生成成长轨迹预测
    Map<String, Object> getGrowthTrajectory(Long childId);
}
