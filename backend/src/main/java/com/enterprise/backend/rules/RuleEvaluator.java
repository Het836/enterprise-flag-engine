package com.enterprise.backend.rules;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RuleEvaluator {

    private final ObjectMapper objectMapper;

//    *valuates a collection of targeting rules against a user context map.
    public boolean evaluteRules(String targetingRuleJson, Map<String,String> userContext){
        if (targetingRuleJson==null || targetingRuleJson.isBlank() || userContext==null){
            return false;
        }

        try {
            // Read the JSON array directly into our lightweight record objects
            List<TargetingRuleConstraint> constraints = objectMapper.readValue(
                    targetingRuleJson,
                    new TypeReference<List<TargetingRuleConstraint>>() {}
            );

            if (constraints.isEmpty()){
                return false;
            }

            // Execute the rule criteria: by default, our system treats individual conditions
            // as an "AND" sequence. All defined rules must be true for the flag to trigger.
            return constraints.stream().allMatch(constraint -> constraint.matches(userContext));
        } catch (Exception e) {
            // If the database row holds bad JSON syntax, fail closed rather than crashing
            // the customer thread, ensuring continuous engine availability.
            return false;
        }
    }
}
