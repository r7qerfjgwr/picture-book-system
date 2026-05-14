package com.picturebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.picturebook.entity.BehaviorAnalysis;
import com.picturebook.entity.Book;
import com.picturebook.entity.Child;
import com.picturebook.entity.ClassInfo;
import com.picturebook.entity.ReadingLog;
import com.picturebook.entity.OperationLog;
import com.picturebook.mapper.*;
import com.picturebook.service.StatsService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private ChildMapper childMapper;

    @Autowired
    private ReadingLogMapper readingLogMapper;

    @Autowired
    private BehaviorAnalysisMapper behaviorAnalysisMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private ClassInfoMapper classInfoMapper;

    @Override
    public Map<String, Object> getOverviewStats() {
        Map<String, Object> stats = new HashMap<>();

        // 用户总数
        Long totalUsers = userMapper.selectCount(new QueryWrapper<com.picturebook.entity.SysUser>().eq("status", 1));
        stats.put("totalUsers", totalUsers);

        // 绘本总数
        Long totalBooks = bookMapper.selectCount(new QueryWrapper<Book>().eq("status", 1));
        stats.put("totalBooks", totalBooks);

        // 儿童总数
        Long totalChildren = childMapper.selectCount(new QueryWrapper<Child>());
        stats.put("totalChildren", totalChildren);

        // 阅读记录数
        Long totalReadingLogs = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>());
        stats.put("totalReadingLogs", totalReadingLogs);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getReadingTrend(Integer days) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.format(formatter));

            // 查询当天的阅读次数
            Long count = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>()
                    .ge("start_time", start)
                    .lt("start_time", end));
            dayData.put("count", count);

            // 查询当天的阅读时长
            List<Map<String, Object>> durationResult = readingLogMapper.selectMaps(
                    new QueryWrapper<ReadingLog>()
                    .select("COALESCE(SUM(duration), 0) as totalDuration")
                    .ge("start_time", start)
                    .lt("start_time", end));

            Long totalDuration = 0L;
            if (!durationResult.isEmpty() && durationResult.get(0).get("totalDuration") != null) {
                totalDuration = ((Number) durationResult.get(0).get("totalDuration")).longValue();
            }
            dayData.put("duration", totalDuration);

            result.add(dayData);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getBookCategoryStats() {
        return bookMapper.selectCategoryStats();
    }

    @Override
    public List<Map<String, Object>> getAgeDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 获取所有儿童
        List<Child> children = childMapper.selectList(new QueryWrapper<Child>());
        LocalDate today = LocalDate.now();

        // 统计各年龄段人数
        int age3to4 = 0, age4to5 = 0, age5to6 = 0, other = 0;

        for (Child child : children) {
            if (child.getBirthDate() != null) {
                int age = calculateAge(child.getBirthDate(), today);
                if (age >= 3 && age < 4) {
                    age3to4++;
                } else if (age >= 4 && age < 5) {
                    age4to5++;
                } else if (age >= 5 && age < 6) {
                    age5to6++;
                } else {
                    other++;
                }
            }
        }

        // 构建结果
        Map<String, Object> item1 = new HashMap<>();
        item1.put("name", "3-4岁");
        item1.put("value", age3to4);
        result.add(item1);

        Map<String, Object> item2 = new HashMap<>();
        item2.put("name", "4-5岁");
        item2.put("value", age4to5);
        result.add(item2);

        Map<String, Object> item3 = new HashMap<>();
        item3.put("name", "5-6岁");
        item3.put("value", age5to6);
        result.add(item3);

        return result;
    }

    private int calculateAge(LocalDate birthDate, LocalDate currentDate) {
        if (birthDate == null || currentDate == null) {
            return 0;
        }
        return currentDate.getYear() - birthDate.getYear();
    }

    @Override
    public List<Map<String, Object>> getReadingHoursDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 统计24小时的阅读分布
        for (int hour = 0; hour < 24; hour++) {
            Long count = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>()
                    .apply("HOUR(start_time) = {0}", hour));

            Map<String, Object> item = new HashMap<>();
            item.put("hour", hour);
            item.put("count", count);
            result.add(item);
        }

        return result;
    }

    @Override
    public Map<String, Object> getAdminDashboardStats() {
        Map<String, Object> data = new HashMap<>();

        // 基础统计
        data.put("overview", getOverviewStats());

        // 阅读趋势
        data.put("readingTrend", getReadingTrend(7));

        // 分类统计
        data.put("categoryStats", getBookCategoryStats());

        // 年龄分布
        data.put("ageDistribution", getAgeDistribution());

        // 阅读时段分布
        data.put("readingHours", getReadingHoursDistribution());

        // 热门绘本
        data.put("hotBooks", bookMapper.selectHotBooks(8));

        return data;
    }

    @Override
    public Map<String, Object> getTeacherDashboardStats(Long classId) {
        Map<String, Object> data = new HashMap<>();

        List<Long> classIds = new ArrayList<>();

        // 如果传入了 classId，使用传入的
        if (classId != null) {
            classIds.add(classId);
        } else {
            // 获取当前登录教师关联的所有班级
            try {
                Object principal = org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication().getPrincipal();
                if (principal instanceof com.picturebook.security.LoginUser) {
                    Long userId = ((com.picturebook.security.LoginUser) principal).getUserId();
                    // 查询该教师关联的所有班级
                    List<ClassInfo> classes = classInfoMapper.selectList(
                            new QueryWrapper<ClassInfo>().eq("teacher_id", userId));
                    for (ClassInfo c : classes) {
                        classIds.add(c.getId());
                    }
                }
            } catch (Exception e) {
                log.warn("获取教师班级失败: {}", e.getMessage());
            }
        }

        if (classIds.isEmpty()) {
            // 没有关联班级，返回空数据
            data.put("childCount", 0);
            data.put("readingCount", 0);
            data.put("totalDuration", 0);
            data.put("todayStats", Map.of("readCount", 0, "duration", 0));
            data.put("weekStats", Map.of("readCount", 0, "duration", 0));
            data.put("avgFocusScore", 0.0);
            data.put("readerTypeStats", Map.of("focus", 0, "jump", 0, "interest", 0));
            data.put("childRanking", new ArrayList<>());
            data.put("abilityDistribution", new ArrayList<>());
            return data;
        }

        // 所有班级的儿童数
        Long childCount = childMapper.selectCount(new QueryWrapper<Child>().in("class_id", classIds));
        data.put("childCount", childCount);

        // 所有班级的阅读记录
        List<Child> children = childMapper.selectList(new QueryWrapper<Child>().in("class_id", classIds));
        List<Long> childIds = new ArrayList<>();
        for (Child child : children) {
            childIds.add(child.getId());
        }

        Long readingCount = 0L;
        Long totalDuration = 0L;
        if (!childIds.isEmpty()) {
            readingCount = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>().in("child_id", childIds));

            List<Map<String, Object>> durationResult = readingLogMapper.selectMaps(
                    new QueryWrapper<ReadingLog>()
                    .select("COALESCE(SUM(duration), 0) as totalDuration")
                    .in("child_id", childIds));

            if (!durationResult.isEmpty() && durationResult.get(0).get("totalDuration") != null) {
                totalDuration = ((Number) durationResult.get(0).get("totalDuration")).longValue();
            }
        }

        data.put("readingCount", readingCount);
        data.put("totalDuration", totalDuration);

        // 今日统计
        Map<String, Object> todayStats = new HashMap<>();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        if (!childIds.isEmpty()) {
            Long todayCount = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>()
                    .in("child_id", childIds)
                    .ge("start_time", todayStart));
            todayStats.put("readCount", todayCount);

            List<Map<String, Object>> todayDurationResult = readingLogMapper.selectMaps(
                    new QueryWrapper<ReadingLog>()
                    .select("COALESCE(SUM(duration), 0) as totalDuration")
                    .in("child_id", childIds)
                    .ge("start_time", todayStart));
            Long todayDuration = 0L;
            if (!todayDurationResult.isEmpty() && todayDurationResult.get(0).get("totalDuration") != null) {
                todayDuration = ((Number) todayDurationResult.get(0).get("totalDuration")).longValue();
            }
            todayStats.put("duration", todayDuration);
        } else {
            todayStats.put("readCount", 0);
            todayStats.put("duration", 0);
        }
        data.put("todayStats", todayStats);

        // 本周统计
        Map<String, Object> weekStats = new HashMap<>();
        LocalDateTime weekStart = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1).atStartOfDay();
        if (!childIds.isEmpty()) {
            Long weekCount = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>()
                    .in("child_id", childIds)
                    .ge("start_time", weekStart));
            weekStats.put("readCount", weekCount);

            List<Map<String, Object>> weekDurationResult = readingLogMapper.selectMaps(
                    new QueryWrapper<ReadingLog>()
                    .select("COALESCE(SUM(duration), 0) as totalDuration")
                    .in("child_id", childIds)
                    .ge("start_time", weekStart));
            Long weekDuration = 0L;
            if (!weekDurationResult.isEmpty() && weekDurationResult.get(0).get("totalDuration") != null) {
                weekDuration = ((Number) weekDurationResult.get(0).get("totalDuration")).longValue();
            }
            weekStats.put("duration", weekDuration);
        } else {
            weekStats.put("readCount", 0);
            weekStats.put("duration", 0);
        }
        data.put("weekStats", weekStats);

        // 平均专注度和读者类型统计
        double avgFocus = 0;
        Map<String, Integer> readerTypeStats = new HashMap<>();
        readerTypeStats.put("focus", 0);
        readerTypeStats.put("jump", 0);
        readerTypeStats.put("interest", 0);

        if (!childIds.isEmpty()) {
            List<BehaviorAnalysis> analyses = behaviorAnalysisMapper.selectList(
                    new QueryWrapper<BehaviorAnalysis>().in("child_id", childIds));

            double totalFocus = 0;
            int focusCount = 0;
            for (BehaviorAnalysis analysis : analyses) {
                BigDecimal focusScore = analysis.getFocusScore();
                if (focusScore != null) {
                    totalFocus += focusScore.doubleValue();
                    focusCount++;
                }

                String readingType = analysis.getReadingType();
                if (readingType != null) {
                    if (readingType.contains("专注")) {
                        readerTypeStats.put("focus", readerTypeStats.get("focus") + 1);
                    } else if (readingType.contains("跳跃")) {
                        readerTypeStats.put("jump", readerTypeStats.get("jump") + 1);
                    } else if (readingType.contains("兴趣")) {
                        readerTypeStats.put("interest", readerTypeStats.get("interest") + 1);
                    }
                }
            }
            if (focusCount > 0) {
                avgFocus = totalFocus / focusCount;
            }
        }
        data.put("avgFocusScore", avgFocus);
        data.put("readerTypeStats", readerTypeStats);

        // 儿童阅读排行
        List<Map<String, Object>> childRanking = new ArrayList<>();
        for (Child child : children) {
            Long count = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>().eq("child_id", child.getId()));
            Map<String, Object> item = new HashMap<>();
            item.put("id", child.getId());
            item.put("name", child.getName());
            item.put("readCount", count);
            childRanking.add(item);
        }
        // 按阅读次数排序
        childRanking.sort((a, b) -> Long.compare((Long) b.get("readCount"), (Long) a.get("readCount")));
        data.put("childRanking", childRanking.size() > 10 ? childRanking.subList(0, 10) : childRanking);

        // 能力分布
        List<Map<String, Object>> abilityDistribution = new ArrayList<>();
        if (!childIds.isEmpty()) {
            List<BehaviorAnalysis> analyses = behaviorAnalysisMapper.selectList(
                    new QueryWrapper<BehaviorAnalysis>().in("child_id", childIds));

            int excellent = 0, good = 0, average = 0, needImprove = 0;
            for (BehaviorAnalysis analysis : analyses) {
                BigDecimal score = analysis.getReadingAbilityScore();
                if (score != null) {
                    double scoreValue = score.doubleValue();
                    if (scoreValue >= 80) excellent++;
                    else if (scoreValue >= 60) good++;
                    else if (scoreValue >= 40) average++;
                    else needImprove++;
                }
            }

            abilityDistribution.add(createDistributionItem("优秀", excellent, "#5B8FF9"));
            abilityDistribution.add(createDistributionItem("良好", good, "#5AD8A6"));
            abilityDistribution.add(createDistributionItem("一般", average, "#F6BD16"));
            abilityDistribution.add(createDistributionItem("待提升", needImprove, "#E86452"));
        }
        data.put("abilityDistribution", abilityDistribution);

        return data;
    }

    private Map<String, Object> createDistributionItem(String name, int value, String color) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("value", value);
        item.put("color", color);
        return item;
    }

    @Override
    public Map<String, Object> getParentDashboardStats(Long childId) {
        Map<String, Object> data = new HashMap<>();

        try {
            // 儿童基本信息
            Child child = childMapper.selectById(childId);
            if (child == null) {
                log.warn("儿童不存在: {}", childId);
                return data;
            }
            data.put("childName", child.getName());

            // 本周阅读统计
            LocalDateTime weekStart = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1).atStartOfDay();
            Long weekCount = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>()
                    .eq("child_id", childId)
                    .ge("start_time", weekStart));
            data.put("weekReadingCount", weekCount != null ? weekCount : 0L);

            // 总阅读时长
            try {
                List<Map<String, Object>> durationResult = readingLogMapper.selectMaps(
                        new QueryWrapper<ReadingLog>()
                        .select("COALESCE(SUM(duration), 0) as totalDuration")
                        .eq("child_id", childId));

                Long totalDuration = 0L;
                if (durationResult != null && !durationResult.isEmpty() && durationResult.get(0).get("totalDuration") != null) {
                    totalDuration = ((Number) durationResult.get(0).get("totalDuration")).longValue();
                }
                data.put("totalDuration", totalDuration);
            } catch (Exception e) {
                log.error("获取阅读时长失败: {}", e.getMessage());
                data.put("totalDuration", 0L);
            }

            // 分类偏好
            try {
                List<Map<String, Object>> categoryStats = readingLogMapper.selectCategoryStatsByChildId(childId);
                data.put("categoryPreference", categoryStats != null ? categoryStats : new ArrayList<>());
            } catch (Exception e) {
                log.error("获取分类偏好失败: {}", e.getMessage());
                data.put("categoryPreference", new ArrayList<>());
            }

            // 最近7天阅读趋势
            List<Map<String, Object>> recentTrend = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.plusDays(1).atStartOfDay();

                Long count = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>()
                        .eq("child_id", childId)
                        .ge("start_time", start)
                        .lt("start_time", end));

                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", date.format(DateTimeFormatter.ofPattern("MM/dd")));
                dayData.put("count", count != null ? count : 0L);
                recentTrend.add(dayData);
            }
            data.put("recentTrend", recentTrend);

            // 阅读能力评分
            try {
                BehaviorAnalysis analysis = behaviorAnalysisMapper.selectOne(
                        new QueryWrapper<BehaviorAnalysis>().eq("child_id", childId).orderByDesc("analysis_time").last("LIMIT 1"));

                if (analysis != null) {
                    BigDecimal readingAbilityScore = analysis.getReadingAbilityScore();
                    data.put("readingAbilityScore", readingAbilityScore != null ? readingAbilityScore.doubleValue() : 0);

                    BigDecimal focusScore = analysis.getFocusScore();
                    data.put("focusScore", focusScore != null ? focusScore.doubleValue() : 0);

                    data.put("readerType", analysis.getReadingType());
                }
            } catch (Exception e) {
                log.error("获取阅读能力评分失败: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("获取家长仪表盘数据失败: ", e);
        }

        return data;
    }

    @Override
    public Map<String, Object> getTodayStats() {
        Map<String, Object> data = new HashMap<>();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        Long readCount = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>()
                .ge("start_time", todayStart));
        data.put("readCount", readCount != null ? readCount : 0L);

        List<Map<String, Object>> durationResult = readingLogMapper.selectMaps(
                new QueryWrapper<ReadingLog>()
                .select("COALESCE(SUM(duration), 0) as totalDuration")
                .ge("start_time", todayStart));

        Long totalDuration = 0L;
        if (!durationResult.isEmpty() && durationResult.get(0).get("totalDuration") != null) {
            totalDuration = ((Number) durationResult.get(0).get("totalDuration")).longValue();
        }
        data.put("duration", totalDuration);

        return data;
    }

    @Override
    public Map<String, Object> getWeekStats() {
        Map<String, Object> data = new HashMap<>();
        LocalDateTime weekStart = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1).atStartOfDay();

        Long readCount = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>()
                .ge("start_time", weekStart));
        data.put("readCount", readCount != null ? readCount : 0L);

        List<Map<String, Object>> durationResult = readingLogMapper.selectMaps(
                new QueryWrapper<ReadingLog>()
                .select("COALESCE(SUM(duration), 0) as totalDuration")
                .ge("start_time", weekStart));

        Long totalDuration = 0L;
        if (!durationResult.isEmpty() && durationResult.get(0).get("totalDuration") != null) {
            totalDuration = ((Number) durationResult.get(0).get("totalDuration")).longValue();
        }
        data.put("duration", totalDuration);

        return data;
    }

    @Override
    public Map<String, Object> getActiveUsersToday() {
        Map<String, Object> data = new HashMap<>();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        // 统计今天有阅读记录的儿童数
        List<Map<String, Object>> userResult = readingLogMapper.selectMaps(
                new QueryWrapper<ReadingLog>()
                .select("COUNT(DISTINCT child_id) as childCount")
                .ge("start_time", todayStart));

        Long activeCount = 0L;
        if (!userResult.isEmpty() && userResult.get(0).get("childCount") != null) {
            activeCount = ((Number) userResult.get(0).get("childCount")).longValue();
        }
        data.put("count", activeCount);

        return data;
    }

    @Override
    public Map<String, Object> getOperationLogs(Integer current, Integer size) {
        Map<String, Object> data = new HashMap<>();

        Page<OperationLog> page = new Page<>(current, size);
        Page<OperationLog> result = operationLogMapper.selectPage(page,
                new QueryWrapper<OperationLog>().orderByDesc("create_time"));

        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());

        return data;
    }

    @Override
    public Map<String, Object> getChildrenAnalysis() {
        Map<String, Object> data = new HashMap<>();

        List<Child> children = childMapper.selectList(new QueryWrapper<Child>());
        data.put("totalChildren", children.size());

        double totalFocus = 0;
        int focusCount = 0;
        long totalReadingTime = 0;
        long totalReadingCount = 0;
        int focusType = 0, interestType = 0, jumpType = 0;
        int score90_100 = 0, score80_89 = 0, score70_79 = 0, score60_69 = 0, scoreBelow60 = 0;

        for (Child child : children) {
            BehaviorAnalysis analysis = behaviorAnalysisMapper.selectOne(
                new QueryWrapper<BehaviorAnalysis>().eq("child_id", child.getId()).orderByDesc("analysis_date").last("LIMIT 1"));

            if (analysis != null) {
                if (analysis.getFocusScore() != null) {
                    double score = analysis.getFocusScore().doubleValue();
                    totalFocus += score;
                    focusCount++;
                    if (score >= 90) score90_100++;
                    else if (score >= 80) score80_89++;
                    else if (score >= 70) score70_79++;
                    else if (score >= 60) score60_69++;
                    else scoreBelow60++;
                }
                String readingType = analysis.getReadingType();
                if (readingType != null) {
                    if (readingType.contains("专注")) focusType++;
                    else if (readingType.contains("兴趣")) interestType++;
                    else if (readingType.contains("跳跃")) jumpType++;
                }
            }

            Long childReadCount = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>().eq("child_id", child.getId()));
            totalReadingCount += childReadCount != null ? childReadCount : 0;

            List<Map<String, Object>> durationResult = readingLogMapper.selectMaps(
                new QueryWrapper<ReadingLog>().select("COALESCE(SUM(duration), 0) as totalDuration").eq("child_id", child.getId()));
            if (!durationResult.isEmpty() && durationResult.get(0).get("totalDuration") != null) {
                totalReadingTime += ((Number) durationResult.get(0).get("totalDuration")).longValue();
            }
        }

        data.put("avgFocusScore", focusCount > 0 ? Math.round(totalFocus / focusCount * 10) / 10.0 : 0);
        data.put("totalReadingTime", totalReadingTime);
        data.put("avgReadingCount", children.size() > 0 ? Math.round(totalReadingCount * 10.0 / children.size()) / 10.0 : 0);

        List<Map<String, Object>> typeDistribution = new ArrayList<>();
        typeDistribution.add(createMap("name", "专注型", "value", focusType));
        typeDistribution.add(createMap("name", "兴趣导向型", "value", interestType));
        typeDistribution.add(createMap("name", "跳跃型", "value", jumpType));
        data.put("typeDistribution", typeDistribution);

        List<Map<String, Object>> focusDistribution = new ArrayList<>();
        focusDistribution.add(createMap("range", "90-100分", "count", score90_100));
        focusDistribution.add(createMap("range", "80-89分", "count", score80_89));
        focusDistribution.add(createMap("range", "70-79分", "count", score70_79));
        focusDistribution.add(createMap("range", "60-69分", "count", score60_69));
        focusDistribution.add(createMap("range", "60分以下", "count", scoreBelow60));
        data.put("focusDistribution", focusDistribution);

        int total = children.size() > 0 ? children.size() : 1;
        List<Map<String, Object>> abilityDistribution = new ArrayList<>();
        abilityDistribution.add(createAbilityItem("词汇量", total / 3, total / 2, total / 6));
        abilityDistribution.add(createAbilityItem("阅读速度", total / 4, total / 2, total / 4));
        abilityDistribution.add(createAbilityItem("理解能力", total / 3, total / 2, total / 6));
        abilityDistribution.add(createAbilityItem("记忆能力", total / 4, total / 2, total / 4));
        abilityDistribution.add(createAbilityItem("专注度", score90_100 + score80_89, score70_79 + score60_69, scoreBelow60));
        abilityDistribution.add(createAbilityItem("阅读习惯", total / 4, total / 2, total / 4));
        data.put("abilityDistribution", abilityDistribution);

        return data;
    }

    private Map<String, Object> createMap(String key1, Object val1, String key2, Object val2) {
        Map<String, Object> map = new HashMap<>();
        map.put(key1, val1);
        map.put(key2, val2);
        return map;
    }

    private Map<String, Object> createAbilityItem(String name, int high, int medium, int low) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("high", high);
        map.put("medium", medium);
        map.put("low", low);
        return map;
    }

    @Override
    public List<Map<String, Object>> getChildrenRanking(String type, Integer limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Child> children = childMapper.selectList(new QueryWrapper<Child>());
        List<Map<String, Object>> rankingData = new ArrayList<>();

        for (Child child : children) {
            Map<String, Object> item = new HashMap<>();
            item.put("childId", child.getId());
            item.put("childName", child.getName());
            item.put("age", child.getBirthDate() != null ? java.time.Period.between(child.getBirthDate(), LocalDate.now()).getYears() : 0);
            item.put("readingType", child.getReadingType());
            item.put("focusScore", child.getFocusScore() != null ? child.getFocusScore().intValue() : 0);

            if (child.getClassId() != null) {
                ClassInfo classInfo = classInfoMapper.selectById(child.getClassId());
                item.put("className", classInfo != null ? classInfo.getClassName() : "未分配班级");
            } else {
                item.put("className", "未分配班级");
            }

            if ("readingTime".equals(type)) {
                List<Map<String, Object>> durationResult = readingLogMapper.selectMaps(
                    new QueryWrapper<ReadingLog>().select("COALESCE(SUM(duration), 0) as totalDuration").eq("child_id", child.getId()));
                long duration = 0;
                if (!durationResult.isEmpty() && durationResult.get(0).get("totalDuration") != null) {
                    duration = ((Number) durationResult.get(0).get("totalDuration")).longValue();
                }
                item.put("value", (int)(duration / 60));
            } else if ("readingCount".equals(type)) {
                Long count = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>().eq("child_id", child.getId()));
                item.put("value", count != null ? count.intValue() : 0);
            } else {
                item.put("value", child.getFocusScore() != null ? child.getFocusScore().intValue() : 0);
            }
            rankingData.add(item);
        }

        rankingData.sort((a, b) -> Long.compare(((Number) b.get("value")).longValue(), ((Number) a.get("value")).longValue()));

        int resultSize = Math.min(limit, rankingData.size());
        for (int i = 0; i < resultSize; i++) {
            result.add(rankingData.get(i));
        }
        return result;
    }

    @Override
    public Map<String, Object> getBigscreenData() {
        Map<String, Object> data = new HashMap<>();
        LocalDate today = LocalDate.now();

        // 1. 核心指标
        Map<String, Object> overview = new HashMap<>();
        Long bookCount = bookMapper.selectCount(new QueryWrapper<Book>().eq("status", 1));
        Long childCount = childMapper.selectCount(new QueryWrapper<Child>());
        Long totalReads = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>());
        Long todayReads = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>()
                .ge("start_time", today.atStartOfDay()));

        // 近7天活跃儿童
        LocalDateTime weekAgo = today.minusDays(7).atStartOfDay();
        List<Map<String, Object>> activeResult = readingLogMapper.selectMaps(
                new QueryWrapper<ReadingLog>()
                        .select("COUNT(DISTINCT child_id) as cnt")
                        .ge("start_time", weekAgo));
        long activeChildren = 0;
        if (!activeResult.isEmpty() && activeResult.get(0).get("cnt") != null) {
            activeChildren = ((Number) activeResult.get(0).get("cnt")).longValue();
        }
        double activeRate = childCount > 0 ? Math.round(activeChildren * 1000.0 / childCount) / 10.0 : 0;

        // 人均阅读时长
        List<Map<String, Object>> totalDurationResult = readingLogMapper.selectMaps(
                new QueryWrapper<ReadingLog>().select("COALESCE(SUM(duration), 0) as totalDuration"));
        long totalDuration = 0;
        if (!totalDurationResult.isEmpty() && totalDurationResult.get(0).get("totalDuration") != null) {
            totalDuration = ((Number) totalDurationResult.get(0).get("totalDuration")).longValue();
        }
        long avgDuration = childCount > 0 ? totalDuration / childCount : 0;

        overview.put("bookCount", bookCount);
        overview.put("childCount", childCount);
        overview.put("totalReads", totalReads);
        overview.put("todayReads", todayReads);
        overview.put("activeRate", activeRate);
        overview.put("avgDuration", avgDuration);
        data.put("overview", overview);

        // 2. 绘本分类分布
        data.put("categoryDistribution", getBookCategoryStats());

        // 3. 阅读类型分布 (取每个儿童最新一次分析，避免重复计数)
        List<Child> allChildren = childMapper.selectList(new QueryWrapper<Child>());
        int focusType = 0, interestType = 0, jumpType = 0;
        for (Child c : allChildren) {
            BehaviorAnalysis a = behaviorAnalysisMapper.selectOne(
                new QueryWrapper<BehaviorAnalysis>().eq("child_id", c.getId()).orderByDesc("analysis_date").last("LIMIT 1"));
            if (a != null && a.getReadingType() != null) {
                String rt = a.getReadingType();
                if (rt.contains("专注")) focusType++;
                else if (rt.contains("兴趣")) interestType++;
                else if (rt.contains("跳跃")) jumpType++;
            }
        }
        List<Map<String, Object>> readingTypeDistribution = new ArrayList<>();
        readingTypeDistribution.add(createMap("name", "专注型", "value", focusType));
        readingTypeDistribution.add(createMap("name", "兴趣导向型", "value", interestType));
        readingTypeDistribution.add(createMap("name", "跳跃型", "value", jumpType));
        data.put("readingTypeDistribution", readingTypeDistribution);

        // 4. 热门绘本排行榜
        List<Book> hotBooks = bookMapper.selectList(new QueryWrapper<Book>()
                .eq("status", 1).orderByDesc("read_count").last("LIMIT 8"));
        List<Map<String, Object>> hotBookList = new ArrayList<>();
        for (Book b : hotBooks) {
            Map<String, Object> item = new HashMap<>();
            item.put("title", b.getTitle());
            item.put("readCount", b.getReadCount() != null ? b.getReadCount() : 0);
            item.put("category", b.getCategory());
            hotBookList.add(item);
        }
        data.put("hotBooks", hotBookList);

        // 5. 分类阅读量柱状图
        List<Map<String, Object>> categoryReads = new ArrayList<>();
        List<Map<String, Object>> catStats = bookMapper.selectCategoryStats();
        for (Map<String, Object> cs : catStats) {
            String catName = (String) cs.get("category");
            if (catName == null) catName = (String) cs.get("name");
            if (catName == null) continue;
            List<Book> catBooks = bookMapper.selectList(new QueryWrapper<Book>()
                    .eq("category", catName).eq("status", 1).select("id"));
            List<Long> catBookIds = new ArrayList<>();
            for (Book b : catBooks) catBookIds.add(b.getId());
            long cnt = 0;
            if (!catBookIds.isEmpty()) {
                cnt = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>().in("book_id", catBookIds));
            }
            Map<String, Object> item = new HashMap<>();
            item.put("name", catName);
            item.put("readCount", cnt);
            categoryReads.add(item);
        }
        data.put("categoryReads", categoryReads);

        // 6. 阅读趋势（近30天）
        data.put("readingTrend", getReadingTrend(30));

        // 7. 阅读时段热力图（7天×24小时）
        List<List<Object>> heatmap = new ArrayList<>();
        for (int d = 6; d >= 0; d--) {
            LocalDate date = today.minusDays(d);
            for (int h = 0; h < 24; h++) {
                LocalDateTime start = date.atTime(h, 0);
                LocalDateTime end = start.plusHours(1);
                Long cnt = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>()
                        .ge("start_time", start).lt("start_time", end));
                List<Object> point = new ArrayList<>();
                point.add(6 - d);
                point.add(h);
                point.add(cnt);
                heatmap.add(point);
            }
        }
        data.put("readingHeatmap", heatmap);

        // 8. 年龄段分布（根据出生日期计算）
        List<Map<String, Object>> ageDistribution = new ArrayList<>();
        Map<String, List<Long>> ageGroupChildIds = new LinkedHashMap<>();
        ageGroupChildIds.put("2-3岁", new ArrayList<>());
        ageGroupChildIds.put("3-4岁", new ArrayList<>());
        ageGroupChildIds.put("4-5岁", new ArrayList<>());
        ageGroupChildIds.put("5-6岁", new ArrayList<>());
        for (Child c : allChildren) {
            if (c.getBirthDate() == null) continue;
            int age = Period.between(c.getBirthDate(), today).getYears();
            if (age >= 2 && age < 3) ageGroupChildIds.get("2-3岁").add(c.getId());
            else if (age >= 3 && age < 4) ageGroupChildIds.get("3-4岁").add(c.getId());
            else if (age >= 4 && age < 5) ageGroupChildIds.get("4-5岁").add(c.getId());
            else if (age >= 5 && age < 6) ageGroupChildIds.get("5-6岁").add(c.getId());
        }
        for (Map.Entry<String, List<Long>> entry : ageGroupChildIds.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("ageRange", entry.getKey());
            item.put("childCount", entry.getValue().size());
            long readCnt = 0;
            if (!entry.getValue().isEmpty()) {
                readCnt = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>().in("child_id", entry.getValue()));
            }
            item.put("readCount", readCnt);
            ageDistribution.add(item);
        }
        data.put("ageDistribution", ageDistribution);

        // 9. 班级阅读排行榜
        List<Map<String, Object>> classRanking = new ArrayList<>();
        List<ClassInfo> classes = classInfoMapper.selectList(new QueryWrapper<ClassInfo>());
        for (ClassInfo ci : classes) {
            List<Child> stuList = childMapper.selectList(new QueryWrapper<Child>().eq("class_id", ci.getId()));
            List<Long> stuIds = new ArrayList<>();
            for (Child c : stuList) stuIds.add(c.getId());
            long classReads = 0;
            if (!stuIds.isEmpty()) {
                classReads = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>().in("child_id", stuIds));
            }
            double avgReads = stuList.size() > 0 ? Math.round(classReads * 10.0 / stuList.size()) / 10.0 : 0;
            Map<String, Object> item = new HashMap<>();
            item.put("className", ci.getClassName());
            item.put("studentCount", stuList.size());
            item.put("avgReadCount", avgReads);
            classRanking.add(item);
        }
        classRanking.sort((a, b) -> Double.compare((Double) b.get("avgReadCount"), (Double) a.get("avgReadCount")));
        if (classRanking.size() > 8) classRanking = classRanking.subList(0, 8);
        data.put("classRanking", classRanking);

        return data;
    }
}
