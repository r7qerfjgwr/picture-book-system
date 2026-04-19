package com.picturebook.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookDTO {

    private Long id;

    @NotBlank(message = "绘本名称不能为空")
    private String title;

    private String author;

    private String publisher;

    @NotBlank(message = "绘本类别不能为空")
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

    private Integer status;
}
