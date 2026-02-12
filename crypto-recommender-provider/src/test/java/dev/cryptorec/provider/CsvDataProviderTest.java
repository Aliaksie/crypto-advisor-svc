package dev.cryptorec.provider;

import dev.cryptorec.model.CryptoPrice;
import dev.cryptorec.model.exception.CryptoNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
class CsvDataProviderTest {

    private CsvDataProvider provider;

    @BeforeEach
    void setup() {
        // Find the prices directory by looking up from current working directory
        Path pricesDir = Paths.get("").toAbsolutePath();

        // If we're in a subdirectory (like crypto-recommender-provider), go up to find prices
        while (!Files.exists(pricesDir.resolve("prices")) && pricesDir.getParent() != null) {
            pricesDir = pricesDir.getParent();
        }

        String pricesDirPath = pricesDir.resolve("prices").toString();
        provider = new CsvDataProvider(pricesDirPath);
    }

    @Test
    void testGetAllCryptos() {
        List<String> cryptos = provider.getAllCryptos();

        assertNotNull(cryptos);
        assertFalse(cryptos.isEmpty());
        assertTrue(cryptos.contains("BTC"));
        assertTrue(cryptos.contains("ETH"));
    }

    @Test
    void testGetCryptoPrices() {
        CryptoPrice btcPrices = provider.getCryptoPrices("BTC");

        assertNotNull(btcPrices);
        assertEquals("BTC", btcPrices.symbol());
        assertFalse(btcPrices.prices().isEmpty());

        // Verify prices are sorted by timestamp
        for (int i = 0; i < btcPrices.prices().size() - 1; i++) {
            assertTrue(btcPrices.prices().get(i).timestamp() <=
                    btcPrices.prices().get(i + 1).timestamp());
        }
    }

    @Test
    void testGetCryptosPricesCaseInsensitive() {
        CryptoPrice btcUpper = provider.getCryptoPrices("BTC");
        CryptoPrice btcLower = provider.getCryptoPrices("btc");

        assertEquals(btcUpper.symbol(), btcLower.symbol());
        assertEquals(btcUpper.prices().size(), btcLower.prices().size());
    }

    @Test
    void testGetCryptoPricesNotFound() {
        assertThrows(CryptoNotFoundException.class,
                () -> provider.getCryptoPrices("NONEXISTENT"));
    }

    @Test
    void testGetCryptoPricesByTimeframe() {
        LocalDate from = LocalDate.of(2022, 1, 1);
        LocalDate to = LocalDate.of(2022, 1, 31);

        CryptoPrice filteredPrices = provider.getCryptoPricesByTimeframe("BTC", from, to);

        assertNotNull(filteredPrices);
        assertEquals("BTC", filteredPrices.symbol());
        assertFalse(filteredPrices.prices().isEmpty());
    }

    @Test
    void testGetCryptoPricesByTimeframeEmpty() {
        LocalDate from = LocalDate.of(2030, 1, 1);
        LocalDate to = LocalDate.of(2030, 1, 31);

        CryptoPrice filteredPrices = provider.getCryptoPricesByTimeframe("BTC", from, to);

        assertNotNull(filteredPrices);
        assertTrue(filteredPrices.prices().isEmpty());
    }
}

