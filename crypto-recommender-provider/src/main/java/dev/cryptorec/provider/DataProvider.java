package dev.cryptorec.provider;

import dev.cryptorec.model.CryptoPrice;
import dev.cryptorec.model.CryptoStats;
import dev.cryptorec.model.PaginatedResult;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface for data providers.
 * Implementations can read from CSV, database, or external APIs.
 * Designed for extensibility and future support of multiple data sources.
 */
public interface DataProvider {

    /**
     * Retrieves all available cryptos.
     *
     * @return list of crypto symbols (e.g., BTC, ETH, XRP)
     */
    List<String> getAllCryptos();

    /**
     * Retrieves all price data for a specific cryptocurrency.
     *
     * @param symbol cryptocurrency symbol
     * @return CryptoPrice containing all price data for the symbol
     * @throws dev.cryptorec.model.exception.CryptoNotFoundException if symbol not found
     */
    CryptoPrice getCryptoPrices(String symbol);

    /**
     * Retrieves price data for a specific cryptocurrency within a date range.
     * Dates are inclusive.
     *
     * @param symbol   cryptocurrency symbol
     * @param fromDate start date (inclusive)
     * @param toDate   end date (inclusive)
     * @return CryptoPrice containing filtered price data
     * @throws dev.cryptorec.model.exception.CryptoNotFoundException if symbol not found
     */
    CryptoPrice getCryptoPricesByTimeframe(String symbol, LocalDate fromDate, LocalDate toDate);

    /**
     * Retrieves paginated cryptocurrency statistics for all cryptos.
     * Includes sorting and pagination logic.
     *
     * @param page         zero-based page index
     * @param size         page size
     * @param sortBy       sort field and direction (e.g., "normalizedRange_desc")
     * @param fromDate     start date (nullable)
     * @param toDate       end date (nullable)
     * @param periodMonths months to look back (nullable)
     * @return PaginatedResult containing crypto statistics and pagination metadata
     */
    PaginatedResult<CryptoStats> getPaginatedStats(
            int page,
            int size,
            String sortBy,
            LocalDate fromDate,
            LocalDate toDate,
            Integer periodMonths
    );
}

