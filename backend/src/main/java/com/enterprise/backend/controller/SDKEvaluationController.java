package com.enterprise.backend.controller;
import com.enterprise.backend.dto.EvaluationRequest;
import com.enterprise.backend.dto.EvaluationResponse;
import com.enterprise.backend.service.impl.SDKEvaluationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/sdk/eval")
@RequiredArgsConstructor
public class SDKEvaluationController {
    private final SdkEvaluationServiceImpl evaluationService;

    @PostMapping("/{flagKey}")
    public ResponseEntity<EvaluationResponse> evaluate(
            @PathVariable String flagKey,
            @RequestBody EvaluationRequest request) {

        return ResponseEntity.ok(evaluationService.evaluateFlag(flagKey, request));
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> evaluateBulk(@RequestBody Map<String, Object> context) {
        // Implement bulk evaluation logic here fetching all cached flags
        // and evaluating them against the provided context map.
        return ResponseEntity.ok().build();
    }
}
