package com.example.d_trade.dto;

import com.example.d_trade.entity.Order;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    
    private Long id;
    private ProductDTO product;
    private UserDTO buyer;
    private UserDTO seller;
    private Order.Status status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    public static OrderDTO fromEntity(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setProduct(ProductDTO.fromEntity(order.getProduct()));
        dto.setBuyer(UserDTO.fromEntity(order.getBuyer()));
        dto.setSeller(UserDTO.fromEntity(order.getSeller()));
        dto.setStatus(order.getStatus());
        dto.setCreateTime(order.getCreateTime());
        dto.setUpdateTime(order.getUpdateTime());
        return dto;
    }
} 