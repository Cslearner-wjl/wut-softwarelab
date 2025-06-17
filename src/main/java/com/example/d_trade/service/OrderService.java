package com.example.d_trade.service;

import com.example.d_trade.dto.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    
    /**
     * 获取用户相关订单
     * @param studentId 学号
     * @param type 订单类型（buy/sell）
     * @param status 订单状态
     * @param pageable 分页
     * @return 订单分页列表
     */
    Page<OrderDTO> getOrders(String studentId, String type, String status, Pageable pageable);
    
    /**
     * 根据ID获取订单详情
     * @param studentId 学号
     * @param id 订单ID
     * @return 订单DTO
     */
    OrderDTO getOrderById(String studentId, Long id);
    
    /**
     * 更新订单状态
     * @param studentId 学号
     * @param id 订单ID
     * @param status 新状态
     * @return 更新后的订单DTO
     */
    OrderDTO updateOrderStatus(String studentId, Long id, String status);
} 