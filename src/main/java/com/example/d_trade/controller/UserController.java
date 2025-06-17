package com.example.d_trade.controller;

import com.example.d_trade.dto.UserDTO;
import com.example.d_trade.dto.request.PasswordChangeRequest;
import com.example.d_trade.dto.request.UserUpdateRequest;
import com.example.d_trade.dto.response.ApiResponse;
import com.example.d_trade.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("isAuthenticated()")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("获取用户个人资料: {}", userDetails.getUsername());
        try {
            UserDTO userDTO = userService.getByStudentId(userDetails.getUsername());
            logger.info("获取用户个人资料成功: {}", userDTO.getStudentId());
            return ResponseEntity.ok(ApiResponse.success(userDTO));
        } catch (Exception e) {
            logger.error("获取用户个人资料失败: {}, 原因: {}", userDetails.getUsername(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取用户信息失败: " + e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest updateRequest) {
        logger.info("更新用户个人资料: {}", userDetails.getUsername());
        try {
            UserDTO updatedUser = userService.updateProfile(userDetails.getUsername(), updateRequest);
            logger.info("更新用户个人资料成功: {}", userDetails.getUsername());
            return ResponseEntity.ok(ApiResponse.success("个人信息更新成功", updatedUser));
        } catch (Exception e) {
            logger.error("更新用户个人资料失败: {}, 原因: {}", userDetails.getUsername(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("更新个人信息失败: " + e.getMessage()));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
        logger.info("修改用户密码: {}", userDetails.getUsername());
        try {
            userService.changePassword(userDetails.getUsername(), passwordChangeRequest);
            logger.info("修改用户密码成功: {}", userDetails.getUsername());
            return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
        } catch (Exception e) {
            logger.error("修改用户密码失败: {}, 原因: {}", userDetails.getUsername(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("修改密码失败: " + e.getMessage()));
        }
    }
} 