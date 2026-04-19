package com.picturebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.picturebook.entity.BehaviorAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface BehaviorAnalysisMapper extends BaseMapper<BehaviorAnalysis> {

    @Select("SELECT ba.*, c.name as child_name " +
            "FROM behavior_analysis ba " +
            "LEFT JOIN child c ON ba.child_id = c.id " +
            "WHERE ba.child_id = #{childId} " +
            "ORDER BY ba.analysis_date DESC LIMIT 1")
    BehaviorAnalysis selectLatestByChildId(Long childId);

    @Select("SELECT ba.*, c.name as child_name " +
            "FROM behavior_analysis ba " +
            "LEFT JOIN child c ON ba.child_id = c.id " +
            "WHERE ba.child_id = #{childId} " +
            "ORDER BY ba.analysis_date ASC")
    List<BehaviorAnalysis> selectByChildId(Long childId);
}
