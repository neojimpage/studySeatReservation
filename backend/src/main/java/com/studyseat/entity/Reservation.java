package com.studyseat.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@Data
@TableName("reservation")
public class Reservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long seatId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // 已预约/已开始/已结束/已取消/爽约
    private LocalDateTime leaveTime;
    private LocalDateTime backTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}