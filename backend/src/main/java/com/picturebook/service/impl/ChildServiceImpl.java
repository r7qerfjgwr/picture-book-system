package com.picturebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.picturebook.dto.ChildDTO;
import com.picturebook.dto.PageDTO;
import com.picturebook.entity.Child;
import com.picturebook.entity.ClassInfo;
import com.picturebook.exception.BusinessException;
import com.picturebook.mapper.ChildMapper;
import com.picturebook.mapper.ClassInfoMapper;
import com.picturebook.security.LoginUser;
import com.picturebook.service.ChildService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ChildServiceImpl implements ChildService {
    
    @Autowired
    private ChildMapper childMapper;
    
    @Autowired
    private ClassInfoMapper classInfoMapper;
    
    @Override
    public List<Child> getChildrenByParentId(Long parentId) {
        return childMapper.selectChildrenByParentId(parentId);
    }
    
    @Override
    public List<Child> getChildrenByClassId(Long classId) {
        return childMapper.selectChildrenByClassId(classId);
    }

    @Override
    public List<Child> getChildrenByTeacherId(Long teacherId) {
        return childMapper.selectChildrenByTeacherId(teacherId);
    }

    @Override
    public List<Child> getAllChildren() {
        return childMapper.selectList(null);
    }
    
    @Override
    public Child getChildById(Long id) {
        Child child = childMapper.selectChildWithDetailsById(id);
        if (child == null) {
            throw new BusinessException("儿童信息不存在");
        }
        return child;
    }
    
    @Override
    public Page<Child> getChildList(PageDTO dto) {
        Page<Child> page = new Page<>(dto.getCurrent(), dto.getSize());
        LambdaQueryWrapper<Child> wrapper = new LambdaQueryWrapper<>();
        if (dto.getParentId() != null) {
            wrapper.eq(Child::getParentId, dto.getParentId());
        }
        if (dto.getClassId() != null) {
            wrapper.eq(Child::getClassId, dto.getClassId());
        }
        wrapper.orderByDesc(Child::getCreateTime);
        return childMapper.selectPage(page, wrapper);
    }
    
    @Override
    public void bindChild(ChildDTO dto) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (!loginUser.getRoleKey().equals("PARENT") && !loginUser.getRoleKey().equals("ADMIN")) {
            throw new BusinessException("无权限绑定儿童");
        }
        Child child = new Child();
        BeanUtils.copyProperties(dto, child);
        if (dto.getParentId() == null) {
            child.setParentId(loginUser.getUserId());
        }
        if (dto.getClassId() != null) {
            ClassInfo classInfo = classInfoMapper.selectById(dto.getClassId());
            if (classInfo == null) {
                throw new BusinessException("班级不存在");
            }
        }
        childMapper.insert(child);
        if (dto.getClassId() != null) {
            classInfoMapper.updateStudentCount(dto.getClassId(), 1);
        }
    }
    
    @Override
    public void updateChild(ChildDTO dto) {
        Child existChild = childMapper.selectById(dto.getId());
        if (existChild == null) {
            throw new BusinessException("儿童信息不存在");
        }
        Child child = new Child();
        BeanUtils.copyProperties(dto, child);
        childMapper.updateById(child);
    }
    
    @Override
    public void deleteChild(Long id) {
        childMapper.deleteById(id);
    }
}
