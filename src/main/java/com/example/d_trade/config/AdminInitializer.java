package com.example.d_trade.config;

import com.example.d_trade.entity.User;
import com.example.d_trade.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 检查是否已存在管理员账号
        if (!userRepository.existsByStudentId("admin")) {
            logger.info("未找到管理员账号，正在创建...");
            
            // 创建管理员账号
            User adminUser = new User();
            adminUser.setStudentId("admin");
            adminUser.setUsername("管理员");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setNickname("系统管理员");
            adminUser.setEnabled(true);
            adminUser.setRole(User.Role.ADMIN);
            
            userRepository.save(adminUser);
            
            logger.info("管理员账号创建成功");
        } else {
            logger.info("管理员账号已存在");
        }
    }
} 