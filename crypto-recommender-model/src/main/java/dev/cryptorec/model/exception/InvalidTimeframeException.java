package dev.cryptorec.model.exception;

/**
 * Thrown when request parameters specify an invalid or conflicting timeframe.
 */
public class InvalidTimeframeException extends RuntimeException {
    public InvalidTimeframeException(String message) {
        super(message);
    }

    public InvalidTimeframeException(String message, Throwable cause) {
        super(message, cause);
    }
}

