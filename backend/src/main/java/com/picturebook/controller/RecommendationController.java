package com.picturebook.controller;

import com.picturebook.entity.Book;
import com.picturebook.entity.Recommendation;
import com.picturebook.service.RecommendationService;
import com.picturebook.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Tag(name = "绘本推荐", description = "个性化绘本推荐接口")
@RestController
@RequestMapping("/recommend")
@SecurityRequirement(name = "Authorization")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Operation(summary = "获取个性化推荐")
    @GetMapping("/{childId}")
    public Result<List<Recommendation>> getRecommendations(@PathVariable Long childId) {
        return Result.success(recommendationService.getRecommendations(childId));
    }

    @Operation(summary = "获取指定类型推荐")
    @GetMapping("/{childId}/{recommendType}")
    public Result<List<Recommendation>> getRecommendationsByType(
            @PathVariable Long childId,
            @PathVariable String recommendType) {
        return Result.success(recommendationService.getRecommendationsByType(childId, recommendType));
    }

    @Operation(summary = "获取热门绘本")
    @GetMapping("/hot")
    public Result<List<Book>> getHotBooks(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(recommendationService.getHotBooks(limit));
    }

    @Operation(summary = "更新推荐数据")
    @PostMapping("/update")
    public Result<Void> updateRecommendations() {
        recommendationService.updateAllRecommendations();
        return Result.success();
    }

    @Operation(summary = "获取多策略融合推荐")
    @GetMapping("/multi-strategy/{childId}")
    public Result<List<Map<String, Object>>> getMultiStrategyRecommendations(
            @PathVariable Long childId,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(recommendationService.getMultiStrategyRecommendations(childId, limit));
    }

    @Operation(summary = "获取难度匹配推荐")
    @GetMapping("/difficulty/{childId}")
    public Result<List<Book>> getDifficultyMatchedRecommendations(
            @PathVariable Long childId,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(recommendationService.getDifficultyMatchedRecommendations(childId, limit));
    }

    @Operation(summary = "获取年龄段推荐")
    @GetMapping("/age-range/{childId}")
    public Result<List<Book>> getAgeRangeRecommendations(
            @PathVariable Long childId,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(recommendationService.getAgeRangeRecommendations(childId, limit));
    }

    @Operation(summary = "获取推荐解释")
    @GetMapping("/explanation/{childId}/{bookId}")
    public Result<Map<String, Object>> getRecommendationExplanation(
            @PathVariable Long childId,
            @PathVariable Long bookId) {
        return Result.success(recommendationService.getRecommendationExplanation(childId, bookId));
    }

    @Operation(summary = "计算匹配度")
    @GetMapping("/match-score/{childId}/{bookId}")
    public Result<Map<String, Object>> calculateMatchScore(
            @PathVariable Long childId,
            @PathVariable Long bookId) {
        return Result.success(recommendationService.calculateMatchScore(childId, bookId));
    }
}
