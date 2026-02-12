package dev.cryptorec.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Immutable record representing aggregated statistics for a cryptocurrency in a given timeframe.
 *
 * @param symbol          Cryptocurrency symbol (e.g., BTC, ETH)
 * @param normalizedRange (max - min) / min, representing price volatility
 * @param min             The minimum price point within the timeframe
 * @param max             The maximum price point within the timeframe
 * @param oldest          The oldest (earliest) price point within the timeframe
 * @param newest          The newest (latest) price point within the timeframe
 * @param timeframeFrom   Start date of the timeframe (inclusive)
 * @param timeframeTo     End date of the timeframe (inclusive)
 */
public record CryptoStats(
        String symbol,
        BigDecimal normalizedRange,
        PriceData min,
        PriceData max,
        PriceData oldest,
        PriceData newest,
        LocalDate timeframeFrom,
        LocalDate timeframeTo
) {
    /**
     * Validates the record upon creation.
     *
     * @throws IllegalArgumentException if invariants are violated
     */
    public CryptoStats {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol must not be blank");
        }
        if (normalizedRange == null || normalizedRange.signum() < 0) {
            throw new IllegalArgumentException("Normalized range must be non-negative");
        }
        if (min == null || max == null || oldest == null || newest == null) {
            throw new IllegalArgumentException("Price points must not be null");
        }
        if (timeframeFrom == null || timeframeTo == null) {
            throw new IllegalArgumentException("Timeframe dates must not be null");
        }
        if (timeframeFrom.isAfter(timeframeTo)) {
            throw new IllegalArgumentException("Timeframe from must not be after to");
        }
    }
}

