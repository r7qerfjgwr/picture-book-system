package com.picturebook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("class_info")
public class ClassInfo implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String className;
    
    private Long teacherId;
    
    private String institutionName;
    
    private Integer studentCount;
    
    private String description;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String teacherName;
}
