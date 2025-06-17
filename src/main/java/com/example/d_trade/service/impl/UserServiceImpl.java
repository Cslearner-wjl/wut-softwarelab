package com.example.d_trade.service.impl;

import com.example.d_trade.dto.UserDTO;
import com.example.d_trade.dto.request.PasswordChangeRequest;
import com.example.d_trade.dto.request.UserUpdateRequest;
import com.example.d_trade.entity.User;
import com.example.d_trade.repository.UserRepository;
import com.example.d_trade.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO getByStudentId(String studentId) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return UserDTO.fromEntity(user);
    }

    @Override
    @Transactional
    public UserDTO updateProfile(String studentId, UserUpdateRequest updateRequest) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 更新用户信息
        if (updateRequest.getNickname() != null) {
            user.setNickname(updateRequest.getNickname());
        }
        if (updateRequest.getContactInfo() != null) {
            user.setContactInfo(updateRequest.getContactInfo());
        }

        User updatedUser = userRepository.save(user);
        return UserDTO.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(String studentId, PasswordChangeRequest passwordChangeRequest) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证旧密码
        if (!passwordEncoder.matches(passwordChangeRequest.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码不正确");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));
        userRepository.save(user);
    }
    
    @Override
    public Page<UserDTO> getUsers(String keyword, Pageable pageable) {
        // 创建动态查询条件
        Specification<User> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(keyword)) {
                // 搜索用户名、学号或昵称
                Predicate usernameLike = criteriaBuilder.like(root.get("username"), "%" + keyword + "%");
                Predicate studentIdLike = criteriaBuilder.like(root.get("studentId"), "%" + keyword + "%");
                Predicate nicknameLike = criteriaBuilder.like(root.get("nickname"), "%" + keyword + "%");
                predicates.add(criteriaBuilder.or(usernameLike, studentIdLike, nicknameLike));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        // 执行查询并转换结果
        return userRepository.findAll(specification, pageable).map(UserDTO::fromEntity);
    }
    
    @Override
    @Transactional
    public UserDTO updateUserStatus(Long id, Boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 不允许禁用管理员账号
        if (user.getRole() == User.Role.ADMIN && !enabled) {
            throw new RuntimeException("不能禁用管理员账号");
        }
        
        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);
        return UserDTO.fromEntity(updatedUser);
    }
    
    @Override
    @Transactional
    public String resetPassword(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 不允许重置管理员密码
        if (user.getRole() == User.Role.ADMIN) {
            throw new RuntimeException("不能重置管理员密码");
        }
        
        // 生成随机密码
        String newPassword = generateRandomPassword();
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return newPassword;
    }
    
    /**
     * 生成8位随机密码
     * @return 随机密码
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }
} 