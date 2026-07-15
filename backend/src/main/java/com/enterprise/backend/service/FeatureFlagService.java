package com.enterprise.backend.service;

import java.util.Map;

public interface FeatureFlagService {
    boolean evaluate(String key, Map<String, String> userContext);
}
