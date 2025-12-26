package com.example.policy;

import com.example.policy.model.*;
import com.example.expression.ExpressionEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Executes policies against feature maps
 */
@Component
@RequiredArgsConstructor
public class PolicyExecutor {
    private final PolicyLoader policyLoader;
    private final FeatureMapResolver featureResolver = new FeatureMapResolver();
    private final ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator();

    /**
     * Constructor for backward compatibility with Main class
     */
    public PolicyExecutor() {
        this.policyLoader = new PolicyLoader();
    }

    /**
     * Main entry point for policy execution
     */
    public PolicyResult executePolicy(Map<String, Object> featureMap, String policyName) {
        long startTime = System.currentTimeMillis();
        PolicyResult result = new PolicyResult(policyName);

        try {
            // Load policy
            Policy policy = policyLoader.loadPolicy(policyName);

            // Resolve execution order
            List<String> executionOrder = policyLoader.resolveDependencyOrder(policy);

            // Execute rules in order
            Map<String, Object> ruleContext = new HashMap<>();
            for (String ruleName : executionOrder) {
                Rule rule = policy.getRule(ruleName);
                RuleResult ruleResult = executeRule(rule, featureMap, ruleContext);

                result.addRuleResult(ruleName, ruleResult);

                // Add rule result to context for dependent rules
                ruleContext.put(ruleName, ruleResult.getResult());

                // If rule failed and it's not a dependency, note it but continue
                if (!ruleResult.isSuccess()) {
                    result.addError("Rule '" + ruleName + "' failed: " + ruleResult.getError());
                }
            }

            result.setSuccess(!result.hasErrors());

        } catch (Exception e) {
            result.setSuccess(false);
            result.addError("Policy execution failed: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        result.setExecutionTimeMs(endTime - startTime);

        return result;
    }

    /**
     * Executes a single rule using the expression evaluator
     */
    private RuleResult executeRule(Rule rule, Map<String, Object> featureMap, Map<String, Object> ruleContext) {
        long startTime = System.currentTimeMillis();
        RuleResult result = new RuleResult(rule.getName());

        try {
            // Use the full expression evaluator
            Object evaluationResult = expressionEvaluator.evaluate(rule.getExpression(), featureMap, ruleContext);

            result.setResult(evaluationResult);
            result.setSuccess(true);

        } catch (Exception e) {
            result.setError(e.getMessage());
            result.setSuccess(false);
        }

        long endTime = System.currentTimeMillis();
        result.setExecutionTimeMs(endTime - startTime);

        return result;
    }

}