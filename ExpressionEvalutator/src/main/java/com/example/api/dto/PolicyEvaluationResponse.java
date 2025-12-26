package com.example.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for policy evaluation
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PolicyEvaluationResponse {

    private String policyName;
    private boolean success;
    private Map<String, RuleResultDto> ruleResults;
    private List<String> errors;
    private Long executionTimeMs;
    private Instant timestamp;
}