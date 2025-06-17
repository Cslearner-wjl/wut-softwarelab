package com.example.d_trade.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title; // 商品标题
    
    @Column(nullable = false)
    private BigDecimal price; // 商品价格
    
    @Column(columnDefinition = "TEXT")
    private String description; // 商品描述
    
    private String tradeLocation; // 交易地点
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_path")
    private List<String> imagePaths = new ArrayList<>(); // 商品图片路径
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.AVAILABLE; // 商品状态
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller; // 卖家
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Order> orders = new HashSet<>(); // 与该商品相关的订单
    
    @CreationTimestamp
    private LocalDateTime createTime; // 创建时间
    
    @UpdateTimestamp
    private LocalDateTime updateTime; // 更新时间
    
    public enum Status {
        AVAILABLE, // 可用
        SOLD, // 已售出
        REMOVED // 已下架
    }
} 