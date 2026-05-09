package com.picturebook.service;

import com.picturebook.dto.NotificationDTO;
import com.picturebook.entity.Notification;
import java.util.List;
import java.util.Map;

public interface NotificationService {

    /**
     * 获取用户的通知列表
     */
    List<Notification> getUserNotifications(Long userId, Integer limit);

    /**
     * 获取未读通知数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 标记所有通知为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 创建通知
     */
    void createNotification(Long userId, String title, String content, String type, Long childId);

    /**
     * 发送成长里程碑通知
     */
    void sendMilestoneNotification(Long parentId, Long childId, String milestone);

    /**
     * 发送报告生成通知
     */
    void sendReportNotification(Long parentId, Long childId, String reportType);

    /**
     * 管理员发送通知
     * @param dto 通知参数
     * @return 发送成功数量
     */
    int sendNotificationByAdmin(NotificationDTO dto);

    /**
     * 获取所有用户列表（用于管理员选择接收者）
     */
    List<Map<String, Object>> getAllUsersForNotification();
}
