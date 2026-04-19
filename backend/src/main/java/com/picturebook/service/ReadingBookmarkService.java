package com.picturebook.service;

import com.picturebook.entity.ReadingBookmark;
import java.util.List;
import java.util.Map;

public interface ReadingBookmarkService {

    List<ReadingBookmark> getBookmarksByChildId(Long childId);

    void addBookmark(ReadingBookmark bookmark);

    void removeBookmark(Long childId, Long bookId);

    boolean isBookmarked(Long childId, Long bookId);

    List<Map<String, Object>> getBookmarkStats(Long childId);

    void updateBookmarkType(Long childId, Long bookId, Integer bookmarkType);
}
