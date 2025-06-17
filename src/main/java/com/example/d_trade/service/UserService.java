package com.example.d_trade.service;

import com.example.d_trade.dto.UserDTO;
import com.example.d_trade.dto.request.PasswordChangeRequest;
import com.example.d_trade.dto.request.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    
    /**
     * 根据学号获取用户信息
     * @param studentId 学号
     * @return 用户DTO
     */
    UserDTO getByStudentId(String studentId);
    
    /**
     * 更新用户个人信息
     * @param studentId 学号
     * @param updateRequest 更新请求
     * @return 更新后的用户DTO
     */
    UserDTO updateProfile(String studentId, UserUpdateRequest updateRequest);
    
    /**
     * 修改密码
     * @param studentId 学号
     * @param passwordChangeRequest 密码修改请求
     */
    void changePassword(String studentId, PasswordChangeRequest passwordChangeRequest);
    
    /**
     * 管理员获取用户列表
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 用户列表
     */
    Page<UserDTO> getUsers(String keyword, Pageable pageable);
    
    /**
     * 管理员更新用户状态
     * @param id 用户ID
     * @param enabled 是否启用
     * @return 更新后的用户DTO
     */
    UserDTO updateUserStatus(Long id, Boolean enabled);
    
    /**
     * 管理员重置用户密码
     * @param id 用户ID
     * @return 新密码
     */
    String resetPassword(Long id);
} 