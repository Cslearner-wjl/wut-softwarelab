package com.example.d_trade.service.impl;

import com.example.d_trade.dto.UserDTO;
import com.example.d_trade.dto.request.LoginRequest;
import com.example.d_trade.dto.request.RegisterRequest;
import com.example.d_trade.dto.response.JwtResponse;
import com.example.d_trade.entity.User;
import com.example.d_trade.repository.UserRepository;
import com.example.d_trade.security.JwtUtils;
import com.example.d_trade.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Override
    @Transactional
    public UserDTO register(RegisterRequest registerRequest) {
        // 检查学号是否已存在
        if (userRepository.existsByStudentId(registerRequest.getStudentId())) {
            throw new RuntimeException("该学号已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setStudentId(registerRequest.getStudentId());
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setNickname(registerRequest.getNickname());
        user.setContactInfo(registerRequest.getContactInfo());
        user.setEnabled(true);
        user.setRole(User.Role.USER);

        // 保存用户
        User savedUser = userRepository.save(user);

        // 返回用户DTO
        return UserDTO.fromEntity(savedUser);
    }

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        // 认证用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getStudentId(),
                        loginRequest.getPassword()
                )
        );

        // 设置认证信息
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成JWT令牌
        String jwt = jwtUtils.generateToken((UserDetails) authentication.getPrincipal());

        // 获取用户信息
        String studentId = authentication.getName();
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 返回JWT响应
        return new JwtResponse(jwt, UserDTO.fromEntity(user));
    }
} 