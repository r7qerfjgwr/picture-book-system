package com.picturebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.picturebook.entity.BookPage;
import com.picturebook.mapper.BookPageMapper;
import com.picturebook.service.BookPageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookPageServiceImpl extends ServiceImpl<BookPageMapper, BookPage> implements BookPageService {

    @Override
    public List<BookPage> getPagesByBookId(Long bookId) {
        return list(new LambdaQueryWrapper<BookPage>()
                .eq(BookPage::getBookId, bookId)
                .orderByAsc(BookPage::getPageNum));
    }
}
