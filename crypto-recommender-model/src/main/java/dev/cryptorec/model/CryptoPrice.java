package dev.cryptorec.model;

import java.util.List;

/**
 * Immutable record representing all price data for a single cryptocurrency.
 *
 * @param symbol Cryptocurrency symbol (e.g., BTC, ETH)
 * @param prices Sorted list of price data points (chronologically ordered, earliest first)
 */
public record CryptoPrice(
        String symbol,
        List<PriceData> prices
) {
    /**
     * Validates the record upon creation.
     *
     * @throws IllegalArgumentException if invariants are violated
     */
    public CryptoPrice {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol must not be blank");
        }
        if (prices == null) {
            throw new IllegalArgumentException("Prices list must not be null");
        }
    }
}

