package com.example.d_trade.dto;

import com.example.d_trade.entity.Product;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    
    private Long id;
    private String title;
    private BigDecimal price;
    private String description;
    private String tradeLocation;
    private List<String> imagePaths;
    private Product.Status status;
    private UserDTO seller;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime updateTime;
    
    public static ProductDTO fromEntity(Product product) {
        if (product == null) {
            return null;
        }
        
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        dto.setTradeLocation(product.getTradeLocation());
        dto.setImagePaths(product.getImagePaths());
        dto.setStatus(product.getStatus());
        
        if (product.getSeller() != null) {
            dto.setSeller(UserDTO.fromEntity(product.getSeller()));
        }
        
        dto.setCreateTime(product.getCreateTime());
        dto.setUpdateTime(product.getUpdateTime());
        return dto;
    }
} 