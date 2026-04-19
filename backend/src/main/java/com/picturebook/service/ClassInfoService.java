package com.picturebook.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.picturebook.dto.PageDTO;
import com.picturebook.entity.Child;
import com.picturebook.entity.ClassInfo;
import java.util.List;

public interface ClassInfoService {

    Page<ClassInfo> getClassList(PageDTO dto);

    ClassInfo getClassById(Long id);

    List<ClassInfo> getAllClasses();

    List<ClassInfo> getClassesByTeacherId(Long teacherId);

    void addClass(ClassInfo classInfo);

    void updateClass(ClassInfo classInfo);

    void deleteClass(Long id);

    List<Child> getChildrenByClassId(Long classId);
}
