-- 更新book表的read_count统计
UPDATE book SET read_count = (SELECT COUNT(*) FROM reading_log WHERE reading_log.book_id = book.id);

-- 更新class_info的student_count
UPDATE class_info SET student_count = (SELECT COUNT(*) FROM child WHERE child.class_id = class_info.id);
