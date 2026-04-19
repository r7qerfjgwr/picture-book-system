package com.picturebook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class SysRole implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String roleName;
    
    private String roleKey;
    
    private String description;
    
    private LocalDateTime createTime;
}
