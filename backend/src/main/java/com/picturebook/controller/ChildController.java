package com.picturebook.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.picturebook.dto.ChildDTO;
import com.picturebook.dto.PageDTO;
import com.picturebook.entity.Child;
import com.picturebook.security.LoginUser;
import com.picturebook.service.ChildService;
import com.picturebook.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "儿童管理", description = "儿童信息增删改查接口")
@RestController
@RequestMapping("/child")
@SecurityRequirement(name = "Authorization")
public class ChildController {
    
    @Autowired
    private ChildService childService;
    
    @Operation(summary = "获取我的孩子列表")
    @GetMapping("/my-children")
    public Result<List<Child>> getMyChildren() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String roleKey = loginUser.getRoleKey();
        Long userId = loginUser.getUserId();

        if ("PARENT".equals(roleKey)) {
            // 家长：查询自己绑定的孩子
            return Result.success(childService.getChildrenByParentId(userId));
        } else if ("TEACHER".equals(roleKey)) {
            // 教师：查询自己管理班级的孩子
            return Result.success(childService.getChildrenByTeacherId(userId));
        } else {
            // 管理员：查询所有孩子
            return Result.success(childService.getAllChildren());
        }
    }
    
    @Operation(summary = "获取班级儿童列表")
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    public Result<List<Child>> getChildrenByClassId(@PathVariable Long classId) {
        return Result.success(childService.getChildrenByClassId(classId));
    }
    
    @Operation(summary = "获取儿童详情")
    @GetMapping("/{id}")
    public Result<Child> getChildById(@PathVariable Long id) {
        return Result.success(childService.getChildById(id));
    }
    
    @Operation(summary = "获取儿童列表（分页）")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Page<Child>> getChildList(PageDTO dto) {
        return Result.success(childService.getChildList(dto));
    }
    
    @Operation(summary = "绑定孩子")
    @PostMapping("/bind")
    @PreAuthorize("hasAuthority('PARENT')")
    public Result<Void> bindChild(@Valid @RequestBody ChildDTO dto) {
        childService.bindChild(dto);
        return Result.success();
    }
    
    @Operation(summary = "更新儿童信息")
    @PutMapping("/{id}")
    public Result<Void> updateChild(@PathVariable Long id, @Valid @RequestBody ChildDTO dto) {
        dto.setId(id);
        childService.updateChild(dto);
        return Result.success();
    }
    
    @Operation(summary = "删除儿童")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> deleteChild(@PathVariable Long id) {
        childService.deleteChild(id);
        return Result.success();
    }
    
    private Long getCurrentUserId() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return loginUser.getUserId();
    }
}
