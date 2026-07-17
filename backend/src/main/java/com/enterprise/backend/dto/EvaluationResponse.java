package com.enterprise.backend.dto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EvaluationResponse {
    private String flagKey;
    private boolean enabled;
}
