package com.picturebook.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

@Data
public class PageDTO extends Page<Object> {

    private String keyword;

    private String startDate;

    private String endDate;

    private Long parentId;

    private Long classId;

    private Long childId;

    private String category;

    private String status;

    private String role;

    private String username;
}
