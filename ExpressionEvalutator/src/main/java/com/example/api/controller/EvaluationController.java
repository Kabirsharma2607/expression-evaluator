package com.example.api.controller;

import com.example.api.dto.*;
import com.example.api.service.ExpressionEvaluationService;
import com.example.api.service.PolicyEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REST controller for expression and policy evaluation
 */
@RestController
@RequestMapping("/v1/evaluation")
@Validated
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Evaluation API", description = "Endpoints for policy and expression evaluation")
public class EvaluationController {

    private final PolicyEvaluationService policyService;
    private final ExpressionEvaluationService expressionService;

    @Operation(summary = "Evaluate a policy",
               description = "Evaluates a policy against the provided feature map")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful evaluation"),
        @ApiResponse(responseCode = "404", description = "Policy not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/policy")
    public ResponseEntity<PolicyEvaluationResponse> evaluatePolicy(
            @Valid @RequestBody PolicyEvaluationRequest request) {

        log.info("Evaluating policy: {} with feature map: {}",
                request.getPolicyName(), request.getFeatureMap());

        PolicyEvaluationResponse response = policyService.evaluatePolicy(
            request.getPolicyName(),
            request.getFeatureMap()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Evaluate an expression",
               description = "Evaluates a single expression with optional feature map and rule context")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful evaluation"),
        @ApiResponse(responseCode = "400", description = "Invalid expression or request")
    })
    @PostMapping("/expression")
    public ResponseEntity<ExpressionEvaluationResponse> evaluateExpression(
            @Valid @RequestBody ExpressionEvaluationRequest request) {

        log.info("Evaluating expression: {}", request.getExpression());

        Object result = expressionService.evaluateExpression(
            request.getExpression(),
            request.getFeatureMap(),
            request.getRuleContext()
        );

        ExpressionEvaluationResponse response = ExpressionEvaluationResponse.builder()
            .expression(request.getExpression())
            .result(result)
            .timestamp(Instant.now())
            .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get available policies",
               description = "Returns a list of all available policy names")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of policy names")
    })
    @GetMapping("/policies")
    public ResponseEntity<List<String>> getAvailablePolicies() {
        log.info("Getting available policies");

        List<String> policies = policyService.getAvailablePolicies();
        return ResponseEntity.ok(policies);
    }

    @Operation(summary = "Get policy details",
               description = "Returns detailed information about a specific policy")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Policy details"),
        @ApiResponse(responseCode = "404", description = "Policy not found")
    })
    @GetMapping("/policy/{policyName}")
    public ResponseEntity<PolicyDetailsResponse> getPolicyDetails(
            @PathVariable String policyName) {

        log.info("Getting details for policy: {}", policyName);

        PolicyDetailsResponse details = policyService.getPolicyDetails(policyName);
        return ResponseEntity.ok(details);
    }

    @Operation(summary = "Health check",
               description = "Simple health check endpoint")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Expression Evaluator API is running!");
    }
}