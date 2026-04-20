package com.example.backen.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("seat")
public class Seat {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("area_id")
    private Long areaId;
    @TableField("seat_no")
    private String seatNo;
    private SeatStatus status = SeatStatus.FREE;

    public Seat() {}

    public Seat(Long id, Long areaId, String seatNo) {
        this.id = id;
        this.areaId = areaId;
        this.seatNo = seatNo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public String getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(String seatNo) {
        this.seatNo = seatNo;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }
}
