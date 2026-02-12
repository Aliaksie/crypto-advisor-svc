package dev.cryptorec.model;

import java.math.BigDecimal;

/**
 * Immutable record representing a single price point for a cryptocurrency.
 * All prices are denominated in USD (United States Dollars).
 * <p>
 * The semantic meaning:
 * - A cryptocurrency (identified by CryptoStats.symbol) has a price point
 * - This price point was recorded at a specific timestamp (UTC)
 * - The price value is in USD currency
 * <p>
 * Example: 1 BTC (from CryptoStats) = 46813.21 USD (from PriceData)
 *
 * @param timestamp Epoch milliseconds (UTC) since 1970-01-01 when the price was recorded
 * @param price     Price value in USD with precision up to 2 decimal places (e.g., 46813.21)
 */
public record PriceData(
        long timestamp,
        String currency, // Always "USD", included for clarity and potential future extension
        BigDecimal price
) {

    // Constant for currency, since all prices are in USD
    public static final String CURRENCY = "USD";

    //  constructor that defaults currency to USD
    public PriceData(long timestamp, BigDecimal priceInUsd) {
        this(timestamp, CURRENCY, priceInUsd);
    }

    /**
     * Validates the record upon creation.
     *
     * @throws IllegalArgumentException if price is null, negative
     */
    public PriceData {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        if (timestamp < 0) {
            throw new IllegalArgumentException("Timestamp must be non-negative");
        }
    }


}

