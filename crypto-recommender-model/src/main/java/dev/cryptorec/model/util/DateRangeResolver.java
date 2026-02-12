package dev.cryptorec.model.util;

import dev.cryptorec.model.exception.InvalidTimeframeException;

import java.time.LocalDate;

/**
 * Utility class for resolving and validating timeframe parameters.
 * Handles conversion of periodMonths to actual date ranges.
 */
public final class DateRangeResolver {

    private DateRangeResolver() {
        // Utility class, no instantiation
    }

    /**
     * Resolves timeframe parameters to actual from/to dates.
     *
     * @param fromDate     explicit start date (nullable)
     * @param toDate       explicit end date (nullable)
     * @param periodMonths number of months to look back (nullable)
     * @return array [fromDate, toDate] as LocalDate objects
     * @throws InvalidTimeframeException if parameters are invalid or ambiguous
     */
    public static LocalDate[] resolveTimeframe(LocalDate fromDate, LocalDate toDate, Integer periodMonths) {
        // Check for ambiguous parameters
        if (periodMonths != null && (fromDate != null || toDate != null)) {
            throw new InvalidTimeframeException(
                    "Cannot use periodMonths together with explicit fromDate/toDate"
            );
        }

        // If explicit dates provided
        if (fromDate != null || toDate != null) {
            LocalDate resolvedTo = toDate;

            if (fromDate == null) {
                throw new InvalidTimeframeException("fromDate must be provided if toDate is specified without periodMonths");
                // todo: or make it now ?
            }

            if (resolvedTo == null) {
                resolvedTo = LocalDate.now();
            }

            if (fromDate.isAfter(resolvedTo)) {
                throw new InvalidTimeframeException("fromDate cannot be after toDate");
            }

            return new LocalDate[]{fromDate, resolvedTo};
        }

        // Use periodMonths (default 1 if not specified)
        int months = periodMonths != null ? periodMonths : 1;
        if (months < 1 || months > 60) {
            throw new InvalidTimeframeException("periodMonths must be between 1 and 60");
        }

        LocalDate to = LocalDate.now();
        LocalDate from = to.minusMonths(months);

        return new LocalDate[]{from, to};
    }
}

