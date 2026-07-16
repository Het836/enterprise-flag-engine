package com.enterprise.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeatureFlagCreateRequest {

    @NotBlank(message = "Flag key is required")
    @Size( max = 50, message = "Flag key must not exceed 50 characters" )
    @Pattern(
            regexp = "^[a-z0-9_]+$",
            message = "Flag key must be lowercase alphanumeric and can only contain dashes or underscores."
    )
    private String flagKey;

    @Size(max = 255, message = "Description cannot exceed 255 characters.")
    private String description;

    private boolean isEnabled = false;

    @NotNull(message = "Environment ID identifier is required.")
    private Long environmentId;
}
