package dev.cryptorec.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PriceDataTest {

    @Test
    void testValidPriceData() {
        PriceData priceData = new PriceData(1641009600000L, new BigDecimal("46813.21"));
        assertEquals(1641009600000L, priceData.timestamp());
        assertEquals(new BigDecimal("46813.21"), priceData.price());
    }

    @Test
    void testCurrencyConstant() {
        assertEquals("USD", PriceData.CURRENCY);
    }

    @Test
    void testNegativePriceThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new PriceData(1641009600000L, new BigDecimal("-100")));
    }

    @Test
    void testZeroPriceIsValid() {
        PriceData priceData = new PriceData(1641009600000L, BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, priceData.price());
    }

    @Test
    void testNullPriceThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new PriceData(1641009600000L, null));
    }

    @Test
    void testNegativeTimestampThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new PriceData(-1L, new BigDecimal("100")));
    }
}


