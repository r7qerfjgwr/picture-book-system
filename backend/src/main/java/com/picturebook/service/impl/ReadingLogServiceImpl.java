package com.picturebook.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.picturebook.dto.PageDTO;
import com.picturebook.dto.ReadingLogDTO;
import com.picturebook.entity.Book;
import com.picturebook.entity.Child;
import com.picturebook.entity.ClassInfo;
import com.picturebook.entity.ReadingLog;
import com.picturebook.exception.BusinessException;
import com.picturebook.mapper.BookMapper;
import com.picturebook.mapper.ChildMapper;
import com.picturebook.mapper.ClassInfoMapper;
import com.picturebook.mapper.ReadingLogMapper;
import com.picturebook.security.LoginUser;
import com.picturebook.service.ReadingLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReadingLogServiceImpl implements ReadingLogService {

    @Autowired
    private ReadingLogMapper readingLogMapper;

    @Autowired
    private ChildMapper childMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private ClassInfoMapper classInfoMapper;

    private static final Random RANDOM = new Random();

    @Override
    public void submitReadingLog(ReadingLogDTO dto) {
        ReadingLog log = new ReadingLog();
        log.setChildId(dto.getChildId());
        log.setBookId(dto.getBookId());
        log.setStartTime(dto.getStartTime());
        log.setEndTime(dto.getEndTime());

        if (dto.getPageStayTimes() != null && !dto.getPageStayTimes().isEmpty()) {
            log.setPageStayTimes(JSONUtil.toJsonStr(dto.getPageStayTimes()));
            int totalStayTime = dto.getPageStayTimes().stream().mapToInt(Integer::intValue).sum();
            BigDecimal avgStayTime = BigDecimal.valueOf(totalStayTime)
                    .divide(BigDecimal.valueOf(dto.getPageStayTimes().size()), 2, RoundingMode.HALF_UP);
            log.setAvgPageStayTime(avgStayTime);
        }

        // 计算阅读时长
        int duration = 0;
        if (dto.getEndTime() != null && dto.getStartTime() != null) {
            duration = (int) java.time.Duration.between(dto.getStartTime(), dto.getEndTime()).getSeconds();
            log.setDuration(duration);
        }

        log.setPageTurnCount(dto.getPageTurnCount() != null ? dto.getPageTurnCount() : 0);

        // 优化阅读完成判断逻辑：阅读时长至少30秒且翻页次数至少2页才算完成
        int isCompleted = dto.getIsCompleted() != null ? dto.getIsCompleted() : 0;
        int minDuration = 30; // 最小阅读时长30秒
        int minPageTurns = 2; // 最小翻页次数2页

        if (isCompleted == 1) {
            // 检查是否满足最小条件
            if (duration < minDuration || (dto.getPageTurnCount() != null && dto.getPageTurnCount() < minPageTurns)) {
                isCompleted = 0; // 不满足条件，标记为未完成
            }
        }
        log.setIsCompleted(isCompleted);

        if (dto.getPageTurnCount() != null && dto.getPageTurnCount() > 0) {
            BigDecimal completionRate = BigDecimal.valueOf(dto.getPageTurnCount())
                    .divide(BigDecimal.valueOf(20), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            log.setCompletionRate(completionRate.min(BigDecimal.valueOf(100)));
        }

        BigDecimal focusScore = calculateFocusScore(log);
        log.setFocusScore(focusScore);

        // 保存互动数据
        log.setBookmarkCount(dto.getBookmarkCount() != null ? dto.getBookmarkCount() : 0);
        log.setAnnotationCount(dto.getAnnotationCount() != null ? dto.getAnnotationCount() : 0);
        log.setVoiceRecordCount(dto.getVoiceRecordCount() != null ? dto.getVoiceRecordCount() : 0);

        readingLogMapper.insert(log);
    }

    private BigDecimal calculateFocusScore(ReadingLog log) {
        BigDecimal score = BigDecimal.ZERO;

        if (log.getAvgPageStayTime() != null) {
            BigDecimal stayTimeScore = log.getAvgPageStayTime()
                    .divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(60));
            score = score.add(stayTimeScore.min(BigDecimal.valueOf(60)));
        }

        if (log.getCompletionRate() != null) {
            BigDecimal completionScore = log.getCompletionRate()
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(40));
            score = score.add(completionScore);
        }

        return score.min(BigDecimal.valueOf(100)).max(BigDecimal.ZERO);
    }

    @Override
    public Page<ReadingLog> getReadingLogs(PageDTO dto) {
        Page<ReadingLog> page = new Page<>(dto.getCurrent(), dto.getSize());
        QueryWrapper<ReadingLog> wrapper = new QueryWrapper<>();

        // 获取当前登录用户
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String roleKey = loginUser.getRoleKey();
        Long userId = loginUser.getUserId();

        // 根据角色过滤数据
        if ("PARENT".equals(roleKey)) {
            // 家长：只能查看自己绑定孩子的记录
            List<Child> myChildren = childMapper.selectList(
                    new QueryWrapper<Child>().eq("parent_id", userId));
            if (myChildren.isEmpty()) {
                return new Page<>(dto.getCurrent(), dto.getSize());
            }
            List<Long> childIds = myChildren.stream().map(Child::getId).collect(Collectors.toList());
            wrapper.in("child_id", childIds);

            Long childId = dto.getChildId();
            if (childId != null) {
                if (!childIds.contains(childId)) {
                    return new Page<>(dto.getCurrent(), dto.getSize());
                }
                wrapper.eq("child_id", childId);
            }
        } else if ("TEACHER".equals(roleKey)) {
            // 教师：只能查看自己班级儿童的记录
            List<ClassInfo> myClasses = classInfoMapper.selectList(
                    new QueryWrapper<ClassInfo>().eq("teacher_id", userId));
            if (myClasses.isEmpty()) {
                return new Page<>(dto.getCurrent(), dto.getSize());
            }
            List<Long> classIds = myClasses.stream().map(ClassInfo::getId).collect(Collectors.toList());
            List<Child> classChildren = childMapper.selectList(
                    new QueryWrapper<Child>().in("class_id", classIds));
            if (classChildren.isEmpty()) {
                return new Page<>(dto.getCurrent(), dto.getSize());
            }
            List<Long> childIds = classChildren.stream().map(Child::getId).collect(Collectors.toList());
            wrapper.in("child_id", childIds);

            Long childId = dto.getChildId();
            if (childId != null) {
                if (!childIds.contains(childId)) {
                    return new Page<>(dto.getCurrent(), dto.getSize());
                }
                wrapper.eq("child_id", childId);
            }
        } else {
            // 管理员：可以查看所有记录
            Long childId = dto.getChildId();
            if (childId != null) {
                wrapper.eq("child_id", childId);
            }
        }

        String startDate = dto.getStartDate();
        String endDate = dto.getEndDate();
        if (startDate != null && endDate != null) {
            if (startDate.contains("T")) {
                startDate = startDate.split("T")[0];
            }
            if (endDate.contains("T")) {
                endDate = endDate.split("T")[0];
            }
            wrapper.between("start_time", startDate + " 00:00:00", endDate + " 23:59:59");
        }

        wrapper.orderByDesc("start_time");
        Page<ReadingLog> result = readingLogMapper.selectPage(page, wrapper);

        // 填充关联信息
        for (ReadingLog log : result.getRecords()) {
            if (log.getChildId() != null) {
                Child child = childMapper.selectById(log.getChildId());
                if (child != null) {
                    log.setChildName(child.getName());
                }
            }
            if (log.getBookId() != null) {
                Book book = bookMapper.selectById(log.getBookId());
                if (book != null) {
                    log.setBookTitle(book.getTitle());
                    log.setBookCategory(book.getCategory());
                }
            }
        }

        return result;
    }

    @Override
    public List<ReadingLog> getReadingLogsByChildId(Long childId) {
        return readingLogMapper.selectLogsByChildId(childId);
    }

    @Override
    public Map<String, Object> getReadingStats(Long childId) {
        Map<String, Object> stats = new HashMap<>();
        List<ReadingLog> logs = readingLogMapper.selectLogsByChildId(childId);

        int totalDuration = logs.stream()
                .filter(l -> l.getDuration() != null)
                .mapToInt(ReadingLog::getDuration)
                .sum();
        int totalBooks = (int) logs.stream().map(ReadingLog::getBookId).distinct().count();
        int completedBooks = (int) logs.stream()
                .filter(l -> l.getIsCompleted() != null && l.getIsCompleted() == 1)
                .count();
        double avgFocusScore = logs.stream()
                .filter(l -> l.getFocusScore() != null)
                .mapToDouble(l -> l.getFocusScore().doubleValue())
                .average()
                .orElse(0);

        stats.put("totalDuration", totalDuration);
        stats.put("totalBooks", totalBooks);
        stats.put("completedBooks", completedBooks);
        stats.put("avgFocusScore", BigDecimal.valueOf(avgFocusScore).setScale(2, RoundingMode.HALF_UP));
        stats.put("totalLogs", logs.size());

        return stats;
    }

    @Override
    public List<Map<String, Object>> getReadingTrend(Long childId, Integer days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);
        return readingLogMapper.selectDailyStatsByChildId(childId, startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> getCategoryStats(Long childId) {
        return readingLogMapper.selectCategoryStatsByChildId(childId);
    }

    @Override
    public void generateMockData(Integer count) {
        List<Child> children = childMapper.selectList(null);
        List<Book> books = bookMapper.selectList(
            new QueryWrapper<Book>().eq("status", 1)
        );

        if (children.isEmpty() || books.isEmpty()) {
            throw new BusinessException("请先添加儿童和绘本数据");
        }

        for (int i = 0; i < count; i++) {
            Child child = children.get(RANDOM.nextInt(children.size()));
            Book book = books.get(RANDOM.nextInt(books.size()));
            ReadingLog log = generateRandomLog(child, book);
            readingLogMapper.insert(log);
        }
    }

    private ReadingLog generateRandomLog(Child child, Book book) {
        ReadingLog log = new ReadingLog();
        log.setChildId(child.getId());
        log.setBookId(book.getId());

        int daysAgo = RANDOM.nextInt(30);
        LocalDateTime startTime = LocalDateTime.now()
                .minusDays(daysAgo)
                .withHour(RANDOM.nextInt(12) + 8)
                .withMinute(RANDOM.nextInt(60))
                .withSecond(0);
        log.setStartTime(startTime);

        int pageCount = book.getPageCount() != null ? book.getPageCount() : 20;
        List<Integer> pageStayTimes = new ArrayList<>();
        int baseStayTime = 15 + RANDOM.nextInt(20);
        for (int i = 0; i < pageCount; i++) {
            int stayTime = baseStayTime + RANDOM.nextInt(15) - 7;
            pageStayTimes.add(Math.max(5, stayTime));
        }
        log.setPageStayTimes(JSONUtil.toJsonStr(pageStayTimes));

        int totalDuration = pageStayTimes.stream().mapToInt(Integer::intValue).sum();
        LocalDateTime endTime = startTime.plusSeconds(totalDuration);
        log.setEndTime(endTime);
        log.setDuration(totalDuration);
        log.setPageTurnCount(pageCount);

        boolean completed = RANDOM.nextDouble() > 0.1;
        log.setIsCompleted(completed ? 1 : 0);
        log.setCompletionRate(BigDecimal.valueOf(completed ? 100 : RANDOM.nextInt(80) + 10));

        double avgStayTime = pageStayTimes.stream().mapToInt(Integer::intValue).average().orElse(0);
        log.setAvgPageStayTime(BigDecimal.valueOf(avgStayTime).setScale(2, RoundingMode.HALF_UP));
        log.setFocusScore(calculateFocusScore(log));

        return log;
    }

    @Override
    public void updateReadingLog(Long id, ReadingLogDTO dto) {
        ReadingLog log = readingLogMapper.selectById(id);
        if (log == null) {
            throw new BusinessException("阅读记录不存在");
        }

        // 更新结束时间
        if (dto.getEndTime() != null) {
            log.setEndTime(dto.getEndTime());
        }

        // 计算阅读时长
        if (log.getEndTime() != null && log.getStartTime() != null) {
            long duration = java.time.Duration.between(log.getStartTime(), log.getEndTime()).getSeconds();
            log.setDuration((int) duration);
        }

        // 更新翻页次数
        if (dto.getPageTurnCount() != null) {
            log.setPageTurnCount(dto.getPageTurnCount());
        }

        // 更新是否完成 - 添加合理时长判断
        if (dto.getIsCompleted() != null) {
            int isCompleted = dto.getIsCompleted();
            int minDuration = 30; // 最小阅读时长30秒
            int minPageTurns = 2; // 最小翻页次数2页

            if (isCompleted == 1) {
                int currentDuration = log.getDuration() != null ? log.getDuration() : 0;
                int currentPageTurns = log.getPageTurnCount() != null ? log.getPageTurnCount() : 0;

                // 检查是否满足最小条件
                if (currentDuration < minDuration || currentPageTurns < minPageTurns) {
                    isCompleted = 0; // 不满足条件，标记为未完成
                }
            }
            log.setIsCompleted(isCompleted);
        }

        // 更新页停留时间
        if (dto.getPageStayTimes() != null && !dto.getPageStayTimes().isEmpty()) {
            log.setPageStayTimes(JSONUtil.toJsonStr(dto.getPageStayTimes()));
            int totalStayTime = dto.getPageStayTimes().stream().mapToInt(Integer::intValue).sum();
            BigDecimal avgStayTime = BigDecimal.valueOf(totalStayTime)
                    .divide(BigDecimal.valueOf(dto.getPageStayTimes().size()), 2, RoundingMode.HALF_UP);
            log.setAvgPageStayTime(avgStayTime);
        }

        // 计算完成率
        if (dto.getPageTurnCount() != null && dto.getPageTurnCount() > 0) {
            Book book = bookMapper.selectById(log.getBookId());
            int totalPages = book != null && book.getPageCount() != null ? book.getPageCount() : 20;
            BigDecimal completionRate = BigDecimal.valueOf(dto.getPageTurnCount())
                    .divide(BigDecimal.valueOf(totalPages), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            log.setCompletionRate(completionRate.min(BigDecimal.valueOf(100)));
        }

        // 重新计算专注度分数
        BigDecimal focusScore = calculateFocusScore(log);
        log.setFocusScore(focusScore);

        readingLogMapper.updateById(log);
    }

    @Override
    public ReadingLog getReadingLogById(Long id) {
        ReadingLog log = readingLogMapper.selectById(id);
        if (log != null) {
            if (log.getChildId() != null) {
                Child child = childMapper.selectById(log.getChildId());
                if (child != null) {
                    log.setChildName(child.getName());
                }
            }
            if (log.getBookId() != null) {
                Book book = bookMapper.selectById(log.getBookId());
                if (book != null) {
                    log.setBookTitle(book.getTitle());
                    log.setBookCategory(book.getCategory());
                }
            }
        }
        return log;
    }
}
