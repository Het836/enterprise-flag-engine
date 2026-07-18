package com.enterprise.backend.service.impl;

import com.enterprise.backend.dto.FeatureFlagCreateRequest;
import com.enterprise.backend.entity.AuditLog;
import com.enterprise.backend.entity.FeatureFlag;
import com.enterprise.backend.repository.AuditLogRepository;
import com.enterprise.backend.repository.FeatureFlagRepository;
import com.enterprise.backend.rules.RuleEvaluator;
import com.enterprise.backend.service.FeatureFlagCacheService;
import com.enterprise.backend.service.FeatureFlagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class FeatureFlagServiceImpl implements FeatureFlagService {

    private final FeatureFlagRepository flagRepository;
    private final RuleEvaluator ruleEvaluator;
    private final FeatureFlagCacheService l2CacheService; // Inject Track B's service
    private final CacheManager cacheManager;
    private final FeatureFlagCacheService cacheService;// Inject your Caffeine manager
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;


    @Override
    @Transactional(readOnly = true)
    public boolean evaluate(String flagKey, Map<String, Object> userContext) {
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
        return ruleEvaluator.evaluateRules(flag.getTargetingRules(), userContext);
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
        FeatureFlag dbFlag = flagRepository.findByFlagKey(flagKey).orElse(null);

        // Populate L2 Redis cache for other nodes to read next time
        if (dbFlag != null) {
            l2CacheService.saveFlagToCache(flagKey, dbFlag);
        }

        return dbFlag;
    }

    // This is the direct hook for your teammate's FlagInvalidationListener!
    @CacheEvict(value = "featureFlags", key = "#flagKey")
    public void evictLocalCache(String flagKey) {
        // This method is a hook for the teammate's FlagInvalidationListener.
        // The actual eviction is handled by the @CacheEvict annotation above.
    }

    @Override
    @Transactional
    public FeatureFlag createFlag(FeatureFlagCreateRequest request) {
        // Business Rule check: Make sure the key doesn't already exist globally/in environment
        if (flagRepository.findByFlagKey(request.getFlagKey()).isPresent()) {
            throw new IllegalArgumentException("Feature flag with key '" + request.getFlagKey() + "' already exists.");
        }

        FeatureFlag flag = FeatureFlag.builder()
                .flagKey(request.getFlagKey()) // Unified method call//                .name(request.getFlagKey().replace("-"," ").substring(0,1).toUpperCase() + request.getFlagKey().replace("-"," ").substring(1))
                .description(request.getDescription())
                .isEnabled(request.isEnabled())
                .environmentId(request.getEnvironmentId())
                .type("BOOLEAN") // <-- Make sure this line is explicitly present!
                .targetingRules("[]")
                .build();

        FeatureFlag savedFlag = flagRepository.save(flag);
        cacheService.saveFlagToCache(savedFlag.getFlagKey(), savedFlag);
        logAction(savedFlag.getFlagKey(), "CREATE", null, savedFlag);

        return savedFlag;
//        return flagRepository.save(flag);

    }

    @Override
    @Transactional(readOnly = true)
    public List<FeatureFlag> getFlags(Long environmentId) {
        // Delegate to our dynamic JPQL filter query
        return flagRepository.findByEnvironmentOptional(environmentId);
    }

    @Override
    public FeatureFlag toggleFlag(Long flagId, boolean isEnabled) {
        // 1. Locate the flag configuration row
        FeatureFlag existingFlag = flagRepository.findById(flagId)
                .orElseThrow(() -> new IllegalArgumentException("Feature flag with ID " + flagId + " not found."));

        // 2. Clone/Serialize a snapshot of the current state *before* modification
        String oldValueJson = serializeEntity(existingFlag);

        // 3. Apply the changes
        existingFlag.setIsEnabled(isEnabled);
        FeatureFlag updatedFlag = flagRepository.save(existingFlag);

        evictLocalCache(updatedFlag.getFlagKey());
        // 4. Update the Redis cache cluster layer
        cacheService.saveFlagToCache(updatedFlag.getFlagKey(), updatedFlag);

        // 5. Commit the differential changes to the audit ledger
        logAction(updatedFlag.getFlagKey(), "TOGGLE", oldValueJson, updatedFlag);

        return updatedFlag;
    }

    // --- Helper Audit Utilities ---
    private void logAction(String flagKey, String actionType, String preSerializedOldValue ,FeatureFlag newEntityState) {
        try{
            String newValueJson = serializeEntity(newEntityState);

            AuditLog log = AuditLog.builder()
                    .flagKey(flagKey)
                    .actionType(actionType)
                    .changedBy("het")
                    .oldValue(preSerializedOldValue)
                    .newValue(newValueJson)
                    .build();

            auditLogRepository.save(log);
            System.out.println("🪵 Audit Ledger updated successfully for action: " + actionType);
        } catch (Exception e) {
            System.err.println("⚠️ Could not write to audit ledger: " + e.getMessage());
        }
    }

    private String serializeEntity(FeatureFlag flag){
        try {
            return objectMapper.writeValueAsString(flag);
        } catch (Exception e) {
            return "{\"error\": \"Serialization failed\"}";
        }
    }
}