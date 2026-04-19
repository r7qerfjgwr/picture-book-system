package com.picturebook.controller;

import com.picturebook.entity.BehaviorAnalysis;
import com.picturebook.service.AnalysisService;
import com.picturebook.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Tag(name = "行为分析", description = "阅读行为分析接口")
@RestController
@RequestMapping("/analysis")
@SecurityRequirement(name = "Authorization")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    @Operation(summary = "分析所有儿童")
    @PostMapping("/analyze-all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> analyzeAllChildren() {
        analysisService.analyzeAllChildren();
        return Result.success();
    }

    @Operation(summary = "分析单个儿童")
    @PostMapping("/analyze/{childId}")
    public Result<BehaviorAnalysis> analyzeChild(@PathVariable Long childId) {
        return Result.success(analysisService.analyzeChild(childId));
    }

    @Operation(summary = "获取儿童最新分析结果")
    @GetMapping("/{childId}")
    public Result<BehaviorAnalysis> getLatestAnalysis(@PathVariable Long childId) {
        return Result.success(analysisService.getLatestAnalysis(childId));
    }

    @Operation(summary = "获取班级分析列表")
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    public Result<List<BehaviorAnalysis>> getAnalysisList(@PathVariable Long classId) {
        return Result.success(analysisService.getAnalysisList(classId));
    }

    @Operation(summary = "获取班级分析统计")
    @GetMapping("/class-stats/{classId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    public Result<Map<String, Object>> getClassAnalysisStats(@PathVariable Long classId) {
        return Result.success(analysisService.getClassAnalysisStats(classId));
    }

    @Operation(summary = "获取多维度分析数据")
    @GetMapping("/multi-dimension/{childId}")
    public Result<Map<String, Object>> getMultiDimensionAnalysis(@PathVariable Long childId) {
        return Result.success(analysisService.getMultiDimensionAnalysis(childId));
    }

    @Operation(summary = "获取阅读时长分布")
    @GetMapping("/duration-distribution/{childId}")
    public Result<Map<String, Object>> getReadingDurationDistribution(@PathVariable Long childId) {
        return Result.success(analysisService.getReadingDurationDistribution(childId));
    }

    @Operation(summary = "获取翻页速率分析")
    @GetMapping("/turn-speed-analysis/{childId}")
    public Result<Map<String, Object>> getTurnSpeedAnalysis(@PathVariable Long childId) {
        return Result.success(analysisService.getTurnSpeedAnalysis(childId));
    }

    @Operation(summary = "获取成长轨迹预测")
    @GetMapping("/growth-trajectory/{childId}")
    public Result<Map<String, Object>> getGrowthTrajectory(@PathVariable Long childId) {
        return Result.success(analysisService.getGrowthTrajectory(childId));
    }

    @Operation(summary = "计算重复阅读偏好分数")
    @GetMapping("/replay-score/{childId}")
    public Result<Double> getReplayPreferenceScore(@PathVariable Long childId) {
        return Result.success(analysisService.calculateReplayPreferenceScore(childId));
    }

    @Operation(summary = "计算互动行为分数")
    @GetMapping("/interaction-score/{childId}")
    public Result<Double> getInteractionScore(@PathVariable Long childId) {
        return Result.success(analysisService.calculateInteractionScore(childId));
    }

    @Operation(summary = "计算情绪稳定性分数")
    @GetMapping("/emotion-score/{childId}")
    public Result<Double> getEmotionStabilityScore(@PathVariable Long childId) {
        return Result.success(analysisService.calculateEmotionStabilityScore(childId));
    }

    @Operation(summary = "评估认知发展阶段")
    @GetMapping("/cognitive-stage/{childId}")
    public Result<String> assessCognitiveStage(@PathVariable Long childId) {
        return Result.success(analysisService.assessCognitiveStage(childId));
    }

    @Operation(summary = "计算阅读能力评分")
    @GetMapping("/reading-ability/{childId}")
    public Result<Double> getReadingAbilityScore(@PathVariable Long childId) {
        return Result.success(analysisService.calculateReadingAbilityScore(childId));
    }

    @Operation(summary = "计算阅读习惯养成度")
    @GetMapping("/habit-score/{childId}")
    public Result<Double> getHabitFormationScore(@PathVariable Long childId) {
        return Result.success(analysisService.calculateHabitFormationScore(childId));
    }
}
