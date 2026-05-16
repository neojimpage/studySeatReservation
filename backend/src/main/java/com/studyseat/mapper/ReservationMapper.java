package com.studyseat.mapper;

import com.studyseat.entity.Reservation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.time.LocalDateTime;

public interface ReservationMapper extends BaseMapper<Reservation> {
    @Select("""
            SELECT * FROM reservation
            WHERE user_id = #{userId}
              AND status IN ('已预约','已开始')
            ORDER BY start_time DESC
            """)
    List<Reservation> findCurrentByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT * FROM reservation
            WHERE user_id = #{userId}
            ORDER BY start_time DESC
            """)
    List<Reservation> findByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT * FROM reservation
            WHERE seat_id = #{seatId}
              AND start_time < #{endTime}
              AND end_time > #{startTime}
              AND status IN ('已预约','已开始')
            ORDER BY start_time
            """)
    List<Reservation> findBySeatIdAndTimeRange(
            @Param("seatId") Long seatId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}