package com.picturebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.picturebook.entity.Recommendation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface RecommendationMapper extends BaseMapper<Recommendation> {
    
    @Select("SELECT r.*, b.* " +
            "FROM recommendation r " +
            "LEFT JOIN book b ON r.book_id = b.id " +
            "WHERE r.child_id = #{childId} " +
            "ORDER BY r.recommend_score DESC")
    List<Recommendation> selectRecommendationsWithBook(Long childId);
    
    @Select("SELECT r.*, b.* " +
            "FROM recommendation r " +
            "LEFT JOIN book b ON r.book_id = b.id " +
            "WHERE r.child_id = #{childId} AND r.recommend_type = #{recommendType} " +
            "ORDER BY r.recommend_score DESC")
    List<Recommendation> selectRecommendationsByType(@Param("childId") Long childId, 
                                                      @Param("recommendType") String recommendType);
}
