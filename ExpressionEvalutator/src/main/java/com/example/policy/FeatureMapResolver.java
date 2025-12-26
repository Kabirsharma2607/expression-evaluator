package com.example.policy;

import java.util.Map;

/**
 * Resolves values from feature maps using dot notation and handles nested objects
 */
public class FeatureMapResolver {

    /**
     * Resolves a value from the feature map using dot notation
     * Example: "featureMap.user.profile.age" or "featureMap.accountBalance"
     */
    public Object resolveValue(String path, Map<String, Object> featureMap) {
        if (path == null || path.trim().isEmpty()) {
            throw new InvalidPathException("Path cannot be null or empty");
        }

        // Remove "featureMap." prefix if present
        String actualPath = path.startsWith("featureMap.") ? path.substring("featureMap.".length()) : path;

        return resolveNestedValue(actualPath, featureMap);
    }

    /**
     * Resolves nested value using dot notation
     */
    @SuppressWarnings("unchecked")
    private Object resolveNestedValue(String path, Map<String, Object> currentMap) {
        if (currentMap == null) {
            return null;
        }

        String[] parts = path.split("\\.", 2);
        String currentKey = parts[0];

        // Check for array indexing like "items[0]"
        if (currentKey.contains("[") && currentKey.contains("]")) {
            return resolveArrayAccess(currentKey, parts.length > 1 ? parts[1] : null, currentMap);
        }

        Object value = currentMap.get(currentKey);

        // If this is the last part of the path, return the value
        if (parts.length == 1) {
            return value;
        }

        // Continue resolving if there are more parts and value is a map
        if (value instanceof Map) {
            return resolveNestedValue(parts[1], (Map<String, Object>) value);
        }

        // Value is not a map but we have more path parts - invalid path
        throw new InvalidPathException("Cannot resolve path '" + path + "' - '" + currentKey + "' is not a nested object");
    }

    /**
     * Handles array access like "items[0]" or "items[0].property"
     */
    @SuppressWarnings("unchecked")
    private Object resolveArrayAccess(String arrayExpression, String remainingPath, Map<String, Object> currentMap) {
        int openBracket = arrayExpression.indexOf('[');
        int closeBracket = arrayExpression.indexOf(']');

        if (openBracket == -1 || closeBracket == -1 || closeBracket <= openBracket) {
            throw new InvalidPathException("Invalid array access syntax: " + arrayExpression);
        }

        String arrayKey = arrayExpression.substring(0, openBracket);
        String indexStr = arrayExpression.substring(openBracket + 1, closeBracket);

        Object arrayObject = currentMap.get(arrayKey);
        if (arrayObject == null) {
            return null;
        }

        if (!(arrayObject instanceof Object[])) {
            throw new InvalidPathException("'" + arrayKey + "' is not an array");
        }

        try {
            int index = Integer.parseInt(indexStr);
            Object[] array = (Object[]) arrayObject;

            if (index < 0 || index >= array.length) {
                throw new InvalidPathException("Array index " + index + " out of bounds for array '" + arrayKey + "'");
            }

            Object value = array[index];

            // If there's no remaining path, return the value
            if (remainingPath == null || remainingPath.trim().isEmpty()) {
                return value;
            }

            // Continue resolving if value is a map
            if (value instanceof Map) {
                return resolveNestedValue(remainingPath, (Map<String, Object>) value);
            }

            throw new InvalidPathException("Cannot continue resolving path - array element is not a nested object");

        } catch (NumberFormatException e) {
            throw new InvalidPathException("Invalid array index: " + indexStr);
        }
    }

    /**
     * Checks if a value exists at the given path
     */
    public boolean hasValue(String path, Map<String, Object> featureMap) {
        try {
            Object value = resolveValue(path, featureMap);
            return value != null;
        } catch (InvalidPathException e) {
            return false;
        }
    }

    /**
     * Gets the type of value at the given path
     */
    public Class<?> getValueType(String path, Map<String, Object> featureMap) {
        Object value = resolveValue(path, featureMap);
        return value != null ? value.getClass() : null;
    }

    /**
     * Validates that all required paths exist in the feature map
     */
    public void validateRequiredPaths(String[] paths, Map<String, Object> featureMap) {
        for (String path : paths) {
            if (!hasValue(path, featureMap)) {
                throw new MissingFeatureException("Required feature not found: " + path);
            }
        }
    }

    // Custom Exceptions
    public static class InvalidPathException extends RuntimeException {
        public InvalidPathException(String message) {
            super(message);
        }
    }

    public static class MissingFeatureException extends RuntimeException {
        public MissingFeatureException(String message) {
            super(message);
        }
    }
}