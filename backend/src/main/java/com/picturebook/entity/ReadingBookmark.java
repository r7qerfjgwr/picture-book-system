package com.picturebook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("reading_bookmark")
public class ReadingBookmark implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long childId;

    private Long bookId;

    // 收藏类型：1-喜欢 2-想读 3-在读 4-读完
    private Integer bookmarkType;

    private String note;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String bookTitle;

    @TableField(exist = false)
    private String bookCover;
}
