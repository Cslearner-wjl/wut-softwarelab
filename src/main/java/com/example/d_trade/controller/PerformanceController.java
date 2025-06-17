package com.example.d_trade.controller;

import com.example.d_trade.dto.response.ApiResponse;
import com.example.d_trade.utils.PerformanceMonitor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 性能监控控制器
 * 用于查看应用性能统计数据
 */
@RestController
@RequestMapping("/api/admin/performance")
@PreAuthorize("hasRole('ADMIN')")
public class PerformanceController {

    private final PerformanceMonitor performanceMonitor;

    public PerformanceController(PerformanceMonitor performanceMonitor) {
        this.performanceMonitor = performanceMonitor;
    }

    /**
     * 获取性能统计数据
     * @return 性能统计数据
     */
    @GetMapping
    public ApiResponse<Map<String, Map<String, Object>>> getPerformanceStats() {
        return ApiResponse.success("获取性能统计数据成功", performanceMonitor.getMethodStats());
    }

    /**
     * 重置性能统计数据
     * @return 操作结果
     */
    @PostMapping("/reset")
    public ApiResponse<Void> resetPerformanceStats() {
        performanceMonitor.reset();
        return ApiResponse.success("重置性能统计数据成功", null);
    }
} 