package com.example.api.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for individual rule result
 */
@Data
@Builder
public class RuleResultDto {

    private Object result;
    private boolean success;
    private String error;
    private Long executionTimeMs;
}