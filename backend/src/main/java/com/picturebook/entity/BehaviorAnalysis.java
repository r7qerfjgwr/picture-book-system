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
@TableName("behavior_analysis")
public class BehaviorAnalysis implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long childId;
    
    private String readingType;
    
    private String interestTags;
    
    private BigDecimal focusScore;
    
    private BigDecimal avgReadingDuration;
    
    private BigDecimal avgTurnSpeed;
    
    private BigDecimal completionRate;
    
    private Integer totalReadingCount;

    private BigDecimal replayPreferenceScore;

    private BigDecimal interactionScore;

    private BigDecimal emotionStabilityScore;

    private String cognitiveStage;

    private BigDecimal readingAbilityScore;

    private BigDecimal habitFormationScore;

    private BigDecimal growthPredictionScore;

    private String dimensionData;

    private LocalDate analysisDate;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String childName;
}
