package com.example.d_trade.service;

import com.example.d_trade.dto.MessageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {
    
    /**
     * 获取用户消息列表
     * @param studentId 学号
     * @param read 是否已读
     * @param pageable 分页
     * @return 消息分页列表
     */
    Page<MessageDTO> getMessages(String studentId, Boolean read, Pageable pageable);
    
    /**
     * 根据ID获取消息详情
     * @param studentId 学号
     * @param id 消息ID
     * @return 消息DTO
     */
    MessageDTO getMessageById(String studentId, Long id);
    
    /**
     * 标记消息为已读
     * @param studentId 学号
     * @param id 消息ID
     * @return 更新后的消息DTO
     */
    MessageDTO markAsRead(String studentId, Long id);
    
    /**
     * 标记所有消息为已读
     * @param studentId 学号
     */
    void markAllAsRead(String studentId);
    
    /**
     * 获取未读消息数量
     * @param studentId 学号
     * @return 未读消息数量
     */
    Long getUnreadCount(String studentId);
} 