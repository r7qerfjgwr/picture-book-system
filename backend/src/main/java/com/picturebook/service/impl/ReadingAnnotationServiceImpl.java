package com.picturebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.picturebook.entity.ReadingAnnotation;
import com.picturebook.mapper.ReadingAnnotationMapper;
import com.picturebook.service.ReadingAnnotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReadingAnnotationServiceImpl implements ReadingAnnotationService {

    @Autowired
    private ReadingAnnotationMapper annotationMapper;

    @Override
    public List<ReadingAnnotation> getAnnotationsByChildId(Long childId) {
        return annotationMapper.selectByChildId(childId);
    }

    @Override
    public List<ReadingAnnotation> getAnnotationsByChildIdAndBookId(Long childId, Long bookId) {
        return annotationMapper.selectByChildIdAndBookId(childId, bookId);
    }

    @Override
    public void addAnnotation(ReadingAnnotation annotation) {
        annotationMapper.insert(annotation);
    }

    @Override
    public void updateAnnotation(ReadingAnnotation annotation) {
        annotationMapper.updateById(annotation);
    }

    @Override
    public void deleteAnnotation(Long id) {
        annotationMapper.deleteById(id);
    }

    @Override
    public ReadingAnnotation getAnnotationById(Long id) {
        return annotationMapper.selectById(id);
    }
}
