package com.example.d_trade.service;

import com.example.d_trade.dto.MessageDTO;
import com.example.d_trade.entity.Message;
import com.example.d_trade.entity.User;
import com.example.d_trade.repository.MessageRepository;
import com.example.d_trade.repository.UserRepository;
import com.example.d_trade.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageServiceImpl messageService;

    private User testUser;
    private Message testMessage;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setStudentId("2023001");
        testUser.setRole(User.Role.USER);

        // 创建测试消息
        testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setType(Message.Type.PRODUCT_INTEREST);
        testMessage.setTitle("测试消息");
        testMessage.setContent("这是一条测试消息");
        testMessage.setRead(false);
        testMessage.setReceiver(testUser);
        testMessage.setCreateTime(LocalDateTime.now());
    }

    @Test
    void testGetMessages() {
        // 创建测试消息列表
        List<Message> messageList = new ArrayList<>();
        messageList.add(testMessage);
        
        // 创建分页结果
        Page<Message> messagePage = new PageImpl<>(messageList);
        
        // 设置模拟行为
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(testUser));
        when(messageRepository.findByReceiverOrderByCreateTimeDesc(eq(testUser), any(Pageable.class)))
            .thenReturn(messagePage);

        // 调用测试方法
        Pageable pageable = PageRequest.of(0, 10);
        Page<MessageDTO> result = messageService.getMessages("2023001", null, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testMessage.getId(), result.getContent().get(0).getId());
        assertEquals(testMessage.getTitle(), result.getContent().get(0).getTitle());
        assertEquals(testMessage.getContent(), result.getContent().get(0).getContent());
        assertEquals(testMessage.getType(), result.getContent().get(0).getType());
        assertEquals(testMessage.isRead(), result.getContent().get(0).isRead());
    }

    @Test
    void testGetMessagesWithReadFilter() {
        // 创建测试消息列表
        List<Message> messageList = new ArrayList<>();
        messageList.add(testMessage);
        
        // 创建分页结果
        Page<Message> messagePage = new PageImpl<>(messageList);
        
        // 设置模拟行为
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(testUser));
        when(messageRepository.findByReceiverAndIsReadOrderByCreateTimeDesc(eq(testUser), eq(false), any(Pageable.class)))
            .thenReturn(messagePage);

        // 调用测试方法
        Pageable pageable = PageRequest.of(0, 10);
        Page<MessageDTO> result = messageService.getMessages("2023001", false, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testMessage.getId(), result.getContent().get(0).getId());
        assertFalse(result.getContent().get(0).isRead());
    }

    @Test
    void testGetMessageById() {
        // 设置模拟行为
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(testUser));
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));

        // 调用测试方法
        MessageDTO result = messageService.getMessageById("2023001", 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(testMessage.getId(), result.getId());
        assertEquals(testMessage.getTitle(), result.getTitle());
        assertEquals(testMessage.getContent(), result.getContent());
        assertEquals(testMessage.getType(), result.getType());
        assertEquals(testMessage.isRead(), result.isRead());
    }

    @Test
    void testGetMessageById_AccessDenied() {
        // 创建另一个用户
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setStudentId("2023002");

        // 设置模拟行为
        when(userRepository.findByStudentId("2023002")).thenReturn(Optional.of(anotherUser));
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));

        // 验证异常
        Exception exception = assertThrows(AccessDeniedException.class, () -> {
            messageService.getMessageById("2023002", 1L);
        });

        assertEquals("无权查看此消息", exception.getMessage());
    }

    @Test
    void testMarkAsRead() {
        // 设置模拟行为
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(testUser));
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message savedMessage = invocation.getArgument(0);
            savedMessage.setRead(true);
            return savedMessage;
        });

        // 调用测试方法
        MessageDTO result = messageService.markAsRead("2023001", 1L);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isRead());
        
        // 验证保存方法被调用
        verify(messageRepository, times(1)).save(testMessage);
    }

    @Test
    void testMarkAsRead_AccessDenied() {
        // 创建另一个用户
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setStudentId("2023002");

        // 设置模拟行为
        when(userRepository.findByStudentId("2023002")).thenReturn(Optional.of(anotherUser));
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));

        // 验证异常
        Exception exception = assertThrows(AccessDeniedException.class, () -> {
            messageService.markAsRead("2023002", 1L);
        });

        assertEquals("无权操作此消息", exception.getMessage());
    }

    @Test
    void testMarkAllAsRead() {
        // 创建测试消息列表
        List<Message> messageList = new ArrayList<>();
        messageList.add(testMessage);
        
        // 创建分页结果
        Page<Message> messagePage = new PageImpl<>(messageList);
        
        // 设置模拟行为
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(testUser));
        when(messageRepository.findByReceiverAndIsReadOrderByCreateTimeDesc(eq(testUser), eq(false), any(Pageable.class)))
            .thenReturn(messagePage);
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 调用测试方法
        messageService.markAllAsRead("2023001");

        // 验证保存方法被调用
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void testGetUnreadCount() {
        // 设置模拟行为
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(testUser));
        when(messageRepository.countByReceiverAndIsRead(testUser, false)).thenReturn(5L);

        // 调用测试方法
        Long result = messageService.getUnreadCount("2023001");

        // 验证结果
        assertEquals(5L, result);
    }
} 