package com.picturebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.picturebook.dto.PageDTO;
import com.picturebook.entity.Child;
import com.picturebook.entity.ClassInfo;
import com.picturebook.entity.ReadingLog;
import com.picturebook.exception.BusinessException;
import com.picturebook.mapper.ChildMapper;
import com.picturebook.mapper.ClassInfoMapper;
import com.picturebook.mapper.ReadingLogMapper;
import com.picturebook.service.ClassInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;

@Service
public class ClassInfoServiceImpl implements ClassInfoService {

    @Autowired
    private ClassInfoMapper classInfoMapper;

    @Autowired
    private ChildMapper childMapper;

    @Autowired
    private ReadingLogMapper readingLogMapper;

    @Override
    public Page<ClassInfo> getClassList(PageDTO dto) {
        Page<ClassInfo> page = new Page<>(dto.getCurrent(), dto.getSize());
        LambdaQueryWrapper<ClassInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.like(ClassInfo::getClassName, dto.getKeyword());
        }
        wrapper.orderByDesc(ClassInfo::getCreateTime);
        return classInfoMapper.selectPage(page, wrapper);
    }

    @Override
    public ClassInfo getClassById(Long id) {
        ClassInfo classInfo = classInfoMapper.selectById(id);
        if (classInfo == null) {
            throw new BusinessException("班级不存在");
        }
        return classInfo;
    }

    @Override
    public List<ClassInfo> getAllClasses() {
        return classInfoMapper.selectList(new LambdaQueryWrapper<ClassInfo>().orderByDesc(ClassInfo::getCreateTime));
    }

    @Override
    public List<ClassInfo> getClassesByTeacherId(Long teacherId) {
        return classInfoMapper.selectList(
            new LambdaQueryWrapper<ClassInfo>()
                .eq(ClassInfo::getTeacherId, teacherId)
                .orderByDesc(ClassInfo::getCreateTime)
        );
    }

    @Override
    public void addClass(ClassInfo classInfo) {
        if (classInfo.getStudentCount() == null) {
            classInfo.setStudentCount(0);
        }
        classInfoMapper.insert(classInfo);
    }

    @Override
    public void updateClass(ClassInfo classInfo) {
        ClassInfo exist = classInfoMapper.selectById(classInfo.getId());
        if (exist == null) {
            throw new BusinessException("班级不存在");
        }
        classInfoMapper.updateById(classInfo);
    }

    @Override
    public void deleteClass(Long id) {
        classInfoMapper.deleteById(id);
    }

    @Override
    public List<Child> getChildrenByClassId(Long classId) {
        List<Child> children = childMapper.selectList(
            new LambdaQueryWrapper<Child>().eq(Child::getClassId, classId)
        );

        // 为每个儿童统计阅读次数
        for (Child child : children) {
            Long readCount = readingLogMapper.selectCount(
                new LambdaQueryWrapper<ReadingLog>()
                    .eq(ReadingLog::getChildId, child.getId())
            );
            child.setReadCount(readCount != null ? readCount.intValue() : 0);
        }

        return children;
    }
}
