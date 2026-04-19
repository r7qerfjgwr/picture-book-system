package com.picturebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.picturebook.entity.ReadingAnnotation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ReadingAnnotationMapper extends BaseMapper<ReadingAnnotation> {

    @Select("SELECT ra.*, b.title as book_title, c.name as child_name " +
            "FROM reading_annotation ra " +
            "LEFT JOIN book b ON ra.book_id = b.id " +
            "LEFT JOIN child c ON ra.child_id = c.id " +
            "WHERE ra.child_id = #{childId} " +
            "ORDER BY ra.create_time DESC")
    List<ReadingAnnotation> selectByChildId(Long childId);

    @Select("SELECT ra.*, b.title as book_title, c.name as child_name " +
            "FROM reading_annotation ra " +
            "LEFT JOIN book b ON ra.book_id = b.id " +
            "LEFT JOIN child c ON ra.child_id = c.id " +
            "WHERE ra.child_id = #{childId} AND ra.book_id = #{bookId} " +
            "ORDER BY ra.page_num, ra.create_time DESC")
    List<ReadingAnnotation> selectByChildIdAndBookId(Long childId, Long bookId);
}
