package com.picturebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.picturebook.entity.*;
import com.picturebook.mapper.*;
import com.picturebook.service.AnalysisService;
import com.picturebook.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Autowired
    private RecommendationMapper recommendationMapper;

    @Autowired
    private ChildMapper childMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private ReadingLogMapper readingLogMapper;

    @Autowired
    private BehaviorAnalysisMapper behaviorAnalysisMapper;

    @Autowired
    private AnalysisService analysisService;

    @Override
    public List<Recommendation> getRecommendations(Long childId) {
        List<Recommendation> recommendations = recommendationMapper.selectList(
                new LambdaQueryWrapper<Recommendation>()
                        .eq(Recommendation::getChildId, childId)
                        .orderByDesc(Recommendation::getRecommendScore)
        );
        // 如果没有推荐数据，自动生成
        if (recommendations == null || recommendations.isEmpty()) {
            generateRecommendations(childId);
            recommendations = recommendationMapper.selectList(
                    new LambdaQueryWrapper<Recommendation>()
                            .eq(Recommendation::getChildId, childId)
                            .orderByDesc(Recommendation::getRecommendScore)
            );
        }
        // 填充 Book 信息
        for (Recommendation rec : recommendations) {
            if (rec.getBookId() != null) {
                rec.setBook(bookMapper.selectById(rec.getBookId()));
            }
        }
        return recommendations;
    }

    @Override
    public List<Recommendation> getRecommendationsByType(Long childId, String recommendType) {
        return recommendationMapper.selectRecommendationsByType(childId, recommendType);
    }

    @Override
    public List<Book> getHotBooks(int limit) {
        return bookMapper.selectHotBooks(limit);
    }

    @Override
    public void updateAllRecommendations() {
        List<Child> children = childMapper.selectList(null);
        for (Child child : children) {
            generateRecommendations(child.getId());
        }
    }

    @Override
    public void generateRecommendations(Long childId) {
        recommendationMapper.delete(
                new LambdaQueryWrapper<Recommendation>().eq(Recommendation::getChildId, childId)
        );
        Child child = childMapper.selectById(childId);
        if (child == null) {
            return;
        }
        generateContentBasedRecommendations(child);
        generateCollaborativeRecommendations(child);
        generateHotRecommendations(child);
        generateAgeBasedRecommendations(child);
        generateAbilityBasedRecommendations(child);
    }

    private void generateContentBasedRecommendations(Child child) {
        if (child.getInterestTags() == null || child.getInterestTags().isEmpty()) {
            return;
        }
        String[] tags = child.getInterestTags().split(",");
        List<Book> allBooks = bookMapper.selectList(
                new LambdaQueryWrapper<Book>().eq(Book::getStatus, 1)
        );
        List<ReadingLog> readLogs = readingLogMapper.selectLogsByChildId(child.getId());
        Set<Long> readBookIds = readLogs.stream()
                .map(ReadingLog::getBookId)
                .collect(Collectors.toSet());

        for (String tag : tags) {
            String trimmedTag = tag.trim();
            List<Book> matchingBooks = allBooks.stream()
                    .filter(b -> !readBookIds.contains(b.getId()) &&
                            (trimmedTag.equals(b.getCategory()) ||
                                    (b.getSubCategory() != null && b.getSubCategory().contains(trimmedTag))))
                    .limit(3)
                    .collect(Collectors.toList());

            for (Book book : matchingBooks) {
                Recommendation rec = new Recommendation();
                rec.setChildId(child.getId());
                rec.setBookId(book.getId());
                rec.setRecommendType("content_based");
                rec.setRecommendScore(BigDecimal.valueOf(85.0));
                rec.setReason("根据您对" + trimmedTag + "类绘本的兴趣推荐");
                rec.setIsRead(0);
                recommendationMapper.insert(rec);
            }
        }
    }

    private void generateCollaborativeRecommendations(Child child) {
        List<ReadingLog> childLogs = readingLogMapper.selectLogsByChildId(child.getId());
        if (childLogs.isEmpty()) {
            return;
        }

        Set<Long> readBookIds = childLogs.stream()
                .map(ReadingLog::getBookId)
                .collect(Collectors.toSet());

        // 找到相似阅读兴趣的儿童
        Map<Long, Long> childBookCount = childLogs.stream()
                .collect(Collectors.groupingBy(ReadingLog::getBookId, Collectors.counting()));

        // 获取同龄儿童
        LambdaQueryWrapper<Child> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Child::getAge, child.getAge());
        List<Child> peers = childMapper.selectList(wrapper);

        // 找出同龄儿童读过的热门绘本
        Map<Long, Integer> bookScore = new HashMap<>();
        for (Child peer : peers) {
            if (peer.getId().equals(child.getId())) continue;

            List<ReadingLog> peerLogs = readingLogMapper.selectLogsByChildId(peer.getId());
            for (ReadingLog log : peerLogs) {
                if (!readBookIds.contains(log.getBookId())) {
                    bookScore.merge(log.getBookId(), 1, Integer::sum);
                }
            }
        }

        // 排序推荐
        List<Map.Entry<Long, Integer>> sortedBooks = bookScore.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .collect(Collectors.toList());

        for (Map.Entry<Long, Integer> entry : sortedBooks) {
            Book book = bookMapper.selectById(entry.getKey());
            if (book != null) {
                Recommendation rec = new Recommendation();
                rec.setChildId(child.getId());
                rec.setBookId(book.getId());
                rec.setRecommendType("collaborative");
                rec.setRecommendScore(BigDecimal.valueOf(80.0 + Math.min(entry.getValue() * 2, 15)));
                rec.setReason("同龄小朋友也喜欢这本绘本");
                rec.setIsRead(0);
                recommendationMapper.insert(rec);
            }
        }
    }

    private void generateHotRecommendations(Child child) {
        List<ReadingLog> childLogs = readingLogMapper.selectLogsByChildId(child.getId());
        Set<Long> readBookIds = childLogs.stream()
                .map(ReadingLog::getBookId)
                .collect(Collectors.toSet());

        List<Book> hotBooks = bookMapper.selectHotBooks(5);
        for (Book book : hotBooks) {
            if (!readBookIds.contains(book.getId())) {
                Recommendation rec = new Recommendation();
                rec.setChildId(child.getId());
                rec.setBookId(book.getId());
                rec.setRecommendType("hot");
                rec.setRecommendScore(BigDecimal.valueOf(75.0));
                rec.setReason("热门绘本推荐");
                rec.setIsRead(0);
                recommendationMapper.insert(rec);
            }
        }
    }

    private void generateAgeBasedRecommendations(Child child) {
        if (child.getAge() == null) {
            return;
        }

        String ageRange = getAgeRange(child.getAge());
        List<ReadingLog> childLogs = readingLogMapper.selectLogsByChildId(child.getId());
        Set<Long> readBookIds = childLogs.stream()
                .map(ReadingLog::getBookId)
                .collect(Collectors.toSet());

        List<Book> ageBooks = bookMapper.selectList(
                new LambdaQueryWrapper<Book>()
                        .eq(Book::getStatus, 1)
                        .eq(Book::getAgeRange, ageRange)
                        .orderByDesc(Book::getReadCount)
                        .last("LIMIT 5")
        );

        for (Book book : ageBooks) {
            if (!readBookIds.contains(book.getId())) {
                Recommendation rec = new Recommendation();
                rec.setChildId(child.getId());
                rec.setBookId(book.getId());
                rec.setRecommendType("age_based");
                rec.setRecommendScore(BigDecimal.valueOf(82.0));
                rec.setReason("适合" + ageRange + "儿童阅读");
                rec.setIsRead(0);
                recommendationMapper.insert(rec);
            }
        }
    }

    private void generateAbilityBasedRecommendations(Child child) {
        BehaviorAnalysis analysis = behaviorAnalysisMapper.selectLatestByChildId(child.getId());
        if (analysis == null) {
            return;
        }

        List<ReadingLog> childLogs = readingLogMapper.selectLogsByChildId(child.getId());
        Set<Long> readBookIds = childLogs.stream()
                .map(ReadingLog::getBookId)
                .collect(Collectors.toSet());

        // 根据阅读能力推荐相应难度的绘本
        int difficultyLevel = 1;
        if (analysis.getReadingAbilityScore() != null) {
            double ability = analysis.getReadingAbilityScore().doubleValue();
            if (ability >= 80) {
                difficultyLevel = 3;
            } else if (ability >= 60) {
                difficultyLevel = 2;
            }
        }

        List<Book> abilityBooks = bookMapper.selectList(
                new LambdaQueryWrapper<Book>()
                        .eq(Book::getStatus, 1)
                        .eq(Book::getDifficultyLevel, difficultyLevel)
                        .orderByDesc(Book::getReadCount)
                        .last("LIMIT 5")
        );

        for (Book book : abilityBooks) {
            if (!readBookIds.contains(book.getId())) {
                Recommendation rec = new Recommendation();
                rec.setChildId(child.getId());
                rec.setBookId(book.getId());
                rec.setRecommendType("ability_based");
                rec.setRecommendScore(BigDecimal.valueOf(88.0));
                rec.setReason("根据您的阅读能力水平推荐");
                rec.setIsRead(0);
                recommendationMapper.insert(rec);
            }
        }
    }

    private String getAgeRange(Integer age) {
        if (age == null) return "3-6岁";
        if (age <= 3) return "3-4岁";
        if (age <= 4) return "4-5岁";
        return "5-6岁";
    }

    @Override
    public List<Map<String, Object>> getMultiStrategyRecommendations(Long childId, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();

        Child child = childMapper.selectById(childId);
        if (child == null) {
            return result;
        }

        List<ReadingLog> childLogs = readingLogMapper.selectLogsByChildId(childId);
        Set<Long> readBookIds = childLogs.stream()
                .map(ReadingLog::getBookId)
                .collect(Collectors.toSet());

        // 综合多种策略
        Map<Long, Double> bookScores = new HashMap<>();
        Map<Long, List<String>> bookReasons = new HashMap<>();

        // 策略1: 兴趣匹配
        if (child.getInterestTags() != null) {
            String[] tags = child.getInterestTags().split(",");
            for (String tag : tags) {
                List<Book> matchedBooks = bookMapper.selectList(
                        new LambdaQueryWrapper<Book>()
                                .eq(Book::getStatus, 1)
                                .eq(Book::getCategory, tag.trim())
                                .last("LIMIT 10")
                );
                for (Book book : matchedBooks) {
                    if (!readBookIds.contains(book.getId())) {
                        bookScores.merge(book.getId(), 30.0, Double::sum);
                        bookReasons.computeIfAbsent(book.getId(), k -> new ArrayList<>())
                                .add("符合" + tag.trim() + "兴趣");
                    }
                }
            }
        }

        // 策略2: 年龄适合
        if (child.getAge() != null) {
            String ageRange = getAgeRange(child.getAge());
            List<Book> ageBooks = bookMapper.selectList(
                    new LambdaQueryWrapper<Book>()
                            .eq(Book::getStatus, 1)
                            .eq(Book::getAgeRange, ageRange)
                            .last("LIMIT 10")
            );
            for (Book book : ageBooks) {
                if (!readBookIds.contains(book.getId())) {
                    bookScores.merge(book.getId(), 25.0, Double::sum);
                    bookReasons.computeIfAbsent(book.getId(), k -> new ArrayList<>())
                            .add("适合年龄段");
                }
            }
        }

        // 策略3: 能力匹配
        BehaviorAnalysis analysis = behaviorAnalysisMapper.selectLatestByChildId(childId);
        if (analysis != null && analysis.getReadingAbilityScore() != null) {
            int diffLevel = analysis.getReadingAbilityScore().doubleValue() >= 70 ? 2 : 1;
            List<Book> abilityBooks = bookMapper.selectList(
                    new LambdaQueryWrapper<Book>()
                            .eq(Book::getStatus, 1)
                            .eq(Book::getDifficultyLevel, diffLevel)
                            .last("LIMIT 10")
            );
            for (Book book : abilityBooks) {
                if (!readBookIds.contains(book.getId())) {
                    bookScores.merge(book.getId(), 25.0, Double::sum);
                    bookReasons.computeIfAbsent(book.getId(), k -> new ArrayList<>())
                            .add("能力匹配");
                }
            }
        }

        // 策略4: 热门推荐
        List<Book> hotBooks = bookMapper.selectHotBooks(10);
        for (Book book : hotBooks) {
            if (!readBookIds.contains(book.getId())) {
                bookScores.merge(book.getId(), 20.0, Double::sum);
                bookReasons.computeIfAbsent(book.getId(), k -> new ArrayList<>())
                        .add("热门绘本");
            }
        }

        // 排序并返回结果
        List<Map.Entry<Long, Double>> sortedBooks = bookScores.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .collect(Collectors.toList());

        for (Map.Entry<Long, Double> entry : sortedBooks) {
            Book book = bookMapper.selectById(entry.getKey());
            if (book != null) {
                Map<String, Object> rec = new HashMap<>();
                rec.put("book", book);
                rec.put("score", BigDecimal.valueOf(entry.getValue()).setScale(2, RoundingMode.HALF_UP));
                rec.put("reasons", bookReasons.get(entry.getKey()));
                result.add(rec);
            }
        }

        return result;
    }

    @Override
    public List<Book> getDifficultyMatchedRecommendations(Long childId, int limit) {
        List<Book> result = new ArrayList<>();

        BehaviorAnalysis analysis = behaviorAnalysisMapper.selectLatestByChildId(childId);
        int difficultyLevel = 1;

        if (analysis != null && analysis.getReadingAbilityScore() != null) {
            double ability = analysis.getReadingAbilityScore().doubleValue();
            if (ability >= 80) {
                difficultyLevel = 3;
            } else if (ability >= 60) {
                difficultyLevel = 2;
            }
        }

        List<ReadingLog> childLogs = readingLogMapper.selectLogsByChildId(childId);
        Set<Long> readBookIds = childLogs.stream()
                .map(ReadingLog::getBookId)
                .collect(Collectors.toSet());

        List<Book> books = bookMapper.selectList(
                new LambdaQueryWrapper<Book>()
                        .eq(Book::getStatus, 1)
                        .eq(Book::getDifficultyLevel, difficultyLevel)
                        .orderByDesc(Book::getReadCount)
                        .last("LIMIT " + limit)
        );

        for (Book book : books) {
            if (!readBookIds.contains(book.getId())) {
                result.add(book);
            }
        }

        return result;
    }

    @Override
    public List<Book> getAgeRangeRecommendations(Long childId, int limit) {
        Child child = childMapper.selectById(childId);
        if (child == null || child.getAge() == null) {
            return Collections.emptyList();
        }

        String ageRange = getAgeRange(child.getAge());

        List<ReadingLog> childLogs = readingLogMapper.selectLogsByChildId(childId);
        Set<Long> readBookIds = childLogs.stream()
                .map(ReadingLog::getBookId)
                .collect(Collectors.toSet());

        List<Book> books = bookMapper.selectList(
                new LambdaQueryWrapper<Book>()
                        .eq(Book::getStatus, 1)
                        .eq(Book::getAgeRange, ageRange)
                        .orderByDesc(Book::getReadCount)
                        .last("LIMIT " + limit)
        );

        return books.stream()
                .filter(b -> !readBookIds.contains(b.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getRecommendationExplanation(Long childId, Long bookId) {
        Map<String, Object> explanation = new HashMap<>();

        Child child = childMapper.selectById(childId);
        Book book = bookMapper.selectById(bookId);

        if (child == null || book == null) {
            return explanation;
        }

        List<String> matchReasons = new ArrayList<>();
        double totalScore = 0;

        // 兴趣匹配度
        if (child.getInterestTags() != null && book.getCategory() != null) {
            if (child.getInterestTags().contains(book.getCategory())) {
                matchReasons.add("符合您的" + book.getCategory() + "兴趣偏好");
                totalScore += 30;
            }
        }

        // 年龄匹配度
        if (child.getAge() != null && book.getAgeRange() != null) {
            String childAgeRange = getAgeRange(child.getAge());
            if (childAgeRange.equals(book.getAgeRange())) {
                matchReasons.add("适合您的年龄段(" + childAgeRange + ")");
                totalScore += 25;
            }
        }

        // 能力匹配度
        BehaviorAnalysis analysis = behaviorAnalysisMapper.selectLatestByChildId(childId);
        if (analysis != null && book.getDifficultyLevel() != null) {
            int expectedLevel = 1;
            if (analysis.getReadingAbilityScore() != null) {
                double ability = analysis.getReadingAbilityScore().doubleValue();
                if (ability >= 80) expectedLevel = 3;
                else if (ability >= 60) expectedLevel = 2;
            }
            if (expectedLevel == book.getDifficultyLevel()) {
                matchReasons.add("难度适合您当前的阅读能力");
                totalScore += 25;
            }
        }

        // 知识拓展
        if (book.getKnowledgeType() != null) {
            matchReasons.add("可拓展" + book.getKnowledgeType() + "知识");
            totalScore += 10;
        }

        explanation.put("matchReasons", matchReasons);
        explanation.put("matchScore", BigDecimal.valueOf(Math.min(totalScore, 100)).setScale(2, RoundingMode.HALF_UP));
        explanation.put("bookInfo", book);

        return explanation;
    }

    @Override
    public Map<String, Object> calculateMatchScore(Long childId, Long bookId) {
        Map<String, Object> result = new HashMap<>();

        Child child = childMapper.selectById(childId);
        Book book = bookMapper.selectById(bookId);

        if (child == null || book == null) {
            result.put("score", 0);
            return result;
        }

        double interestScore = calculateInterestMatch(child, book);
        double ageScore = calculateAgeMatch(child, book);
        double abilityScore = calculateAbilityMatch(childId, book);

        Map<String, Double> dimensionScores = new HashMap<>();
        dimensionScores.put("interest", interestScore);
        dimensionScores.put("age", ageScore);
        dimensionScores.put("ability", abilityScore);

        double totalScore = interestScore * 0.4 + ageScore * 0.3 + abilityScore * 0.3;

        result.put("dimensionScores", dimensionScores);
        result.put("totalScore", BigDecimal.valueOf(totalScore).setScale(2, RoundingMode.HALF_UP));

        return result;
    }

    private double calculateInterestMatch(Child child, Book book) {
        if (child.getInterestTags() == null || book.getCategory() == null) {
            return 50;
        }
        return child.getInterestTags().contains(book.getCategory()) ? 100 : 30;
    }

    private double calculateAgeMatch(Child child, Book book) {
        if (child.getAge() == null || book.getAgeRange() == null) {
            return 50;
        }
        String childAgeRange = getAgeRange(child.getAge());
        return childAgeRange.equals(book.getAgeRange()) ? 100 : 50;
    }

    private double calculateAbilityMatch(Long childId, Book book) {
        if (book.getDifficultyLevel() == null) {
            return 50;
        }

        BehaviorAnalysis analysis = behaviorAnalysisMapper.selectLatestByChildId(childId);
        if (analysis == null || analysis.getReadingAbilityScore() == null) {
            return 50;
        }

        double ability = analysis.getReadingAbilityScore().doubleValue();
        int expectedLevel = ability >= 80 ? 3 : (ability >= 60 ? 2 : 1);

        return expectedLevel == book.getDifficultyLevel() ? 100 : 50;
    }
}
