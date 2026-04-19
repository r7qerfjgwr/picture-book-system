package com.picturebook.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.picturebook.dto.BookDTO;
import com.picturebook.dto.PageDTO;
import com.picturebook.entity.Book;
import com.picturebook.service.BookService;
import com.picturebook.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Tag(name = "绘本管理", description = "绘本增删改查接口")
@RestController
@RequestMapping("/book")
@SecurityRequirement(name = "Authorization")
public class BookController {

    @Autowired
    private BookService bookService;

    @Operation(summary = "获取绘本列表")
    @GetMapping("/list")
    public Result<Page<Book>> getBookList(PageDTO dto) {
        return Result.success(bookService.getBookList(dto));
    }

    @Operation(summary = "获取绘本详情")
    @GetMapping("/{id}")
    public Result<Book> getBookById(@PathVariable Long id) {
        return Result.success(bookService.getBookById(id));
    }

    @Operation(summary = "添加绘本")
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> addBook(@Valid @RequestBody BookDTO dto) {
        bookService.addBook(dto);
        return Result.success();
    }

    @Operation(summary = "更新绘本")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> updateBook(@PathVariable Long id, @Valid @RequestBody BookDTO dto) {
        dto.setId(id);
        bookService.updateBook(dto);
        return Result.success();
    }

    @Operation(summary = "删除绘本")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success();
    }

    @Operation(summary = "获取绘本类别统计")
    @GetMapping("/category-stats")
    public Result<List<Map<String, Object>>> getCategoryStats() {
        return Result.success(bookService.getCategoryStats());
    }

    @Operation(summary = "获取热门绘本")
    @GetMapping("/hot")
    public Result<List<Book>> getHotBooks(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(bookService.getHotBooks(limit));
    }

    @Operation(summary = "获取绘本类别列表")
    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        return Result.success(bookService.getCategories());
    }

    @Operation(summary = "按分类获取绘本")
    @GetMapping("/category")
    public Result<List<Book>> getBooksByCategory(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subCategory) {
        return Result.success(bookService.getBooksByCategory(category, subCategory));
    }

    @Operation(summary = "按年龄段获取绘本")
    @GetMapping("/age-range/{ageRange}")
    public Result<List<Book>> getBooksByAgeRange(@PathVariable String ageRange) {
        return Result.success(bookService.getBooksByAgeRange(ageRange));
    }

    @Operation(summary = "按知识点类型获取绘本")
    @GetMapping("/knowledge-type/{knowledgeType}")
    public Result<List<Book>> getBooksByKnowledgeType(@PathVariable String knowledgeType) {
        return Result.success(bookService.getBooksByKnowledgeType(knowledgeType));
    }

    @Operation(summary = "按画风风格获取绘本")
    @GetMapping("/art-style/{artStyle}")
    public Result<List<Book>> getBooksByArtStyle(@PathVariable String artStyle) {
        return Result.success(bookService.getBooksByArtStyle(artStyle));
    }

    @Operation(summary = "多条件筛选绘本")
    @GetMapping("/filter")
    public Result<List<Book>> filterBooks(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String ageRange,
            @RequestParam(required = false) String knowledgeType,
            @RequestParam(required = false) String artStyle,
            @RequestParam(required = false) Integer difficultyLevel) {
        return Result.success(bookService.filterBooks(category, ageRange, knowledgeType, artStyle, difficultyLevel));
    }

    @Operation(summary = "获取年龄段列表")
    @GetMapping("/age-ranges")
    public Result<List<String>> getAgeRanges() {
        return Result.success(bookService.getAgeRanges());
    }

    @Operation(summary = "获取知识点类型列表")
    @GetMapping("/knowledge-types")
    public Result<List<String>> getKnowledgeTypes() {
        return Result.success(bookService.getKnowledgeTypes());
    }

    @Operation(summary = "获取画风风格列表")
    @GetMapping("/art-styles")
    public Result<List<String>> getArtStyles() {
        return Result.success(bookService.getArtStyles());
    }

    @Operation(summary = "获取绘本统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getBookStats() {
        return Result.success(bookService.getBookStatistics());
    }
}
