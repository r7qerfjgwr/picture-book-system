package com.picturebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.picturebook.dto.BookDTO;
import com.picturebook.dto.PageDTO;
import com.picturebook.entity.Book;
import com.picturebook.exception.BusinessException;
import com.picturebook.mapper.BookMapper;
import com.picturebook.service.BookService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookMapper bookMapper;

    private static final List<String> CATEGORIES = Arrays.asList("动物", "科普", "情感", "童话");
    private static final List<String> AGE_RANGES = Arrays.asList("3-4岁", "4-5岁", "5-6岁");
    private static final List<String> KNOWLEDGE_TYPES = Arrays.asList("语言", "数学", "科学", "艺术", "社会");
    private static final List<String> ART_STYLES = Arrays.asList("水彩", "卡通", "写实", "剪纸", "拼贴");

    @Override
    public Page<Book> getBookList(PageDTO dto) {
        Page<Book> page = new Page<>(dto.getCurrent(), dto.getSize());
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.like(Book::getTitle, dto.getKeyword())
                   .or().like(Book::getAuthor, dto.getKeyword());
        }
        if (StringUtils.hasText(dto.getCategory())) {
            wrapper.eq(Book::getCategory, dto.getCategory());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(Book::getStatus, Integer.parseInt(dto.getStatus()));
        }
        wrapper.orderByDesc(Book::getCreateTime);
        return bookMapper.selectPage(page, wrapper);
    }

    @Override
    public Book getBookById(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException("绘本不存在");
        }
        return book;
    }

    @Override
    public void addBook(BookDTO dto) {
        Book book = new Book();
        BeanUtils.copyProperties(dto, book);
        if (book.getStatus() == null) {
            book.setStatus(1);
        }
        if (book.getReadCount() == null) {
            book.setReadCount(0);
        }
        bookMapper.insert(book);
    }

    @Override
    public void updateBook(BookDTO dto) {
        Book existBook = bookMapper.selectById(dto.getId());
        if (existBook == null) {
            throw new BusinessException("绘本不存在");
        }
        Book book = new Book();
        BeanUtils.copyProperties(dto, book);
        bookMapper.updateById(book);
    }

    @Override
    public void deleteBook(Long id) {
        bookMapper.deleteById(id);
    }

    @Override
    public List<Map<String, Object>> getCategoryStats() {
        return bookMapper.selectCategoryStats();
    }

    @Override
    public List<Book> getHotBooks(int limit) {
        return bookMapper.selectHotBooks(limit);
    }

    @Override
    public List<Book> getHotBooksByCategory(String category, int limit) {
        return bookMapper.selectHotBooksByCategory(category, limit);
    }

    @Override
    public List<String> getCategories() {
        return CATEGORIES;
    }

    @Override
    public List<Book> getBooksByCategory(String category, String subCategory) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, 1);
        if (StringUtils.hasText(category)) {
            wrapper.eq(Book::getCategory, category);
        }
        if (StringUtils.hasText(subCategory)) {
            wrapper.eq(Book::getSubCategory, subCategory);
        }
        wrapper.orderByDesc(Book::getReadCount);
        return bookMapper.selectList(wrapper);
    }

    @Override
    public List<Book> getBooksByAgeRange(String ageRange) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, 1)
               .eq(Book::getAgeRange, ageRange)
               .orderByDesc(Book::getReadCount);
        return bookMapper.selectList(wrapper);
    }

    @Override
    public List<Book> getBooksByKnowledgeType(String knowledgeType) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, 1)
               .eq(Book::getKnowledgeType, knowledgeType)
               .orderByDesc(Book::getReadCount);
        return bookMapper.selectList(wrapper);
    }

    @Override
    public List<Book> getBooksByArtStyle(String artStyle) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, 1)
               .eq(Book::getArtStyle, artStyle)
               .orderByDesc(Book::getReadCount);
        return bookMapper.selectList(wrapper);
    }

    @Override
    public List<Book> filterBooks(String category, String ageRange, String knowledgeType, String artStyle, Integer difficultyLevel) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, 1);
        if (StringUtils.hasText(category)) {
            wrapper.eq(Book::getCategory, category);
        }
        if (StringUtils.hasText(ageRange)) {
            wrapper.eq(Book::getAgeRange, ageRange);
        }
        if (StringUtils.hasText(knowledgeType)) {
            wrapper.eq(Book::getKnowledgeType, knowledgeType);
        }
        if (StringUtils.hasText(artStyle)) {
            wrapper.eq(Book::getArtStyle, artStyle);
        }
        if (difficultyLevel != null) {
            wrapper.eq(Book::getDifficultyLevel, difficultyLevel);
        }
        wrapper.orderByDesc(Book::getReadCount);
        return bookMapper.selectList(wrapper);
    }

    @Override
    public List<String> getAgeRanges() {
        return AGE_RANGES;
    }

    @Override
    public List<String> getKnowledgeTypes() {
        return KNOWLEDGE_TYPES;
    }

    @Override
    public List<String> getArtStyles() {
        return ART_STYLES;
    }

    @Override
    public Map<String, Object> getBookStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", bookMapper.selectCount(null));
        stats.put("categories", bookMapper.selectCategoryStats());
        return stats;
    }
}
