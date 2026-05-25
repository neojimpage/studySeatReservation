package com.studyseat.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@Data
@TableName("seat")
public class Seat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long areaId;
    private String seatNumber;
    private String status; // 空闲/已预约/占用/暂离/禁用
    private Boolean isEnabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}