package com.example.d_trade.dto.response;

import com.example.d_trade.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    
    private String token;
    private String type = "Bearer";
    private UserDTO user;
    
    public JwtResponse(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }
} 