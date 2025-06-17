package com.example.d_trade.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    
    @NotBlank(message = "学号不能为空")
    private String studentId;
    
    @NotBlank(message = "密码不能为空")
    private String password;
} 