package dev.cryptorec.model.util;

import dev.cryptorec.model.PriceData;
import dev.cryptorec.model.CryptoStats;
import dev.cryptorec.model.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StatsCalculatorTest {

    @Test
    void testCalculateNormalizedRange() {
        BigDecimal min = new BigDecimal("100");
        BigDecimal max = new BigDecimal("150");
        BigDecimal result = StatsCalculator.calculateNormalizedRange(min, max);

        // (150 - 100) / 100 = 0.5 with USD scale (2 decimal places)
        assertEquals(new BigDecimal("0.50"), result);
    }

    @Test
    void testCalculateNormalizedRangeZeroMin() {
        BigDecimal min = BigDecimal.ZERO;
        BigDecimal max = new BigDecimal("150");

        assertThrows(ValidationException.class,
                () -> StatsCalculator.calculateNormalizedRange(min, max));
    }

    @Test
    void testFindMin() {
        List<PriceData> prices = List.of(
                new PriceData(1000, new BigDecimal("100")),
                new PriceData(2000, new BigDecimal("50")),
                new PriceData(3000, new BigDecimal("75"))
        );

        PriceData min = StatsCalculator.findMin(prices);
        assertEquals(new BigDecimal("50"), min.price());
    }

    @Test
    void testFindMax() {
        List<PriceData> prices = List.of(
                new PriceData(1000, new BigDecimal("100")),
                new PriceData(2000, new BigDecimal("200")),
                new PriceData(3000, new BigDecimal("75"))
        );

        PriceData max = StatsCalculator.findMax(prices);
        assertEquals(new BigDecimal("200"), max.price());
    }

    @Test
    void testCalculateStats() {
        List<PriceData> prices = List.of(
                new PriceData(1000, new BigDecimal("46000")),
                new PriceData(2000, new BigDecimal("47000")),
                new PriceData(3000, new BigDecimal("45000"))
        );

        LocalDate fromDate = LocalDate.of(2022, 1, 1);
        LocalDate toDate = LocalDate.of(2022, 1, 31);

        CryptoStats stats = StatsCalculator.calculateStats("BTC", prices, fromDate, toDate);

        assertEquals("BTC", stats.symbol());
        assertEquals(new BigDecimal("45000"), stats.min().price());
        assertEquals(new BigDecimal("47000"), stats.max().price());
        assertEquals(prices.get(0), stats.oldest());
        assertEquals(prices.get(2), stats.newest());
        assertTrue(stats.normalizedRange().signum() >= 0);
    }

    @Test
    void testCalculateStatsEmptyPrices() {
        LocalDate fromDate = LocalDate.of(2022, 1, 1);
        LocalDate toDate = LocalDate.of(2022, 1, 31);

        assertThrows(ValidationException.class,
                () -> StatsCalculator.calculateStats("BTC", List.of(), fromDate, toDate));
    }

    @Test
    void testEpochMillisToLocalDate() {
        long epochMillis = 1641009600000L; // 2022-01-01 00:00:00 UTC
        LocalDate date = StatsCalculator.epochMillisToLocalDate(epochMillis);

        assertEquals(LocalDate.of(2022, 1, 1), date);
    }
}

