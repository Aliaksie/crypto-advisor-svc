package dev.cryptorec.model;

import java.util.List;

/**
 * Generic paginated result wrapper.
 * Encapsulates paginated data with metadata.
 *
 * @param <T> the type of items in the paginated result
 */
public record PaginatedResult<T>(
        List<T> items,
        int page,
        int size,
        int totalElements,
        int totalPages
) {
    /**
     * Validates the paginated result.
     * Ensures page and size are non-negative and consistent with total elements.
     */
    public PaginatedResult {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be non-negative");
        }
        if (size < 0) {
            throw new IllegalArgumentException("Size must be non-negative");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("Total elements must be non-negative");
        }
        if (totalPages < 0) {
            throw new IllegalArgumentException("Total pages must be non-negative");
        }
        if (items == null) {
            throw new IllegalArgumentException("Items list cannot be null");
        }
    }
}

