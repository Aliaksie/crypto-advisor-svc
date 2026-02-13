package dev.cryptorec.provider;

import dev.cryptorec.model.PriceData;
import dev.cryptorec.model.CryptoPrice;
import dev.cryptorec.model.CryptoStats;
import dev.cryptorec.model.PaginatedResult;
import dev.cryptorec.model.exception.CryptoNotFoundException;
import dev.cryptorec.model.exception.ValidationException;
import dev.cryptorec.model.util.StatsCalculator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * CSV-based data provider that loads cryptocurrency price data from CSV files.
 * Caches data in memory and thread-safe by using ConcurrentHashMap for MVP.
 * <p>
 * CSV file format: timestamp (epoch ms), symbol, price
 * Files are expected in the format: {symbol}_values.csv in the configured(mount) directory.
 */
public class CsvDataProvider implements DataProvider {

    private static final Logger log = LoggerFactory.getLogger(CsvDataProvider.class);
    private static final String CSV_EXTENSION = "_values.csv";
    // todo: in real we would want to dynamically discover available cryptos by scanning the directory, but for MVP we hardcode supported list
    private static final List<String> SUPPORTED_CRYPTOS = List.of("BTC", "ETH", "LTC", "DOGE", "XRP");

    private final Path csvDirectory;
    private final Map<String, CryptoPrice> cache = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;

    /**
     * Creates a CSV provider with the specified directory.
     *
     * @param csvDirectory path to the directory containing CSV files
     */
    public CsvDataProvider(String csvDirectory) {
        this.csvDirectory = Path.of(csvDirectory);
        this.initializeCache();
    }

    /**
     * Initializes the cache by loading all CSV files for supported cryptos.
     * todo: in a real implementation, we would want to watch the directory for changes and update the cache accordingly,
     * but for MVP we load once at startup.
     */
    private void initializeCache() {
        synchronized (this) {
            if (initialized) {
                return;
            }

            log.info("Initializing CSV provider with directory: {}", csvDirectory.toAbsolutePath());

            for (String symbol : SUPPORTED_CRYPTOS) {
                try {
                    loadCryptoData(symbol);
                } catch (Exception e) {
                    log.warn("Failed to load data for crypto {}: {}", symbol, e.getMessage());
                }
            }

            initialized = true;
            log.info("CSV provider initialized with {} cryptos", cache.size());
        }
    }

    /**
     * Loads cryptocurrency data from CSV file.
     *
     * @param symbol cryptocurrency symbol
     */
    private void loadCryptoData(String symbol) throws IOException {
        Path csvFile = csvDirectory.resolve(symbol + CSV_EXTENSION);

        if (!Files.exists(csvFile)) {
            throw new IOException("CSV file not found: " + csvFile);
        }

        List<PriceData> prices = new ArrayList<>();

        try (InputStream inputStream = Files.newInputStream(csvFile);
             InputStreamReader reader = new InputStreamReader(inputStream);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader().withIgnoreEmptyLines())) {

            for (CSVRecord record : csvParser) {
                long timestamp = Long.parseLong(record.get("timestamp"));
                BigDecimal priceInUsd = new BigDecimal(record.get("price"));

                // PriceData only contains timestamp and price (in USD)
                // The crypto symbol is stored separately in CryptoPrice
                prices.add(new PriceData(timestamp, priceInUsd));
            }
        }

        // Sort by timestamp (ascending - earliest first)
        prices.sort(Comparator.comparingLong(PriceData::timestamp));

        cache.put(symbol, new CryptoPrice(symbol, Collections.unmodifiableList(prices)));
        log.debug("Loaded {} price points for {}", prices.size(), symbol);
    }

    @Override
    public List<String> getAllCryptos() {
        ensureInitialized();
        return new ArrayList<>(cache.keySet());
    }

    @Override
    public CryptoPrice getCryptoPrices(String symbol) {
        ensureInitialized();
        String upperSymbol = symbol.toUpperCase();

        var cryptoPrice = cache.get(upperSymbol);
        if (cryptoPrice == null) {
            throw new CryptoNotFoundException("Cryptocurrency not found: " + symbol + ". Available: " + cache.keySet());
        }

        return cryptoPrice;
    }

    @Override
    public CryptoPrice getCryptoPricesByTimeframe(String symbol, LocalDate fromDate, LocalDate toDate) {
        CryptoPrice allPrices = getCryptoPrices(symbol);

        long fromEpoch = dateToEpochMillis(fromDate);
        long toEpoch = dateToEpochMillis(toDate) + (24 * 60 * 60 * 1000) - 1; // End of day

        List<PriceData> filtered = allPrices.prices().stream()
                .filter(p -> p.timestamp() >= fromEpoch && p.timestamp() <= toEpoch)
                .collect(Collectors.toList());

        return new CryptoPrice(symbol, filtered);
    }

    @Override
    public PaginatedResult<CryptoStats> getPaginatedStats(int page, int size, String sortBy, LocalDate fromDate,
                                                          LocalDate toDate, Integer periodMonths) {
        List<String> cryptos = getAllCryptos();
        List<CryptoStats> stats = new ArrayList<>();

        for (String crypto : cryptos) {
            try {
                var cryptoPrices = getCryptoPricesByTimeframe(crypto, fromDate, toDate);
                stats.add(StatsCalculator.calculateStats(crypto, cryptoPrices.prices(), fromDate, toDate));
            } catch (Exception e) {
                log.warn("Failed to calculate stats for {}: {}", crypto, e.getMessage());
            }
        }

        // Sort
        stats.sort(createComparator(sortBy));

        // Paginate
        int totalElements = stats.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIdx = Math.min(page * size, totalElements);
        int endIdx = Math.min(startIdx + size, totalElements);

        List<CryptoStats> pageItems = stats.subList(startIdx, endIdx);

        return new PaginatedResult<>(pageItems, page, size, totalElements, totalPages);
    }

    /**
     * Converts LocalDate to epoch milliseconds (start of day in UTC).
     */
    private long dateToEpochMillis(LocalDate date) {
        return date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli();
    }

    /**
     * Ensures the cache is initialized before any operation.
     */
    private void ensureInitialized() {
        if (!initialized) {
            initializeCache();
        }
    }

    /**
     * Creates a comparator based on the sortBy parameter.
     * Format: "field_direction" e.g., "normalizedRange_desc", "symbol_asc"
     * todo: in future DSL query, and resolver per data provider
     */
    private Comparator<CryptoStats> createComparator(String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "normalizedRange_desc";
        }

        String[] parts = sortBy.split("_");
        if (parts.length != 2) {
            throw new ValidationException("Invalid sort format. Expected 'field_direction' (e.g., normalizedRange_desc)");
        }

        String field = parts[0];
        String direction = parts[1];

        Comparator<CryptoStats> comparator = switch (field) {
            case "normalizedRange" -> Comparator.comparing(CryptoStats::normalizedRange);
            case "symbol" -> Comparator.comparing(CryptoStats::symbol);
            case "min" -> Comparator.comparing(s -> s.min().price());
            case "max" -> Comparator.comparing(s -> s.max().price());
            default -> throw new ValidationException("Invalid sort field: " + field);
        };

        if ("asc".equalsIgnoreCase(direction)) {
            return comparator;
        } else if ("desc".equalsIgnoreCase(direction)) {
            return comparator.reversed();
        } else {
            throw new ValidationException("Invalid sort direction: " + direction);
        }
    }
}

