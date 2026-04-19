package com.picturebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.picturebook.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
    
    @Select("SELECT category, COUNT(*) as count FROM book WHERE status = 1 GROUP BY category")
    List<Map<String, Object>> selectCategoryStats();
    
    @Select("SELECT * FROM book WHERE category = #{category} AND status = 1 ORDER BY read_count DESC LIMIT #{limit}")
    List<Book> selectHotBooksByCategory(@Param("category") String category, @Param("limit") int limit);
    
    @Select("SELECT * FROM book WHERE status = 1 ORDER BY read_count DESC LIMIT #{limit}")
    List<Book> selectHotBooks(int limit);
}
