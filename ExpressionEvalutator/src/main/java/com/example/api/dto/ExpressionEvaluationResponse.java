package com.example.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Response DTO for expression evaluation
 */
@Data
@Builder
public class ExpressionEvaluationResponse {

    private String expression;
    private Object result;
    private Instant timestamp;
}