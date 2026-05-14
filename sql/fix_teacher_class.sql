SET NAMES utf8mb4;

-- 分配小班教师
UPDATE class_info SET teacher_id = 2 WHERE id = 1;
UPDATE class_info SET teacher_id = 3 WHERE id = 2;
UPDATE class_info SET teacher_id = 4 WHERE id = 3;
UPDATE class_info SET teacher_id = 5 WHERE id = 4;
UPDATE class_info SET teacher_id = 6 WHERE id = 5;
UPDATE class_info SET teacher_id = 7 WHERE id = 6;
UPDATE class_info SET teacher_id = 8 WHERE id = 7;

-- 新增14位教师
INSERT INTO sys_user (id, username, password, real_name, phone, email, role_id, status) VALUES
(9,  'teacher8',  '$2a$10$5YzbWEIrRFTKlCu.wChMZOSAh.MiL5O4UDquTGM9dKut8UHluciuS', '周秀英', '13800138009', 'zhouxy@kindergarten.com', 2, 1),
(10, 'teacher9',  '$2a$10$5YzbWEIrRFTKlCu.wChMZOSAh.MiL5O4UDquTGM9dKut8UHluciuS', '吴国强', '13800138010', 'wugq@kindergarten.com', 2, 1),
(11, 'teacher10', '$2a$10$5YzbWEIrRFTKlCu.wChMZOSAh.MiL5O4UDquTGM9dKut8UHluciuS', '郑慧芳', '13800138011', 'zhenghf@kindergarten.com', 2, 1),
(12, 'teacher11', '$2a$10$5YzbWEIrRFTKlCu.wChMZOSAh.MiL5O4UDquTGM9dKut8UHluciuS', '马丽华', '13800138012', 'malh@kindergarten.com', 2, 1),
(13, 'teacher12', '$2a$10$5YzbWEIrRFTKlCu.wChMZOSAh.MiL5O4UDquTGM9dKut8UHluciuS', '胡建军', '13800138013', 'hujj@kindergarten.com', 2, 1),
(14, 'teacher13', '$2a$10$5YzbWEIrRFTKlCu.wChMZOSAh.MiL5O4UDquTGM9dKut8UHluciuS', '林美珍', '13800138014', 'linmz@kindergarten.com', 2, 1),
(15, 'teacher14', '$2a$10$5YzbWEIrRFTKlCu.wChMZOSAh.MiL5O4UDquTGM9dKut8UHluciuS', '何志明', '13800138015', 'hezm@kindergarten.com', 2, 1),
(16, 'teacher15', '$2a$10$5YzbWEIrRFTKlCu.wChMZOSAh.MiL5O4UDquTGM9dKut8UHluciuS', '高秀兰', '13800138016', 'gaoxl@kindergarten.com', 2, 1),
(17, 'teacher16', '$2a$10$5YzbWEIrRFTKlCu.wChMZOSAh.MiL5O4UDquTGM9dKut8UHluciuS', '罗晓燕', '13800138017', 'luoxy@kindergarten.com', 2, 1),
(18, 'teacher17', '$2a$10$5YzbWEIrRFTKlCu.wChMZOSAh.MiL5O4UDquTGM9dKut8UHluciuS', '谢国华', '13800138018', 'xiegh@kindergarten.com', 2, 1),
(19, 'teacher18', '$2a$10$5YzbWEIrRFTKlCu.wChMZOSAh.MiL5O4UDquTGM9dKut8UHluciuS', '宋丽萍', '13800138019', 'songlp@kindergarten.com', 2, 1),
(20, 'teacher19', '$2a$10$5YzbWEIrRFTKlCu.wChMZOSAh.MiL5O4UDquTGM9dKut8UHluciuS', '唐伟民', '13800138020', 'tangwm@kindergarten.com', 2, 1),
(21, 'teacher20', '$2a$10$5YzbWEIrRFTKlCu.wChMZOSAh.MiL5O4UDquTGM9dKut8UHluciuS', '韩雪梅', '13800138021', 'hanxm@kindergarten.com', 2, 1),
(22, 'teacher21', '$2a$10$5YzbWEIrRFTKlCu.wChMZOSAh.MiL5O4UDquTGM9dKut8UHluciuS', '冯丽娟', '13800138022', 'fenglj@kindergarten.com', 2, 1);

-- 分配中班教师
UPDATE class_info SET teacher_id = 9  WHERE id = 8;
UPDATE class_info SET teacher_id = 10 WHERE id = 9;
UPDATE class_info SET teacher_id = 11 WHERE id = 10;
UPDATE class_info SET teacher_id = 12 WHERE id = 11;
UPDATE class_info SET teacher_id = 13 WHERE id = 12;
UPDATE class_info SET teacher_id = 14 WHERE id = 13;
UPDATE class_info SET teacher_id = 15 WHERE id = 14;

-- 分配大班教师
UPDATE class_info SET teacher_id = 16 WHERE id = 15;
UPDATE class_info SET teacher_id = 17 WHERE id = 16;
UPDATE class_info SET teacher_id = 18 WHERE id = 17;
UPDATE class_info SET teacher_id = 19 WHERE id = 18;
UPDATE class_info SET teacher_id = 20 WHERE id = 19;
UPDATE class_info SET teacher_id = 21 WHERE id = 20;
UPDATE class_info SET teacher_id = 22 WHERE id = 21;
