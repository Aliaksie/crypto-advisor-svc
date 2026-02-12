package dev.cryptorec.model.exception;

/**
 * Thrown when a requested cryptocurrency is not found or not supported.
 */
public class CryptoNotFoundException extends RuntimeException {
    public CryptoNotFoundException(String message) {
        super(message);
    }

    public CryptoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

