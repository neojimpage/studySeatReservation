package com.studyseat.mapper;

import com.studyseat.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM `user` WHERE student_id = #{studentId} LIMIT 1")
    User findByStudentId(@Param("studentId") String studentId);
}