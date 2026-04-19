package com.picturebook.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.picturebook.dto.BookDTO;
import com.picturebook.dto.PageDTO;
import com.picturebook.entity.Book;
import java.util.List;
import java.util.Map;

public interface BookService {

    Page<Book> getBookList(PageDTO dto);

    Book getBookById(Long id);

    void addBook(BookDTO dto);

    void updateBook(BookDTO dto);

    void deleteBook(Long id);

    List<Map<String, Object>> getCategoryStats();

    List<Book> getHotBooks(int limit);

    List<Book> getHotBooksByCategory(String category, int limit);

    List<String> getCategories();

    List<Book> getBooksByCategory(String category, String subCategory);

    // 新增方法
    List<Book> getBooksByAgeRange(String ageRange);

    List<Book> getBooksByKnowledgeType(String knowledgeType);

    List<Book> getBooksByArtStyle(String artStyle);

    List<Book> filterBooks(String category, String ageRange, String knowledgeType, String artStyle, Integer difficultyLevel);

    List<String> getAgeRanges();

    List<String> getKnowledgeTypes();

    List<String> getArtStyles();

    Map<String, Object> getBookStatistics();
}
