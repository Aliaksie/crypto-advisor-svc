package dev.cryptorec.model.exception;

/**
 * Thrown when request validation fails due to invalid input parameters.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

