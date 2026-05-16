package com.studyseat.config;

import com.studyseat.entity.User;
import com.studyseat.mapper.UserMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void run(String... args) {
        // Ensure role column exists (for existing databases)
        try (SqlSession session = sqlSessionFactory.openSession()) {
            session.getConnection().createStatement()
                .execute("ALTER TABLE `user` ADD COLUMN `role` VARCHAR(20) NOT NULL DEFAULT 'student'");
        } catch (Exception ignored) {
            // Column already exists — ignore
        }

        // Insert preset admin account if not exists
        User existing = userMapper.findByStudentId("admin");
        if (existing == null) {
            User admin = new User();
            admin.setStudentId("admin");
            admin.setPassword("admin123");
            admin.setName("管理员");
            admin.setRole("admin");
            admin.setViolationCount(0);
            admin.setIsRestricted(false);
            admin.setCreateTime(java.time.LocalDateTime.now());
            admin.setUpdateTime(java.time.LocalDateTime.now());
            userMapper.insert(admin);
            System.out.println("=== 预设管理员账号已创建: admin / admin123 ===");
        }
    }
}
