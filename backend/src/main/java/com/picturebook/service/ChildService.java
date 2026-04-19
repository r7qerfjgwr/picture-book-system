package com.picturebook.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.picturebook.dto.ChildDTO;
import com.picturebook.dto.PageDTO;
import com.picturebook.entity.Child;
import java.util.List;

public interface ChildService {

    List<Child> getChildrenByParentId(Long parentId);

    List<Child> getChildrenByClassId(Long classId);

    List<Child> getChildrenByTeacherId(Long teacherId);

    List<Child> getAllChildren();

    Child getChildById(Long id);

    Page<Child> getChildList(PageDTO dto);

    void bindChild(ChildDTO dto);

    void updateChild(ChildDTO dto);

    void deleteChild(Long id);
}
