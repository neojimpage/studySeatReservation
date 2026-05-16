package com.studyseat.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@Data
@TableName("area")
public class Area {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Integer floor;
    private Boolean isEnabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}