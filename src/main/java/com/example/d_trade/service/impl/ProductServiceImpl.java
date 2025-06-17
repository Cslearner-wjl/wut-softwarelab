package com.example.d_trade.service.impl;

import com.example.d_trade.dto.ProductDTO;
import com.example.d_trade.dto.request.ProductRequest;
import com.example.d_trade.entity.Message;
import com.example.d_trade.entity.Order;
import com.example.d_trade.entity.Product;
import com.example.d_trade.entity.User;
import com.example.d_trade.repository.MessageRepository;
import com.example.d_trade.repository.OrderRepository;
import com.example.d_trade.repository.ProductRepository;
import com.example.d_trade.repository.UserRepository;
import com.example.d_trade.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Value("${app.upload-dir}")
    private String uploadDir;

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final MessageRepository messageRepository;

    public ProductServiceImpl(ProductRepository productRepository, UserRepository userRepository,
                             OrderRepository orderRepository, MessageRepository messageRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    @Cacheable(value = "products", key = "#keyword + '-' + #status + '-' + #sort + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductDTO> getProducts(String keyword, String status, String sort, Pageable pageable) {
        log.debug("获取商品列表: keyword={}, status={}, sort={}, page={}, size={}", 
                 keyword, status, sort, pageable.getPageNumber(), pageable.getPageSize());
        
        // 处理排序
        Pageable sortedPageable = pageable;
        if (sort != null) {
            switch (sort) {
                case "price_asc":
                    sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                            Sort.by(Sort.Direction.ASC, "price"));
                    break;
                case "price_desc":
                    sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                            Sort.by(Sort.Direction.DESC, "price"));
                    break;
                case "newest":
                    sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                            Sort.by(Sort.Direction.DESC, "createTime"));
                    break;
                default:
                    break;
            }
        }

        // 处理状态
        Product.Status productStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                productStatus = Product.Status.valueOf(status);
            } catch (IllegalArgumentException e) {
                log.warn("无效的商品状态: {}", status);
                productStatus = Product.Status.AVAILABLE;
            }
        } else {
            productStatus = Product.Status.AVAILABLE;
        }

        // 执行查询
        Page<Product> products;
        try {
            if (keyword != null && !keyword.isEmpty()) {
                log.debug("按关键词和状态查询商品: keyword={}, status={}", keyword, productStatus);
                products = productRepository.findByTitleContainingAndStatus(keyword, productStatus, sortedPageable);
            } else {
                log.debug("按状态查询商品: status={}", productStatus);
                products = productRepository.findByStatusOrderByCreateTimeDesc(productStatus, sortedPageable);
            }
            log.debug("查询结果: 总数={}", products.getTotalElements());
        } catch (Exception e) {
            log.error("查询商品时发生错误", e);
            throw new RuntimeException("查询商品失败: " + e.getMessage(), e);
        }

        // 转换为DTO
        return products.map(ProductDTO::fromEntity);
    }

    @Override
    @Cacheable(value = "product", key = "#id")
    public ProductDTO getProductById(Long id) {
        log.debug("根据ID获取商品: id={}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        return ProductDTO.fromEntity(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "product", "userProducts"}, allEntries = true)
    public ProductDTO createProduct(String studentId, ProductRequest productRequest) {
        log.debug("创建商品: studentId={}, title={}", studentId, productRequest.getTitle());
        User seller = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Product product = new Product();
        product.setTitle(productRequest.getTitle());
        product.setPrice(productRequest.getPrice());
        product.setDescription(productRequest.getDescription());
        product.setTradeLocation(productRequest.getTradeLocation());
        product.setImagePaths(productRequest.getImagePaths());
        product.setStatus(Product.Status.AVAILABLE);
        product.setSeller(seller);

        Product savedProduct = productRepository.save(product);
        return ProductDTO.fromEntity(savedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ProductDTO updateProduct(String studentId, Long id, ProductRequest productRequest) {
        log.debug("更新商品: studentId={}, id={}", studentId, id);
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 验证是否是商品的发布者
        if (!product.getSeller().getId().equals(user.getId())) {
            throw new AccessDeniedException("无权修改此商品");
        }

        // 更新商品信息
        product.setTitle(productRequest.getTitle());
        product.setPrice(productRequest.getPrice());
        product.setDescription(productRequest.getDescription());
        product.setTradeLocation(productRequest.getTradeLocation());
        product.setImagePaths(productRequest.getImagePaths());

        Product updatedProduct = productRepository.save(product);
        return ProductDTO.fromEntity(updatedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public void deleteProduct(String studentId, Long id) {
        log.debug("删除商品: studentId={}, id={}", studentId, id);
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 验证是否是商品的发布者或管理员
        if (!product.getSeller().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("无权删除此商品");
        }

        // 检查商品是否有关联的订单
        long orderCount = productRepository.countOrdersByProductId(id);
        if (orderCount > 0) {
            throw new IllegalStateException("无法删除，该商品已有关联订单");
        }
        
        // 删除商品
        productRepository.delete(product);
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> files) {
        log.debug("上传图片: 文件数量={}", files.size());
        List<String> imagePaths = new ArrayList<>();

        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 处理每个文件
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }

                // 生成唯一文件名
                String originalFileName = file.getOriginalFilename();
                String fileExtension = originalFileName != null && originalFileName.lastIndexOf(".") > 0 
                    ? originalFileName.substring(originalFileName.lastIndexOf(".")) 
                    : ".jpg";
                String newFileName = UUID.randomUUID() + fileExtension;

                // 保存文件 - 使用NIO的标准复制选项提高性能
                Path filePath = uploadPath.resolve(newFileName);
                Files.copy(file.getInputStream(), filePath, 
                           java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                // 添加到路径列表
                imagePaths.add("/uploads/" + newFileName);
            }
        } catch (IOException e) {
            throw new RuntimeException("图片上传失败", e);
        }

        return imagePaths;
    }

    @Override
    @Transactional
    public void markInterest(String studentId, Long productId) {
        log.debug("标记感兴趣: studentId={}, productId={}", studentId, productId);
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 检查商品是否可用
        if (product.getStatus() != Product.Status.AVAILABLE) {
            throw new RuntimeException("该商品不可用");
        }

        // 检查是否是自己的商品
        if (product.getSeller().getId().equals(user.getId())) {
            throw new RuntimeException("不能对自己的商品标记感兴趣");
        }

        // 创建订单
        Order order = new Order();
        order.setProduct(product);
        order.setBuyer(user);
        order.setSeller(product.getSeller());
        order.setStatus(Order.Status.PENDING);
        Order savedOrder = orderRepository.save(order);

        // 异步发送消息通知卖家
        CompletableFuture.runAsync(() -> {
            sendInterestNotification(user, product, savedOrder);
        });
    }

    private void sendInterestNotification(User user, Product product, Order order) {
        Message message = new Message();
        message.setType(Message.Type.PRODUCT_INTEREST);
        message.setTitle("有人对您的商品感兴趣");
        message.setContent("用户 " + user.getUsername() + " 对您的商品 \"" + product.getTitle() + "\" 感兴趣，请及时联系。");
        message.setReceiver(product.getSeller());
        message.setProduct(product);
        message.setOrder(order);
        messageRepository.save(message);
        log.debug("已发送商品感兴趣通知: orderId={}", order.getId());
    }

    @Override
    @Cacheable(value = "userProducts", key = "#studentId + '-' + #status + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductDTO> getUserProducts(String studentId, String status, Pageable pageable) {
        log.debug("获取用户发布的商品: studentId={}, status={}, page={}, size={}", 
                 studentId, status, pageable.getPageNumber(), pageable.getPageSize());
        
        User seller = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Page<Product> products;
        if (status != null && !status.isEmpty()) {
            try {
                Product.Status productStatus = Product.Status.valueOf(status);
                products = productRepository.findBySellerAndStatus(seller, productStatus, pageable);
            } catch (IllegalArgumentException e) {
                products = productRepository.findBySeller(seller, pageable);
            }
        } else {
            products = productRepository.findBySeller(seller, pageable);
        }
        
        return products.map(ProductDTO::fromEntity);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public void adminRemoveProduct(Long id) {
        log.debug("管理员删除商品: id={}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        // 将商品状态设置为已删除
        product.setStatus(Product.Status.REMOVED);
        
        // 保存更新
        productRepository.save(product);
        
        // 通知卖家商品被管理员删除
        sendProductRemovedNotification(product);
    }
    
    /**
     * 发送商品被管理员删除的通知
     * @param product 被删除的商品
     */
    private void sendProductRemovedNotification(Product product) {
        User seller = product.getSeller();
        
        Message message = new Message();
        message.setTitle("商品已被管理员删除");
        message.setContent("您发布的商品\"" + product.getTitle() + "\"因违反平台规则已被管理员删除。");
        message.setReceiver(seller);
        message.setRead(false);
        message.setType(Message.Type.SYSTEM);
        message.setProduct(product);
        
        messageRepository.save(message);
    }
} 