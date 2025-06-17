package com.example.d_trade.dto;

import com.example.d_trade.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    
    private Long id;
    private String studentId;
    private String username;
    private String nickname;
    private String contactInfo;
    private boolean enabled;
    private User.Role role;
    private LocalDateTime createTime;
    
    public static UserDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }
        
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setStudentId(user.getStudentId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setContactInfo(user.getContactInfo());
        dto.setEnabled(user.isEnabled());
        dto.setRole(user.getRole());
        dto.setCreateTime(user.getCreateTime());
        return dto;
    }
} 