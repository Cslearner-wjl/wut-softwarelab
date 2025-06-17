package com.example.d_trade.controller;

import com.example.d_trade.dto.ProductDTO;
import com.example.d_trade.dto.response.ApiResponse;
import com.example.d_trade.service.ProductService;
import com.example.d_trade.utils.PerformanceMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 性能测试控制器
 */
@RestController
@RequestMapping("/api/admin/performance-test")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class PerformanceTestController {

    private final ProductService productService;
    private final PerformanceMonitor performanceMonitor;

    public PerformanceTestController(ProductService productService, PerformanceMonitor performanceMonitor) {
        this.productService = productService;
        this.performanceMonitor = performanceMonitor;
    }

    /**
     * 测试商品列表查询性能
     * @param concurrency 并发数
     * @param requests 请求数
     * @return 测试结果
     */
    @PostMapping("/products")
    public ApiResponse<Map<String, Object>> testProductsPerformance(
            @RequestParam(defaultValue = "10") int concurrency,
            @RequestParam(defaultValue = "100") int requests) {
        
        log.info("开始性能测试: 并发数={}, 请求数={}", concurrency, requests);
        performanceMonitor.reset();
        
        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        // 记录统计信息
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        
        // 提交任务
        for (int i = 0; i < requests; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // 执行查询
                    Page<ProductDTO> products = productService.getProducts(
                            null, "AVAILABLE", "newest", PageRequest.of(0, 10));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("查询失败", e);
                    errorCount.incrementAndGet();
                }
            }, executor);
            
            futures.add(future);
        }
        
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // 关闭线程池
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // 计算统计信息
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double requestsPerSecond = requests * 1000.0 / totalTime;
        
        // 构建结果
        Map<String, Object> result = new HashMap<>();
        result.put("concurrency", concurrency);
        result.put("requests", requests);
        result.put("successCount", successCount.get());
        result.put("errorCount", errorCount.get());
        result.put("totalTimeMs", totalTime);
        result.put("requestsPerSecond", requestsPerSecond);
        result.put("methodStats", performanceMonitor.getMethodStats());
        
        log.info("性能测试完成: 总时间={}ms, QPS={}", totalTime, requestsPerSecond);
        
        return ApiResponse.success("性能测试完成", result);
    }
} 