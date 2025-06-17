package com.example.d_trade.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type; // 消息类型
    
    @Column(nullable = false)
    private String title; // 消息标题
    
    @Column(columnDefinition = "TEXT")
    private String content; // 消息内容
    
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false; // 是否已读
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver; // 接收者
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // 相关商品（可选）
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order; // 相关订单（可选）
    
    @CreationTimestamp
    private LocalDateTime createTime; // 创建时间
    
    public enum Type {
        PRODUCT_INTEREST, // 商品感兴趣通知
        ORDER_STATUS_CHANGE, // 订单状态变更通知
        SYSTEM // 系统通知
    }
} 