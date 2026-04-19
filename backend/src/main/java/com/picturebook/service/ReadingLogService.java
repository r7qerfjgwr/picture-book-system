package com.picturebook.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.picturebook.dto.PageDTO;
import com.picturebook.dto.ReadingLogDTO;
import com.picturebook.entity.ReadingLog;
import java.util.List;
import java.util.Map;

public interface ReadingLogService {

    void submitReadingLog(ReadingLogDTO dto);

    Page<ReadingLog> getReadingLogs(PageDTO dto);

    List<ReadingLog> getReadingLogsByChildId(Long childId);

    Map<String, Object> getReadingStats(Long childId);

    List<Map<String, Object>> getReadingTrend(Long childId, Integer days);

    List<Map<String, Object>> getCategoryStats(Long childId);

    void generateMockData(Integer count);

    void updateReadingLog(Long id, ReadingLogDTO dto);

    ReadingLog getReadingLogById(Long id);
}
