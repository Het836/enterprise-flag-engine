    package com.enterprise.backend.service;
    import com.enterprise.backend.entity.FeatureFlag;
    import com.enterprise.backend.repository.FeatureFlagRepository;
    import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
    import lombok.RequiredArgsConstructor;
    import org.springframework.data.redis.core.RedisTemplate;
    import org.springframework.stereotype.Service;

    import java.time.Duration;

    @Service
    @RequiredArgsConstructor
    public class FeatureFlagCacheService {
        private final RedisTemplate<String, Object> redisTemplate;
        private final FeatureFlagPublisher publisher;
        private final FeatureFlagRepository flagRepository;

        // We prefix our keys so they don't collide with anything else in Redis
        private static final String CACHE_PREFIX = "flag:";

//        public FeatureFlagCacheService(RedisTemplate<String, Object> redisTemplate, FeatureFlagPublisher publisher) {
//            this.redisTemplate = redisTemplate;
//            this.publisher = publisher;
//        }

        // Method to save a flag into Redis L2 Cache
        public void saveFlagToCache(String flagName, Object flagData) {
            String key = CACHE_PREFIX + flagName;

            // Save it, but give it a Time-To-Live (TTL) of 24 hours just in case
            try {
                redisTemplate.opsForValue().set(key, flagData, Duration.ofHours(24));
                System.out.println("Saved flag to Redis: " + key);

    //            publisher.broadcastInvalidation(flagName);
            }catch (Exception e){
                System.err.println("Redis is down. Could not save L2 cache");
            }
        }
        @CircuitBreaker(name = "redisCache", fallbackMethod = "fallbackGetFlag")
        // Method to fetch a flag from Redis L2 Cache
        public FeatureFlag getFlagFromCache(String flagKey) {
            String key = CACHE_PREFIX + flagKey;
            return (FeatureFlag) redisTemplate.opsForValue().get(key);
        }
        // The Fallback Method (The Safety Net)
        // The signature must match the original method, but with an Exception parameter at the end.
        public FeatureFlag fallbackGetFlag(String flagKey, Throwable throwable) {
            System.err.println("🛡️ Circuit Breaker Tripped! Redis is unreachable. Bypassing L2 cache for: " + flagKey);

            // TODO: In the final version, this is where you ask Track A's L1 Cache or PostgreSQL for the data.
            // For now, we return null so the app doesn't crash.
            return flagRepository.findByFlagKey(flagKey).orElse(null);
        }
    }
