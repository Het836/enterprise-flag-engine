package com.enterprise.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching // Turns on Spring Boot's caching annotation engine proxy
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(){
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("flags", "environments");
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                // Evict entries 5 minutes after they are written to the memory space
                .expireAfterWrite(5, TimeUnit.MINUTES)
                // Limit the maximum number of configurations stored in local RAM
                .maximumSize(1000)
                // Gather diagnostic cache performance stats (hit rates, evictions)
                .recordStats();
    }
}
