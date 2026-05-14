package com.picturebook.controller;

import com.picturebook.service.StatsService;
import com.picturebook.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "统计数据", description = "系统统计数据接口")
@RestController
@RequestMapping("/stats")
@SecurityRequirement(name = "Authorization")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @Operation(summary = "获取系统总体统计")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverviewStats() {
        return Result.success(statsService.getOverviewStats());
    }

    @Operation(summary = "获取阅读趋势数据")
    @GetMapping("/reading-trend")
    public Result<List<Map<String, Object>>> getReadingTrend(
            @RequestParam(defaultValue = "7") Integer days) {
        return Result.success(statsService.getReadingTrend(days));
    }

    @Operation(summary = "获取绘本分类统计")
    @GetMapping("/book-categories")
    public Result<List<Map<String, Object>>> getBookCategoryStats() {
        return Result.success(statsService.getBookCategoryStats());
    }

    @Operation(summary = "获取年龄段分布")
    @GetMapping("/age-distribution")
    public Result<List<Map<String, Object>>> getAgeDistribution() {
        return Result.success(statsService.getAgeDistribution());
    }

    @Operation(summary = "获取阅读时段分布")
    @GetMapping("/reading-hours")
    public Result<List<Map<String, Object>>> getReadingHoursDistribution() {
        return Result.success(statsService.getReadingHoursDistribution());
    }

    @Operation(summary = "获取管理员仪表盘数据")
    @GetMapping("/admin-dashboard")
    public Result<Map<String, Object>> getAdminDashboard() {
        return Result.success(statsService.getAdminDashboardStats());
    }

    @Operation(summary = "获取教师仪表盘数据")
    @GetMapping("/teacher-dashboard")
    public Result<Map<String, Object>> getTeacherDashboard(
            @RequestParam(required = false) Long classId) {
        return Result.success(statsService.getTeacherDashboardStats(classId));
    }

    @Operation(summary = "获取家长仪表盘数据")
    @GetMapping("/parent-dashboard")
    public Result<Map<String, Object>> getParentDashboard(@RequestParam Long childId) {
        return Result.success(statsService.getParentDashboardStats(childId));
    }

    @Operation(summary = "获取今日阅读统计")
    @GetMapping("/today")
    public Result<Map<String, Object>> getTodayStats() {
        return Result.success(statsService.getTodayStats());
    }

    @Operation(summary = "获取本周阅读统计")
    @GetMapping("/week")
    public Result<Map<String, Object>> getWeekStats() {
        return Result.success(statsService.getWeekStats());
    }

    @Operation(summary = "获取今日活跃用户数")
    @GetMapping("/active-users-today")
    public Result<Map<String, Object>> getActiveUsersToday() {
        return Result.success(statsService.getActiveUsersToday());
    }

    @Operation(summary = "获取操作日志列表")
    @GetMapping("/operation-logs")
    public Result<Map<String, Object>> getOperationLogs(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(statsService.getOperationLogs(current, size));
    }

    @Operation(summary = "获取儿童分析概览数据")
    @GetMapping("/children-analysis")
    public Result<Map<String, Object>> getChildrenAnalysis() {
        return Result.success(statsService.getChildrenAnalysis());
    }

    @Operation(summary = "获取儿童排行榜")
    @GetMapping("/children-ranking")
    public Result<List<Map<String, Object>>> getChildrenRanking(
            @RequestParam(defaultValue = "readingTime") String type,
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(statsService.getChildrenRanking(type, limit));
    }

    @Operation(summary = "获取可视化大屏数据")
    @GetMapping("/bigscreen")
    public Result<Map<String, Object>> getBigscreenData() {
        return Result.success(statsService.getBigscreenData());
    }
}
