package com.example.d_trade.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能监控工具类
 * 用于记录方法执行时间和资源使用情况
 */
@Slf4j
@Component
public class PerformanceMonitor {

    private final Map<String, MethodStats> methodStatsMap = new ConcurrentHashMap<>();

    /**
     * 记录方法开始执行的时间
     * @param methodName 方法名称
     * @return 开始时间戳
     */
    public long start(String methodName) {
        long startTime = System.currentTimeMillis();
        log.debug("Method {} started at {}", methodName, startTime);
        return startTime;
    }

    /**
     * 记录方法结束执行的时间并计算执行时间
     * @param methodName 方法名称
     * @param startTime 开始时间戳
     * @return 执行时间（毫秒）
     */
    public long end(String methodName, long startTime) {
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        // 更新方法统计信息
        methodStatsMap.computeIfAbsent(methodName, k -> new MethodStats())
                      .addExecution(executionTime);
        
        log.debug("Method {} ended. Execution time: {} ms", methodName, executionTime);
        return executionTime;
    }

    /**
     * 获取所有方法的统计信息
     * @return 方法统计信息映射
     */
    public Map<String, Map<String, Object>> getMethodStats() {
        Map<String, Map<String, Object>> stats = new HashMap<>();
        
        methodStatsMap.forEach((methodName, methodStats) -> {
            Map<String, Object> stat = new HashMap<>();
            stat.put("totalExecutions", methodStats.getTotalExecutions());
            stat.put("totalExecutionTime", methodStats.getTotalExecutionTime());
            stat.put("averageExecutionTime", methodStats.getAverageExecutionTime());
            stat.put("maxExecutionTime", methodStats.getMaxExecutionTime());
            stat.put("minExecutionTime", methodStats.getMinExecutionTime());
            
            stats.put(methodName, stat);
        });
        
        return stats;
    }

    /**
     * 重置统计信息
     */
    public void reset() {
        methodStatsMap.clear();
        log.info("Performance statistics reset");
    }

    /**
     * 方法统计信息类
     */
    private static class MethodStats {
        private final AtomicLong totalExecutions = new AtomicLong(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);
        private final AtomicLong maxExecutionTime = new AtomicLong(0);
        private final AtomicLong minExecutionTime = new AtomicLong(Long.MAX_VALUE);

        public void addExecution(long executionTime) {
            totalExecutions.incrementAndGet();
            totalExecutionTime.addAndGet(executionTime);
            
            // 更新最大执行时间
            long currentMax;
            do {
                currentMax = maxExecutionTime.get();
                if (executionTime <= currentMax) {
                    break;
                }
            } while (!maxExecutionTime.compareAndSet(currentMax, executionTime));
            
            // 更新最小执行时间
            long currentMin;
            do {
                currentMin = minExecutionTime.get();
                if (executionTime >= currentMin) {
                    break;
                }
            } while (!minExecutionTime.compareAndSet(currentMin, executionTime));
        }

        public long getTotalExecutions() {
            return totalExecutions.get();
        }

        public long getTotalExecutionTime() {
            return totalExecutionTime.get();
        }

        public double getAverageExecutionTime() {
            long executions = totalExecutions.get();
            return executions > 0 ? (double) totalExecutionTime.get() / executions : 0;
        }

        public long getMaxExecutionTime() {
            return maxExecutionTime.get();
        }

        public long getMinExecutionTime() {
            long min = minExecutionTime.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }
    }
} 