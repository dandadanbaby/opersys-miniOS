package com.beastos.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of an input validation check.
 * Contains success/failure status and accumulated error messages.
 */
public class ValidationResult {

    private final boolean valid;
    private final List<String> errors;

    private ValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = Collections.unmodifiableList(errors);
    }

    public static ValidationResult success() {
        return new ValidationResult(true, List.of());
    }

    public static ValidationResult failure(String error) {
        return new ValidationResult(false, List.of(error));
    }

    public static ValidationResult failure(List<String> errors) {
        return new ValidationResult(false, new ArrayList<>(errors));
    }

    public boolean isValid() { return valid; }
    public boolean hasErrors() { return !valid; }
    public List<String> getErrors() { return errors; }
    public String getFirstError() { return errors.isEmpty() ? "" : errors.get(0); }

    /**
     * Combine multiple validation results.
     */
    public static ValidationResult combine(ValidationResult... results) {
        List<String> allErrors = new ArrayList<>();
        for (ValidationResult r : results) {
            allErrors.addAll(r.errors);
        }
        if (allErrors.isEmpty()) {
            return success();
        }
        return failure(allErrors);
    }
}
