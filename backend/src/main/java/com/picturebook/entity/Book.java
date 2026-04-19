package com.picturebook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("book")
public class Book implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String author;

    private String publisher;

    private String category;

    private String subCategory;

    private Integer difficultyLevel;

    private Integer pageCount;

    // 新增字段：适合年龄段
    private String ageRange;

    // 新增字段：知识点类型
    private String knowledgeType;

    // 新增字段：画风风格
    private String artStyle;

    // 新增字段：绘本主题
    private String theme;

    // 新增字段：关键词标签
    private String keywords;

    private String coverUrl;

    private String description;

    private Integer vocabularyCount;

    private Integer readCount;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
