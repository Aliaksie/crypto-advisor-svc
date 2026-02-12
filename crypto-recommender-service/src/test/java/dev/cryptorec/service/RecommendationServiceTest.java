package dev.cryptorec.service;

import dev.cryptorec.model.CryptoPrice;
import dev.cryptorec.model.CryptoStats;
import dev.cryptorec.model.PaginatedResult;
import dev.cryptorec.model.PriceData;
import dev.cryptorec.model.exception.CryptoNotFoundException;
import dev.cryptorec.provider.DataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class RecommendationServiceTest {

    @Mock
    private DataProvider dataProvider;

    private RecommendationService service;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        service = new RecommendationService(dataProvider);
    }

    private List<PriceData> createMockPrices() {
        return List.of(
                new PriceData(1641009600000L, new BigDecimal("46813.21")),
                new PriceData(1641096000000L, new BigDecimal("47143.98")),
                new PriceData(1641182400000L, new BigDecimal("45922.01"))
        );
    }

    @Test
    void testGetRecommendations() {
        LocalDate from = LocalDate.of(2022, 1, 1);
        LocalDate to = LocalDate.of(2022, 1, 31);

        // Create mock stats for the paginated result
        CryptoStats btcStats = new CryptoStats("BTC",
                new BigDecimal("0.0266"),
                new PriceData(1641009600000L, new BigDecimal("45922.01")),
                new PriceData(1641096000000L, new BigDecimal("47143.98")),
                new PriceData(1641009600000L, new BigDecimal("46813.21")),
                new PriceData(1641182400000L, new BigDecimal("47143.98")),
                from, to);
        CryptoStats ethStats = new CryptoStats("ETH",
                new BigDecimal("0.0690"),
                new PriceData(1641009600000L, new BigDecimal("2900.00")),
                new PriceData(1641096000000L, new BigDecimal("3100.00")),
                new PriceData(1641009600000L, new BigDecimal("3000.00")),
                new PriceData(1641182400000L, new BigDecimal("3100.00")),
                from, to);

        when(dataProvider.getPaginatedStats(0, 50, "normalizedRange_desc", from, to, null))
                .thenReturn(new PaginatedResult<>(List.of(btcStats, ethStats), 0, 50, 2, 1));

        var result = service.getRecommendations(0, 50, "normalizedRange_desc", from, to, null);

        assertNotNull(result);

        List<CryptoStats> items = result.items();
        assertEquals(2, items.size());
        assertEquals(0, result.page());
        assertEquals(50, result.size());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());
    }

    @Test
    void testGetRecommendationsPaginated() {
        LocalDate from = LocalDate.of(2021, 12, 1);
        LocalDate to = LocalDate.of(2022, 1, 31);

        // Create mock stats for pagination
        CryptoStats ltcStats = new CryptoStats("LTC",
                new BigDecimal("0.1579"),
                new PriceData(1641009600000L, new BigDecimal("95.00")),
                new PriceData(1641096000000L, new BigDecimal("110.00")),
                new PriceData(1641009600000L, new BigDecimal("100.00")),
                new PriceData(1641182400000L, new BigDecimal("110.00")),
                from, to);

        // Mock paginated result for page 1, size 2 (should return 1 item - the 3rd item)
        // Using any() for dates since they will be resolved by DateRangeResolver
        when(dataProvider.getPaginatedStats(eq(1), eq(2), eq("normalizedRange_desc"), any(), any(), eq(1)))
                .thenReturn(new PaginatedResult<>(List.of(ltcStats), 1, 2, 3, 2));

        var result = service.getRecommendations(1, 2, "normalizedRange_desc", null, null, 1);

        assertNotNull(result);
        List<CryptoStats> items = result.items();
        assertEquals(1, items.size()); // Page 1 with size 2 should have 1 item
        assertEquals(3, result.totalElements());
        assertEquals(1, result.page());
        assertEquals(2, result.totalPages());
    }

    @Test
    void testGetStats() {
        when(dataProvider.getCryptoPricesByTimeframe(eq("BTC"), any(), any()))
                .thenReturn(new CryptoPrice("BTC", createMockPrices()));

        LocalDate from = LocalDate.of(2022, 1, 1);
        LocalDate to = LocalDate.of(2022, 1, 31);

        CryptoStats stats = service.getStats("BTC", from, to, null);

        assertNotNull(stats);
        assertEquals("BTC", stats.symbol());
        assertNotNull(stats.oldest());
        assertNotNull(stats.newest());
        assertNotNull(stats.min());
        assertNotNull(stats.max());
        assertTrue(stats.normalizedRange().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void testGetStatsCryptoNotFound() {
        when(dataProvider.getCryptoPricesByTimeframe(eq("INVALID"), any(), any()))
                .thenThrow(new CryptoNotFoundException("Crypto not found"));

        assertThrows(CryptoNotFoundException.class,
                () -> service.getStats("INVALID", null, null, 1));
    }

    @Test
    void testGetTopCrypto() {
        List<String> cryptos = List.of("BTC", "ETH");
        when(dataProvider.getAllCryptos()).thenReturn(cryptos);

        // BTC prices with higher volatility
        List<PriceData> btcPrices = List.of(
                new PriceData(1641009600000L, new BigDecimal("40000")),
                new PriceData(1641096000000L, new BigDecimal("50000"))
        );

        // ETH prices with lower volatility
        List<PriceData> ethPrices = List.of(
                new PriceData(1641009600000L, new BigDecimal("3000")),
                new PriceData(1641096000000L, new BigDecimal("3100"))
        );

        when(dataProvider.getCryptoPricesByTimeframe(eq("BTC"), any(), any()))
                .thenReturn(new CryptoPrice("BTC", btcPrices));
        when(dataProvider.getCryptoPricesByTimeframe(eq("ETH"), any(), any()))
                .thenReturn(new CryptoPrice("ETH", ethPrices));

        CryptoStats topCrypto = service.getTopCrypto(null, null, 1);

        assertNotNull(topCrypto);
        assertEquals("BTC", topCrypto.symbol()); // BTC has higher normalized range
    }

    @Test
    void testGetRecommendationsSorted() {
        LocalDate from = LocalDate.of(2021, 12, 1);
        LocalDate to = LocalDate.of(2022, 1, 31);

        // ETH has higher normalized range than BTC
        CryptoStats ethStats = new CryptoStats("ETH",
                new BigDecimal("0.5000"),
                new PriceData(1641009600000L, new BigDecimal("2000.00")),
                new PriceData(1641096000000L, new BigDecimal("3000.00")),
                new PriceData(1641009600000L, new BigDecimal("2000.00")),
                new PriceData(1641182400000L, new BigDecimal("3000.00")),
                from, to);

        CryptoStats btcStats = new CryptoStats("BTC",
                new BigDecimal("0.0250"),
                new PriceData(1641009600000L, new BigDecimal("40000.00")),
                new PriceData(1641096000000L, new BigDecimal("41000.00")),
                new PriceData(1641009600000L, new BigDecimal("40000.00")),
                new PriceData(1641182400000L, new BigDecimal("41000.00")),
                from, to);

        // Mock the paginated stats to return sorted results (ETH first, then BTC)
        // Using any() for dates since they will be resolved by DateRangeResolver
        when(dataProvider.getPaginatedStats(eq(0), eq(50), eq("normalizedRange_desc"), any(), any(), eq(1)))
                .thenReturn(new PaginatedResult<>(List.of(ethStats, btcStats), 0, 50, 2, 1));

        var result = service.getRecommendations(0, 50, "normalizedRange_desc", null, null, 1);

        assertNotNull(result);
        List<CryptoStats> items = result.items();
        assertEquals(2, items.size());
        // ETH should come first (higher normalized range)
        assertEquals("ETH", items.get(0).symbol());
        assertEquals("BTC", items.get(1).symbol());
    }
}