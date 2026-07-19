package com.enterprise.backend.controller;

import com.enterprise.backend.dto.EvaluationRequest;
import com.enterprise.backend.dto.EvaluationResponse;
import com.enterprise.backend.service.FeatureFlagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/sdk/eval")
@RequiredArgsConstructor
public class SDKEvaluationController {

    // FIX 1: Directly inject the FeatureFlagService interface
    // This ensures we hit the Spring proxy containing your @Timed metric and @Cacheable logic
    private final FeatureFlagService featureFlagService;

    @PostMapping("/{flagKey}")
    public ResponseEntity<EvaluationResponse> evaluate(
            @PathVariable String flagKey,
            @RequestBody EvaluationRequest request) {

        // FIX 2: Route the execution through the annotated method.
        // Note: Replace .getContext() with whatever getter your EvaluationRequest DTO uses
        // to expose the Map<String, Object> user context.
        boolean isEnabled = featureFlagService.evaluate(flagKey, request.getContext());

        // Wrap the boolean result into your expected DTO
        // (Adjust the constructor/setters based on your actual EvaluationResponse class)
        EvaluationResponse response = new EvaluationResponse(flagKey, isEnabled);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> evaluateBulk(@RequestBody Map<String, Object> context) {
        // Implement bulk evaluation logic here fetching all cached flags
        // and evaluating them against the provided context map.
        return ResponseEntity.ok().build();
    }
}