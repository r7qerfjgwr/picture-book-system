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
@TableName("child")
public class Child implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private LocalDate birthDate;

    private Integer gender;

    private Long parentId;

    private Long classId;

    private String avatar;

    private String readingType;

    // 新增字段：认知发展阶段
    private String cognitiveStage;

    // 新增字段：阅读能力综合评分
    private BigDecimal readingAbilityScore;

    // 新增字段：成长里程碑
    private String milestone;

    private String interestTags;

    private BigDecimal focusScore;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String parentName;

    @TableField(exist = false)
    private String className;

    @TableField(exist = false)
    private Integer age;
}
