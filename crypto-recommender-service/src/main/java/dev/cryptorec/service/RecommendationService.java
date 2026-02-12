package dev.cryptorec.service;

import dev.cryptorec.model.CryptoStats;
import dev.cryptorec.model.PaginatedResult;
import dev.cryptorec.model.exception.ValidationException;
import dev.cryptorec.model.util.DateRangeResolver;
import dev.cryptorec.model.util.StatsCalculator;
import dev.cryptorec.provider.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;

/**
 * Service layer for cryptocurrency recommendation logic.
 * Handles calculation of statistics, sorting, pagination, and filtering.
 */
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final DataProvider dataProvider;

    /**
     * Creates a recommendation service with the given data provider.
     */
    public RecommendationService(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    /**
     * Retrieves recommendations (list of cryptos sorted by normalized range).
     *
     * @param page         zero-based page index
     * @param size         page size
     * @param sortBy       sort field and direction (e.g., "normalizedRange_desc")
     * @param fromDate     start date (nullable)
     * @param toDate       end date (nullable)
     * @param periodMonths months to look back (nullable)
     * @return Map containing "items", "page", "size", "totalElements", "totalPages"
     */
    public PaginatedResult<CryptoStats> getRecommendations(int page, int size, String sortBy, LocalDate fromDate,
                                                           LocalDate toDate, Integer periodMonths) {
        log.debug("getRecommendations: page={}, size={}, sortBy={}", page, size, sortBy);

        var timeframe = DateRangeResolver.resolveTimeframe(fromDate, toDate, periodMonths);
        LocalDate resolvedFromDate = timeframe[0];
        LocalDate resolvedToDate = timeframe[1];

        log.debug("getPaginatedStats: page={}, size={}, sortBy={}, from={}, to={}", page, size, sortBy, resolvedFromDate,
                resolvedToDate);

        return dataProvider.getPaginatedStats(page, size, sortBy, resolvedFromDate, resolvedToDate, periodMonths);
    }

    /**
     * Retrieves statistics for a specific cryptocurrency.
     *
     * @param symbol       cryptocurrency symbol
     * @param fromDate     start date (nullable)
     * @param toDate       end date (nullable)
     * @param periodMonths months to look back (nullable)
     * @return CryptoStats for the cryptocurrency
     */
    public CryptoStats getStats(String symbol, LocalDate fromDate, LocalDate toDate, Integer periodMonths) {
        var timeframe = DateRangeResolver.resolveTimeframe(fromDate, toDate, periodMonths);
        LocalDate resolvedFromDate = timeframe[0];
        LocalDate resolvedToDate = timeframe[1];

        log.debug("getStats: symbol={}, from={}, to={}", symbol, resolvedFromDate, resolvedToDate);

        var cryptoPrices = dataProvider.getCryptoPricesByTimeframe(symbol, resolvedFromDate, resolvedToDate);
        return StatsCalculator.calculateStats(symbol, cryptoPrices.prices(), resolvedFromDate, resolvedToDate);
    }

    /**
     * Retrieves the cryptocurrency with the highest normalized range in the given timeframe.
     *
     * @param fromDate     start date (nullable)
     * @param toDate       end date (nullable)
     * @param periodMonths months to look back (nullable)
     * @return CryptoStats for the top cryptocurrency
     */
    public CryptoStats getTopCrypto(LocalDate fromDate, LocalDate toDate, Integer periodMonths) {
        var timeframe = DateRangeResolver.resolveTimeframe(fromDate, toDate, periodMonths);
        LocalDate resolvedFromDate = timeframe[0];
        LocalDate resolvedToDate = timeframe[1];

        log.debug("getTopCrypto: from={}, to={}", resolvedFromDate, resolvedToDate);

        List<String> cryptos = dataProvider.getAllCryptos();
        List<CryptoStats> stats = new ArrayList<>();

        for (String crypto : cryptos) {
            try {
                var cryptoPrices = dataProvider.getCryptoPricesByTimeframe(crypto, resolvedFromDate, resolvedToDate);
                stats.add(StatsCalculator.calculateStats(crypto, cryptoPrices.prices(), resolvedFromDate, resolvedToDate));
            } catch (Exception e) {
                log.warn("Failed to calculate stats for {}: {}", crypto, e.getMessage());
            }
        }

        if (stats.isEmpty()) {
            throw new ValidationException("No price data available for the specified timeframe");
        }

        return stats.stream()
                .max(Comparator.comparing(CryptoStats::normalizedRange))
                .orElseThrow(() -> new ValidationException("Unable to find top cryptocurrency"));
    }
}

