package com.picturebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.picturebook.entity.Child;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ChildMapper extends BaseMapper<Child> {
    
    @Select("SELECT c.*, u.real_name as parent_name, cl.class_name, " +
            "TIMESTAMPDIFF(YEAR, c.birth_date, CURDATE()) as age " +
            "FROM child c " +
            "LEFT JOIN sys_user u ON c.parent_id = u.id " +
            "LEFT JOIN class_info cl ON c.class_id = cl.id " +
            "WHERE c.id = #{id}")
    Child selectChildWithDetailsById(Long id);
    
    @Select("SELECT c.*, u.real_name as parent_name, cl.class_name, " +
            "TIMESTAMPDIFF(YEAR, c.birth_date, CURDATE()) as age " +
            "FROM child c " +
            "LEFT JOIN sys_user u ON c.parent_id = u.id " +
            "LEFT JOIN class_info cl ON c.class_id = cl.id " +
            "WHERE c.parent_id = #{parentId}")
    List<Child> selectChildrenByParentId(Long parentId);
    
    @Select("SELECT c.*, u.real_name as parent_name, cl.class_name, " +
            "TIMESTAMPDIFF(YEAR, c.birth_date, CURDATE()) as age " +
            "FROM child c " +
            "LEFT JOIN sys_user u ON c.parent_id = u.id " +
            "LEFT JOIN class_info cl ON c.class_id = cl.id " +
            "WHERE c.class_id = #{classId}")
    List<Child> selectChildrenByClassId(Long classId);

    @Select("SELECT c.*, u.real_name as parent_name, cl.class_name, " +
            "TIMESTAMPDIFF(YEAR, c.birth_date, CURDATE()) as age " +
            "FROM child c " +
            "LEFT JOIN sys_user u ON c.parent_id = u.id " +
            "LEFT JOIN class_info cl ON c.class_id = cl.id " +
            "WHERE c.class_id IN (SELECT id FROM class_info WHERE teacher_id = #{teacherId})")
    List<Child> selectChildrenByTeacherId(Long teacherId);
}
