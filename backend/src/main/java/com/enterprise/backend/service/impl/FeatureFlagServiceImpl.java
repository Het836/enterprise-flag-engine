package com.enterprise.backend.service.impl;

import com.enterprise.backend.entity.FeatureFlag;
import com.enterprise.backend.repository.FeatureFlagRepository;
import com.enterprise.backend.rules.RuleEvaluator;
import com.enterprise.backend.service.FeatureFlagService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class FeatureFlagServiceImpl implements FeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;
    private final RuleEvaluator ruleEvaluator;


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
        log.info("L1 Cache Miss encountered for flag key: '{}'. Fetching state from PostgreSQL database...", flagKey);
        return featureFlagRepository.findByKey(flagKey).orElse(null);
    }
}
