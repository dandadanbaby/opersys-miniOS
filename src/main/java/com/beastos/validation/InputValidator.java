package com.beastos.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Centralized input validation used by all BEAST OS modules.
 * Provides reusable validation methods for common input patterns.
 */
public final class InputValidator {

    private InputValidator() {}

    /** Validate that a string is not null or blank. */
    public static ValidationResult requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return ValidationResult.failure(fieldName + " cannot be empty.");
        }
        return ValidationResult.success();
    }

    /** Parse and validate a non-negative integer. */
    public static ValidationResult validateNonNegativeInt(String value, String fieldName) {
        ValidationResult blank = requireNonBlank(value, fieldName);
        if (blank.hasErrors()) return blank;
        try {
            int v = Integer.parseInt(value.trim());
            if (v < 0) {
                return ValidationResult.failure(fieldName + " must be non-negative. Got: " + v);
            }
            return ValidationResult.success();
        } catch (NumberFormatException e) {
            return ValidationResult.failure(fieldName + " must be a valid integer. Got: '" + value.trim() + "'");
        }
    }

    /** Parse and validate a positive integer (>0). */
    public static ValidationResult validatePositiveInt(String value, String fieldName) {
        ValidationResult blank = requireNonBlank(value, fieldName);
        if (blank.hasErrors()) return blank;
        try {
            int v = Integer.parseInt(value.trim());
            if (v <= 0) {
                return ValidationResult.failure(fieldName + " must be a positive integer (> 0). Got: " + v);
            }
            return ValidationResult.success();
        } catch (NumberFormatException e) {
            return ValidationResult.failure(fieldName + " must be a valid integer. Got: '" + value.trim() + "'");
        }
    }

    /** Validate that a value is within a range [min, max]. */
    public static ValidationResult validateRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            return ValidationResult.failure(
                    fieldName + " must be between " + min + " and " + max + ". Got: " + value);
        }
        return ValidationResult.success();
    }

    /** Validate that no duplicate IDs exist in a list. */
    public static ValidationResult validateNoDuplicates(List<String> ids, String fieldName) {
        Set<String> seen = new HashSet<>();
        List<String> dups = new ArrayList<>();
        for (String id : ids) {
            if (!seen.add(id)) {
                dups.add(id);
            }
        }
        if (!dups.isEmpty()) {
            return ValidationResult.failure(
                    "Duplicate " + fieldName + " found: " + String.join(", ", dups));
        }
        return ValidationResult.success();
    }

    /** Parse a space-separated string of integers. Returns null on parse error. */
    public static int[] parseIntArray(String input) {
        if (input == null || input.isBlank()) return null;
        String[] parts = input.trim().split("[\\s,]+");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                result[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return result;
    }

    /** Validate that allocation[i][j] <= max[i][j] for all i,j. */
    public static ValidationResult validateAllocationNotExceedsMax(
            int[][] allocation, int[][] max, int processes, int resources) {
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < processes; i++) {
            for (int j = 0; j < resources; j++) {
                if (allocation[i][j] > max[i][j]) {
                    errors.add("Allocation[P" + i + "][R" + j + "] = " + allocation[i][j] +
                            " exceeds Maximum[P" + i + "][R" + j + "] = " + max[i][j]);
                }
            }
        }
        if (errors.isEmpty()) return ValidationResult.success();
        return ValidationResult.failure(errors);
    }
}
