package com.picturebook.service;

import com.picturebook.entity.GrowthReport;
import java.util.List;
import java.util.Map;

public interface GrowthService {

    GrowthReport generateReport(Long childId, String reportType);

    GrowthReport getLatestReport(Long childId);

    List<GrowthReport> getReportHistory(Long childId);

    List<GrowthReport> getReportHistoryByType(Long childId, String reportType);

    void generateWeeklyReports();

    void generateMonthlyReports();

    // 新增方法：获取成长曲线数据
    Map<String, Object> getGrowthCurve(Long childId, String dimension);

    // 新增方法：获取同龄儿童对比数据
    Map<String, Object> getPeerComparison(Long childId);

    // 新增方法：获取成长里程碑
    List<Map<String, Object>> getMilestones(Long childId);

    // 新增方法：生成个性化阅读计划
    Map<String, Object> generateReadingPlan(Long childId);

    // 新增方法：获取能力雷达图数据
    Map<String, Object> getAbilityRadar(Long childId);

    // 新增方法：生成家长指导手册
    Map<String, Object> generateParentGuide(Long childId);
}
