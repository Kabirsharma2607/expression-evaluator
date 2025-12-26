package com.example.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for policy evaluation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyEvaluationRequest {

    @NotNull(message = "Policy name is required")
    @NotBlank(message = "Policy name cannot be blank")
    private String policyName;

    @NotNull(message = "Feature map is required")
    @NotEmpty(message = "Feature map cannot be empty")
    private Map<String, Object> featureMap;
}