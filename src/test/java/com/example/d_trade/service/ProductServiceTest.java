package com.example.d_trade.service;

import com.example.d_trade.dto.ProductDTO;
import com.example.d_trade.dto.request.ProductRequest;
import com.example.d_trade.entity.Product;
import com.example.d_trade.entity.User;
import com.example.d_trade.repository.MessageRepository;
import com.example.d_trade.repository.OrderRepository;
import com.example.d_trade.repository.ProductRepository;
import com.example.d_trade.repository.UserRepository;
import com.example.d_trade.service.impl.ProductServiceImpl;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private User testUser;
    private Product testProduct;
    private ProductRequest testProductRequest;

    @BeforeEach
    void setUp() {
        // 设置上传目录
        ReflectionTestUtils.setField(productService, "uploadDir", "uploads/images/");

        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setStudentId("2023001");
        testUser.setRole(User.Role.USER);

        // 创建测试商品
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setTitle("测试商品");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setDescription("这是一个测试商品");
        testProduct.setTradeLocation("图书馆");
        testProduct.setStatus(Product.Status.AVAILABLE);
        testProduct.setSeller(testUser);
        testProduct.setImagePaths(Arrays.asList("/uploads/image1.jpg", "/uploads/image2.jpg"));
        testProduct.setCreateTime(LocalDateTime.now());
        testProduct.setUpdateTime(LocalDateTime.now());

        // 创建商品请求
        testProductRequest = new ProductRequest();
        testProductRequest.setTitle("测试商品");
        testProductRequest.setPrice(new BigDecimal("99.99"));
        testProductRequest.setDescription("这是一个测试商品");
        testProductRequest.setTradeLocation("图书馆");
        testProductRequest.setImagePaths(Arrays.asList("/uploads/image1.jpg", "/uploads/image2.jpg"));
    }

    @Test
    void testGetProductById() {
        // 设置模拟行为
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // 调用测试方法
        ProductDTO result = productService.getProductById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        assertEquals(testProduct.getTitle(), result.getTitle());
        assertEquals(testProduct.getPrice(), result.getPrice());
        assertEquals(testProduct.getDescription(), result.getDescription());
        assertEquals(testProduct.getTradeLocation(), result.getTradeLocation());
        assertEquals(testProduct.getStatus(), result.getStatus());
        assertEquals(testProduct.getImagePaths(), result.getImagePaths());
    }

    @Test
    void testGetProductById_NotFound() {
        // 设置模拟行为
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // 验证异常
        Exception exception = assertThrows(RuntimeException.class, () -> {
            productService.getProductById(999L);
        });

        assertEquals("商品不存在", exception.getMessage());
    }

    @Test
    void testCreateProduct() {
        // 设置模拟行为
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(testUser));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product savedProduct = invocation.getArgument(0);
            savedProduct.setId(1L);
            savedProduct.setCreateTime(LocalDateTime.now());
            savedProduct.setUpdateTime(LocalDateTime.now());
            return savedProduct;
        });

        // 调用测试方法
        ProductDTO result = productService.createProduct("2023001", testProductRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals(testProductRequest.getTitle(), result.getTitle());
        assertEquals(testProductRequest.getPrice(), result.getPrice());
        assertEquals(testProductRequest.getDescription(), result.getDescription());
        assertEquals(testProductRequest.getTradeLocation(), result.getTradeLocation());
        assertEquals(testProductRequest.getImagePaths(), result.getImagePaths());
        assertEquals(Product.Status.AVAILABLE, result.getStatus());
        assertEquals(testUser.getStudentId(), result.getSeller().getStudentId());
    }

    @Test
    void testUpdateProduct() {
        // 设置模拟行为
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 修改请求数据
        testProductRequest.setTitle("更新后的标题");
        testProductRequest.setPrice(new BigDecimal("199.99"));

        // 调用测试方法
        ProductDTO result = productService.updateProduct("2023001", 1L, testProductRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals(testProductRequest.getTitle(), result.getTitle());
        assertEquals(testProductRequest.getPrice(), result.getPrice());
    }

    @Test
    void testUpdateProduct_AccessDenied() {
        // 创建另一个用户
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setStudentId("2023002");

        // 设置模拟行为
        when(userRepository.findByStudentId("2023002")).thenReturn(Optional.of(anotherUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // 验证异常
        Exception exception = assertThrows(AccessDeniedException.class, () -> {
            productService.updateProduct("2023002", 1L, testProductRequest);
        });

        assertEquals("无权修改此商品", exception.getMessage());
    }

    @Test
    void testDeleteProduct() {
        // 设置模拟行为
        when(userRepository.findByStudentId("2023001")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 调用测试方法
        productService.deleteProduct("2023001", 1L);

        // 验证商品状态被设置为已下架
        verify(productRepository, times(1)).save(argThat(product -> 
            product.getStatus() == Product.Status.REMOVED
        ));
    }

    @Test
    void testGetProducts() {
        // 创建测试商品列表
        List<Product> productList = new ArrayList<>();
        productList.add(testProduct);
        
        // 创建分页结果
        Page<Product> productPage = new PageImpl<>(productList);
        
        // 设置模拟行为
        when(productRepository.findByStatusOrderByCreateTimeDesc(eq(Product.Status.AVAILABLE), any(Pageable.class)))
            .thenReturn(productPage);

        // 调用测试方法
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductDTO> result = productService.getProducts(null, "AVAILABLE", null, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testProduct.getTitle(), result.getContent().get(0).getTitle());
    }
} 