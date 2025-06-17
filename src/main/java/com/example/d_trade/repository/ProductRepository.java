package com.example.d_trade.repository;

import com.example.d_trade.entity.Product;
import com.example.d_trade.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Page<Product> findByStatusOrderByCreateTimeDesc(Product.Status status, Pageable pageable);
    
    Page<Product> findByStatusOrderByPriceAsc(Product.Status status, Pageable pageable);
    
    Page<Product> findByStatusOrderByPriceDesc(Product.Status status, Pageable pageable);
    
    List<Product> findBySeller(User seller);
    
    Page<Product> findBySeller(User seller, Pageable pageable);
    
    Page<Product> findBySellerAndStatus(User seller, Product.Status status, Pageable pageable);
    
    Page<Product> findByTitleContainingAndStatus(String title, Product.Status status, Pageable pageable);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.product.id = :productId")
    long countOrdersByProductId(Long productId);
} 