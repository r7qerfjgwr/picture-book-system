package com.picturebook.dto;

import lombok.Data;

@Data
public class NotificationDTO {
    // 接收者类型：all-所有用户, role-指定角色, user-指定用户
    private String receiverType;

    // 角色key（当receiverType为role时使用）：ADMIN, TEACHER, PARENT
    private String roleKey;

    // 用户ID（当receiverType为user时使用）
    private Long userId;

    // 通知标题
    private String title;

    // 通知内容
    private String content;

    // 通知类型：system-系统通知, activity-活动通知, reminder-提醒
    private String type;

    // 关联的儿童ID（可选）
    private Long childId;
}
