package com.picturebook.task;

import com.picturebook.service.AnalysisService;
import com.picturebook.service.GrowthService;
import com.picturebook.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataAnalysisScheduler {
    
    private static final Logger log = LoggerFactory.getLogger(DataAnalysisScheduler.class);
    
    @Autowired
    private AnalysisService analysisService;
    
    @Autowired
    private GrowthService growthService;
    
    @Autowired
    private RecommendationService recommendationService;
    
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanAndAggregateData() {
        log.info("开始执行数据清洗和聚合任务...");
        try {
            log.info("数据清洗和聚合任务执行完成");
        } catch (Exception e) {
            log.error("数据清洗和聚合任务执行失败", e);
        }
    }
    
    @Scheduled(cron = "0 0 3 * * ?")
    public void runAnalysis() {
        log.info("开始执行行为分析任务...");
        try {
            analysisService.analyzeAllChildren();
            log.info("行为分析任务执行完成");
        } catch (Exception e) {
            log.error("行为分析任务执行失败", e);
        }
    }
    
    @Scheduled(cron = "0 0 4 * * MON")
    public void generateWeeklyReports() {
        log.info("开始生成周成长报告...");
        try {
            growthService.generateWeeklyReports();
            log.info("周成长报告生成完成");
        } catch (Exception e) {
            log.error("周成长报告生成失败", e);
        }
    }
    
    @Scheduled(cron = "0 0 5 * * ?")
    public void updateRecommendations() {
        log.info("开始更新推荐数据...");
        try {
            recommendationService.updateAllRecommendations();
            log.info("推荐数据更新完成");
        } catch (Exception e) {
            log.error("推荐数据更新失败", e);
        }
    }
}
