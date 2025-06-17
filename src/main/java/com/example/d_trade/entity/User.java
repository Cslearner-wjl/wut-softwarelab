package com.example.d_trade.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String studentId; // 学号
    
    @Column(nullable = false)
    private String username; // 用户名
    
    @Column(nullable = false)
    private String password; // 密码
    
    private String nickname; // 昵称
    
    private String contactInfo; // 联系方式
    
    @Column(nullable = false)
    private boolean enabled = true; // 账号是否启用
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER; // 角色
    
    @CreationTimestamp
    private LocalDateTime createTime; // 创建时间
    
    @UpdateTimestamp
    private LocalDateTime updateTime; // 更新时间
    
    // 用户发布的商品
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Product> products = new HashSet<>();
    
    // 用户的订单（买家）
    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Order> buyOrders = new HashSet<>();
    
    // 用户的订单（卖家）
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Order> sellOrders = new HashSet<>();
    
    // 用户收到的消息
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Message> receivedMessages = new HashSet<>();
    
    public enum Role {
        USER, ADMIN
    }
} 