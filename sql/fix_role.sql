SET NAMES utf8mb4;

-- 插入角色数据
INSERT INTO sys_role (id, role_name, role_key, description) VALUES
(1, '系统管理员', 'ADMIN', '系统管理员，拥有所有权限'),
(2, '教师', 'TEACHER', '教师角色，管理班级儿童'),
(3, '家长', 'PARENT', '家长角色，查看孩子信息');

-- 更新admin用户的角色
UPDATE sys_user SET role_id = 1 WHERE username = 'admin';
