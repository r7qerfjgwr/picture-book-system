package com.picturebook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("book_page")
public class BookPage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bookId;

    private Integer pageNum;

    private String illustration;

    private String text;

    private LocalDateTime createTime;
}
