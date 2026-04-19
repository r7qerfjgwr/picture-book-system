package com.picturebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.picturebook.entity.ClassInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ClassInfoMapper extends BaseMapper<ClassInfo> {
    
    @Select("SELECT c.*, u.real_name as teacher_name " +
            "FROM class_info c " +
            "LEFT JOIN sys_user u ON c.teacher_id = u.id " +
            "WHERE c.id = #{id}")
    ClassInfo selectClassWithTeacherById(Long id);
    
    @Update("UPDATE class_info SET student_count = student_count + #{delta} WHERE id = #{classId}")
    int updateStudentCount(Long classId, int delta);
}
