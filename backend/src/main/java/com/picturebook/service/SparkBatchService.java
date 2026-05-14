package com.picturebook.service;

import java.util.List;
import java.util.Map;

public interface SparkBatchService {

    void batchAnalyzeAllChildren();

    void batchGenerateRecommendations();

    void batchGenerateGrowthReports();

    Map<String, Object> getSparkJobStatus();

    void archiveReadingLogsToHdfs();
}
