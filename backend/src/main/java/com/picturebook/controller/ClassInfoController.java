package com.picturebook.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.picturebook.dto.PageDTO;
import com.picturebook.entity.Child;
import com.picturebook.entity.ClassInfo;
import com.picturebook.security.LoginUser;
import com.picturebook.service.ClassInfoService;
import com.picturebook.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "班级管理", description = "班级增删改查接口")
@RestController
@RequestMapping("/class")
@SecurityRequirement(name = "Authorization")
public class ClassInfoController {

    @Autowired
    private ClassInfoService classInfoService;

    @Operation(summary = "获取班级列表")
    @GetMapping("/list")
    public Result<Page<ClassInfo>> getClassList(PageDTO dto) {
        return Result.success(classInfoService.getClassList(dto));
    }

    @Operation(summary = "获取所有班级")
    @GetMapping("/all")
    public Result<List<ClassInfo>> getAllClasses() {
        return Result.success(classInfoService.getAllClasses());
    }

    @Operation(summary = "获取当前教师的班级")
    @GetMapping("/my-classes")
    public Result<List<ClassInfo>> getMyClasses() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return Result.success(classInfoService.getClassesByTeacherId(loginUser.getUserId()));
    }

    @Operation(summary = "获取班级详情")
    @GetMapping("/{id}")
    public Result<ClassInfo> getClassById(@PathVariable Long id) {
        return Result.success(classInfoService.getClassById(id));
    }

    @Operation(summary = "新增班级")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    public Result<Void> addClass(@RequestBody ClassInfo classInfo) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        // 如果是教师创建班级，自动关联到该教师
        if ("TEACHER".equals(loginUser.getRoleKey()) && classInfo.getTeacherId() == null) {
            classInfo.setTeacherId(loginUser.getUserId());
        }
        classInfoService.addClass(classInfo);
        return Result.success();
    }

    @Operation(summary = "更新班级")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    public Result<Void> updateClass(@PathVariable Long id, @RequestBody ClassInfo classInfo) {
        classInfo.setId(id);
        classInfoService.updateClass(classInfo);
        return Result.success();
    }

    @Operation(summary = "删除班级")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> deleteClass(@PathVariable Long id) {
        classInfoService.deleteClass(id);
        return Result.success();
    }

    @Operation(summary = "获取班级儿童列表")
    @GetMapping("/{id}/children")
    public Result<List<Child>> getClassChildren(@PathVariable Long id) {
        return Result.success(classInfoService.getChildrenByClassId(id));
    }
}
