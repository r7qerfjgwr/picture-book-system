package com.picturebook.controller;

import com.picturebook.vo.Result;
import com.picturebook.service.SparkBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/spark")
public class SparkController {

    @Autowired
    private SparkBatchService sparkBatchService;

    @PostMapping("/analyze")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> triggerBatchAnalysis() {
        sparkBatchService.batchAnalyzeAllChildren();
        return Result.success("Spark batch analysis triggered");
    }

    @PostMapping("/recommend")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> triggerBatchRecommendations() {
        sparkBatchService.batchGenerateRecommendations();
        return Result.success("Spark batch recommendation generation triggered");
    }

    @PostMapping("/growth-reports")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> triggerBatchGrowthReports() {
        sparkBatchService.batchGenerateGrowthReports();
        return Result.success("Spark batch growth report generation triggered");
    }

    @PostMapping("/archive-hdfs")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> triggerHdfsArchive() {
        sparkBatchService.archiveReadingLogsToHdfs();
        return Result.success("HDFS archive triggered");
    }

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Map<String, Object>> getSparkStatus() {
        return Result.success(sparkBatchService.getSparkJobStatus());
    }
}
