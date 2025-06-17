package com.example.d_trade.repository;

import com.example.d_trade.entity.Message;
import com.example.d_trade.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    Page<Message> findByReceiverOrderByCreateTimeDesc(User receiver, Pageable pageable);
    
    Page<Message> findByReceiverAndIsReadOrderByCreateTimeDesc(User receiver, boolean isRead, Pageable pageable);
    
    long countByReceiverAndIsRead(User receiver, boolean isRead);
} 