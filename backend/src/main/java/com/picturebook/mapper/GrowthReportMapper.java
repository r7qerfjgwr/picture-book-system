package com.picturebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.picturebook.entity.GrowthReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface GrowthReportMapper extends BaseMapper<GrowthReport> {
    
    @Select("SELECT gr.*, c.name as child_name " +
            "FROM growth_report gr " +
            "LEFT JOIN child c ON gr.child_id = c.id " +
            "WHERE gr.child_id = #{childId} " +
            "ORDER BY gr.report_date DESC")
    List<GrowthReport> selectReportsByChildId(Long childId);
    
    @Select("SELECT gr.*, c.name as child_name " +
            "FROM growth_report gr " +
            "LEFT JOIN child c ON gr.child_id = c.id " +
            "WHERE gr.child_id = #{childId} " +
            "ORDER BY gr.report_date DESC LIMIT 1")
    GrowthReport selectLatestReportByChildId(Long childId);
    
    @Select("SELECT gr.*, c.name as child_name " +
            "FROM growth_report gr " +
            "LEFT JOIN child c ON gr.child_id = c.id " +
            "WHERE gr.child_id = #{childId} AND gr.report_type = #{reportType} " +
            "ORDER BY gr.report_date DESC")
    List<GrowthReport> selectReportsByChildIdAndType(@Param("childId") Long childId, 
                                                       @Param("reportType") String reportType);
}
