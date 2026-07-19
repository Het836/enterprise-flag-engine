package com.enterprise.backend.service;

import com.enterprise.backend.dto.FeatureFlagCreateRequest;
import com.enterprise.backend.entity.FeatureFlag;
import io.micrometer.core.annotation.Timed;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface FeatureFlagService {
    boolean evaluate(String key, Map<String, Object> userContext);

    FeatureFlag createFlag(FeatureFlagCreateRequest request);

    List<FeatureFlag> getFlags(Long environmentId);

    FeatureFlag toggleFlag(Long flagId, boolean isEnabled);
}
