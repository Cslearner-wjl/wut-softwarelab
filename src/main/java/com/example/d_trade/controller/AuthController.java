package com.example.d_trade.controller;

import com.example.d_trade.dto.UserDTO;
import com.example.d_trade.dto.request.LoginRequest;
import com.example.d_trade.dto.request.RegisterRequest;
import com.example.d_trade.dto.response.ApiResponse;
import com.example.d_trade.dto.response.JwtResponse;
import com.example.d_trade.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            HttpServletRequest request) {
        logger.info("接收到注册请求: {}, IP: {}", registerRequest.getStudentId(), request.getRemoteAddr());
        try {
            UserDTO userDTO = authService.register(registerRequest);
            logger.info("注册成功: {}", userDTO.getStudentId());
            return ResponseEntity.ok(ApiResponse.success("注册成功", userDTO));
        } catch (Exception e) {
            logger.error("注册失败: {}, 原因: {}", registerRequest.getStudentId(), e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("注册失败: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        logger.info("接收到登录请求: {}, IP: {}", loginRequest.getStudentId(), request.getRemoteAddr());
        try {
            JwtResponse jwtResponse = authService.login(loginRequest);
            logger.info("登录成功: {}", loginRequest.getStudentId());
            return ResponseEntity.ok(ApiResponse.success("登录成功", jwtResponse));
        } catch (Exception e) {
            logger.error("登录失败: {}, 原因: {}", loginRequest.getStudentId(), e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("登录失败: " + e.getMessage()));
        }
    }
    
    // 测试接口，验证是否需要认证
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test(HttpServletRequest request) {
        logger.info("接收到测试请求, IP: {}", request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("认证接口测试成功", "这是一个不需要认证的测试接口"));
    }
} 