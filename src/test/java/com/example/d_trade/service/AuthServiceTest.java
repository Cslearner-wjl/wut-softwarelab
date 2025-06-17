package com.example.d_trade.service;

import com.example.d_trade.dto.UserDTO;
import com.example.d_trade.dto.request.LoginRequest;
import com.example.d_trade.dto.request.RegisterRequest;
import com.example.d_trade.dto.response.JwtResponse;
import com.example.d_trade.entity.User;
import com.example.d_trade.repository.UserRepository;
import com.example.d_trade.security.JwtUtils;
import com.example.d_trade.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 创建注册请求
        registerRequest = new RegisterRequest();
        registerRequest.setStudentId("2023001");
        registerRequest.setUsername("testUser");
        registerRequest.setPassword("password123");
        registerRequest.setNickname("测试用户");
        registerRequest.setContactInfo("13800138000");

        // 创建登录请求
        loginRequest = new LoginRequest();
        loginRequest.setStudentId("2023001");
        loginRequest.setPassword("password123");

        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setStudentId("2023001");
        testUser.setUsername("testUser");
        testUser.setPassword("encodedPassword");
        testUser.setNickname("测试用户");
        testUser.setContactInfo("13800138000");
        testUser.setRole(User.Role.USER);
        testUser.setEnabled(true);
    }

    @Test
    void testRegister() {
        // 设置模拟行为
        when(userRepository.existsByStudentId("2023001")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });

        // 调用测试方法
        UserDTO result = authService.register(registerRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals(registerRequest.getStudentId(), result.getStudentId());
        assertEquals(registerRequest.getUsername(), result.getUsername());
        assertEquals(registerRequest.getNickname(), result.getNickname());
        assertEquals(registerRequest.getContactInfo(), result.getContactInfo());
        assertEquals(User.Role.USER, result.getRole());

        // 验证保存方法被调用
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegister_StudentIdExists() {
        // 设置模拟行为
        when(userRepository.existsByStudentId("2023001")).thenReturn(true);

        // 验证异常
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("该学号已被注册", exception.getMessage());

        // 验证保存方法未被调用
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin() {
        // 设置模拟行为
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.getName()).thenReturn("2023001");
        when(jwtUtils.generateToken(userDetails)).thenReturn("jwtToken");
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(testUser));

        // 调用测试方法
        JwtResponse result = authService.login(loginRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals("jwtToken", result.getToken());
        assertEquals("Bearer", result.getType());
        assertNotNull(result.getUser());
        assertEquals(testUser.getStudentId(), result.getUser().getStudentId());
        assertEquals(testUser.getUsername(), result.getUser().getUsername());
    }
} 