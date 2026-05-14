package com.picturebook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.picturebook.entity.BookPage;
import java.util.List;

public interface BookPageService extends IService<BookPage> {

    List<BookPage> getPagesByBookId(Long bookId);
}
