package dev.cryptorec.model.util;

import dev.cryptorec.model.PriceData;
import dev.cryptorec.model.CryptoStats;
import dev.cryptorec.model.exception.ValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for calculating cryptocurrency statistics.
 * All calculations use USD as the base currency with 2 decimal places precision.
 */
public final class StatsCalculator {

    /**
     * Scale for USD currency calculations (2 decimal places: $XX.XX)
     */
    private static final int USD_SCALE = 2;

    /**
     * Rounding mode for USD calculations
     */
    private static final RoundingMode USD_ROUNDING_MODE = RoundingMode.HALF_UP;

    private StatsCalculator() {
        // Utility class, no instantiation
    }

    /**
     * Calculates aggregated statistics for a list of price data.
     * Results are in USD currency with proper scale (2 decimal places).
     *
     * @param symbol   cryptocurrency symbol (e.g., BTC, ETH)
     * @param prices   list of price data points in USD
     * @param fromDate start date of the timeframe
     * @param toDate   end date of the timeframe
     * @return CryptoStats containing aggregated values in USD
     * @throws ValidationException if prices list is empty
     */
    public static CryptoStats calculateStats(
            String symbol,
            List<PriceData> prices,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        if (prices == null || prices.isEmpty()) {
            throw new ValidationException("No price data available for " + symbol + " in the specified timeframe");
        }

        PriceData minPrice = findMin(prices);
        PriceData maxPrice = findMax(prices);
        PriceData oldest = prices.getFirst();
        PriceData newest = prices.getLast();

        BigDecimal normalizedRange = calculateNormalizedRange(minPrice.price(), maxPrice.price());

        return new CryptoStats(
                symbol,
                normalizedRange,
                minPrice,
                maxPrice,
                oldest,
                newest,
                fromDate,
                toDate
        );
    }

    /**
     * Calculates normalized range: (max - min) / min
     * Normalized range shows the volatility as a ratio.
     * Result is scaled to 2 decimal places for USD precision.
     *
     * @param min minimum price in USD
     * @param max maximum price in USD
     * @return normalized range ratio scaled to 2 decimal places
     */
    public static BigDecimal calculateNormalizedRange(BigDecimal min, BigDecimal max) {
        Objects.requireNonNull(min, "min price cannot be null");
        Objects.requireNonNull(max, "max price cannot be null");

        if (min.signum() == 0) {
            throw new ValidationException("Minimum price cannot be zero");
        }
        if (min.signum() < 0 || max.signum() < 0) {
            throw new ValidationException("Prices must be non-negative");
        }

        BigDecimal range = max.subtract(min);
        return range.divide(min, USD_SCALE, USD_ROUNDING_MODE);
    }

    /**
     * Finds the price data point with minimum price.
     *
     * @param prices list of price data points
     * @return PriceData with the lowest price
     */
    public static PriceData findMin(List<PriceData> prices) {
        if (prices == null || prices.isEmpty()) {
            throw new IllegalArgumentException("Prices list cannot be null or empty");
        }

        return prices.stream()
                .min(Comparator.comparing(PriceData::price))
                .orElseThrow(() -> new ValidationException("Unable to find minimum price"));
    }

    /**
     * Finds the price data point with maximum price.
     *
     * @param prices list of price data points
     * @return PriceData with the highest price
     */
    public static PriceData findMax(List<PriceData> prices) {
        if (prices == null || prices.isEmpty()) {
            throw new IllegalArgumentException("Prices list cannot be null or empty");
        }

        return prices.stream()
                .max(Comparator.comparing(PriceData::price))
                .orElseThrow(() -> new ValidationException("Unable to find maximum price"));
    }

    /**
     * Converts epoch milliseconds to LocalDate (UTC timezone).
     *
     * @param epochMillis milliseconds since epoch
     * @return LocalDate in UTC
     */
    public static LocalDate epochMillisToLocalDate(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis)
                .atZone(ZoneId.of("UTC"))
                .toLocalDate();
    }
}

