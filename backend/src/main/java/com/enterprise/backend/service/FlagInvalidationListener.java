package com.enterprise.backend.service;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Service
public class FlagInvalidationListener implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 1. The radio transmission comes in as raw bytes. We convert it back to text.
        String updatedFlag = new String(message.getBody());
        String channel = new String(message.getChannel());

        // 2. This is where Track A and Track B connect!
        System.out.println("\n🚨 [BROADCAST RECEIVED on " + channel + "]");
        System.out.println("🧹 Flag changed: '" + updatedFlag + "'. Wiping from local L1 RAM cache...");

        // TODO: Your teammate from Track A will call their Caffeine Cache eviction method right here.
        // Example: l1CacheService.evict(updatedFlag);
    }
}