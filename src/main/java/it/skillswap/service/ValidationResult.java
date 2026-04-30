package it.skillswap.service;

/**
 * Encapsulates the result of a validation operation.
 * Allows for soft validation that returns success/failure with a message.
 */
public class ValidationResult {
    private final boolean valid;
    private final String message;

    /**
     * Constructs a ValidationResult with the specified state and message.
     *
     * @param valid   true if validation passed, false otherwise
     * @param message detailed message explaining the result
     */
    public ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    /**
     * Factory method for successful validation.
     * @return a successful ValidationResult with no message
     */
    public static ValidationResult success() {
        return new ValidationResult(true, "");
    }

    /**
     * Factory method for failed validation.
     * @param message explanation of the validation failure
     * @return a failed ValidationResult with the provided message
     */
    public static ValidationResult failure(String message) {
        return new ValidationResult(false, message);
    }

    /**
     * Checks if the validation passed.
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Gets the validation message.
     * @return the message describing the result
     */
    public String getMessage() {
        return message;
    }
}
