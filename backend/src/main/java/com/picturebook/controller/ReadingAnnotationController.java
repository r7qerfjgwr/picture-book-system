package com.picturebook.controller;

import com.picturebook.entity.ReadingAnnotation;
import com.picturebook.service.ReadingAnnotationService;
import com.picturebook.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "阅读批注管理", description = "绘本批注相关接口")
@RestController
@RequestMapping("/annotation")
@SecurityRequirement(name = "Authorization")
public class ReadingAnnotationController {

    @Autowired
    private ReadingAnnotationService annotationService;

    @Operation(summary = "获取儿童批注列表")
    @GetMapping("/child/{childId}")
    public Result<List<ReadingAnnotation>> getAnnotationsByChildId(@PathVariable Long childId) {
        return Result.success(annotationService.getAnnotationsByChildId(childId));
    }

    @Operation(summary = "获取儿童对某绘本的批注列表")
    @GetMapping("/child/{childId}/book/{bookId}")
    public Result<List<ReadingAnnotation>> getAnnotationsByChildIdAndBookId(
            @PathVariable Long childId,
            @PathVariable Long bookId) {
        return Result.success(annotationService.getAnnotationsByChildIdAndBookId(childId, bookId));
    }

    @Operation(summary = "添加批注")
    @PostMapping
    public Result<Void> addAnnotation(@RequestBody ReadingAnnotation annotation) {
        annotationService.addAnnotation(annotation);
        return Result.success();
    }

    @Operation(summary = "更新批注")
    @PutMapping
    public Result<Void> updateAnnotation(@RequestBody ReadingAnnotation annotation) {
        annotationService.updateAnnotation(annotation);
        return Result.success();
    }

    @Operation(summary = "删除批注")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnotation(@PathVariable Long id) {
        annotationService.deleteAnnotation(id);
        return Result.success();
    }

    @Operation(summary = "获取批注详情")
    @GetMapping("/{id}")
    public Result<ReadingAnnotation> getAnnotationById(@PathVariable Long id) {
        return Result.success(annotationService.getAnnotationById(id));
    }
}
