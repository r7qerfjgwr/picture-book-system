package com.picturebook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("reading_log")
public class ReadingLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long childId;

    private Long bookId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer duration;

    private String pageStayTimes;

    private Integer pageTurnCount;

    private Integer isCompleted;

    private BigDecimal completionRate;

    private BigDecimal avgPageStayTime;

    private BigDecimal focusScore;

    // 新增字段：重复阅读次数
    private Integer replayCount;

    // 新增字段：收藏次数
    private Integer bookmarkCount;

    // 新增字段：批注次数
    private Integer annotationCount;

    // 新增字段：录音次数
    private Integer voiceRecordCount;

    // 新增字段：情绪数据JSON
    private String emotionData;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String childName;

    @TableField(exist = false)
    private String bookTitle;

    @TableField(exist = false)
    private String bookCategory;
}
