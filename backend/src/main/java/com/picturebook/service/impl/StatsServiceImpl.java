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

        // 定义年龄段
        String[] ageRanges = {"3-4岁", "4-5岁", "5-6岁"};
        for (String range : ageRanges) {
            Long count = bookMapper.selectCount(new QueryWrapper<Book>()
                    .eq("status", 1)
                    .eq("age_range", range));

            Map<String, Object> item = new HashMap<>();
            item.put("name", range);
            item.put("value", count);
            result.add(item);
        }

        return result;
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

        // 统计今天有阅读记录的用户数
        Long count = readingLogMapper.selectCount(new QueryWrapper<ReadingLog>()
                .ge("start_time", todayStart)
                .select("DISTINCT user_id"));

        // 如果上面的方法不行，换一种方式
        List<Map<String, Object>> userResult = readingLogMapper.selectMaps(
                new QueryWrapper<ReadingLog>()
                .select("COUNT(DISTINCT user_id) as userCount")
                .ge("start_time", todayStart));

        Long activeCount = 0L;
        if (!userResult.isEmpty() && userResult.get(0).get("userCount") != null) {
            activeCount = ((Number) userResult.get(0).get("userCount")).longValue();
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
}
