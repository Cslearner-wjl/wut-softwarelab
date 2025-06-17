package com.example.d_trade.aspect;

import com.example.d_trade.utils.PerformanceMonitor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 性能监控切面
 * 用于自动记录方法执行时间
 */
@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    private final PerformanceMonitor performanceMonitor;

    public PerformanceAspect(PerformanceMonitor performanceMonitor) {
        this.performanceMonitor = performanceMonitor;
    }

    /**
     * 定义切入点：所有controller包下的方法
     */
    @Pointcut("execution(* com.example.d_trade.controller..*.*(..))")
    public void controllerMethods() {}

    /**
     * 定义切入点：所有service包下的方法
     */
    @Pointcut("execution(* com.example.d_trade.service..*.*(..))")
    public void serviceMethods() {}

    /**
     * 定义切入点：所有repository包下的方法
     */
    @Pointcut("execution(* com.example.d_trade.repository..*.*(..))")
    public void repositoryMethods() {}

    /**
     * 环绕通知：记录方法执行时间
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("controllerMethods() || serviceMethods() || repositoryMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        long startTime = performanceMonitor.start(methodName);
        
        try {
            return joinPoint.proceed();
        } finally {
            long executionTime = performanceMonitor.end(methodName, startTime);
            if (executionTime > 500) { // 执行时间超过500ms的方法记录警告日志
                log.warn("Long execution time for method {}: {} ms", methodName, executionTime);
            }
        }
    }
} 