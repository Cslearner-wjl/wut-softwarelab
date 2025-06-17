package com.example.d_trade.service;

import com.example.d_trade.dto.UserDTO;
import com.example.d_trade.dto.request.PasswordChangeRequest;
import com.example.d_trade.dto.request.UserUpdateRequest;
import com.example.d_trade.entity.User;
import com.example.d_trade.repository.UserRepository;
import com.example.d_trade.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setStudentId("2023001");
        testUser.setPassword("encodedPassword");
        testUser.setNickname("测试用户");
        testUser.setContactInfo("13800138000");
        testUser.setRole(User.Role.USER);
        testUser.setEnabled(true);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());
    }

    @Test
    void testGetByStudentId() {
        // 设置模拟行为
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(testUser));

        // 调用测试方法
        UserDTO result = userService.getByStudentId("2023001");

        // 验证结果
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getStudentId(), result.getStudentId());
        assertEquals(testUser.getNickname(), result.getNickname());
        assertEquals(testUser.getContactInfo(), result.getContactInfo());
        assertEquals(testUser.getRole(), result.getRole());
    }

    @Test
    void testGetByStudentId_NotFound() {
        // 设置模拟行为
        when(userRepository.findByStudentId("nonexistent")).thenReturn(Optional.empty());

        // 验证异常
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.getByStudentId("nonexistent");
        });

        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    void testUpdateProfile() {
        // 创建更新请求
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setNickname("新昵称");
        updateRequest.setContactInfo("13900139000");

        // 设置模拟行为
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setUpdateTime(LocalDateTime.now());
            return savedUser;
        });

        // 调用测试方法
        UserDTO result = userService.updateProfile("2023001", updateRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals(updateRequest.getNickname(), result.getNickname());
        assertEquals(updateRequest.getContactInfo(), result.getContactInfo());
        
        // 验证保存方法被调用
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testChangePassword_Success() {
        // 创建密码修改请求
        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setOldPassword("oldPassword");
        passwordChangeRequest.setNewPassword("newPassword");
        passwordChangeRequest.setConfirmPassword("newPassword");

        // 设置模拟行为
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

        // 调用测试方法
        userService.changePassword("2023001", passwordChangeRequest);

        // 验证密码被更新
        assertEquals("newEncodedPassword", testUser.getPassword());
        
        // 验证保存方法被调用
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testChangePassword_PasswordMismatch() {
        // 创建密码不匹配的请求
        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setOldPassword("oldPassword");
        passwordChangeRequest.setNewPassword("newPassword");
        passwordChangeRequest.setConfirmPassword("differentPassword");

        // 验证异常
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.changePassword("2023001", passwordChangeRequest);
        });

        assertEquals("新密码和确认密码不一致", exception.getMessage());
        
        // 验证保存方法未被调用
        verify(userRepository, never()).save(any());
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        // 创建密码修改请求
        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setOldPassword("wrongPassword");
        passwordChangeRequest.setNewPassword("newPassword");
        passwordChangeRequest.setConfirmPassword("newPassword");

        // 设置模拟行为
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // 验证异常
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.changePassword("2023001", passwordChangeRequest);
        });

        assertEquals("原密码不正确", exception.getMessage());
        
        // 验证保存方法未被调用
        verify(userRepository, never()).save(any());
    }
} 