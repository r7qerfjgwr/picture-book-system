package com.picturebook.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.picturebook.dto.PageDTO;
import com.picturebook.entity.SysUser;
import com.picturebook.service.UserService;
import com.picturebook.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "用户增删改查接口")
@RestController
@RequestMapping("/user")
@SecurityRequirement(name = "Authorization")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Operation(summary = "获取用户列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Page<SysUser>> getUserList(PageDTO dto) {
        return Result.success(userService.getUserList(dto));
    }
    
    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<SysUser> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }
    
    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        userService.updateUser(user);
        return Result.success();
    }
    
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
    
    @Operation(summary = "重置密码")
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }
}
