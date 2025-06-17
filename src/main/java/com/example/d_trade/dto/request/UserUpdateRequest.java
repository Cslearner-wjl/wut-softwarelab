package com.example.d_trade.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    
    @Size(min = 2, max = 20, message = "昵称长度必须在2-20之间")
    private String nickname;
    
    private String contactInfo;
} 