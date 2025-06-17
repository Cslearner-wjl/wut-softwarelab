package com.example.d_trade.repository;

import com.example.d_trade.entity.Order;
import com.example.d_trade.entity.Product;
import com.example.d_trade.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Page<Order> findByBuyerOrderByCreateTimeDesc(User buyer, Pageable pageable);
    
    Page<Order> findBySellerOrderByCreateTimeDesc(User seller, Pageable pageable);
    
    Page<Order> findByBuyerAndStatusOrderByCreateTimeDesc(User buyer, Order.Status status, Pageable pageable);
    
    Page<Order> findBySellerAndStatusOrderByCreateTimeDesc(User seller, Order.Status status, Pageable pageable);
    
    Page<Order> findByBuyerOrSellerOrderByCreateTimeDesc(User buyer, User seller, Pageable pageable);
    
    Optional<Order> findByProductAndStatus(Product product, Order.Status status);
    
    List<Order> findByProduct(Product product);
} 