package com.example.d_trade.service;

import com.example.d_trade.dto.UserDTO;
import com.example.d_trade.dto.request.LoginRequest;
import com.example.d_trade.dto.request.RegisterRequest;
import com.example.d_trade.dto.response.JwtResponse;

public interface AuthService {
    
    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 注册成功的用户信息
     */
    UserDTO register(RegisterRequest registerRequest);
    
    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return JWT响应
     */
    JwtResponse login(LoginRequest loginRequest);
} 