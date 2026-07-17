package com.enterprise.backend.service.impl;

import com.enterprise.backend.dto.EvaluationRequest;
import com.enterprise.backend.dto.EvaluationResponse;
import com.enterprise.backend.entity.FeatureFlag;
import com.enterprise.backend.rules.RuleEvaluator;
import com.enterprise.backend.service.FeatureFlagCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SDKEvaluationServiceImpl {

    private final FeatureFlagCacheService cacheService;
    private final RuleEvaluator ruleEvaluator;

    public EvaluationResponse evaluateFlag(String flagKey, EvaluationRequest request) {

        // 1. Fetch from Redis via your circuit-breaker cache service
        FeatureFlag flag = cacheService.getFlagFromCache(flagKey);

        // 2. Fail closed if the flag doesn't exist
        if (flag == null) {
            return buildResponse(flagKey, false);
        }

        // 3. Master Kill Switch: If the flag is turned off entirely, ignore rules.
        if (!flag.isEnabled()) {
            return buildResponse(flagKey, false);
        }

        // 4. Global Enable: If the flag is ON, but has no rules, it's enabled for everyone.
        String rulesJson = flag.getTargetingRules();
        if (rulesJson == null || rulesJson.isBlank() || rulesJson.equals("[]")) {
            return buildResponse(flagKey, true);
        }

        // 5. Rule Evaluation: Flag is ON, and rules exist. Let your stream logic decide.
        boolean passesRules = ruleEvaluator.evaluateRules(rulesJson, request.getContext());

        return buildResponse(flagKey, passesRules);
    }

    // Helper method to keep the main logic clean
    private EvaluationResponse buildResponse(String key, boolean isEnabled) {
        return EvaluationResponse.builder()
                .flagKey(key)
                .enabled(isEnabled)
                .build();
    }
}