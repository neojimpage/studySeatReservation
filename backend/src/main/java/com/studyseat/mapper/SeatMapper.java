package com.studyseat.mapper;

import com.studyseat.entity.Seat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SeatMapper extends BaseMapper<Seat> {
    @Select("SELECT * FROM seat WHERE area_id = #{areaId} ORDER BY id")
    List<Seat> findByAreaId(@Param("areaId") Long areaId);
}