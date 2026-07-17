package com.enterprise.backend.rules;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleEvaluatorTest {

    private RuleEvaluator ruleEvaluator;

    @BeforeEach
    void setUp() {
        // Instantiate a clean ObjectMapper instance for isolation
        ObjectMapper objectMapper = new ObjectMapper();
        ruleEvaluator = new RuleEvaluator(objectMapper);
    }

    @Test
    void shouldReturnTrueWhenUserMatchesCriteria() {
        // Given: A JSON rule specifying that a user's plan must be PREMIUM
        String targetingRuleJson = """
            [
                {
                    "attribute": "plan",
                    "operator": "EQUALS",
                    "value": "PREMIUM"
                }
            ]
            """;

        // When: An matching user profile payload comes in
        Map<String, Object> userContext = new HashMap<>();
        userContext.put("plan", "PREMIUM");
        userContext.put("email", "het@adani.com");

        // Then: The engine must approve the flag evaluation
        boolean result = ruleEvaluator.evaluateRules(targetingRuleJson, userContext);
        assertTrue(result, "Evaluation should succeed when user matches constraints.");
    }

    @Test
    void shouldReturnFalseWhenUserFailsCriteria() {
        // Given: A targeting rule constraint
        String targetingRuleJson = """
            [
                {
                    "attribute": "plan",
                    "operator": "EQUALS",
                    "value": "PREMIUM"
                }
            ]
            """;

        // When: A user profile with a FREE plan evaluates
        Map<String, Object> userContext = new HashMap<>();
        userContext.put("plan", "FREE");

        // Then: The engine must reject it and evaluate to false
        boolean result = ruleEvaluator.evaluateRules(targetingRuleJson, userContext);
        assertFalse(result, "Evaluation should fail when user does not match constraints.");
    }
}