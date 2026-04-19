package com.picturebook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("reading_stats")
public class ReadingStats implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long childId;
    
    private LocalDate statDate;
    
    private Integer totalDuration;
    
    private Integer bookCount;
    
    private Integer completedCount;
    
    private BigDecimal avgPageStayTime;
    
    private BigDecimal avgFocusScore;
    
    private Integer pageTurnCount;
    
    private LocalDateTime createTime;
}
