package com.picturebook.service;

import java.util.List;
import java.util.Map;

public interface StatsService {

    /**
     * 获取系统总体统计
     */
    Map<String, Object> getOverviewStats();

    /**
     * 获取阅读趋势数据
     */
    List<Map<String, Object>> getReadingTrend(Integer days);

    /**
     * 获取绘本分类统计
     */
    List<Map<String, Object>> getBookCategoryStats();

    /**
     * 获取年龄段分布
     */
    List<Map<String, Object>> getAgeDistribution();

    /**
     * 获取阅读时段分布
     */
    List<Map<String, Object>> getReadingHoursDistribution();

    /**
     * 获取管理员仪表盘数据
     */
    Map<String, Object> getAdminDashboardStats();

    /**
     * 获取教师仪表盘数据
     */
    Map<String, Object> getTeacherDashboardStats(Long classId);

    /**
     * 获取家长仪表盘数据
     */
    Map<String, Object> getParentDashboardStats(Long childId);

    /**
     * 获取今日阅读统计
     */
    Map<String, Object> getTodayStats();

    /**
     * 获取本周阅读统计
     */
    Map<String, Object> getWeekStats();

    /**
     * 获取今日活跃用户数
     */
    Map<String, Object> getActiveUsersToday();

    /**
     * 获取操作日志列表
     */
    Map<String, Object> getOperationLogs(Integer current, Integer size);

    /**
     * 获取儿童分析概览数据
     */
    Map<String, Object> getChildrenAnalysis();

    /**
     * 获取儿童排行榜
     */
    List<Map<String, Object>> getChildrenRanking(String type, Integer limit);

    Map<String, Object> getBigscreenData();
}
