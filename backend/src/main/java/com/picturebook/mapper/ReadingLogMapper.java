package com.picturebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.picturebook.entity.ReadingLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReadingLogMapper extends BaseMapper<ReadingLog> {
    
    @Select("SELECT rl.*, c.name as child_name, b.title as book_title, b.category as book_category " +
            "FROM reading_log rl " +
            "LEFT JOIN child c ON rl.child_id = c.id " +
            "LEFT JOIN book b ON rl.book_id = b.id " +
            "WHERE rl.child_id = #{childId} " +
            "ORDER BY rl.start_time DESC")
    List<ReadingLog> selectLogsByChildId(Long childId);
    
    @Select("SELECT rl.*, c.name as child_name, b.title as book_title, b.category as book_category " +
            "FROM reading_log rl " +
            "LEFT JOIN child c ON rl.child_id = c.id " +
            "LEFT JOIN book b ON rl.book_id = b.id " +
            "WHERE rl.child_id = #{childId} AND rl.start_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY rl.start_time DESC")
    List<ReadingLog> selectLogsByChildIdAndTimeRange(@Param("childId") Long childId, 
                                                       @Param("startTime") LocalDateTime startTime, 
                                                       @Param("endTime") LocalDateTime endTime);
    
    @Select("SELECT b.category, COUNT(*) as count, SUM(rl.duration) as total_duration " +
            "FROM reading_log rl " +
            "LEFT JOIN book b ON rl.book_id = b.id " +
            "WHERE rl.child_id = #{childId} " +
            "GROUP BY b.category")
    List<Map<String, Object>> selectCategoryStatsByChildId(Long childId);
    
    @Select("SELECT DATE(start_time) as date, SUM(duration) as duration, COUNT(*) as count " +
            "FROM reading_log " +
            "WHERE child_id = #{childId} AND start_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE(start_time) " +
            "ORDER BY date")
    List<Map<String, Object>> selectDailyStatsByChildId(@Param("childId") Long childId,
                                                         @Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime);
    
    @Select("SELECT rl.*, c.name as child_name, b.title as book_title, b.category as book_category " +
            "FROM reading_log rl " +
            "LEFT JOIN child c ON rl.child_id = c.id " +
            "LEFT JOIN book b ON rl.book_id = b.id " +
            "WHERE c.class_id = #{classId} " +
            "ORDER BY rl.start_time DESC")
    List<ReadingLog> selectLogsByClassId(Long classId);
}
