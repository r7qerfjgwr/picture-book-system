package com.picturebook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("reading_annotation")
public class ReadingAnnotation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long childId;

    private Long bookId;

    // 页码
    private Integer pageNum;

    // 批注类型：1-文字 2-语音 3-涂鸦
    private Integer annotationType;

    // 批注内容
    private String content;

    // X坐标位置
    private Integer positionX;

    // Y坐标位置
    private Integer positionY;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String bookTitle;

    @TableField(exist = false)
    private String childName;
}
