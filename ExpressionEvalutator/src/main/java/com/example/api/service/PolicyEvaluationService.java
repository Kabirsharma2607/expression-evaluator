package com.example.api.service;

import com.example.api.dto.PolicyDetailsResponse;
import com.example.api.dto.PolicyEvaluationResponse;
import com.example.api.dto.RuleResultDto;
import com.example.policy.PolicyExecutor;
import com.example.policy.PolicyLoader;
import com.example.policy.model.Policy;
import com.example.policy.model.PolicyResult;
import com.example.policy.model.Rule;
import com.example.policy.model.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for policy evaluation operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PolicyEvaluationService {

    private final PolicyExecutor policyExecutor;
    private final PolicyLoader policyLoader;

    /**
     * Evaluates a policy against a feature map
     */
    public PolicyEvaluationResponse evaluatePolicy(String policyName, Map<String, Object> featureMap) {
        log.info("Evaluating policy: {} with feature map size: {}", policyName, featureMap.size());

        try {
            PolicyResult result = policyExecutor.executePolicy(featureMap, policyName);

            return PolicyEvaluationResponse.builder()
                .policyName(policyName)
                .success(result.isSuccess())
                .ruleResults(convertRuleResults(result.getRuleResults()))
                .errors(result.getErrors())
                .executionTimeMs(result.getExecutionTimeMs())
                .timestamp(Instant.now())
                .build();

        } catch (Exception e) {
            log.error("Error evaluating policy: {}", policyName, e);

            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Policy not found: " + policyName);
            }

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error evaluating policy: " + e.getMessage());
        }
    }

    /**
     * Gets list of available policies
     */
    public List<String> getAvailablePolicies() {
        try {
            // Check if S3PolicyLoader is being used
            if (policyLoader instanceof com.example.api.service.S3PolicyLoader) {
                com.example.api.service.S3PolicyLoader s3Loader =
                    (com.example.api.service.S3PolicyLoader) policyLoader;
                return s3Loader.getAvailablePolicies();
            } else {
                // Fallback to local file listing
                List<String> policies = new ArrayList<>();

                File policiesDir = new File(getClass().getClassLoader()
                    .getResource("policies").getFile());

                if (policiesDir.exists() && policiesDir.isDirectory()) {
                    File[] files = policiesDir.listFiles((dir, name) ->
                        name.endsWith(".yaml") || name.endsWith(".yml"));

                    if (files != null) {
                        for (File file : files) {
                            String fileName = file.getName();
                            String policyName = fileName.substring(0,
                                fileName.lastIndexOf('.'));
                            policies.add(policyName);
                        }
                    }
                }
                return policies;
            }
        } catch (Exception e) {
            log.error("Error listing policies", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets detailed information about a specific policy
     */
    public PolicyDetailsResponse getPolicyDetails(String policyName) {
        try {
            Policy policy = policyLoader.loadPolicy(policyName);

            Map<String, PolicyDetailsResponse.RuleDetailsDto> ruleDetails =
                policy.getRules().entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> PolicyDetailsResponse.RuleDetailsDto.builder()
                            .expression(entry.getValue().getExpression())
                            .dependencies(entry.getValue().getDependencies())
                            .build()
                    ));

            return PolicyDetailsResponse.builder()
                .policyName(policy.getName())
                .rules(ruleDetails)
                .totalRules(policy.getRules().size())
                .build();

        } catch (Exception e) {
            log.error("Error getting policy details: {}", policyName, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Policy not found: " + policyName);
        }
    }

    /**
     * Converts internal RuleResult to DTO
     */
    private Map<String, RuleResultDto> convertRuleResults(Map<String, RuleResult> results) {
        return results.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> RuleResultDto.builder()
                    .result(entry.getValue().getResult())
                    .success(entry.getValue().isSuccess())
                    .error(entry.getValue().getError())
                    .executionTimeMs(entry.getValue().getExecutionTimeMs())
                    .build()
            ));
    }
}