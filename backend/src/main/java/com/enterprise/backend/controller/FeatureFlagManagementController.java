package com.enterprise.backend.controller;

import com.enterprise.backend.dto.FeatureFlagCreateRequest;
import com.enterprise.backend.entity.FeatureFlag;
import com.enterprise.backend.service.FeatureFlagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/flags")
@RequiredArgsConstructor
public class FeatureFlagManagementController {

    private final FeatureFlagService featureFlagService;

    // 1. GET /api/v1/flags (List all or filter by environment)
    @GetMapping
    public ResponseEntity<List<FeatureFlag>> getAllFlags(
            @RequestParam(value = "environmentId", required = false) Long environmentId) {
        List<FeatureFlag> flags = featureFlagService.getFlags(environmentId);
        return ResponseEntity.ok(flags);
    }

    // 2. POST /api/v1/flags (Create a brand-new flag config)
    @PostMapping
    public ResponseEntity<?> createFlag(@Valid @RequestBody FeatureFlagCreateRequest request) {
        try {
            FeatureFlag createdFlag = featureFlagService.createFlag(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFlag);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    // 3. PATCH /api/v1/flags/{id}/toggle (Flip an existing flag's state)
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleFlag(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Boolean> payload) {

        if (!payload.containsKey("is_enabled")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required field: 'is_enabled'"));
        }

        try {
            boolean isEnabled = payload.get("is_enabled");
            FeatureFlag updatedFlag = featureFlagService.toggleFlag(id, isEnabled);
            return ResponseEntity.ok(updatedFlag);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}