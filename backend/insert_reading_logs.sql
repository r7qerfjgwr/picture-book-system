SET NAMES utf8mb4;

-- 张狼(child_id = 32)的真实阅读记录
-- 模拟一个5岁男孩过去30天的阅读习惯

-- 4月15日（今天）- 傍晚阅读
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 1, '2026-04-15 18:30:00', '2026-04-15 18:47:00', 1020, 24, 85.6, 1, 100.00, 42.50, NOW()),
(32, 13, '2026-04-15 19:15:00', '2026-04-15 19:32:00', 1020, 22, 78.3, 1, 95.00, 46.36, NOW());

-- 4月14日 - 睡前阅读
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 86, '2026-04-14 20:30:00', '2026-04-14 20:58:00', 1680, 32, 72.1, 1, 100.00, 52.50, NOW()),
(32, 27, '2026-04-14 21:05:00', '2026-04-14 21:22:00', 1020, 18, 68.5, 0, 75.00, 56.67, NOW());

-- 4月13日 - 周末阅读
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 53, '2026-04-13 10:00:00', '2026-04-13 10:18:00', 1080, 20, 91.2, 1, 100.00, 54.00, NOW()),
(32, 74, '2026-04-13 14:30:00', '2026-04-13 14:48:00', 1080, 22, 88.7, 1, 100.00, 49.09, NOW()),
(32, 33, '2026-04-13 16:00:00', '2026-04-13 16:25:00', 1500, 28, 82.4, 1, 100.00, 53.57, NOW());

-- 4月12日 - 放学后阅读
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 97, '2026-04-12 17:00:00', '2026-04-12 17:22:00', 1320, 24, 79.8, 1, 100.00, 55.00, NOW()),
(32, 96, '2026-04-12 20:00:00', '2026-04-12 20:12:00', 720, 18, 85.0, 1, 100.00, 40.00, NOW());

-- 4月11日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 34, '2026-04-11 19:30:00', '2026-04-11 19:52:00', 1320, 26, 76.3, 1, 100.00, 50.77, NOW()),
(32, 99, '2026-04-11 20:15:00', '2026-04-11 20:28:00', 780, 18, 72.8, 1, 85.00, 43.33, NOW());

-- 4月10日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 100, '2026-04-10 18:45:00', '2026-04-10 19:10:00', 1500, 30, 83.5, 1, 100.00, 50.00, NOW()),
(32, 8, '2026-04-10 20:30:00', '2026-04-10 20:42:00', 720, 16, 88.2, 1, 100.00, 45.00, NOW());

-- 4月9日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 15, '2026-04-09 19:00:00', '2026-04-09 19:18:00', 1080, 20, 79.1, 1, 100.00, 54.00, NOW()),
(32, 22, '2026-04-09 20:00:00', '2026-04-09 20:15:00', 900, 18, 74.6, 1, 90.00, 50.00, NOW());

-- 4月8日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 44, '2026-04-08 18:30:00', '2026-04-08 18:50:00', 1200, 24, 81.2, 1, 100.00, 50.00, NOW()),
(32, 56, '2026-04-08 20:00:00', '2026-04-08 20:18:00', 1080, 22, 77.8, 1, 100.00, 49.09, NOW());

-- 4月7日 - 周末多读
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 17, '2026-04-07 09:30:00', '2026-04-07 09:52:00', 1320, 26, 84.3, 1, 100.00, 50.77, NOW()),
(32, 82, '2026-04-07 14:00:00', '2026-04-07 14:22:00', 1320, 24, 80.1, 1, 100.00, 55.00, NOW()),
(32, 3, '2026-04-07 16:30:00', '2026-04-07 16:45:00', 900, 18, 75.5, 1, 95.00, 50.00, NOW());

-- 4月6日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 7, '2026-04-06 19:00:00', '2026-04-06 19:28:00', 1680, 32, 78.9, 1, 100.00, 52.50, NOW()),
(32, 11, '2026-04-06 20:30:00', '2026-04-06 20:42:00', 720, 16, 82.4, 1, 100.00, 45.00, NOW());

-- 4月5日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 18, '2026-04-05 18:00:00', '2026-04-05 18:25:00', 1500, 28, 76.7, 1, 100.00, 53.57, NOW()),
(32, 30, '2026-04-05 20:00:00', '2026-04-05 20:15:00', 900, 18, 71.2, 1, 90.00, 50.00, NOW());

-- 4月4日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 53, '2026-04-04 19:30:00', '2026-04-04 19:48:00', 1080, 20, 80.5, 1, 100.00, 54.00, NOW());

-- 4月3日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 86, '2026-04-03 18:30:00', '2026-04-03 18:55:00', 1500, 30, 79.2, 1, 100.00, 50.00, NOW()),
(32, 27, '2026-04-03 20:00:00', '2026-04-03 20:18:00', 1080, 20, 73.8, 1, 95.00, 54.00, NOW());

-- 4月2日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 33, '2026-04-02 19:00:00', '2026-04-02 19:25:00', 1500, 28, 81.6, 1, 100.00, 53.57, NOW());

-- 4月1日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 97, '2026-04-01 18:00:00', '2026-04-01 18:22:00', 1320, 24, 78.4, 1, 100.00, 55.00, NOW()),
(32, 74, '2026-04-01 20:00:00', '2026-04-01 20:18:00', 1080, 22, 75.9, 1, 100.00, 49.09, NOW());

-- 3月31日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 96, '2026-03-31 19:30:00', '2026-03-31 19:42:00', 720, 18, 83.1, 1, 100.00, 40.00, NOW());

-- 3月30日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 99, '2026-03-30 18:30:00', '2026-03-30 18:48:00', 1080, 22, 76.5, 1, 100.00, 49.09, NOW()),
(32, 100, '2026-03-30 20:00:00', '2026-03-30 20:25:00', 1500, 30, 80.8, 1, 100.00, 50.00, NOW());

-- 3月29日 周末
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 8, '2026-03-29 10:00:00', '2026-03-29 10:15:00', 900, 18, 85.3, 1, 100.00, 50.00, NOW()),
(32, 15, '2026-03-29 14:30:00', '2026-03-29 14:52:00', 1320, 24, 79.7, 1, 100.00, 55.00, NOW());

-- 3月28日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 44, '2026-03-28 19:00:00', '2026-03-28 19:22:00', 1320, 24, 77.4, 1, 100.00, 55.00, NOW());

-- 3月27日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 1, '2026-03-27 18:30:00', '2026-03-27 18:50:00', 1200, 24, 82.8, 1, 100.00, 50.00, NOW()),
(32, 13, '2026-03-27 20:00:00', '2026-03-27 20:18:00', 1080, 22, 79.1, 1, 100.00, 49.09, NOW());

-- 3月26日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 34, '2026-03-26 19:30:00', '2026-03-26 19:55:00', 1500, 28, 75.3, 1, 100.00, 53.57, NOW());

-- 3月25日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 53, '2026-03-25 18:00:00', '2026-03-25 18:22:00', 1320, 24, 78.6, 1, 100.00, 55.00, NOW());

-- 3月24日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 86, '2026-03-24 19:00:00', '2026-03-24 19:28:00', 1680, 32, 74.9, 1, 100.00, 52.50, NOW());

-- 3月23日 周末
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 27, '2026-03-23 10:30:00', '2026-03-23 10:52:00', 1320, 26, 71.8, 1, 100.00, 50.77, NOW()),
(32, 33, '2026-03-23 15:00:00', '2026-03-23 15:28:00', 1680, 32, 76.2, 1, 100.00, 52.50, NOW());

-- 3月22日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 97, '2026-03-22 18:30:00', '2026-03-22 18:52:00', 1320, 24, 80.2, 1, 100.00, 55.00, NOW());

-- 3月21日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 74, '2026-03-21 19:00:00', '2026-03-21 19:20:00', 1200, 22, 77.6, 1, 100.00, 54.55, NOW()),
(32, 96, '2026-03-21 20:15:00', '2026-03-21 20:30:00', 900, 18, 81.3, 1, 100.00, 50.00, NOW());

-- 3月20日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 99, '2026-03-20 18:00:00', '2026-03-20 18:18:00', 1080, 22, 73.4, 1, 100.00, 49.09, NOW());

-- 3月19日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 100, '2026-03-19 19:30:00', '2026-03-19 19:55:00', 1500, 30, 79.5, 1, 100.00, 50.00, NOW());

-- 3月18日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 8, '2026-03-18 18:30:00', '2026-03-18 18:45:00', 900, 18, 84.7, 1, 100.00, 50.00, NOW()),
(32, 15, '2026-03-18 20:00:00', '2026-03-18 20:22:00', 1320, 24, 78.1, 1, 100.00, 55.00, NOW());

-- 3月17日
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 22, '2026-03-17 19:00:00', '2026-03-17 19:18:00', 1080, 20, 75.8, 1, 100.00, 54.00, NOW());

-- 3月16日 周末
INSERT INTO reading_log (child_id, book_id, start_time, end_time, duration, page_turn_count, focus_score, is_completed, completion_rate, avg_page_stay_time, create_time) VALUES
(32, 44, '2026-03-16 09:30:00', '2026-03-16 09:55:00', 1500, 28, 80.9, 1, 100.00, 53.57, NOW()),
(32, 56, '2026-03-16 14:00:00', '2026-03-16 14:22:00', 1320, 24, 76.4, 1, 100.00, 55.00, NOW()),
(32, 17, '2026-03-16 16:30:00', '2026-03-16 16:52:00', 1320, 26, 82.1, 1, 100.00, 50.77, NOW());

SELECT '插入完成' as result;
