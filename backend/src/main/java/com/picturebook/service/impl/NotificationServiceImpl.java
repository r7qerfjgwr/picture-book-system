package com.picturebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.picturebook.dto.NotificationDTO;
import com.picturebook.entity.Child;
import com.picturebook.entity.Notification;
import com.picturebook.entity.SysUser;
import com.picturebook.mapper.ChildMapper;
import com.picturebook.mapper.NotificationMapper;
import com.picturebook.mapper.SysUserMapper;
import com.picturebook.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private ChildMapper childMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public List<Notification> getUserNotifications(Long userId, Integer limit) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .orderByDesc(Notification::getCreateTime);

        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }

        List<Notification> notifications = notificationMapper.selectList(wrapper);

        // 填充儿童名称
        for (Notification notification : notifications) {
            if (notification.getChildId() != null) {
                Child child = childMapper.selectById(notification.getChildId());
                if (child != null) {
                    notification.setChildName(child.getName());
                }
            }
        }

        return notifications;
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return notificationMapper.selectCount(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
        );
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        notificationMapper.update(null,
            new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getId, notificationId)
                .eq(Notification::getUserId, userId)
                .set(Notification::getIsRead, 1)
        );
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationMapper.update(null,
            new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1)
        );
    }

    @Override
    public void createNotification(Long userId, String title, String content, String type, Long childId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setChildId(childId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    @Override
    public void sendMilestoneNotification(Long parentId, Long childId, String milestone) {
        Child child = childMapper.selectById(childId);
        String childName = child != null ? child.getName() : "孩子";

        createNotification(
            parentId,
            "🎉 成长里程碑达成",
            childName + milestone,
            "milestone",
            childId
        );
    }

    @Override
    public void sendReportNotification(Long parentId, Long childId, String reportType) {
        Child child = childMapper.selectById(childId);
        String childName = child != null ? child.getName() : "孩子";
        String reportName = "weekly".equals(reportType) ? "周报" : "月报";

        createNotification(
            parentId,
            "📊 成长报告已生成",
            childName + "的" + reportName + "已生成，快来查看吧！",
            "report",
            childId
        );
    }

    @Override
    public int sendNotificationByAdmin(NotificationDTO dto) {
        List<Long> userIds = new ArrayList<>();

        if ("all".equals(dto.getReceiverType())) {
            // 发送给所有用户
            List<SysUser> allUsers = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1)
            );
            userIds = allUsers.stream().map(SysUser::getId).toList();
        } else if ("role".equals(dto.getReceiverType())) {
            // 发送给指定角色的用户
            List<SysUser> roleUsers = sysUserMapper.selectUsersByRoleKey(dto.getRoleKey());
            userIds = roleUsers.stream().map(SysUser::getId).toList();
        } else if ("user".equals(dto.getReceiverType()) && dto.getUserId() != null) {
            // 发送给指定用户
            userIds.add(dto.getUserId());
        }

        // 批量创建通知
        int count = 0;
        for (Long userId : userIds) {
            createNotification(
                userId,
                dto.getTitle(),
                dto.getContent(),
                dto.getType() != null ? dto.getType() : "system",
                dto.getChildId()
            );
            count++;
        }

        return count;
    }

    @Override
    public List<Map<String, Object>> getAllUsersForNotification() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<SysUser> users = sysUserMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStatus, 1)
                .orderByAsc(SysUser::getRoleId)
        );

        for (SysUser user : users) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("realName", user.getRealName());
            userMap.put("roleKey", getRoleKeyById(user.getRoleId()));
            result.add(userMap);
        }

        return result;
    }

    private String getRoleKeyById(Long roleId) {
        if (roleId == null) return "USER";
        if (roleId == 1) return "ADMIN";
        if (roleId == 2) return "TEACHER";
        if (roleId == 3) return "PARENT";
        return "USER";
    }
}
