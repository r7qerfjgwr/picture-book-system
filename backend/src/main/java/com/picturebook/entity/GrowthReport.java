package com.picturebook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("growth_report")
public class GrowthReport implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long childId;
    
    private BigDecimal vocabularyScore;
    
    private BigDecimal logicScore;
    
    private BigDecimal readingScore;
    
    private BigDecimal focusScore;
    
    private BigDecimal totalScore;
    
    private String reportType;
    
    private LocalDate reportDate;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private Integer bookCount;
    
    private Integer totalDuration;
    
    private String suggestion;

    // 新增字段：认知发展评分
    private BigDecimal cognitiveScore;

    // 新增字段：社交情感评分
    private BigDecimal socialScore;

    // 新增字段：创造力评分
    private BigDecimal creativityScore;

    // 新增字段：阅读习惯评分
    private BigDecimal habitScore;

    // 新增字段：详细维度数据JSON
    private String detailedData;

    // 新增字段：成长里程碑JSON
    private String milestones;

    private LocalDateTime createTime;
    
    @TableField(exist = false)
    private String childName;
}
