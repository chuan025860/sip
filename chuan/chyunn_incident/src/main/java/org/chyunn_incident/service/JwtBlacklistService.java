package org.chyunn_incident.service;

import org.chyunn_incident.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class JwtBlacklistService {
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    private final String BLACKLIST_PREFIX = "blacklist:";
    private final RedisTemplate<String, String> redisTemplate;

    public JwtBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    //  將 Token 加入 Redis 黑名單
    public void addToBlacklist(String token) {
        long expirationTime = jwtTokenProvider.getTokenRemainingTime(token);  // 設定為 JWT Token 的有效時間
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", expirationTime, TimeUnit.MILLISECONDS);
    }

    // 檢查 Token 是否在黑名單中
    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + token);
    }
}
