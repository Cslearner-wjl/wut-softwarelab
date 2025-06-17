package com.example.d_trade.service;

import com.example.d_trade.dto.OrderDTO;
import com.example.d_trade.entity.Order;
import com.example.d_trade.entity.Product;
import com.example.d_trade.entity.User;
import com.example.d_trade.repository.MessageRepository;
import com.example.d_trade.repository.OrderRepository;
import com.example.d_trade.repository.ProductRepository;
import com.example.d_trade.repository.UserRepository;
import com.example.d_trade.service.impl.OrderServiceImpl;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User buyer;
    private User seller;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        // 创建测试买家
        buyer = new User();
        buyer.setId(1L);
        buyer.setUsername("buyer");
        buyer.setStudentId("2023001");
        buyer.setRole(User.Role.USER);

        // 创建测试卖家
        seller = new User();
        seller.setId(2L);
        seller.setUsername("seller");
        seller.setStudentId("2023002");
        seller.setRole(User.Role.USER);

        // 创建测试商品
        product = new Product();
        product.setId(1L);
        product.setTitle("测试商品");
        product.setPrice(new BigDecimal("99.99"));
        product.setDescription("这是一个测试商品");
        product.setStatus(Product.Status.AVAILABLE);
        product.setSeller(seller);

        // 创建测试订单
        order = new Order();
        order.setId(1L);
        order.setProduct(product);
        order.setBuyer(buyer);
        order.setSeller(seller);
        order.setStatus(Order.Status.PENDING);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
    }

    @Test
    void testGetOrderById() {
        // 设置模拟行为
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // 调用测试方法
        OrderDTO result = orderService.getOrderById("2023001", 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(order.getId(), result.getId());
        assertEquals(order.getStatus(), result.getStatus());
        assertEquals(order.getProduct().getId(), result.getProduct().getId());
        assertEquals(order.getBuyer().getId(), result.getBuyer().getId());
        assertEquals(order.getSeller().getId(), result.getSeller().getId());
    }

    @Test
    void testGetOrderById_AccessDenied() {
        // 创建另一个用户
        User anotherUser = new User();
        anotherUser.setId(3L);
        anotherUser.setStudentId("2023003");

        // 设置模拟行为
        when(userRepository.findByStudentId("2023003")).thenReturn(Optional.of(anotherUser));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // 验证异常
        Exception exception = assertThrows(AccessDeniedException.class, () -> {
            orderService.getOrderById("2023003", 1L);
        });

        assertEquals("无权查看此订单", exception.getMessage());
    }

    @Test
    void testGetOrders_Buyer() {
        // 创建测试订单列表
        List<Order> orderList = new ArrayList<>();
        orderList.add(order);
        
        // 创建分页结果
        Page<Order> orderPage = new PageImpl<>(orderList);
        
        // 设置模拟行为
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(buyer));
        when(orderRepository.findByBuyerOrderByCreateTimeDesc(eq(buyer), any(Pageable.class)))
            .thenReturn(orderPage);

        // 调用测试方法
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderDTO> result = orderService.getOrders("2023001", "buy", null, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(order.getId(), result.getContent().get(0).getId());
    }

    @Test
    void testGetOrders_Seller() {
        // 创建测试订单列表
        List<Order> orderList = new ArrayList<>();
        orderList.add(order);
        
        // 创建分页结果
        Page<Order> orderPage = new PageImpl<>(orderList);
        
        // 设置模拟行为
        when(userRepository.findByStudentId("2023002")).thenReturn(Optional.of(seller));
        when(orderRepository.findBySellerOrderByCreateTimeDesc(eq(seller), any(Pageable.class)))
            .thenReturn(orderPage);

        // 调用测试方法
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderDTO> result = orderService.getOrders("2023002", "sell", null, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(order.getId(), result.getContent().get(0).getId());
    }

    @Test
    void testUpdateOrderStatus_BuyerCancel() {
        // 设置模拟行为
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // 调用测试方法
        OrderDTO result = orderService.updateOrderStatus("2023001", 1L, "CANCELLED");

        // 验证结果
        assertNotNull(result);
        assertEquals(Order.Status.CANCELLED, result.getStatus());
        
        // 验证消息通知
        verify(messageRepository, times(1)).save(any());
    }

    @Test
    void testUpdateOrderStatus_BuyerComplete() {
        // 设置模拟行为
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // 调用测试方法
        OrderDTO result = orderService.updateOrderStatus("2023001", 1L, "COMPLETED");

        // 验证结果
        assertNotNull(result);
        assertEquals(Order.Status.COMPLETED, result.getStatus());
        
        // 验证商品状态被更新
        verify(productRepository, times(1)).save(argThat(p -> 
            p.getStatus() == Product.Status.SOLD
        ));
        
        // 验证消息通知
        verify(messageRepository, times(1)).save(any());
    }

    @Test
    void testUpdateOrderStatus_InvalidStatus() {
        // 设置模拟行为
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // 验证异常
        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.updateOrderStatus("2023001", 1L, "INVALID_STATUS");
        });

        assertEquals("无效的订单状态", exception.getMessage());
    }

    @Test
    void testUpdateOrderStatus_AlreadyCompleted() {
        // 修改订单状态为已完成
        order.setStatus(Order.Status.COMPLETED);
        
        // 设置模拟行为
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // 验证异常
        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.updateOrderStatus("2023001", 1L, "CANCELLED");
        });

        assertEquals("只有待交易的订单才能被更新", exception.getMessage());
    }
} 