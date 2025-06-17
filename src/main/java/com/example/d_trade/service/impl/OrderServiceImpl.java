package com.example.d_trade.service.impl;

import com.example.d_trade.dto.OrderDTO;
import com.example.d_trade.entity.Message;
import com.example.d_trade.entity.Order;
import com.example.d_trade.entity.Product;
import com.example.d_trade.entity.User;
import com.example.d_trade.repository.MessageRepository;
import com.example.d_trade.repository.OrderRepository;
import com.example.d_trade.repository.ProductRepository;
import com.example.d_trade.repository.UserRepository;
import com.example.d_trade.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final MessageRepository messageRepository;

    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository,
                           ProductRepository productRepository, MessageRepository messageRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    @Cacheable(value = "orders", key = "#studentId + '-' + #type + '-' + #status + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<OrderDTO> getOrders(String studentId, String type, String status, Pageable pageable) {
        log.debug("获取订单列表: studentId={}, type={}, status={}, page={}, size={}", 
                 studentId, type, status, pageable.getPageNumber(), pageable.getPageSize());
        
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Page<Order> orders;
        if ("buy".equalsIgnoreCase(type)) {
            // 买家订单
            if (status != null && !status.isEmpty()) {
                try {
                    Order.Status orderStatus = Order.Status.valueOf(status);
                    orders = orderRepository.findByBuyerAndStatusOrderByCreateTimeDesc(user, orderStatus, pageable);
                } catch (IllegalArgumentException e) {
                    orders = orderRepository.findByBuyerOrderByCreateTimeDesc(user, pageable);
                }
            } else {
                orders = orderRepository.findByBuyerOrderByCreateTimeDesc(user, pageable);
            }
        } else if ("sell".equalsIgnoreCase(type)) {
            // 卖家订单
            if (status != null && !status.isEmpty()) {
                try {
                    Order.Status orderStatus = Order.Status.valueOf(status);
                    orders = orderRepository.findBySellerAndStatusOrderByCreateTimeDesc(user, orderStatus, pageable);
                } catch (IllegalArgumentException e) {
                    orders = orderRepository.findBySellerOrderByCreateTimeDesc(user, pageable);
                }
            } else {
                orders = orderRepository.findBySellerOrderByCreateTimeDesc(user, pageable);
            }
        } else {
            // 所有订单
            orders = orderRepository.findByBuyerOrSellerOrderByCreateTimeDesc(user, user, pageable);
        }

        return orders.map(OrderDTO::fromEntity);
    }

    @Override
    @Cacheable(value = "order", key = "#studentId + '-' + #id")
    public OrderDTO getOrderById(String studentId, Long id) {
        log.debug("获取订单详情: studentId={}, id={}", studentId, id);
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        // 验证用户是否是订单的买家或卖家
        if (!order.getBuyer().getId().equals(user.getId()) && !order.getSeller().getId().equals(user.getId())) {
            throw new AccessDeniedException("无权查看此订单");
        }

        return OrderDTO.fromEntity(order);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"orders", "order"}, allEntries = true)
    public OrderDTO updateOrderStatus(String studentId, Long id, String statusStr) {
        log.debug("更新订单状态: studentId={}, id={}, status={}", studentId, id, statusStr);
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        // 验证用户是否有权限更新订单状态
        boolean isBuyer = order.getBuyer().getId().equals(user.getId());
        boolean isSeller = order.getSeller().getId().equals(user.getId());
        if (!isBuyer && !isSeller) {
            throw new AccessDeniedException("无权更新此订单");
        }

        // 解析状态
        Order.Status newStatus;
        try {
            newStatus = Order.Status.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的订单状态");
        }

        // 验证状态变更是否合法
        validateStatusChange(order, newStatus, isBuyer, isSeller);

        // 更新订单状态
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        // 如果订单完成，更新商品状态
        if (newStatus == Order.Status.COMPLETED) {
            Product product = order.getProduct();
            product.setStatus(Product.Status.SOLD);
            productRepository.save(product);
        }

        // 异步发送消息通知
        CompletableFuture.runAsync(() -> {
            sendOrderStatusChangeNotification(order, newStatus, isBuyer);
        });

        return OrderDTO.fromEntity(updatedOrder);
    }

    private void validateStatusChange(Order order, Order.Status newStatus, boolean isBuyer, boolean isSeller) {
        Order.Status currentStatus = order.getStatus();

        // 只有待交易的订单才能被取消或完成
        if (currentStatus != Order.Status.PENDING) {
            throw new RuntimeException("只有待交易的订单才能被更新");
        }

        // 买家只能取消或确认完成订单
        if (isBuyer && newStatus != Order.Status.CANCELLED && newStatus != Order.Status.COMPLETED) {
            throw new RuntimeException("买家只能取消或确认完成订单");
        }

        // 卖家只能取消订单
        if (isSeller && newStatus != Order.Status.CANCELLED) {
            throw new RuntimeException("卖家只能取消订单");
        }
    }

    @Async
    private void sendOrderStatusChangeNotification(Order order, Order.Status newStatus, boolean isBuyer) {
        Message message = new Message();
        message.setType(Message.Type.ORDER_STATUS_CHANGE);
        
        // 确定消息接收者
        User receiver = isBuyer ? order.getSeller() : order.getBuyer();
        message.setReceiver(receiver);
        
        // 设置消息内容
        String statusText = newStatus == Order.Status.COMPLETED ? "已完成" : "已取消";
        message.setTitle("订单状态变更");
        message.setContent("您的订单 #" + order.getId() + " (" + order.getProduct().getTitle() + ") 已" + statusText);
        message.setOrder(order);
        message.setProduct(order.getProduct());
        
        messageRepository.save(message);
        log.debug("已发送订单状态变更通知: orderId={}, status={}", order.getId(), newStatus);
    }
} 