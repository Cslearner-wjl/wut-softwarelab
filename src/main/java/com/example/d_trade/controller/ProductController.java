package com.example.d_trade.controller;

import com.example.d_trade.dto.ProductDTO;
import com.example.d_trade.dto.request.ProductRequest;
import com.example.d_trade.dto.response.ApiResponse;
import com.example.d_trade.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "AVAILABLE") String status,
            @RequestParam(required = false) String sort,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ProductDTO> products = productService.getProducts(keyword, status, sort, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProduct(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProductRequest productRequest) {
        ProductDTO createdProduct = productService.createProduct(userDetails.getUsername(), productRequest);
        return ResponseEntity.ok(ApiResponse.success("商品发布成功", createdProduct));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest productRequest) {
        ProductDTO updatedProduct = productService.updateProduct(userDetails.getUsername(), id, productRequest);
        return ResponseEntity.ok(ApiResponse.success("商品更新成功", updatedProduct));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        productService.deleteProduct(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("商品已成功删除", null));
    }

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<String>>> uploadImages(
            @RequestParam("files") List<MultipartFile> files) {
        List<String> imagePaths = productService.uploadImages(files);
        return ResponseEntity.ok(ApiResponse.success("图片上传成功", imagePaths));
    }

    @PostMapping("/{id}/interest")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markInterest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        productService.markInterest(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("已成功标记感兴趣", null));
    }

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getUserProducts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ProductDTO> products = productService.getUserProducts(userDetails.getUsername(), status, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
} 