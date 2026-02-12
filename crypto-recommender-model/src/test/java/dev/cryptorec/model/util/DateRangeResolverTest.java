package dev.cryptorec.model.util;

import dev.cryptorec.model.exception.InvalidTimeframeException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DateRangeResolverTest {

    @Test
    void testResolveTimeframeWithExplicitDates() {
        LocalDate from = LocalDate.of(2022, 1, 1);
        LocalDate to = LocalDate.of(2022, 1, 31);

        LocalDate[] result = DateRangeResolver.resolveTimeframe(from, to, null);

        assertEquals(from, result[0]);
        assertEquals(to, result[1]);
    }

    @Test
    void testResolveTimeframeWithPeriodMonths() {
        LocalDate[] result = DateRangeResolver.resolveTimeframe(null, null, 1);

        LocalDate today = LocalDate.now();
        LocalDate expectedFrom = today.minusMonths(1);

        assertEquals(expectedFrom, result[0]);
        assertEquals(today, result[1]);
    }

    @Test
    void testResolveTimeframeDefaultPeriod() {
        LocalDate[] result = DateRangeResolver.resolveTimeframe(null, null, null);

        LocalDate today = LocalDate.now();
        LocalDate expectedFrom = today.minusMonths(1);

        assertEquals(expectedFrom, result[0]);
        assertEquals(today, result[1]);
    }

    @Test
    void testResolveTimeframeAmbiguousParameters() {
        LocalDate from = LocalDate.of(2022, 1, 1);

        assertThrows(InvalidTimeframeException.class,
                () -> DateRangeResolver.resolveTimeframe(from, null, 6));
    }

    @Test
    void testResolveTimeframeFromAfterTo() {
        LocalDate from = LocalDate.of(2022, 2, 1);
        LocalDate to = LocalDate.of(2022, 1, 1);

        assertThrows(InvalidTimeframeException.class,
                () -> DateRangeResolver.resolveTimeframe(from, to, null));
    }

    @Test
    void testResolveTimeframeInvalidPeriodMonths() {
        assertThrows(InvalidTimeframeException.class,
                () -> DateRangeResolver.resolveTimeframe(null, null, 0));

        assertThrows(InvalidTimeframeException.class,
                () -> DateRangeResolver.resolveTimeframe(null, null, 61));
    }

    @Test
    void testResolveTimeframeOnlyToDate() {
        LocalDate to = LocalDate.of(2022, 1, 31);

        // When only toDate is provided without fromDate and without periodMonths, it should throw an exception
        assertThrows(InvalidTimeframeException.class,
                () -> DateRangeResolver.resolveTimeframe(null, to, null));
    }

    @Test
    void testResolveTimeframeOnlyFromDate() {
        LocalDate from = LocalDate.of(2022, 1, 1);

        // When only fromDate is provided, toDate defaults to LocalDate.now()
        LocalDate[] result = DateRangeResolver.resolveTimeframe(from, null, null);

        assertEquals(from, result[0]);
        assertEquals(LocalDate.now(), result[1]);
    }
}

