package com.picturebook.service;

import com.picturebook.entity.Book;
import com.picturebook.entity.Recommendation;
import java.util.List;
import java.util.Map;

public interface RecommendationService {

    List<Recommendation> getRecommendations(Long childId);

    List<Recommendation> getRecommendationsByType(Long childId, String recommendType);

    List<Book> getHotBooks(int limit);

    void updateAllRecommendations();

    void generateRecommendations(Long childId);

    // 新增方法：获取多策略融合推荐
    List<Map<String, Object>> getMultiStrategyRecommendations(Long childId, int limit);

    // 新增方法：获取难度匹配推荐
    List<Book> getDifficultyMatchedRecommendations(Long childId, int limit);

    // 新增方法：获取年龄段推荐
    List<Book> getAgeRangeRecommendations(Long childId, int limit);

    // 新增方法：获取推荐解释
    Map<String, Object> getRecommendationExplanation(Long childId, Long bookId);

    // 新增方法：计算绘本与儿童的匹配度
    Map<String, Object> calculateMatchScore(Long childId, Long bookId);
}
