package com.example.d_trade.service.impl;

import com.example.d_trade.dto.MessageDTO;
import com.example.d_trade.entity.Message;
import com.example.d_trade.entity.User;
import com.example.d_trade.repository.MessageRepository;
import com.example.d_trade.repository.UserRepository;
import com.example.d_trade.service.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageServiceImpl(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Page<MessageDTO> getMessages(String studentId, Boolean read, Pageable pageable) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Page<Message> messages;
        if (read != null) {
            messages = messageRepository.findByReceiverAndIsReadOrderByCreateTimeDesc(user, read, pageable);
        } else {
            messages = messageRepository.findByReceiverOrderByCreateTimeDesc(user, pageable);
        }

        return messages.map(MessageDTO::fromEntity);
    }

    @Override
    public MessageDTO getMessageById(String studentId, Long id) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("消息不存在"));

        // 验证用户是否是消息的接收者
        if (!message.getReceiver().getId().equals(user.getId())) {
            throw new AccessDeniedException("无权查看此消息");
        }

        return MessageDTO.fromEntity(message);
    }

    @Override
    @Transactional
    public MessageDTO markAsRead(String studentId, Long id) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("消息不存在"));

        // 验证用户是否是消息的接收者
        if (!message.getReceiver().getId().equals(user.getId())) {
            throw new AccessDeniedException("无权操作此消息");
        }

        // 标记为已读
        message.setRead(true);
        Message updatedMessage = messageRepository.save(message);

        return MessageDTO.fromEntity(updatedMessage);
    }

    @Override
    @Transactional
    public void markAllAsRead(String studentId) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 获取用户所有未读消息
        Page<Message> unreadMessages = messageRepository.findByReceiverAndIsReadOrderByCreateTimeDesc(
                user, false, Pageable.unpaged());

        // 标记为已读
        unreadMessages.forEach(message -> {
            message.setRead(true);
            messageRepository.save(message);
        });
    }

    @Override
    public Long getUnreadCount(String studentId) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return messageRepository.countByReceiverAndIsRead(user, false);
    }
}
