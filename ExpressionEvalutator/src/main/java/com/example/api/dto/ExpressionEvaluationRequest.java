package com.example.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Request DTO for direct expression evaluation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpressionEvaluationRequest {

    @NotNull(message = "Expression is required")
    @NotBlank(message = "Expression cannot be blank")
    private String expression;

    private Map<String, Object> featureMap = new HashMap<>();
    private Map<String, Object> ruleContext = new HashMap<>();
}