package com.example.d_trade.controller;

import com.example.d_trade.dto.ProductDTO;
import com.example.d_trade.dto.UserDTO;
import com.example.d_trade.dto.response.ApiResponse;
import com.example.d_trade.service.ProductService;
import com.example.d_trade.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminController {

    private final UserService userService;
    private final ProductService productService;

    public AdminController(UserService userService, ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getUsers(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<UserDTO> users = userService.getUsers(keyword, pageable);
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取用户列表失败: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserStatus(
            @PathVariable Long id,
            @RequestParam Boolean enabled) {
        try {
            UserDTO user = userService.updateUserStatus(id, enabled);
            return ResponseEntity.ok(ApiResponse.success("用户状态已更新", user));
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("更新用户状态失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/users/{id}/reset-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetPassword(@PathVariable Long id) {
        try {
            String newPassword = userService.resetPassword(id);
            Map<String, String> result = new HashMap<>();
            result.put("password", newPassword);
            return ResponseEntity.ok(ApiResponse.success("密码重置成功", result));
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("重置密码失败: " + e.getMessage()));
        }
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            log.info("管理员获取商品列表: keyword={}, status={}, page={}, size={}", 
                    keyword, status, pageable.getPageNumber(), pageable.getPageSize());
            Page<ProductDTO> products = productService.getProducts(keyword, status, null, pageable);
            log.info("管理员获取商品列表成功: 总数={}", products.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(products));
        } catch (Exception e) {
            log.error("获取商品列表失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取商品列表失败: " + e.getMessage()));
        }
    }

    @PutMapping("/products/{id}/remove")
    public ResponseEntity<ApiResponse<Void>> removeProduct(@PathVariable Long id) {
        try {
            productService.adminRemoveProduct(id);
            return ResponseEntity.ok(ApiResponse.success("商品已删除", null));
        } catch (Exception e) {
            log.error("删除商品失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("删除商品失败: " + e.getMessage()));
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        try {
            // 创建统计数据
            Map<String, Object> statistics = new HashMap<>();
            
            // 用户统计
            long totalUsers = userService.getUsers(null, Pageable.unpaged()).getTotalElements();
            
            // 商品统计
            long totalProducts = productService.getProducts(null, null, null, Pageable.unpaged()).getTotalElements();
            long availableProducts = productService.getProducts(null, "AVAILABLE", null, Pageable.unpaged()).getTotalElements();
            
            // 填充统计数据
            statistics.put("userCount", totalUsers);
            statistics.put("productCount", totalProducts);
            statistics.put("availableProductCount", availableProducts);
            
            return ResponseEntity.ok(ApiResponse.success("获取统计数据成功", statistics));
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取统计数据失败: " + e.getMessage()));
        }
    }
} 