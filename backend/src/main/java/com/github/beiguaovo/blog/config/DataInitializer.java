package com.github.beiguaovo.blog.config;

import com.github.beiguaovo.blog.entity.User;
import com.github.beiguaovo.blog.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        long count;
        try {
            count = userMapper.selectCount(null);
        } catch (Exception e) {
            log.error("==================================================");
            log.error("数据库表不存在或无法访问，请先导入建表脚本：");
            log.error("  cmd> E:\\mysql\\bin\\mysql.exe -u root -p --default-character-set=utf8mb4 < backend\\sql\\schema.sql");
            log.error("原因: {}", e.getMessage());
            log.error("==================================================");
            throw e;
        }
        if (count > 0) {
            return;
        }
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setNickname("管理员");
        admin.setRole("ADMIN");
        admin.setStatus(1);
        userMapper.insert(admin);
        log.warn("已创建初始管理员 admin / admin123，请立即修改密码！");
    }
}