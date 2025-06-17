package com.example.d_trade.controller;

import com.example.d_trade.dto.MessageDTO;
import com.example.d_trade.dto.response.ApiResponse;
import com.example.d_trade.service.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@PreAuthorize("isAuthenticated()")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MessageDTO>>> getMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Boolean read,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MessageDTO> messages = messageService.getMessages(userDetails.getUsername(), read, pageable);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MessageDTO>> getMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        MessageDTO message = messageService.getMessageById(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<MessageDTO>> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        MessageDTO message = messageService.markAsRead(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("消息已标记为已读", message));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        messageService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("所有消息已标记为已读", null));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long count = messageService.getUnreadCount(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(count));
    }
} 