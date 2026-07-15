package com.enterprise.backend.rules;

import com.enterprise.backend.constant.RuleOperator;

//Represents a single rule condition parsed from the JSONB array.
//Example: attribute="plan", operator=EQUALS, value="PREMIUM"
public record TargetingRuleConstraint(
        String attribute,
        RuleOperator operator,
        String value
) {
//    Evaluates this specific constraint against the user's incoming context data map.
    public boolean matches(java.util.Map<String, String> userContext){
        if (userContext == null || attribute == null || operator == null) {
            return false;
        }

        // Fetch what trait the user actually has for this attribute
        String userValue = userContext.get(attribute);

        // Delegate the actual mathematical/string comparison to our RuleOperator enum
        return operator.evaluate(userValue, value);
    }
}
