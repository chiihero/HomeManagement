package com.chii.homemanagement.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * JWT工具类，用于生成和验证JWT令牌
 */
@Component
public class JwtUtil {

    // Token有效期（毫秒）- 默认24小时
    private static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60 * 1000;
    
    // 刷新Token有效期（毫秒）- 默认7天
    private static final long JWT_REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000;

    // 签名密钥
    private Key key;
    
    @Value("${jwt.secret:defaultSecretKeyWhichIsAtLeast32BytesLongForHS512Algorithm}")
    private String secret;
    
    // 使用@PostConstruct确保在依赖注入完成后初始化密钥
    @jakarta.annotation.PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从token中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从token中获取过期日期
     *
     * @param token JWT令牌
     * @return 过期日期
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从token中获取指定声明
     *
     * @param token          JWT令牌
     * @param claimsResolver 声明解析函数
     * @return 声明值
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析token获取所有声明
     *
     * @param token JWT令牌
     * @return 所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查token是否过期
     *
     * @param token JWT令牌
     * @return 是否过期
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 为指定用户生成token
     *
     * @param username 用户名
     * @return JWT令牌
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, username, JWT_TOKEN_VALIDITY);
    }
    
    /**
     * 为指定用户生成刷新token
     *
     * @param username 用户名
     * @return 刷新JWT令牌
     */
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("id", UUID.randomUUID().toString()); // 添加随机ID确保每次生成的刷新令牌都不同
        return doGenerateToken(claims, username, JWT_REFRESH_TOKEN_VALIDITY);
    }

    /**
     * 生成token
     *
     * @param claims  声明
     * @param subject 主题（用户名）
     * @param validity 有效期（毫秒）
     * @return JWT令牌
     */
    private String doGenerateToken(Map<String, Object> claims, String subject, long validity) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validity);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 验证token是否有效
     *
     * @param token    JWT令牌
     * @param username 用户名
     * @return 是否有效
     */
    public Boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }
    
    /**
     * 验证刷新token是否有效
     *
     * @param token 刷新JWT令牌
     * @return 是否有效
     */
    public Boolean validateRefreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            String type = (String) claims.get("type");
            return "refresh".equals(type) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取令牌有效期（秒）
     * 
     * @return 有效期（秒）
     */
    public int getTokenExpiresIn() {
        return (int) (JWT_TOKEN_VALIDITY / 1000);
    }
} 