package com.picturebook.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.picturebook.entity.BehaviorAnalysis;
import com.picturebook.entity.Child;
import com.picturebook.entity.ReadingLog;
import com.picturebook.mapper.BehaviorAnalysisMapper;
import com.picturebook.mapper.ChildMapper;
import com.picturebook.mapper.ReadingLogMapper;
import com.picturebook.service.AnalysisService;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    @Autowired
    private ChildMapper childMapper;

    @Autowired
    private ReadingLogMapper readingLogMapper;

    @Autowired
    private BehaviorAnalysisMapper behaviorAnalysisMapper;

    @Value("${picturebook.analysis.k-means-clusters:3}")
    private int kMeansClusters;

    private static final String[] READING_TYPES = {"专注型", "跳跃型", "兴趣导向型"};
    private static final String[] COGNITIVE_STAGES = {"感知运动阶段", "前运算阶段", "具体运算阶段", "形式运算阶段"};

    @Override
    public void analyzeAllChildren() {
        List<Child> children = childMapper.selectList(null);
        for (Child child : children) {
            analyzeChild(child.getId());
        }
    }

    @Override
    public BehaviorAnalysis analyzeChild(Long childId) {
        List<ReadingLog> logs = readingLogMapper.selectLogsByChildId(childId);
        if (logs.isEmpty()) {
            return null;
        }

        // 基础指标计算
        double avgDuration = logs.stream()
                .filter(l -> l.getDuration() != null)
                .mapToDouble(l -> l.getDuration().doubleValue())
                .average()
                .orElse(0);

        double avgTurnSpeed = calculateTurnSpeed(logs);

        double completionRate = logs.stream()
                .filter(l -> l.getCompletionRate() != null)
                .mapToDouble(l -> l.getCompletionRate().doubleValue())
                .average()
                .orElse(0);

        double focusScore = calculateFocusScore(childId);

        String readingType = classifyReadingType(avgDuration, avgTurnSpeed, completionRate);

        List<String> interestTags = generateInterestTags(childId);

        // 新增指标计算
        double replayScore = calculateReplayPreferenceScore(childId);
        double interactionScore = calculateInteractionScore(childId);
        double emotionScore = calculateEmotionStabilityScore(childId);
        String cognitiveStage = assessCognitiveStage(childId);
        double readingAbility = calculateReadingAbilityScore(childId);
        double habitScore = calculateHabitFormationScore(childId);
        double growthPrediction = predictGrowthTrend(childId);

        // 多维度数据
        Map<String, Object> dimensionData = getMultiDimensionAnalysis(childId);

        BehaviorAnalysis analysis = new BehaviorAnalysis();
        analysis.setChildId(childId);
        analysis.setReadingType(readingType);
        analysis.setInterestTags(String.join(",", interestTags));
        analysis.setFocusScore(BigDecimal.valueOf(focusScore).setScale(2, RoundingMode.HALF_UP));
        analysis.setAvgReadingDuration(BigDecimal.valueOf(avgDuration).setScale(2, RoundingMode.HALF_UP));
        analysis.setAvgTurnSpeed(BigDecimal.valueOf(avgTurnSpeed).setScale(2, RoundingMode.HALF_UP));
        analysis.setCompletionRate(BigDecimal.valueOf(completionRate).setScale(2, RoundingMode.HALF_UP));
        analysis.setTotalReadingCount(logs.size());

        // 新增字段
        analysis.setReplayPreferenceScore(BigDecimal.valueOf(replayScore).setScale(2, RoundingMode.HALF_UP));
        analysis.setInteractionScore(BigDecimal.valueOf(interactionScore).setScale(2, RoundingMode.HALF_UP));
        analysis.setEmotionStabilityScore(BigDecimal.valueOf(emotionScore).setScale(2, RoundingMode.HALF_UP));
        analysis.setCognitiveStage(cognitiveStage);
        analysis.setReadingAbilityScore(BigDecimal.valueOf(readingAbility).setScale(2, RoundingMode.HALF_UP));
        analysis.setHabitFormationScore(BigDecimal.valueOf(habitScore).setScale(2, RoundingMode.HALF_UP));
        analysis.setGrowthPredictionScore(BigDecimal.valueOf(growthPrediction).setScale(2, RoundingMode.HALF_UP));
        analysis.setDimensionData(JSONUtil.toJsonStr(dimensionData));

        analysis.setAnalysisDate(LocalDate.now());

        behaviorAnalysisMapper.insert(analysis);

        // 更新儿童信息
        Child child = new Child();
        child.setId(childId);
        child.setReadingType(readingType);
        child.setInterestTags(String.join(",", interestTags));
        child.setFocusScore(BigDecimal.valueOf(focusScore).setScale(2, RoundingMode.HALF_UP));
        child.setCognitiveStage(cognitiveStage);
        child.setReadingAbilityScore(BigDecimal.valueOf(readingAbility).setScale(2, RoundingMode.HALF_UP));
        childMapper.updateById(child);

        return analysis;
    }

    private double calculateTurnSpeed(List<ReadingLog> logs) {
        double totalSpeed = 0;
        int count = 0;
        for (ReadingLog log : logs) {
            if (log.getDuration() != null && log.getDuration() > 0
                && log.getPageTurnCount() != null && log.getPageTurnCount() > 0) {
                double speed = (double) log.getPageTurnCount() / (log.getDuration() / 60.0);
                totalSpeed += speed;
                count++;
            }
        }
        return count > 0 ? totalSpeed / count : 0;
    }

    @Override
    public String classifyReadingType(Double avgDuration, Double turnSpeed, Double completionRate) {
        double avgDur = avgDuration != null ? avgDuration : 0;
        double turnSpd = turnSpeed != null ? turnSpeed : 0;
        double compRate = completionRate != null ? completionRate : 0;

        double normalizedDuration = Math.min(avgDur / 1800.0, 1.0);
        double normalizedCompletion = compRate / 100.0;
        double normalizedSpeed = Math.min(turnSpd / 5.0, 1.0);

        double focusScore = normalizedDuration * 0.4 + normalizedCompletion * 0.4 + (1 - normalizedSpeed) * 0.2;

        if (focusScore >= 0.7) {
            return "专注型";
        } else if (focusScore >= 0.4) {
            return "兴趣导向型";
        } else {
            return "跳跃型";
        }
    }

    @Override
    public BehaviorAnalysis getLatestAnalysis(Long childId) {
        return behaviorAnalysisMapper.selectLatestByChildId(childId);
    }

    @Override
    public List<BehaviorAnalysis> getAnalysisList(Long classId) {
        List<Child> children = childMapper.selectChildrenByClassId(classId);
        return children.stream()
                .map(c -> behaviorAnalysisMapper.selectLatestByChildId(c.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getClassAnalysisStats(Long classId) {
        Map<String, Object> stats = new HashMap<>();

        List<BehaviorAnalysis> analyses = getAnalysisList(classId);

        Map<String, Long> typeDistribution = analyses.stream()
                .collect(Collectors.groupingBy(BehaviorAnalysis::getReadingType, Collectors.counting()));
        stats.put("typeDistribution", typeDistribution);

        double avgFocusScore = analyses.stream()
                .filter(a -> a.getFocusScore() != null)
                .mapToDouble(a -> a.getFocusScore().doubleValue())
                .average()
                .orElse(0);
        stats.put("avgFocusScore", BigDecimal.valueOf(avgFocusScore).setScale(2, RoundingMode.HALF_UP));

        double avgCompletionRate = analyses.stream()
                .filter(a -> a.getCompletionRate() != null)
                .mapToDouble(a -> a.getCompletionRate().doubleValue())
                .average()
                .orElse(0);
        stats.put("avgCompletionRate", BigDecimal.valueOf(avgCompletionRate).setScale(2, RoundingMode.HALF_UP));

        // 新增班级统计
        double avgReadingAbility = analyses.stream()
                .filter(a -> a.getReadingAbilityScore() != null)
                .mapToDouble(a -> a.getReadingAbilityScore().doubleValue())
                .average()
                .orElse(0);
        stats.put("avgReadingAbility", BigDecimal.valueOf(avgReadingAbility).setScale(2, RoundingMode.HALF_UP));

        double avgHabitScore = analyses.stream()
                .filter(a -> a.getHabitFormationScore() != null)
                .mapToDouble(a -> a.getHabitFormationScore().doubleValue())
                .average()
                .orElse(0);
        stats.put("avgHabitScore", BigDecimal.valueOf(avgHabitScore).setScale(2, RoundingMode.HALF_UP));

        // 认知阶段分布
        Map<String, Long> stageDistribution = analyses.stream()
                .filter(a -> a.getCognitiveStage() != null)
                .collect(Collectors.groupingBy(BehaviorAnalysis::getCognitiveStage, Collectors.counting()));
        stats.put("stageDistribution", stageDistribution);

        return stats;
    }

    @Override
    public Double calculateFocusScore(Long childId) {
        List<ReadingLog> logs = readingLogMapper.selectLogsByChildId(childId);
        if (logs.isEmpty()) {
            return 0.0;
        }

        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (ReadingLog log : logs) {
            if (log.getAvgPageStayTime() != null) {
                stats.addValue(log.getAvgPageStayTime().doubleValue());
            }
        }

        if (stats.getN() == 0) {
            return 0.0;
        }

        double avgStayTime = stats.getMean();
        double variance = stats.getVariance();

        double stayTimeScore = Math.min(avgStayTime / 30.0 * 60, 60);
        double stabilityScore = Math.max(0, 40 - variance / 10.0);

        return Math.min(stayTimeScore + stabilityScore, 100);
    }

    @Override
    public List<String> generateInterestTags(Long childId) {
        List<Map<String, Object>> categoryStats = readingLogMapper.selectCategoryStatsByChildId(childId);

        return categoryStats.stream()
                .sorted((a, b) -> {
                    Long countA = ((Number) a.get("count")).longValue();
                    Long countB = ((Number) b.get("count")).longValue();
                    return countB.compareTo(countA);
                })
                .limit(3)
                .map(m -> (String) m.get("category"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Double calculateReplayPreferenceScore(Long childId) {
        List<ReadingLog> logs = readingLogMapper.selectLogsByChildId(childId);
        if (logs.isEmpty()) {
            return 0.0;
        }

        // 统计每本书的阅读次数
        Map<Long, Long> bookReadCount = logs.stream()
                .filter(l -> l.getBookId() != null)
                .collect(Collectors.groupingBy(ReadingLog::getBookId, Collectors.counting()));

        // 计算重复阅读率
        long totalReads = logs.size();
        long uniqueBooks = bookReadCount.size();
        long replayCount = totalReads - uniqueBooks;

        double replayRate = (double) replayCount / totalReads;

        // 计算重复阅读深度（重复阅读超过2次的绘本比例）
        long deepReplayBooks = bookReadCount.values().stream()
                .filter(count -> count >= 2)
                .count();
        double deepReplayRate = uniqueBooks > 0 ? (double) deepReplayBooks / uniqueBooks : 0;

        // 综合评分：重复阅读率占60%，深度重复阅读率占40%
        double score = replayRate * 60 + deepReplayRate * 40;

        return Math.min(score, 100);
    }

    @Override
    public Double calculateInteractionScore(Long childId) {
        List<ReadingLog> logs = readingLogMapper.selectLogsByChildId(childId);
        if (logs.isEmpty()) {
            return 0.0;
        }

        int totalBookmarks = logs.stream()
                .filter(l -> l.getBookmarkCount() != null)
                .mapToInt(ReadingLog::getBookmarkCount)
                .sum();

        int totalAnnotations = logs.stream()
                .filter(l -> l.getAnnotationCount() != null)
                .mapToInt(ReadingLog::getAnnotationCount)
                .sum();

        int totalVoiceRecords = logs.stream()
                .filter(l -> l.getVoiceRecordCount() != null)
                .mapToInt(ReadingLog::getVoiceRecordCount)
                .sum();

        // 每次互动得10分，上限100分
        double score = (totalBookmarks + totalAnnotations + totalVoiceRecords) * 10.0;

        return Math.min(score, 100);
    }

    @Override
    public Double calculateEmotionStabilityScore(Long childId) {
        List<ReadingLog> logs = readingLogMapper.selectLogsByChildId(childId);
        if (logs.isEmpty()) {
            return 50.0; // 默认中等分数
        }

        // 分析情绪数据
        List<String> emotionDataList = logs.stream()
                .filter(l -> l.getEmotionData() != null)
                .map(ReadingLog::getEmotionData)
                .collect(Collectors.toList());

        if (emotionDataList.isEmpty()) {
            // 如果没有情绪数据，根据阅读行为推断
            double completionRate = logs.stream()
                    .filter(l -> l.getCompletionRate() != null)
                    .mapToDouble(l -> l.getCompletionRate().doubleValue())
                    .average()
                    .orElse(50);

            double focusScore = calculateFocusScore(childId);

            return (completionRate * 0.4 + focusScore * 0.6);
        }

        // 解析情绪数据并计算稳定性
        int positiveCount = 0;
        int negativeCount = 0;
        int neutralCount = 0;

        for (String emotionJson : emotionDataList) {
            try {
                Map<String, Object> emotionData = JSONUtil.toBean(emotionJson, Map.class);
                String dominantEmotion = (String) emotionData.getOrDefault("dominantEmotion", "neutral");

                if ("happy".equals(dominantEmotion) || "excited".equals(dominantEmotion)) {
                    positiveCount++;
                } else if ("confused".equals(dominantEmotion) || "frustrated".equals(dominantEmotion)) {
                    negativeCount++;
                } else {
                    neutralCount++;
                }
            } catch (Exception e) {
                neutralCount++;
            }
        }

        int total = positiveCount + negativeCount + neutralCount;
        if (total == 0) {
            return 50.0;
        }

        // 正面情绪占比高，稳定性高
        double positiveRatio = (double) positiveCount / total;
        double negativeRatio = (double) negativeCount / total;

        double score = positiveRatio * 80 + (1 - negativeRatio) * 20;

        return Math.min(score, 100);
    }

    @Override
    public String assessCognitiveStage(Long childId) {
        Child child = childMapper.selectById(childId);
        if (child == null || child.getAge() == null) {
            return "前运算阶段"; // 默认阶段
        }

        int age = child.getAge();

        // 根据年龄和阅读能力判断认知阶段
        double readingAbility = calculateReadingAbilityScore(childId);

        if (age <= 2) {
            return "感知运动阶段";
        } else if (age <= 4) {
            return readingAbility >= 60 ? "前运算阶段（高级）" : "前运算阶段";
        } else if (age <= 6) {
            return readingAbility >= 70 ? "具体运算阶段" : "前运算阶段（高级）";
        } else {
            return readingAbility >= 80 ? "形式运算阶段" : "具体运算阶段";
        }
    }

    @Override
    public Double calculateReadingAbilityScore(Long childId) {
        List<ReadingLog> logs = readingLogMapper.selectLogsByChildId(childId);
        if (logs.isEmpty()) {
            return 0.0;
        }

        // 多维度计算阅读能力
        double focusScore = calculateFocusScore(childId);
        double completionRate = logs.stream()
                .filter(l -> l.getCompletionRate() != null)
                .mapToDouble(l -> l.getCompletionRate().doubleValue())
                .average()
                .orElse(0);

        double interactionScore = calculateInteractionScore(childId);
        double replayScore = calculateReplayPreferenceScore(childId);

        // 加权计算
        double abilityScore = focusScore * 0.3 + completionRate * 0.3
                + interactionScore * 0.2 + replayScore * 0.2;

        return Math.min(abilityScore, 100);
    }

    @Override
    public Double calculateHabitFormationScore(Long childId) {
        List<ReadingLog> logs = readingLogMapper.selectLogsByChildId(childId);
        if (logs.isEmpty()) {
            return 0.0;
        }

        // 分析阅读频率和时间规律性
        Map<LocalDate, Long> dailyReadCount = logs.stream()
                .filter(l -> l.getStartTime() != null)
                .collect(Collectors.groupingBy(
                        l -> l.getStartTime().toLocalDate(),
                        Collectors.counting()
                ));

        // 计算阅读天数
        int readingDays = dailyReadCount.size();

        // 计算阅读频率稳定性（使用标准差）
        if (readingDays < 2) {
            return readingDays * 20.0; // 少于2天，按天数计分
        }

        DescriptiveStatistics stats = new DescriptiveStatistics();
        dailyReadCount.values().forEach(count -> stats.addValue(count.doubleValue()));

        double avgDailyReads = stats.getMean();
        double stdDev = stats.getStandardDeviation();

        // 频率稳定性：标准差越小，分数越高
        double stabilityScore = Math.max(0, 50 - stdDev * 10);

        // 频率得分：平均每天阅读次数
        double frequencyScore = Math.min(avgDailyReads * 20, 50);

        return Math.min(stabilityScore + frequencyScore, 100);
    }

    @Override
    public Double predictGrowthTrend(Long childId) {
        // 获取历史分析数据
        List<BehaviorAnalysis> historyAnalyses = behaviorAnalysisMapper.selectByChildId(childId);

        if (historyAnalyses.size() < 2) {
            return calculateReadingAbilityScore(childId);
        }

        // 按日期排序
        historyAnalyses.sort(Comparator.comparing(BehaviorAnalysis::getAnalysisDate));

        // 使用线性回归预测成长趋势
        List<Double> scores = historyAnalyses.stream()
                .filter(a -> a.getReadingAbilityScore() != null)
                .map(a -> a.getReadingAbilityScore().doubleValue())
                .collect(Collectors.toList());

        if (scores.size() < 2) {
            return calculateReadingAbilityScore(childId);
        }

        // 简单线性回归预测
        int n = scores.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += scores.get(i);
            sumXY += i * scores.get(i);
            sumX2 += i * i;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        // 预测下一个周期的得分
        double predictedScore = slope * n + intercept;

        return Math.max(0, Math.min(predictedScore, 100));
    }

    @Override
    public Map<String, Object> getMultiDimensionAnalysis(Long childId) {
        Map<String, Object> result = new HashMap<>();

        // 阅读时长分布
        result.put("durationDistribution", getReadingDurationDistribution(childId));

        // 翻页速率分析
        result.put("turnSpeedAnalysis", getTurnSpeedAnalysis(childId));

        // 阅读时段分布
        result.put("timeSlotDistribution", getTimeSlotDistribution(childId));

        // 类别偏好
        result.put("categoryPreference", readingLogMapper.selectCategoryStatsByChildId(childId));

        // 成长轨迹
        result.put("growthTrajectory", getGrowthTrajectory(childId));

        return result;
    }

    @Override
    public Map<String, Object> getReadingDurationDistribution(Long childId) {
        List<ReadingLog> logs = readingLogMapper.selectLogsByChildId(childId);

        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("0-5分钟", 0L);
        distribution.put("5-10分钟", 0L);
        distribution.put("10-20分钟", 0L);
        distribution.put("20-30分钟", 0L);
        distribution.put("30分钟以上", 0L);

        for (ReadingLog log : logs) {
            if (log.getDuration() != null) {
                int minutes = log.getDuration() / 60;
                String key;
                if (minutes < 5) key = "0-5分钟";
                else if (minutes < 10) key = "5-10分钟";
                else if (minutes < 20) key = "10-20分钟";
                else if (minutes < 30) key = "20-30分钟";
                else key = "30分钟以上";

                distribution.put(key, distribution.get(key) + 1);
            }
        }

        return distribution.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<String, Object> getTurnSpeedAnalysis(Long childId) {
        List<ReadingLog> logs = readingLogMapper.selectLogsByChildId(childId);

        List<Double> speeds = new ArrayList<>();
        for (ReadingLog log : logs) {
            if (log.getDuration() != null && log.getDuration() > 0
                    && log.getPageTurnCount() != null && log.getPageTurnCount() > 0) {
                speeds.add((double) log.getPageTurnCount() / (log.getDuration() / 60.0));
            }
        }

        Map<String, Object> result = new HashMap<>();
        if (speeds.isEmpty()) {
            result.put("avgSpeed", 0);
            result.put("maxSpeed", 0);
            result.put("minSpeed", 0);
            result.put("fluctuation", 0);
            return result;
        }

        DescriptiveStatistics stats = new DescriptiveStatistics();
        speeds.forEach(stats::addValue);

        result.put("avgSpeed", BigDecimal.valueOf(stats.getMean()).setScale(2, RoundingMode.HALF_UP));
        result.put("maxSpeed", BigDecimal.valueOf(stats.getMax()).setScale(2, RoundingMode.HALF_UP));
        result.put("minSpeed", BigDecimal.valueOf(stats.getMin()).setScale(2, RoundingMode.HALF_UP));

        // 波动率（标准差/均值）
        double fluctuation = stats.getMean() > 0 ? stats.getStandardDeviation() / stats.getMean() : 0;
        result.put("fluctuation", BigDecimal.valueOf(fluctuation).setScale(2, RoundingMode.HALF_UP));

        return result;
    }

    @Override
    public Map<String, Object> getGrowthTrajectory(Long childId) {
        Map<String, Object> result = new HashMap<>();

        List<BehaviorAnalysis> history = behaviorAnalysisMapper.selectByChildId(childId);

        if (history.isEmpty()) {
            result.put("trajectory", Collections.emptyList());
            result.put("trend", "stable");
            return result;
        }

        // 按日期排序
        history.sort(Comparator.comparing(BehaviorAnalysis::getAnalysisDate));

        // 提取各维度轨迹
        List<Map<String, Object>> trajectory = history.stream()
                .map(a -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("date", a.getAnalysisDate().toString());
                    point.put("focusScore", a.getFocusScore());
                    point.put("completionRate", a.getCompletionRate());
                    point.put("readingAbilityScore", a.getReadingAbilityScore());
                    return point;
                })
                .collect(Collectors.toList());

        result.put("trajectory", trajectory);

        // 判断趋势
        if (history.size() >= 2) {
            BigDecimal firstScore = history.get(0).getReadingAbilityScore();
            BigDecimal lastScore = history.get(history.size() - 1).getReadingAbilityScore();

            if (firstScore != null && lastScore != null) {
                double diff = lastScore.subtract(firstScore).doubleValue();
                if (diff > 5) {
                    result.put("trend", "improving");
                } else if (diff < -5) {
                    result.put("trend", "declining");
                } else {
                    result.put("trend", "stable");
                }
            }
        }

        return result;
    }

    private Map<String, Object> getTimeSlotDistribution(Long childId) {
        List<ReadingLog> logs = readingLogMapper.selectLogsByChildId(childId);

        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("早晨(6-9点)", 0L);
        distribution.put("上午(9-12点)", 0L);
        distribution.put("中午(12-14点)", 0L);
        distribution.put("下午(14-18点)", 0L);
        distribution.put("傍晚(18-20点)", 0L);
        distribution.put("晚上(20-22点)", 0L);
        distribution.put("夜间(22-6点)", 0L);

        for (ReadingLog log : logs) {
            if (log.getStartTime() != null) {
                int hour = log.getStartTime().getHour();
                String key;
                if (hour >= 6 && hour < 9) key = "早晨(6-9点)";
                else if (hour >= 9 && hour < 12) key = "上午(9-12点)";
                else if (hour >= 12 && hour < 14) key = "中午(12-14点)";
                else if (hour >= 14 && hour < 18) key = "下午(14-18点)";
                else if (hour >= 18 && hour < 20) key = "傍晚(18-20点)";
                else if (hour >= 20 && hour < 22) key = "晚上(20-22点)";
                else key = "夜间(22-6点)";

                distribution.put(key, distribution.get(key) + 1);
            }
        }

        return distribution.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
