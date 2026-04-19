package com.picturebook.controller;

import com.picturebook.entity.ReadingBookmark;
import com.picturebook.service.ReadingBookmarkService;
import com.picturebook.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Tag(name = "阅读收藏管理", description = "绘本收藏相关接口")
@RestController
@RequestMapping("/bookmark")
@SecurityRequirement(name = "Authorization")
public class ReadingBookmarkController {

    @Autowired
    private ReadingBookmarkService bookmarkService;

    @Operation(summary = "获取儿童收藏列表")
    @GetMapping("/child/{childId}")
    public Result<List<ReadingBookmark>> getBookmarksByChildId(@PathVariable Long childId) {
        return Result.success(bookmarkService.getBookmarksByChildId(childId));
    }

    @Operation(summary = "添加收藏")
    @PostMapping
    public Result<Void> addBookmark(@RequestBody ReadingBookmark bookmark) {
        bookmarkService.addBookmark(bookmark);
        return Result.success();
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{childId}/{bookId}")
    public Result<Void> removeBookmark(@PathVariable Long childId, @PathVariable Long bookId) {
        bookmarkService.removeBookmark(childId, bookId);
        return Result.success();
    }

    @Operation(summary = "检查是否已收藏")
    @GetMapping("/check/{childId}/{bookId}")
    public Result<Boolean> isBookmarked(@PathVariable Long childId, @PathVariable Long bookId) {
        return Result.success(bookmarkService.isBookmarked(childId, bookId));
    }

    @Operation(summary = "获取收藏统计")
    @GetMapping("/stats/{childId}")
    public Result<List<Map<String, Object>>> getBookmarkStats(@PathVariable Long childId) {
        return Result.success(bookmarkService.getBookmarkStats(childId));
    }

    @Operation(summary = "更新收藏类型")
    @PutMapping("/type/{childId}/{bookId}")
    public Result<Void> updateBookmarkType(
            @PathVariable Long childId,
            @PathVariable Long bookId,
            @RequestParam Integer bookmarkType) {
        bookmarkService.updateBookmarkType(childId, bookId, bookmarkType);
        return Result.success();
    }
}
