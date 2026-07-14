package com.enterprise.backend.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class FeatureFlagPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    // This MUST match the exact channel name we put in the RedisConfig listener
    private static final String TOPIC = "flag-updates";

    public FeatureFlagPublisher(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void broadcastInvalidation(String flagName) {
        // convertAndSend is the magic Pub/Sub command in Spring
        redisTemplate.convertAndSend(TOPIC, flagName);

        System.out.println("📡 [BROADCAST SENT] Alerting network to drop cache for: " + flagName);
    }
}