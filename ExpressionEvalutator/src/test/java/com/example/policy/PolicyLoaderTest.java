package com.example.policy;

import com.example.policy.model.Policy;
import com.example.policy.model.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PolicyLoader functionality
 */
public class PolicyLoaderTest {

    private PolicyLoader policyLoader;

    @BeforeEach
    void setUp() {
        policyLoader = new PolicyLoader();
    }

    @Test
    void testLoadFC1Policy() {
        Policy policy = policyLoader.loadPolicy("fc1Rules");

        assertNotNull(policy);
        assertEquals("fc1Rules", policy.getName());
        assertEquals("Feature check policy 1 - Basic user validation", policy.getDescription());
        assertEquals("1.0", policy.getVersion());

        // Check rules
        assertEquals(3, policy.getRules().size());
        assertTrue(policy.hasRule("rule1"));
        assertTrue(policy.hasRule("rule2"));
        assertTrue(policy.hasRule("rule3"));

        // Check rule1
        Rule rule1 = policy.getRule("rule1");
        assertEquals("rule1", rule1.getName());
        assertEquals("featureMap.userAge > 18", rule1.getExpression());
        assertEquals("Check if user is adult", rule1.getDescription());
        assertTrue(rule1.getDependencies().isEmpty());

        // Check rule3 dependencies
        Rule rule3 = policy.getRule("rule3");
        assertEquals(2, rule3.getDependencies().size());
        assertTrue(rule3.getDependencies().contains("rule1"));
        assertTrue(rule3.getDependencies().contains("rule2"));
    }

    @Test
    void testLoadAccessControlPolicy() {
        Policy policy = policyLoader.loadPolicy("accessControlRules");

        assertNotNull(policy);
        assertEquals("accessControlRules", policy.getName());
        assertEquals(4, policy.getRules().size());

        // Check that accessGranted rule has proper dependencies
        Rule accessGranted = policy.getRule("accessGranted");
        assertEquals(3, accessGranted.getDependencies().size());
    }

    @Test
    void testPolicyNotFound() {
        assertThrows(PolicyLoader.PolicyLoadException.class, () -> {
            policyLoader.loadPolicy("nonExistentPolicy");
        });
    }

    @Test
    void testResolveDependencyOrder() {
        Policy policy = policyLoader.loadPolicy("fc1Rules");
        List<String> executionOrder = policyLoader.resolveDependencyOrder(policy);

        assertEquals(3, executionOrder.size());

        // rule1 and rule2 should come before rule3
        int rule1Index = executionOrder.indexOf("rule1");
        int rule2Index = executionOrder.indexOf("rule2");
        int rule3Index = executionOrder.indexOf("rule3");

        assertTrue(rule1Index < rule3Index);
        assertTrue(rule2Index < rule3Index);
    }

    @Test
    void testResolveDependencyOrderComplex() {
        Policy policy = policyLoader.loadPolicy("accessControlRules");
        List<String> executionOrder = policyLoader.resolveDependencyOrder(policy);

        assertEquals(4, executionOrder.size());

        // accessGranted should be last
        assertEquals("accessGranted", executionOrder.get(executionOrder.size() - 1));

        // All dependencies should come before accessGranted
        int accessGrantedIndex = executionOrder.indexOf("accessGranted");
        assertTrue(executionOrder.indexOf("adminCheck") < accessGrantedIndex);
        assertTrue(executionOrder.indexOf("departmentCheck") < accessGrantedIndex);
        assertTrue(executionOrder.indexOf("clearanceCheck") < accessGrantedIndex);
    }
}