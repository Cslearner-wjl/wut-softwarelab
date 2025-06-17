package com.example.d_trade.service;

import com.example.d_trade.dto.ProductDTO;
import com.example.d_trade.dto.request.ProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    
    /**
     * 获取商品列表
     * @param keyword 关键字
     * @param status 状态
     * @param sort 排序
     * @param pageable 分页
     * @return 商品分页列表
     */
    Page<ProductDTO> getProducts(String keyword, String status, String sort, Pageable pageable);
    
    /**
     * 根据ID获取商品详情
     * @param id 商品ID
     * @return 商品DTO
     */
    ProductDTO getProductById(Long id);
    
    /**
     * 创建商品
     * @param studentId 学号
     * @param productRequest 商品请求
     * @return 创建的商品DTO
     */
    ProductDTO createProduct(String studentId, ProductRequest productRequest);
    
    /**
     * 更新商品
     * @param studentId 学号
     * @param id 商品ID
     * @param productRequest 商品请求
     * @return 更新后的商品DTO
     */
    ProductDTO updateProduct(String studentId, Long id, ProductRequest productRequest);
    
    /**
     * 删除商品
     * @param studentId 学号
     * @param id 商品ID
     */
    void deleteProduct(String studentId, Long id);
    
    /**
     * 上传商品图片
     * @param files 图片文件列表
     * @return 图片路径列表
     */
    List<String> uploadImages(List<MultipartFile> files);
    
    /**
     * 标记对商品感兴趣
     * @param studentId 学号
     * @param productId 商品ID
     */
    void markInterest(String studentId, Long productId);
    
    /**
     * 获取用户发布的商品
     * @param studentId 学号
     * @param status 状态
     * @param pageable 分页
     * @return 商品分页列表
     */
    Page<ProductDTO> getUserProducts(String studentId, String status, Pageable pageable);
    
    /**
     * 管理员删除商品
     * @param id 商品ID
     */
    void adminRemoveProduct(Long id);
} 