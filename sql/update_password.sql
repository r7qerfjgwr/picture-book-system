-- 更新用户密码为 123456 的正确 BCrypt 哈希值
-- 并重置登录失败次数
UPDATE sys_user 
SET password = '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
    login_fail_count = 0;
