package com.picturebook.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.picturebook.dto.PageDTO;
import com.picturebook.dto.ReadingLogDTO;
import com.picturebook.entity.ReadingLog;
import com.picturebook.service.ReadingLogService;
import com.picturebook.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Tag(name = "阅读日志管理", description = "阅读日志提交和查询接口")
@RestController
@RequestMapping("/reading")
@SecurityRequirement(name = "Authorization")
public class ReadingLogController {
    
    @Autowired
    private ReadingLogService readingLogService;
    
    @Operation(summary = "提交阅读日志")
    @PostMapping("/log")
    public Result<Void> submitReadingLog(@Valid @RequestBody ReadingLogDTO dto) {
        readingLogService.submitReadingLog(dto);
        return Result.success();
    }
    
    @Operation(summary = "获取阅读日志列表")
    @GetMapping("/list")
    public Result<Page<ReadingLog>> getReadingLogs(PageDTO dto) {
        return Result.success(readingLogService.getReadingLogs(dto));
    }
    
    @Operation(summary = "获取儿童阅读日志")
    @GetMapping("/logs/{childId}")
    public Result<List<ReadingLog>> getReadingLogsByChildId(@PathVariable Long childId) {
        return Result.success(readingLogService.getReadingLogsByChildId(childId));
    }
    
    @Operation(summary = "获取儿童阅读统计")
    @GetMapping("/stats/{childId}")
    public Result<Map<String, Object>> getReadingStats(@PathVariable Long childId) {
        return Result.success(readingLogService.getReadingStats(childId));
    }
    
    @Operation(summary = "获取阅读趋势")
    @GetMapping("/trend/{childId}")
    public Result<List<Map<String, Object>>> getReadingTrend(
            @PathVariable Long childId,
            @RequestParam(defaultValue = "7") Integer days) {
        return Result.success(readingLogService.getReadingTrend(childId, days));
    }
    
    @Operation(summary = "获取类别统计")
    @GetMapping("/category-stats/{childId}")
    public Result<List<Map<String, Object>>> getCategoryStats(@PathVariable Long childId) {
        return Result.success(readingLogService.getCategoryStats(childId));
    }
    
    @Operation(summary = "生成模拟数据")
    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> generateMockData(@RequestParam(defaultValue = "50") Integer count) {
        readingLogService.generateMockData(count);
        return Result.success();
    }

    @Operation(summary = "更新阅读日志")
    @PutMapping("/log/{id}")
    public Result<Void> updateReadingLog(@PathVariable Long id, @RequestBody ReadingLogDTO dto) {
        readingLogService.updateReadingLog(id, dto);
        return Result.success();
    }

    @Operation(summary = "获取阅读日志详情")
    @GetMapping("/log/{id}")
    public Result<ReadingLog> getReadingLogById(@PathVariable Long id) {
        return Result.success(readingLogService.getReadingLogById(id));
    }
}
