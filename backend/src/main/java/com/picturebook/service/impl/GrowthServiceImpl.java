package com.picturebook.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.picturebook.entity.*;
import com.picturebook.mapper.*;
import com.picturebook.service.AnalysisService;
import com.picturebook.service.GrowthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GrowthServiceImpl implements GrowthService {

    @Autowired
    private GrowthReportMapper growthReportMapper;

    @Autowired
    private ChildMapper childMapper;

    @Autowired
    private ReadingLogMapper readingLogMapper;

    @Autowired
    private BehaviorAnalysisMapper behaviorAnalysisMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private AnalysisService analysisService;

    @Override
    public GrowthReport generateReport(Long childId, String reportType) {
        Child child = childMapper.selectById(childId);
        if (child == null) {
            return null;
        }

        // 检查今天是否已生成过相同类型的报告
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<GrowthReport> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(GrowthReport::getChildId, childId)
                .eq(GrowthReport::getReportType, reportType)
                .eq(GrowthReport::getReportDate, today);
        GrowthReport existingReport = growthReportMapper.selectOne(checkWrapper);
        if (existingReport != null) {
            return existingReport; // 返回已存在的报告
        }

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime;
        if ("weekly".equals(reportType)) {
            startTime = endTime.minusWeeks(1);
        } else {
            startTime = endTime.minusMonths(1);
        }

        List<ReadingLog> logs = readingLogMapper.selectLogsByChildIdAndTimeRange(
                childId, startTime, endTime);
        if (logs.isEmpty()) {
            return null;
        }

        GrowthReport report = new GrowthReport();
        report.setChildId(childId);
        report.setReportType(reportType);
        report.setReportDate(LocalDate.now());
        report.setStartDate(startTime.toLocalDate());
        report.setEndDate(endTime.toLocalDate());

        // 原有维度评分
        BigDecimal vocabularyScore = calculateVocabularyScore(logs);
        report.setVocabularyScore(vocabularyScore);
        BigDecimal logicScore = calculateLogicScore(logs);
        report.setLogicScore(logicScore);
        BigDecimal readingScore = calculateReadingScore(logs);
        report.setReadingScore(readingScore);

        // 从行为分析获取专注度
        BehaviorAnalysis analysis = behaviorAnalysisMapper.selectLatestByChildId(childId);
        BigDecimal focusScore = analysis != null && analysis.getFocusScore() != null
                ? analysis.getFocusScore()
                : BigDecimal.ZERO;
        report.setFocusScore(focusScore);

        // 新增维度评分
        BigDecimal cognitiveScore = calculateCognitiveScore(childId, logs);
        report.setCognitiveScore(cognitiveScore);
        BigDecimal socialScore = calculateSocialScore(childId, logs);
        report.setSocialScore(socialScore);
        BigDecimal creativityScore = calculateCreativityScore(childId, logs);
        report.setCreativityScore(creativityScore);
        BigDecimal habitScore = BigDecimal.valueOf(analysisService.calculateHabitFormationScore(childId));
        report.setHabitScore(habitScore);

        // 计算总分（8个维度平均）
        BigDecimal totalScore = vocabularyScore.add(logicScore).add(readingScore).add(focusScore)
                .add(cognitiveScore).add(socialScore).add(creativityScore).add(habitScore)
                .divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP);
        report.setTotalScore(totalScore);

        report.setBookCount(logs.size());
        int totalDuration = logs.stream()
                .mapToInt(l -> l.getDuration() != null ? l.getDuration() : 0)
                .sum();
        report.setTotalDuration(totalDuration);

        // 生成里程碑
        List<Map<String, Object>> milestones = generateMilestones(childId, report);
        report.setMilestones(JSONUtil.toJsonStr(milestones));

        // 详细维度数据
        Map<String, Object> detailedData = new HashMap<>();
        detailedData.put("categoryDistribution", readingLogMapper.selectCategoryStatsByChildId(childId));
        detailedData.put("durationDistribution", analysisService.getReadingDurationDistribution(childId));
        detailedData.put("turnSpeedAnalysis", analysisService.getTurnSpeedAnalysis(childId));
        report.setDetailedData(JSONUtil.toJsonStr(detailedData));

        String suggestion = generateSuggestion(report);
        report.setSuggestion(suggestion);

        growthReportMapper.insert(report);
        return report;
    }

    private BigDecimal calculateVocabularyScore(List<ReadingLog> logs) {
        double totalVocabulary = 0;
        for (ReadingLog log : logs) {
            Book book = bookMapper.selectById(log.getBookId());
            if (book != null && book.getVocabularyCount() != null) {
                int difficulty = book.getDifficultyLevel() != null ? book.getDifficultyLevel() : 1;
                totalVocabulary += book.getVocabularyCount() * difficulty * 0.1;
            }
        }
        double maxVocabulary = 5000;
        double score = Math.min(totalVocabulary / maxVocabulary * 100, 100);
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateLogicScore(List<ReadingLog> logs) {
        double totalCompletion = 0;
        for (ReadingLog log : logs) {
            if (log.getCompletionRate() != null) {
                totalCompletion += log.getCompletionRate().doubleValue();
            }
        }
        double avgCompletion = logs.isEmpty() ? 0 : totalCompletion / logs.size();
        double score = avgCompletion * 0.7 + 50;
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateReadingScore(List<ReadingLog> logs) {
        int bookCount = (int) logs.stream().map(ReadingLog::getBookId).distinct().count();
        int totalDuration = logs.stream()
                .mapToInt(l -> l.getDuration() != null ? l.getDuration() : 0)
                .sum();
        double normalizedDuration = Math.min(totalDuration / 3600.0 / 60, 100);
        double normalizedBookCount = Math.min(bookCount / 20.0 * 100, 100);
        double score = normalizedBookCount * 0.6 + normalizedDuration * 0.4;
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCognitiveScore(Long childId, List<ReadingLog> logs) {
        // 基于绘本的知识点类型多样性评分
        Set<String> knowledgeTypes = new HashSet<>();
        for (ReadingLog log : logs) {
            Book book = bookMapper.selectById(log.getBookId());
            if (book != null && book.getKnowledgeType() != null) {
                knowledgeTypes.add(book.getKnowledgeType());
            }
        }

        // 知识类型多样性得分
        double diversityScore = Math.min(knowledgeTypes.size() * 20, 60);

        // 阅读理解力得分（基于完成度和重复阅读）
        double avgCompletion = logs.stream()
                .filter(l -> l.getCompletionRate() != null)
                .mapToDouble(l -> l.getCompletionRate().doubleValue())
                .average()
                .orElse(0);

        double understandingScore = avgCompletion * 0.4;

        return BigDecimal.valueOf(diversityScore + understandingScore).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSocialScore(Long childId, List<ReadingLog> logs) {
        // 基于情感类绘本阅读情况
        int emotionBookCount = 0;
        for (ReadingLog log : logs) {
            Book book = bookMapper.selectById(log.getBookId());
            if (book != null && "情感".equals(book.getCategory())) {
                emotionBookCount++;
            }
        }

        // 社交情感绘本比例得分
        double ratio = logs.isEmpty() ? 0 : (double) emotionBookCount / logs.size();
        double score = ratio * 100;

        // 加上互动分数的影响
        double interactionScore = analysisService.calculateInteractionScore(childId);
        score = score * 0.7 + interactionScore * 0.3;

        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCreativityScore(Long childId, List<ReadingLog> logs) {
        // 基于艺术风格多样性和童话类绘本
        Set<String> artStyles = new HashSet<>();
        int fairyTaleCount = 0;

        for (ReadingLog log : logs) {
            Book book = bookMapper.selectById(log.getBookId());
            if (book != null) {
                if (book.getArtStyle() != null) {
                    artStyles.add(book.getArtStyle());
                }
                if ("童话".equals(book.getCategory())) {
                    fairyTaleCount++;
                }
            }
        }

        // 艺术风格多样性得分
        double styleScore = Math.min(artStyles.size() * 15, 45);

        // 童话阅读比例得分
        double fairyTaleRatio = logs.isEmpty() ? 0 : (double) fairyTaleCount / logs.size();
        double fairyTaleScore = fairyTaleRatio * 55;

        return BigDecimal.valueOf(styleScore + fairyTaleScore).setScale(2, RoundingMode.HALF_UP);
    }

    private List<Map<String, Object>> generateMilestones(Long childId, GrowthReport report) {
        List<Map<String, Object>> milestones = new ArrayList<>();

        // 阅读量里程碑
        if (report.getBookCount() != null) {
            if (report.getBookCount() >= 10) {
                milestones.add(createMilestone("阅读小达人", "累计阅读" + report.getBookCount() + "本绘本", "trophy"));
            } else if (report.getBookCount() >= 5) {
                milestones.add(createMilestone("阅读新星", "累计阅读" + report.getBookCount() + "本绘本", "star"));
            }
        }

        // 专注力里程碑
        if (report.getFocusScore() != null && report.getFocusScore().compareTo(BigDecimal.valueOf(80)) >= 0) {
            milestones.add(createMilestone("专注之星", "阅读专注度达到" + report.getFocusScore() + "分", "medal"));
        }

        // 综合能力里程碑
        if (report.getTotalScore() != null && report.getTotalScore().compareTo(BigDecimal.valueOf(85)) >= 0) {
            milestones.add(createMilestone("全面发展", "综合能力评分达到" + report.getTotalScore() + "分", "crown"));
        }

        // 阅读时长里程碑
        if (report.getTotalDuration() != null) {
            int hours = report.getTotalDuration() / 3600;
            if (hours >= 10) {
                milestones.add(createMilestone("阅读马拉松", "累计阅读" + hours + "小时", "clock"));
            }
        }

        return milestones;
    }

    private Map<String, Object> createMilestone(String title, String description, String icon) {
        Map<String, Object> milestone = new HashMap<>();
        milestone.put("title", title);
        milestone.put("description", description);
        milestone.put("icon", icon);
        milestone.put("date", LocalDate.now().toString());
        return milestone;
    }

    private String generateSuggestion(GrowthReport report) {
        StringBuilder sb = new StringBuilder();
        BigDecimal total = report.getTotalScore() != null ? report.getTotalScore() : BigDecimal.ZERO;

        if (total.compareTo(BigDecimal.valueOf(80)) >= 0) {
            sb.append("🎉 阅读表现优秀！继续保持！\n");
            if (report.getVocabularyScore() != null && report.getVocabularyScore().compareTo(BigDecimal.valueOf(80)) >= 0) {
                sb.append("📚 词汇积累丰富，阅读范围广泛。建议尝试更高难度的绘本。\n");
            }
            if (report.getFocusScore() != null && report.getFocusScore().compareTo(BigDecimal.valueOf(80)) >= 0) {
                sb.append("🎯 阅读专注度高，习惯良好。可以尝试自主阅读。\n");
            }
            if (report.getCreativityScore() != null && report.getCreativityScore().compareTo(BigDecimal.valueOf(80)) >= 0) {
                sb.append("🎨 创造力发展良好，可以尝试绘本创作活动。\n");
            }
        } else if (total.compareTo(BigDecimal.valueOf(60)) >= 0) {
            sb.append("👍 阅读表现良好！仍有提升空间。\n");
            if (report.getVocabularyScore() != null && report.getVocabularyScore().compareTo(BigDecimal.valueOf(60)) < 0) {
                sb.append("📖 建议增加阅读量，拓展词汇积累。\n");
            }
            if (report.getFocusScore() != null && report.getFocusScore().compareTo(BigDecimal.valueOf(60)) < 0) {
                sb.append("⏰ 建议培养专注阅读习惯，每次阅读时间控制在15-20分钟。\n");
            }
            if (report.getCognitiveScore() != null && report.getCognitiveScore().compareTo(BigDecimal.valueOf(60)) < 0) {
                sb.append("🧩 建议阅读不同知识类型的绘本，拓展认知领域。\n");
            }
        } else {
            sb.append("💪 阅读表现有待提高，我们一起努力！\n");
            sb.append("👨‍👩‍👧 建议家长陪伴阅读，培养阅读兴趣和习惯。\n");
            sb.append("📚 可以从简单的绘本开始，逐步增加阅读时间。\n");
        }

        // 添加个性化建议
        if (report.getHabitScore() != null && report.getHabitScore().compareTo(BigDecimal.valueOf(50)) < 0) {
            sb.append("📅 建议制定固定阅读时间，培养每日阅读习惯。\n");
        }

        return sb.toString();
    }

    @Override
    public GrowthReport getLatestReport(Long childId) {
        return growthReportMapper.selectLatestReportByChildId(childId);
    }

    @Override
    public List<GrowthReport> getReportHistory(Long childId) {
        return growthReportMapper.selectReportsByChildId(childId);
    }

    @Override
    public List<GrowthReport> getReportHistoryByType(Long childId, String reportType) {
        return growthReportMapper.selectReportsByChildIdAndType(childId, reportType);
    }

    @Override
    public void generateWeeklyReports() {
        List<Child> children = childMapper.selectList(null);
        for (Child child : children) {
            try {
                generateReport(child.getId(), "weekly");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void generateMonthlyReports() {
        List<Child> children = childMapper.selectList(null);
        for (Child child : children) {
            try {
                generateReport(child.getId(), "monthly");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Map<String, Object> getGrowthCurve(Long childId, String dimension) {
        Map<String, Object> result = new HashMap<>();
        List<GrowthReport> reports = growthReportMapper.selectReportsByChildId(childId);

        if (reports.isEmpty()) {
            result.put("labels", Collections.emptyList());
            result.put("data", Collections.emptyList());
            return result;
        }

        // 按日期排序
        reports.sort(Comparator.comparing(GrowthReport::getReportDate));

        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        for (GrowthReport report : reports) {
            labels.add(report.getReportDate().toString());

            BigDecimal value;
            switch (dimension) {
                case "vocabulary":
                    value = report.getVocabularyScore();
                    break;
                case "logic":
                    value = report.getLogicScore();
                    break;
                case "reading":
                    value = report.getReadingScore();
                    break;
                case "focus":
                    value = report.getFocusScore();
                    break;
                case "cognitive":
                    value = report.getCognitiveScore();
                    break;
                case "social":
                    value = report.getSocialScore();
                    break;
                case "creativity":
                    value = report.getCreativityScore();
                    break;
                case "habit":
                    value = report.getHabitScore();
                    break;
                default:
                    value = report.getTotalScore();
            }
            data.add(value != null ? value : BigDecimal.ZERO);
        }

        result.put("labels", labels);
        result.put("data", data);

        return result;
    }

    @Override
    public Map<String, Object> getPeerComparison(Long childId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Child child = childMapper.selectById(childId);
            if (child == null) {
                return result;
            }

            // 获取同龄儿童
            int childAge = child.getAge() != null ? child.getAge() : 4;
            LambdaQueryWrapper<Child> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Child::getAge, childAge);
            List<Child> peers = childMapper.selectList(wrapper);

            if (peers.size() < 2) {
                // 如果同龄儿童不足，返回默认数据
                result.put("childScores", createDefaultScores());
                result.put("peerAvgScores", createDefaultScores());
                result.put("percentile", 50);
                return result;
            }

            // 获取当前儿童评分
            GrowthReport childReport = getLatestReport(childId);
            if (childReport == null) {
                // 没有报告数据，返回默认值
                result.put("childScores", createDefaultScores());
                result.put("peerAvgScores", createDefaultScores());
                result.put("percentile", 50);
                return result;
            }

            // 计算同龄儿童平均分
            List<GrowthReport> peerReports = peers.stream()
                    .filter(c -> !c.getId().equals(childId))
                    .map(c -> getLatestReport(c.getId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (peerReports.isEmpty()) {
                // 没有同龄儿童的报告数据
                result.put("childScores", createDefaultScores());
                result.put("peerAvgScores", createDefaultScores());
                result.put("percentile", 50);
                return result;
            }

            Map<String, BigDecimal> childScores = new HashMap<>();
            Map<String, BigDecimal> peerAvgScores = new HashMap<>();

            String[] dimensions = {"vocabulary", "logic", "reading", "focus", "total"};

            for (String dim : dimensions) {
                BigDecimal childScore = getScoreByDimension(childReport, dim);
                childScores.put(dim, childScore);

                BigDecimal avgScore = peerReports.stream()
                        .map(r -> getScoreByDimension(r, dim))
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(peerReports.size()), 2, RoundingMode.HALF_UP);
                peerAvgScores.put(dim, avgScore);
            }

            result.put("childScores", childScores);
            result.put("peerAvgScores", peerAvgScores);
            result.put("percentile", calculatePercentile(childReport.getTotalScore(), peerReports));
        } catch (Exception e) {
            // 发生异常时返回默认数据
            result.put("childScores", createDefaultScores());
            result.put("peerAvgScores", createDefaultScores());
            result.put("percentile", 50);
        }

        return result;
    }

    private Map<String, BigDecimal> createDefaultScores() {
        Map<String, BigDecimal> scores = new HashMap<>();
        scores.put("vocabulary", BigDecimal.valueOf(50));
        scores.put("logic", BigDecimal.valueOf(50));
        scores.put("reading", BigDecimal.valueOf(50));
        scores.put("focus", BigDecimal.valueOf(50));
        scores.put("total", BigDecimal.valueOf(50));
        return scores;
    }

    private BigDecimal getScoreByDimension(GrowthReport report, String dimension) {
        if (report == null) return BigDecimal.ZERO;

        switch (dimension) {
            case "vocabulary":
                return report.getVocabularyScore() != null ? report.getVocabularyScore() : BigDecimal.ZERO;
            case "logic":
                return report.getLogicScore() != null ? report.getLogicScore() : BigDecimal.ZERO;
            case "reading":
                return report.getReadingScore() != null ? report.getReadingScore() : BigDecimal.ZERO;
            case "focus":
                return report.getFocusScore() != null ? report.getFocusScore() : BigDecimal.ZERO;
            default:
                return report.getTotalScore() != null ? report.getTotalScore() : BigDecimal.ZERO;
        }
    }

    private int calculatePercentile(BigDecimal score, List<GrowthReport> peerReports) {
        if (score == null || peerReports.isEmpty()) {
            return 50;
        }

        long countBelow = peerReports.stream()
                .filter(r -> r.getTotalScore() != null && r.getTotalScore().compareTo(score) < 0)
                .count();

        return (int) ((countBelow * 100) / peerReports.size());
    }

    @Override
    public List<Map<String, Object>> getMilestones(Long childId) {
        GrowthReport latestReport = getLatestReport(childId);
        if (latestReport != null && latestReport.getMilestones() != null) {
            try {
                List<?> rawList = JSONUtil.toList(latestReport.getMilestones(), Map.class);
                List<Map<String, Object>> result = new ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) item;
                        result.add(map);
                    }
                }
                return result;
            } catch (Exception e) {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> generateReadingPlan(Long childId) {
        Map<String, Object> plan = new HashMap<>();

        Child child = childMapper.selectById(childId);
        GrowthReport report = getLatestReport(childId);
        BehaviorAnalysis analysis = behaviorAnalysisMapper.selectLatestByChildId(childId);

        if (child == null) {
            return plan;
        }

        // 目标设定
        List<Map<String, Object>> goals = new ArrayList<>();

        if (report != null) {
            if (report.getVocabularyScore() != null && report.getVocabularyScore().compareTo(BigDecimal.valueOf(70)) < 0) {
                goals.add(createGoal("词汇提升", "每周阅读3本新绘本，拓展词汇量", "vocabulary"));
            }
            if (report.getFocusScore() != null && report.getFocusScore().compareTo(BigDecimal.valueOf(70)) < 0) {
                goals.add(createGoal("专注力培养", "每次阅读保持15分钟以上专注", "focus"));
            }
            if (report.getCognitiveScore() != null && report.getCognitiveScore().compareTo(BigDecimal.valueOf(70)) < 0) {
                goals.add(createGoal("认知拓展", "阅读不同知识类型的绘本", "cognitive"));
            }
        }

        plan.put("goals", goals);

        // 推荐阅读时段
        List<String> recommendedTimes = Arrays.asList(
                "早餐后 8:00-8:30",
                "午睡前 12:30-13:00",
                "睡前 20:00-20:30"
        );
        plan.put("recommendedTimes", recommendedTimes);

        // 推荐绘本类型
        List<String> recommendedCategories = new ArrayList<>();
        if (analysis != null && analysis.getInterestTags() != null) {
            recommendedCategories.addAll(Arrays.asList(analysis.getInterestTags().split(",")));
        }
        // 补充推荐类型
        if (recommendedCategories.size() < 3) {
            List<String> allCategories = Arrays.asList("动物", "科普", "情感", "童话");
            for (String cat : allCategories) {
                if (!recommendedCategories.contains(cat) && recommendedCategories.size() < 3) {
                    recommendedCategories.add(cat);
                }
            }
        }
        plan.put("recommendedCategories", recommendedCategories);

        // 每周计划
        Map<String, String> weeklyPlan = new LinkedHashMap<>();
        weeklyPlan.put("周一", "自主阅读时间，选择喜欢的绘本");
        weeklyPlan.put("周二", "亲子共读，家长引导提问");
        weeklyPlan.put("周三", "新绘本探索日");
        weeklyPlan.put("周四", "重复阅读喜爱的绘本");
        weeklyPlan.put("周五", "主题阅读（根据兴趣标签）");
        weeklyPlan.put("周六", "家庭阅读活动，分享阅读感受");
        weeklyPlan.put("周日", "自由阅读日");
        plan.put("weeklyPlan", weeklyPlan);

        return plan;
    }

    private Map<String, Object> createGoal(String title, String description, String type) {
        Map<String, Object> goal = new HashMap<>();
        goal.put("title", title);
        goal.put("description", description);
        goal.put("type", type);
        return goal;
    }

    @Override
    public Map<String, Object> getAbilityRadar(Long childId) {
        Map<String, Object> result = new HashMap<>();

        GrowthReport report = getLatestReport(childId);
        if (report == null) {
            return result;
        }

        List<String> dimensions = Arrays.asList("词汇量", "逻辑思维", "阅读能力", "专注度", "认知发展", "社交情感", "创造力", "阅读习惯");
        List<BigDecimal> values = Arrays.asList(
                report.getVocabularyScore() != null ? report.getVocabularyScore() : BigDecimal.ZERO,
                report.getLogicScore() != null ? report.getLogicScore() : BigDecimal.ZERO,
                report.getReadingScore() != null ? report.getReadingScore() : BigDecimal.ZERO,
                report.getFocusScore() != null ? report.getFocusScore() : BigDecimal.ZERO,
                report.getCognitiveScore() != null ? report.getCognitiveScore() : BigDecimal.ZERO,
                report.getSocialScore() != null ? report.getSocialScore() : BigDecimal.ZERO,
                report.getCreativityScore() != null ? report.getCreativityScore() : BigDecimal.ZERO,
                report.getHabitScore() != null ? report.getHabitScore() : BigDecimal.ZERO
        );

        result.put("dimensions", dimensions);
        result.put("values", values);

        return result;
    }

    @Override
    public Map<String, Object> generateParentGuide(Long childId) {
        Map<String, Object> guide = new HashMap<>();

        Child child = childMapper.selectById(childId);
        GrowthReport report = getLatestReport(childId);
        BehaviorAnalysis analysis = behaviorAnalysisMapper.selectLatestByChildId(childId);

        if (child == null) {
            return guide;
        }

        // 儿童基本信息
        Map<String, Object> childInfo = new HashMap<>();
        childInfo.put("name", child.getName());
        childInfo.put("age", child.getAge());
        childInfo.put("readingType", analysis != null ? analysis.getReadingType() : "未分析");
        childInfo.put("cognitiveStage", analysis != null ? analysis.getCognitiveStage() : "未评估");
        guide.put("childInfo", childInfo);

        // 阅读状况总结
        List<String> summary = new ArrayList<>();
        if (report != null) {
            int bookCount = report.getBookCount() != null ? report.getBookCount() : 0;
            int totalDuration = report.getTotalDuration() != null ? report.getTotalDuration() : 0;
            BigDecimal totalScore = report.getTotalScore() != null ? report.getTotalScore() : BigDecimal.ZERO;

            summary.add("本周共阅读 " + bookCount + " 本绘本");
            summary.add("累计阅读时长 " + (totalDuration / 60) + " 分钟");
            summary.add("综合能力评分 " + totalScore + " 分");
        }
        guide.put("summary", summary);

        // 优势领域
        List<String> strengths = new ArrayList<>();
        if (report != null) {
            if (report.getVocabularyScore() != null && report.getVocabularyScore().compareTo(BigDecimal.valueOf(70)) >= 0) {
                strengths.add("词汇积累能力强，阅读面广");
            }
            if (report.getFocusScore() != null && report.getFocusScore().compareTo(BigDecimal.valueOf(70)) >= 0) {
                strengths.add("阅读专注度高，能静心阅读");
            }
            if (report.getCreativityScore() != null && report.getCreativityScore().compareTo(BigDecimal.valueOf(70)) >= 0) {
                strengths.add("创造力发展良好，想象力丰富");
            }
        }
        guide.put("strengths", strengths);

        // 提升建议
        List<String> suggestions = new ArrayList<>();
        if (report != null && report.getSuggestion() != null) {
            suggestions.addAll(Arrays.asList(report.getSuggestion().split("\n")));
        }
        guide.put("suggestions", suggestions);

        // 亲子阅读技巧
        List<String> readingTips = Arrays.asList(
                "创造舒适的阅读环境，减少干扰",
                "阅读时与孩子互动，提问引导思考",
                "鼓励孩子复述故事，培养表达能力",
                "根据孩子兴趣选择绘本，激发阅读热情",
                "设定固定阅读时间，培养阅读习惯"
        );
        guide.put("readingTips", readingTips);

        // 推荐活动
        List<Map<String, String>> activities = new ArrayList<>();
        activities.add(createActivity("绘本角色扮演", "选择喜欢的绘本，扮演其中角色，增强理解", "创意"));
        activities.add(createActivity("绘本续编", "引导孩子为绘本编续集，培养创造力", "创作"));
        activities.add(createActivity("阅读打卡", "制作阅读记录表，完成目标给予奖励", "习惯"));
        guide.put("activities", activities);

        return guide;
    }

    private Map<String, String> createActivity(String name, String description, String type) {
        Map<String, String> activity = new HashMap<>();
        activity.put("name", name);
        activity.put("description", description);
        activity.put("type", type);
        return activity;
    }
}
