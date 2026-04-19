package com.picturebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.picturebook.entity.ReadingBookmark;
import com.picturebook.mapper.ReadingBookmarkMapper;
import com.picturebook.service.ReadingBookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class ReadingBookmarkServiceImpl implements ReadingBookmarkService {

    @Autowired
    private ReadingBookmarkMapper bookmarkMapper;

    @Override
    public List<ReadingBookmark> getBookmarksByChildId(Long childId) {
        return bookmarkMapper.selectByChildId(childId);
    }

    @Override
    public void addBookmark(ReadingBookmark bookmark) {
        // 检查是否已收藏
        LambdaQueryWrapper<ReadingBookmark> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReadingBookmark::getChildId, bookmark.getChildId())
               .eq(ReadingBookmark::getBookId, bookmark.getBookId());
        ReadingBookmark existing = bookmarkMapper.selectOne(wrapper);
        if (existing != null) {
            // 更新收藏类型
            existing.setBookmarkType(bookmark.getBookmarkType());
            existing.setNote(bookmark.getNote());
            bookmarkMapper.updateById(existing);
        } else {
            bookmarkMapper.insert(bookmark);
        }
    }

    @Override
    public void removeBookmark(Long childId, Long bookId) {
        LambdaQueryWrapper<ReadingBookmark> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReadingBookmark::getChildId, childId)
               .eq(ReadingBookmark::getBookId, bookId);
        bookmarkMapper.delete(wrapper);
    }

    @Override
    public boolean isBookmarked(Long childId, Long bookId) {
        LambdaQueryWrapper<ReadingBookmark> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReadingBookmark::getChildId, childId)
               .eq(ReadingBookmark::getBookId, bookId);
        return bookmarkMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<Map<String, Object>> getBookmarkStats(Long childId) {
        return bookmarkMapper.selectCategoryStatsByChildId(childId);
    }

    @Override
    public void updateBookmarkType(Long childId, Long bookId, Integer bookmarkType) {
        LambdaQueryWrapper<ReadingBookmark> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReadingBookmark::getChildId, childId)
               .eq(ReadingBookmark::getBookId, bookId);
        ReadingBookmark bookmark = bookmarkMapper.selectOne(wrapper);
        if (bookmark != null) {
            bookmark.setBookmarkType(bookmarkType);
            bookmarkMapper.updateById(bookmark);
        }
    }
}
