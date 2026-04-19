package com.picturebook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReadingLogDTO {

    @NotNull(message = "儿童ID不能为空")
    private Long childId;

    @NotNull(message = "绘本ID不能为空")
    private Long bookId;

    @NotNull(message = "阅读开始时间不能为空")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private List<Integer> pageStayTimes;

    private Integer pageTurnCount;

    private Integer isCompleted;

    // 互动数据
    private Integer bookmarkCount;
    private Integer annotationCount;
    private Integer voiceRecordCount;
}
