package com.example.d_trade.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 定义不需要过滤的路径列表
    private final List<String> excludedPaths = Arrays.asList(
            "/api/auth/**",
            "/api/auth/test",
            "/api/public/**",
            "/uploads/**",
            "/h2-console/**"
    );

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestPath = request.getServletPath();
        logger.info("请求路径: {}", requestPath);
        
        boolean shouldNotFilter = excludedPaths.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
        
        logger.info("是否跳过JWT过滤: {}, 请求路径: {}", shouldNotFilter, requestPath);
        return shouldNotFilter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 打印请求信息
            logger.info("处理请求: {} {}", request.getMethod(), request.getRequestURI());
            logger.info("请求头Authorization: {}", request.getHeader("Authorization") != null ? "存在" : "不存在");
            
            // 如果请求路径是排除的路径之一，直接放行
            if (shouldNotFilter(request)) {
                logger.info("跳过JWT过滤, 路径: {}", request.getServletPath());
                filterChain.doFilter(request, response);
                return;
            }
            
            logger.info("正在处理JWT认证, 路径: {}", request.getServletPath());
            String jwt = parseJwt(request);
            if (jwt != null) {
                logger.info("发现JWT令牌, 长度: {}", jwt.length());
                String username = jwtUtils.extractUsername(jwt);
                
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    logger.info("从JWT中提取的用户名: {}", username);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    logger.info("已加载用户详情: {}", userDetails.getUsername());
                    
                    if (jwtUtils.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null,
                                        userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.info("成功设置认证信息, 用户: {}, 权限: {}", username, userDetails.getAuthorities());
                    } else {
                        logger.warn("JWT令牌验证失败, 用户: {}", username);
                    }
                } else {
                    logger.warn("未能从JWT中提取用户名或认证上下文非空");
                }
            } else {
                logger.warn("请求中未找到JWT令牌");
            }
            
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("设置用户认证失败: {}", e.getMessage(), e);
            filterChain.doFilter(request, response);
        }
    }
    
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            logger.info("成功解析Authorization头, 获取到JWT令牌");
            return token;
        }
        
        logger.warn("Authorization头格式不正确或不存在");
        return null;
    }
}
