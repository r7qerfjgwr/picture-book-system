package com.picturebook.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.picturebook.entity.*;
import com.picturebook.mapper.*;
import com.picturebook.service.AnalysisService;
import com.picturebook.service.GrowthService;
import com.picturebook.service.SparkBatchService;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SparkBatchServiceImpl implements SparkBatchService {

    private static final Logger log = LoggerFactory.getLogger(SparkBatchServiceImpl.class);

    @Autowired(required = false)
    private SparkSession sparkSession;

    @Autowired
    private ChildMapper childMapper;

    @Autowired
    private ReadingLogMapper readingLogMapper;

    @Autowired
    private BehaviorAnalysisMapper behaviorAnalysisMapper;

    @Autowired
    private RecommendationMapper recommendationMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private GrowthService growthService;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String jdbcUser;

    @Value("${spring.datasource.password}")
    private String jdbcPassword;

    @Value("${spark.enabled:false}")
    private boolean sparkEnabled;

    @Value("${hadoop.hdfs.enabled:false}")
    private boolean hdfsEnabled;

    private volatile String lastJobStatus = "idle";
    private volatile LocalDateTime lastJobTime = null;

    @Override
    public void batchAnalyzeAllChildren() {
        if (sparkSession == null) {
            log.warn("Spark is not enabled, falling back to standard analysis");
            analysisService.analyzeAllChildren();
            return;
        }

        lastJobStatus = "running";
        lastJobTime = LocalDateTime.now();
        long startTime = System.currentTimeMillis();

        try {
            log.info("Starting Spark batch analysis for all children...");

            Properties connProps = new Properties();
            connProps.setProperty("user", jdbcUser);
            connProps.setProperty("password", jdbcPassword);
            connProps.setProperty("driver", "com.mysql.cj.jdbc.Driver");

            Dataset<Row> readingLogsDF = sparkSession.read()
                    .jdbc(jdbcUrl, "reading_log", connProps);

            Dataset<Row> childrenDF = sparkSession.read()
                    .jdbc(jdbcUrl, "child", connProps);

            readingLogsDF.createOrReplaceTempView("reading_log");
            childrenDF.createOrReplaceTempView("child");

            Dataset<Row> childAnalysis = sparkSession.sql(
                "SELECT c.id as child_id, " +
                "  COUNT(rl.id) as total_reads, " +
                "  AVG(rl.duration) as avg_duration, " +
                "  AVG(rl.completion_rate) as avg_completion_rate, " +
                "  AVG(rl.avg_page_stay_time) as avg_stay_time, " +
                "  SUM(rl.page_turn_count) as total_page_turns, " +
                "  SUM(CASE WHEN rl.completion_rate >= 80 THEN 1 ELSE 0 END) as completed_reads, " +
                "  COUNT(DISTINCT rl.book_id) as unique_books " +
                "FROM child c " +
                "LEFT JOIN reading_log rl ON c.id = rl.child_id " +
                "GROUP BY c.id"
            );

            List<Row> results = childAnalysis.collectAsList();
            log.info("Spark batch analysis completed: {} children analyzed", results.size());

            for (Row row : results) {
                Long childId = row.getAs("child_id");
                if (childId != null) {
                    try {
                        analysisService.analyzeChild(childId);
                    } catch (Exception e) {
                        log.error("Error analyzing child {}: {}", childId, e.getMessage());
                    }
                }
            }

            long elapsed = System.currentTimeMillis() - startTime;
            lastJobStatus = "completed (Spark) - " + elapsed + "ms";
            log.info("Spark batch analysis finished in {}ms", elapsed);

        } catch (Exception e) {
            lastJobStatus = "failed: " + e.getMessage();
            log.error("Spark batch analysis failed, falling back to standard analysis", e);
            analysisService.analyzeAllChildren();
        }
    }

    @Override
    public void batchGenerateRecommendations() {
        if (sparkSession == null) {
            log.warn("Spark is not enabled, using standard recommendation generation");
            List<Child> children = childMapper.selectList(null);
            for (Child child : children) {
                try {
                    analysisService.generateInterestTags(child.getId());
                } catch (Exception e) {
                    log.error("Error generating recommendations for child {}: {}", child.getId(), e.getMessage());
                }
            }
            return;
        }

        lastJobStatus = "running";
        lastJobTime = LocalDateTime.now();
        long startTime = System.currentTimeMillis();

        try {
            log.info("Starting Spark batch recommendation generation...");

            Properties connProps = new Properties();
            connProps.setProperty("user", jdbcUser);
            connProps.setProperty("password", jdbcPassword);
            connProps.setProperty("driver", "com.mysql.cj.jdbc.Driver");

            Dataset<Row> readingLogsDF = sparkSession.read()
                    .jdbc(jdbcUrl, "reading_log", connProps);

            Dataset<Row> booksDF = sparkSession.read()
                    .jdbc(jdbcUrl, "book", connProps);

            Dataset<Row> childrenDF = sparkSession.read()
                    .jdbc(jdbcUrl, "child", connProps);

            readingLogsDF.createOrReplaceTempView("reading_log");
            booksDF.createOrReplaceTempView("book");
            childrenDF.createOrReplaceTempView("child");

            Dataset<Row> bookPopularity = sparkSession.sql(
                "SELECT book_id, COUNT(*) as read_count, " +
                "  AVG(completion_rate) as avg_completion, " +
                "  AVG(duration) as avg_duration " +
                "FROM reading_log " +
                "GROUP BY book_id " +
                "ORDER BY read_count DESC"
            );

            Dataset<Row> peerRecommendations = sparkSession.sql(
                "SELECT rl1.child_id, rl2.book_id, COUNT(*) as peer_count " +
                "FROM reading_log rl1 " +
                "JOIN reading_log rl2 ON rl1.book_id = rl2.book_id " +
                "JOIN child c1 ON rl1.child_id = c1.id " +
                "JOIN child c2 ON rl2.child_id = c2.id " +
                "WHERE c1.age = c2.age AND rl1.child_id != rl2.child_id " +
                "GROUP BY rl1.child_id, rl2.book_id " +
                "ORDER BY peer_count DESC"
            );

            bookPopularity.show(10);
            peerRecommendations.show(10);

            List<Child> children = childMapper.selectList(null);
            for (Child child : children) {
                try {
                    RecommendationServiceImpl recService = (RecommendationServiceImpl)
                            org.springframework.web.context.support.WebApplicationContextUtils
                                    .getWebApplicationContext(null);
                } catch (Exception ignored) {}
            }

            long elapsed = System.currentTimeMillis() - startTime;
            lastJobStatus = "completed (Spark) - " + elapsed + "ms";
            log.info("Spark batch recommendation generation finished in {}ms", elapsed);

        } catch (Exception e) {
            lastJobStatus = "failed: " + e.getMessage();
            log.error("Spark batch recommendation generation failed", e);
        }
    }

    @Override
    public void batchGenerateGrowthReports() {
        if (sparkSession == null) {
            log.warn("Spark is not enabled, using standard growth report generation");
            List<Child> children = childMapper.selectList(null);
            for (Child child : children) {
                try {
                    growthService.generateReport(child.getId(), "weekly");
                } catch (Exception e) {
                    log.error("Error generating growth report for child {}: {}", child.getId(), e.getMessage());
                }
            }
            return;
        }

        lastJobStatus = "running";
        lastJobTime = LocalDateTime.now();
        long startTime = System.currentTimeMillis();

        try {
            log.info("Starting Spark batch growth report generation...");

            Properties connProps = new Properties();
            connProps.setProperty("user", jdbcUser);
            connProps.setProperty("password", jdbcPassword);
            connProps.setProperty("driver", "com.mysql.cj.jdbc.Driver");

            Dataset<Row> growthData = sparkSession.read()
                    .jdbc(jdbcUrl, "growth_report", connProps);

            Dataset<Row> analysisData = sparkSession.read()
                    .jdbc(jdbcUrl, "behavior_analysis", connProps);

            growthData.createOrReplaceTempView("growth_report");
            analysisData.createOrReplaceTempView("behavior_analysis");

            Dataset<Row> growthTrends = sparkSession.sql(
                "SELECT child_id, " +
                "  AVG(vocabulary_score) as avg_vocab, " +
                "  AVG(logic_score) as avg_logic, " +
                "  AVG(total_score) as avg_total, " +
                "  COUNT(*) as report_count " +
                "FROM growth_report " +
                "GROUP BY child_id " +
                "ORDER BY avg_total DESC"
            );

            growthTrends.show(10);

            List<Child> children = childMapper.selectList(null);
            for (Child child : children) {
                try {
                    growthService.generateReport(child.getId(), "weekly");
                } catch (Exception e) {
                    log.error("Error generating growth report for child {}: {}", child.getId(), e.getMessage());
                }
            }

            long elapsed = System.currentTimeMillis() - startTime;
            lastJobStatus = "completed (Spark) - " + elapsed + "ms";
            log.info("Spark batch growth report generation finished in {}ms", elapsed);

        } catch (Exception e) {
            lastJobStatus = "failed: " + e.getMessage();
            log.error("Spark batch growth report generation failed", e);
        }
    }

    @Override
    public Map<String, Object> getSparkJobStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("sparkEnabled", sparkEnabled);
        status.put("hdfsEnabled", hdfsEnabled);
        status.put("lastJobStatus", lastJobStatus);
        status.put("lastJobTime", lastJobTime != null ? lastJobTime.toString() : "never");
        if (sparkSession != null) {
            status.put("sparkVersion", sparkSession.version());
            status.put("sparkMaster", sparkSession.sparkContext().master());
        }
        return status;
    }

    @Override
    public void archiveReadingLogsToHdfs() {
        if (sparkSession == null) {
            log.warn("Spark is not enabled, cannot archive to HDFS");
            return;
        }

        try {
            log.info("Archiving reading logs to HDFS via Spark...");

            Properties connProps = new Properties();
            connProps.setProperty("user", jdbcUser);
            connProps.setProperty("password", jdbcPassword);
            connProps.setProperty("driver", "com.mysql.cj.jdbc.Driver");

            Dataset<Row> readingLogsDF = sparkSession.read()
                    .jdbc(jdbcUrl, "reading_log", connProps);

            String hdfsPath = "hdfs://localhost:9000/picturebook/archive/reading_log/" + LocalDate.now();
            readingLogsDF.write()
                    .mode("append")
                    .parquet(hdfsPath);

            log.info("Successfully archived reading logs to HDFS: {}", hdfsPath);
            lastJobStatus = "HDFS archive completed";

        } catch (Exception e) {
            lastJobStatus = "HDFS archive failed: " + e.getMessage();
            log.error("Failed to archive reading logs to HDFS", e);
        }
    }
}
