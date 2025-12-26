package com.example.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for policy details
 */
@Data
@Builder
public class PolicyDetailsResponse {

    private String policyName;
    private Map<String, RuleDetailsDto> rules;
    private int totalRules;

    @Data
    @Builder
    public static class RuleDetailsDto {
        private String expression;
        private List<String> dependencies;
    }
}