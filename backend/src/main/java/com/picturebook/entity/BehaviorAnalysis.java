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

    // 新增字段：重复阅读偏好分数
    private BigDecimal replayPreferenceScore;

    // 新增字段：互动行为分数（收藏、批注、录音）
    private BigDecimal interactionScore;

    // 新增字段：情绪稳定性分数
    private BigDecimal emotionStabilityScore;

    // 新增字段：认知发展阶段
    private String cognitiveStage;

    // 新增字段：阅读能力评分
    private BigDecimal readingAbilityScore;

    // 新增字段：阅读习惯养成度
    private BigDecimal habitFormationScore;

    // 新增字段：成长预测分数
    private BigDecimal growthPredictionScore;

    // 新增字段：分析维度数据JSON
    private String dimensionData;

    private LocalDate analysisDate;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String childName;
}
