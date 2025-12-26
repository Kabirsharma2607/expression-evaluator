package com.example.api.service;

import com.example.expression.ExpressionEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Service for direct expression evaluation
 */
@Service
@Slf4j
public class ExpressionEvaluationService {

    private final ExpressionEvaluator expressionEvaluator;

    public ExpressionEvaluationService() {
        this.expressionEvaluator = new ExpressionEvaluator();
    }

    /**
     * Evaluates a single expression
     */
    public Object evaluateExpression(String expression,
                                    Map<String, Object> featureMap,
                                    Map<String, Object> ruleContext) {
        log.debug("Evaluating expression: {}", expression);

        try {
            return expressionEvaluator.evaluate(expression, featureMap, ruleContext);
        } catch (Exception e) {
            log.error("Error evaluating expression: {}", expression, e);

            // Provide more specific error messages based on exception type
            if (e.getMessage() != null) {
                if (e.getMessage().contains("Unknown identifier") ||
                    e.getMessage().contains("Failed to access")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid expression - " + e.getMessage());
                }

                if (e.getMessage().contains("Unexpected token") ||
                    e.getMessage().contains("parse")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Syntax error in expression - " + e.getMessage());
                }

                if (e.getMessage().contains("Type mismatch") ||
                    e.getMessage().contains("requires")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Type error in expression - " + e.getMessage());
                }
            }

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid expression: " + e.getMessage());
        }
    }
}