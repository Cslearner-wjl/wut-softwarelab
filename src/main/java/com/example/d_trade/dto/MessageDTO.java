package com.example.d_trade.dto;

import com.example.d_trade.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    
    private Long id;
    private Message.Type type;
    private String title;
    private String content;
    private boolean read;
    private UserDTO receiver;
    private Long productId;
    private Long orderId;
    private LocalDateTime createTime;
    
    public static MessageDTO fromEntity(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setType(message.getType());
        dto.setTitle(message.getTitle());
        dto.setContent(message.getContent());
        // 修复调用错误的方法名
        dto.setRead(message.isRead());
        dto.setReceiver(UserDTO.fromEntity(message.getReceiver()));
        if (message.getProduct() != null) {
            dto.setProductId(message.getProduct().getId());
        }
        if (message.getOrder() != null) {
            dto.setOrderId(message.getOrder().getId());
        }
        dto.setCreateTime(message.getCreateTime());
        return dto;
    }
}
