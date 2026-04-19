package com.picturebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.picturebook.entity.ReadingBookmark;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReadingBookmarkMapper extends BaseMapper<ReadingBookmark> {

    @Select("SELECT rb.*, b.title as book_title, b.cover_url as book_cover " +
            "FROM reading_bookmark rb " +
            "LEFT JOIN book b ON rb.book_id = b.id " +
            "WHERE rb.child_id = #{childId} " +
            "ORDER BY rb.create_time DESC")
    List<ReadingBookmark> selectByChildId(Long childId);

    @Select("SELECT b.category, COUNT(*) as count " +
            "FROM reading_bookmark rb " +
            "LEFT JOIN book b ON rb.book_id = b.id " +
            "WHERE rb.child_id = #{childId} " +
            "GROUP BY b.category")
    List<Map<String, Object>> selectCategoryStatsByChildId(Long childId);
}
