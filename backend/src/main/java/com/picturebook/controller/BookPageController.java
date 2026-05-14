package com.picturebook.controller;

import com.picturebook.vo.Result;
import com.picturebook.entity.BookPage;
import com.picturebook.service.BookPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/book-pages")
@RequiredArgsConstructor
public class BookPageController {

    private final BookPageService bookPageService;

    @GetMapping("/{bookId}")
    public Result<List<BookPage>> getPagesByBookId(@PathVariable Long bookId) {
        return Result.success(bookPageService.getPagesByBookId(bookId));
    }
}
