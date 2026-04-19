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
@TableName("recommendation")
public class Recommendation implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long childId;
    
    private Long bookId;
    
    private String recommendType;
    
    private BigDecimal recommendScore;
    
    private String reason;
    
    private Integer isRead;
    
    private LocalDateTime createTime;
    
    @TableField(exist = false)
    private String childName;
    
    @TableField(exist = false)
    private Book book;
}
