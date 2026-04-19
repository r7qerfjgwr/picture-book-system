package com.picturebook.controller;

import com.picturebook.entity.Book;
import com.picturebook.entity.GrowthReport;
import com.picturebook.entity.Recommendation;
import com.picturebook.service.GrowthService;
import com.picturebook.service.RecommendationService;
import com.picturebook.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Tag(name = "成长报告", description = "成长报告生成和查询接口")
@RestController
@RequestMapping("/growth")
@SecurityRequirement(name = "Authorization")
public class GrowthController {

    @Autowired
    private GrowthService growthService;

    @Operation(summary = "生成成长报告")
    @PostMapping("/generate/{childId}")
    public Result<GrowthReport> generateReport(
            @PathVariable Long childId,
            @RequestParam(defaultValue = "weekly") String reportType) {
        return Result.success(growthService.generateReport(childId, reportType));
    }

    @Operation(summary = "获取最新成长报告")
    @GetMapping("/latest/{childId}")
    public Result<GrowthReport> getLatestReport(@PathVariable Long childId) {
        return Result.success(growthService.getLatestReport(childId));
    }

    @Operation(summary = "获取成长报告历史")
    @GetMapping("/history/{childId}")
    public Result<List<GrowthReport>> getReportHistory(@PathVariable Long childId) {
        return Result.success(growthService.getReportHistory(childId));
    }

    @Operation(summary = "获取指定类型成长报告历史")
    @GetMapping("/history/{childId}/{reportType}")
    public Result<List<GrowthReport>> getReportHistoryByType(
            @PathVariable Long childId,
            @PathVariable String reportType) {
        return Result.success(growthService.getReportHistoryByType(childId, reportType));
    }

    @Operation(summary = "获取成长曲线数据")
    @GetMapping("/curve/{childId}")
    public Result<Map<String, Object>> getGrowthCurve(
            @PathVariable Long childId,
            @RequestParam(defaultValue = "total") String dimension) {
        return Result.success(growthService.getGrowthCurve(childId, dimension));
    }

    @Operation(summary = "获取同龄儿童对比数据")
    @GetMapping("/peer-comparison/{childId}")
    public Result<Map<String, Object>> getPeerComparison(@PathVariable Long childId) {
        return Result.success(growthService.getPeerComparison(childId));
    }

    @Operation(summary = "获取成长里程碑")
    @GetMapping("/milestones/{childId}")
    public Result<List<Map<String, Object>>> getMilestones(@PathVariable Long childId) {
        return Result.success(growthService.getMilestones(childId));
    }

    @Operation(summary = "生成个性化阅读计划")
    @GetMapping("/reading-plan/{childId}")
    public Result<Map<String, Object>> generateReadingPlan(@PathVariable Long childId) {
        return Result.success(growthService.generateReadingPlan(childId));
    }

    @Operation(summary = "获取能力雷达图数据")
    @GetMapping("/ability-radar/{childId}")
    public Result<Map<String, Object>> getAbilityRadar(@PathVariable Long childId) {
        return Result.success(growthService.getAbilityRadar(childId));
    }

    @Operation(summary = "生成家长指导手册")
    @GetMapping("/parent-guide/{childId}")
    public Result<Map<String, Object>> generateParentGuide(@PathVariable Long childId) {
        return Result.success(growthService.generateParentGuide(childId));
    }
}
