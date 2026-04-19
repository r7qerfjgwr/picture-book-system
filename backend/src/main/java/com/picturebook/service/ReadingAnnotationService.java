package com.picturebook.service;

import com.picturebook.entity.ReadingAnnotation;
import java.util.List;

public interface ReadingAnnotationService {

    List<ReadingAnnotation> getAnnotationsByChildId(Long childId);

    List<ReadingAnnotation> getAnnotationsByChildIdAndBookId(Long childId, Long bookId);

    void addAnnotation(ReadingAnnotation annotation);

    void updateAnnotation(ReadingAnnotation annotation);

    void deleteAnnotation(Long id);

    ReadingAnnotation getAnnotationById(Long id);
}
