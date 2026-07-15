package com.enterprise.backend.constant;

public enum RuleOperator {
    EQUALS,
    NOT_EQUALS,
    CONTAINS,
    NOT_CONTAINS,
    GREATER_THAN,
    LESS_THAN;

//    Evaluates whether a user's context attribute value matches the rule's target value.
    public boolean evaluate(String userValue, String targetValue) {
        if (userValue == null || targetValue == null) {
            return false;
        }

        return switch (this){
            case EQUALS -> userValue.equalsIgnoreCase(targetValue);
            case NOT_EQUALS -> !userValue.equalsIgnoreCase(targetValue);
            case CONTAINS -> userValue.toLowerCase().contains(targetValue.toLowerCase());
            case NOT_CONTAINS -> !userValue.toLowerCase().contains(targetValue.toLowerCase());
            case GREATER_THAN -> compareNumerical(userValue, targetValue) > 0;
            case LESS_THAN -> compareNumerical(userValue, targetValue) < 0;
        };
    }

    private int compareNumerical(String userValue, String targetValue){
        try {
            double userNum = Double.parseDouble(userValue);
            double targetNum = Double.parseDouble(targetValue);
            return Double.compare(userNum, targetNum);
        } catch (NumberFormatException e){
            return 0;
        }
    }
}
