package com.enterprise.backend.service.impl;

import com.enterprise.backend.entity.FeatureFlag;
import com.enterprise.backend.repository.FeatureFlagRepository;
import com.enterprise.backend.rules.RuleEvaluator;
import com.enterprise.backend.service.FeatureFlagCacheService;
import com.enterprise.backend.service.FeatureFlagService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class FeatureFlagServiceImpl implements FeatureFlagService {

    private final FeatureFlagRepository flagRepository;
    private final RuleEvaluator ruleEvaluator;
    private final FeatureFlagCacheService l2CacheService; // Inject Track B's service
    private final CacheManager cacheManager;             // Inject your Caffeine manager


    @Override
    @Transactional(readOnly = true)
    public boolean evaluate(String flagKey, Map<String, String> userContext) {
        // 1. Fetch the active state configuration of the target flag
        FeatureFlag flag = getFlagConfiguration(flagKey);

        if (flag == null) {
            log.warn("Feature flag key '{}' not found. Defaulting evaluation to closed/false.", flagKey);
            return false;
        }

        // 2. Kill processing early if the administrative toggle is completely off
        if (!flag.isEnabled()) {
            return false;
        }

        // 3. If there are no custom targeting constraints, global enablement rules apply
        if (flag.getTargetingRules() == null || flag.getTargetingRules().isEmpty()) {
            return true;
        }

        // 4. Delegate to our highly optimized switch expression matrix for attribute matching
        return ruleEvaluator.evaluteRules(flag.getTargetingRules(), userContext);
    }

    @Cacheable(value = "featureFlags", key = "#flagKey", unless = "#result == null")
    public FeatureFlag getFlagConfiguration(String flagKey){
//        log.info("L1 Cache Miss encountered for flag key: '{}'. Fetching state from PostgreSQL database...", flagKey);
//        return featureFlagRepository.findByKey(flagKey).orElse(null);
        // Tier 1: Local Caffeine RAM Cache Missed. Let's ask Tier 2: Redis
        try {
            FeatureFlag cachedFlag = (FeatureFlag) l2CacheService.getFlagFromCache(flagKey);
            if (cachedFlag != null) {
                return cachedFlag;
            }
        } catch (Exception e) {
            // Track B's Circuit breaker will handle this, fallback gracefully
        }

        // Tier 3: Both L1 and L2 Missed. Fetch directly from PostgreSQL database
        FeatureFlag dbFlag = flagRepository.findByKey(flagKey).orElse(null);

        // Populate L2 Redis cache for other nodes to read next time
        if (dbFlag != null) {
            l2CacheService.saveFlagToCache(flagKey, dbFlag);
        }

        return dbFlag;
    }

    // This is the direct hook for your teammate's FlagInvalidationListener!
    @Cacheable(value = "featureFlags", key = "#flagKey", unless = "#result == null")
    public void evictLocalCache(String flagKey) {
        org.springframework.cache.Cache localCache = cacheManager.getCache("flags");
        if (localCache != null) {
            localCache.evict(flagKey);
        }
    }
}
