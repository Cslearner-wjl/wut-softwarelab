package com.example.d_trade.controller;

import com.example.d_trade.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * JProfiler性能测试报告控制器
 * 用于生成JVM性能报告
 */
@RestController
@RequestMapping("/api/admin/jprofiler-report")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class JProfilerReportController {

    /**
     * 获取JVM内存使用情况
     * @return JVM内存使用情况
     */
    @GetMapping("/memory")
    public ApiResponse<Map<String, Object>> getMemoryUsage() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
        
        Map<String, Object> memoryInfo = new HashMap<>();
        
        // 堆内存信息
        Map<String, Object> heapInfo = new HashMap<>();
        heapInfo.put("init", formatSize(heapMemoryUsage.getInit()));
        heapInfo.put("used", formatSize(heapMemoryUsage.getUsed()));
        heapInfo.put("committed", formatSize(heapMemoryUsage.getCommitted()));
        heapInfo.put("max", formatSize(heapMemoryUsage.getMax()));
        heapInfo.put("usagePercentage", getPercentage(heapMemoryUsage.getUsed(), heapMemoryUsage.getCommitted()));
        
        // 非堆内存信息
        Map<String, Object> nonHeapInfo = new HashMap<>();
        nonHeapInfo.put("init", formatSize(nonHeapMemoryUsage.getInit()));
        nonHeapInfo.put("used", formatSize(nonHeapMemoryUsage.getUsed()));
        nonHeapInfo.put("committed", formatSize(nonHeapMemoryUsage.getCommitted()));
        nonHeapInfo.put("max", formatSize(nonHeapMemoryUsage.getMax()));
        nonHeapInfo.put("usagePercentage", getPercentage(nonHeapMemoryUsage.getUsed(), nonHeapMemoryUsage.getCommitted()));
        
        memoryInfo.put("heap", heapInfo);
        memoryInfo.put("nonHeap", nonHeapInfo);
        memoryInfo.put("timestamp", System.currentTimeMillis());
        
        return ApiResponse.success("获取内存使用情况成功", memoryInfo);
    }
    
    /**
     * 获取线程信息
     * @return 线程信息
     */
    @GetMapping("/threads")
    public ApiResponse<Map<String, Object>> getThreadsInfo() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        
        Map<String, Object> threadInfo = new HashMap<>();
        threadInfo.put("threadCount", threadMXBean.getThreadCount());
        threadInfo.put("peakThreadCount", threadMXBean.getPeakThreadCount());
        threadInfo.put("totalStartedThreadCount", threadMXBean.getTotalStartedThreadCount());
        threadInfo.put("daemonThreadCount", threadMXBean.getDaemonThreadCount());
        threadInfo.put("timestamp", System.currentTimeMillis());
        
        return ApiResponse.success("获取线程信息成功", threadInfo);
    }
    
    /**
     * 获取JVM信息
     * @return JVM信息
     */
    @GetMapping("/jvm")
    public ApiResponse<Map<String, Object>> getJvmInfo() {
        Map<String, Object> jvmInfo = new HashMap<>();
        
        // 运行时信息
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> runtimeInfo = new HashMap<>();
        runtimeInfo.put("availableProcessors", runtime.availableProcessors());
        runtimeInfo.put("freeMemory", formatSize(runtime.freeMemory()));
        runtimeInfo.put("totalMemory", formatSize(runtime.totalMemory()));
        runtimeInfo.put("maxMemory", formatSize(runtime.maxMemory()));
        
        // JVM参数
        Map<String, String> jvmArgs = new HashMap<>();
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (arg.startsWith("-X") || arg.startsWith("-D") || arg.startsWith("-XX")) {
                String[] parts = arg.split("=", 2);
                jvmArgs.put(parts[0], parts.length > 1 ? parts[1] : "");
            }
        }
        
        jvmInfo.put("runtime", runtimeInfo);
        jvmInfo.put("jvmArgs", jvmArgs);
        jvmInfo.put("uptime", formatDuration(ManagementFactory.getRuntimeMXBean().getUptime()));
        jvmInfo.put("timestamp", System.currentTimeMillis());
        
        return ApiResponse.success("获取JVM信息成功", jvmInfo);
    }
    
    /**
     * 格式化内存大小
     * @param bytes 字节数
     * @return 格式化后的大小
     */
    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
    
    /**
     * 计算百分比
     * @param used 已使用
     * @param total 总量
     * @return 百分比
     */
    private String getPercentage(long used, long total) {
        if (total <= 0) {
            return "0%";
        }
        return String.format("%.2f%%", (used * 100.0) / total);
    }
    
    /**
     * 格式化时间
     * @param millis 毫秒
     * @return 格式化后的时间
     */
    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        return String.format("%d天 %02d:%02d:%02d", 
                days, hours % 24, minutes % 60, seconds % 60);
    }
} 