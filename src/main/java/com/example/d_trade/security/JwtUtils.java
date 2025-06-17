package com.example.d_trade.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private int jwtExpirationMs;
    
    public String generateToken(UserDetails userDetails) {
        logger.info("为用户生成JWT: {}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        logger.info("创建JWT令牌, 主题: {}, 过期时间: {}", subject, expiryDate);
        
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key())
                .compact();
    }
    
    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
    
    public String extractUsername(String token) {
        try {
            String username = extractClaim(token, Claims::getSubject);
            logger.info("从JWT中提取用户名: {}", username);
            return username;
        } catch (Exception e) {
            logger.error("从JWT中提取用户名失败: {}", e.getMessage());
            return null;
        }
    }
    
    public Date extractExpiration(String token) {
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            logger.info("从JWT中提取过期时间: {}", expiration);
            return expiration;
        } catch (Exception e) {
            logger.error("从JWT中提取过期时间失败: {}", e.getMessage());
            return null;
        }
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            logger.info("成功解析JWT声明");
            return claims;
        } catch (MalformedJwtException e) {
            logger.error("JWT格式错误: {}", e.getMessage());
            throw e;
        } catch (ExpiredJwtException e) {
            logger.error("JWT已过期: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.error("不支持的JWT: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("JWT参数不合法: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("解析JWT时发生未知错误: {}", e.getMessage());
            throw e;
        }
    }
    
    private Boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            boolean isExpired = expiration != null && expiration.before(new Date());
            logger.info("检查JWT是否过期: {}", isExpired);
            return isExpired;
        } catch (Exception e) {
            logger.error("检查JWT过期状态时发生错误: {}", e.getMessage());
            return true;
        }
    }
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid = (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
            logger.info("验证JWT: 用户名匹配={}, 令牌未过期={}, 最终结果={}", 
                username != null && username.equals(userDetails.getUsername()), 
                !isTokenExpired(token), 
                isValid);
            return isValid;
        } catch (ExpiredJwtException e) {
            logger.error("JWT已过期: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("验证JWT时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }
} 