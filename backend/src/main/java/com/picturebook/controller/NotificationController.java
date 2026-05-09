package com.picturebook.controller;

import com.picturebook.dto.NotificationDTO;
import com.picturebook.entity.Notification;
import com.picturebook.security.LoginUser;
import com.picturebook.service.NotificationService;
import com.picturebook.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "通知管理", description = "通知相关接口")
@RestController
@RequestMapping("/notification")
@SecurityRequirement(name = "Authorization")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "获取通知列表")
    @GetMapping("/list")
    public Result<List<Notification>> getNotifications(
            @RequestParam(defaultValue = "20") Integer limit) {
        Long userId = getCurrentUserId();
        return Result.success(notificationService.getUserNotifications(userId, limit));
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> getUnreadCount() {
        Long userId = getCurrentUserId();
        Map<String, Object> data = new HashMap<>();
        data.put("count", notificationService.getUnreadCount(userId));
        return Result.success(data);
    }

    @Operation(summary = "标记通知为已读")
    @PostMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    @Operation(summary = "标记所有通知为已读")
    @PostMapping("/read-all")
    public Result<Void> markAllAsRead() {
        Long userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    // ========== 管理员接口 ==========

    @Operation(summary = "管理员发送通知")
    @PostMapping("/admin/send")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Map<String, Object>> sendNotification(@RequestBody NotificationDTO dto) {
        int count = notificationService.sendNotificationByAdmin(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("sentCount", count);
        return Result.success(data);
    }

    @Operation(summary = "获取所有用户列表（用于选择通知接收者）")
    @GetMapping("/admin/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<List<Map<String, Object>>> getAllUsersForNotification() {
        return Result.success(notificationService.getAllUsersForNotification());
    }

    private Long getCurrentUserId() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return loginUser.getUserId();
    }
}
