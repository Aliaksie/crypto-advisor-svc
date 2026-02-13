package dev.cryptorec;


import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.cryptorec.api.generated.model.RecommendationsResponse;
import com.cryptorec.api.generated.model.CryptoStats;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(
        classes = CryptoRecommenderApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@WireMockTest(httpPort = 8089)  // provider stubs
public class RecommenderIntegrationTest {
    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    private String url() {
        return "http://localhost:" + port + "/crypto/api/v1/recommendations";
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token test-user");
        return headers;
    }

    @Test
    void testResponseContentValidation() {
        ResponseEntity<RecommendationsResponse> response = restTemplate.exchange(
                url() + "?page=0&size=50&periodMonths=60",
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                RecommendationsResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Validate each crypto in response
        for (CryptoStats crypto : response.getBody().getRecommendations()) {
            assertNotNull(crypto.getName(), "Crypto should have name");
            assertNotNull(crypto.getNormalizedRange(), "Crypto should have normalized range");
            assertNotNull(crypto.getMin(), "Crypto should have min");
            assertNotNull(crypto.getMax(), "Crypto should have max");
            assertNotNull(crypto.getOldest(), "Crypto should have oldest price");
            assertNotNull(crypto.getNewest(), "Crypto should have newest price");
        }
    }

    @Test
    void testGetAllRecommendations() {
        ResponseEntity<RecommendationsResponse> response = restTemplate.exchange(
                url() + "?page=0&size=50&periodMonths=60",
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                RecommendationsResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200");
        assertNotNull(response.getBody(), "Response body should not be null");

        RecommendationsResponse body = response.getBody();
        if (response.getStatusCode() == HttpStatus.OK) {
            assertNotNull(body.getRecommendations(), "Recommendations array should not be null");
            assertEquals(0, body.getPage(), "Page should be 0");
            assertEquals(50, body.getSize(), "Size should be 50");
            assertNotNull(body.getTotalElements(), "Total elements should not be null");
            assertNotNull(body.getTotalPages(), "Total pages should not be null");
        }
    }

    @Test
    void testPaginationParameters() {
        ResponseEntity<RecommendationsResponse> response = restTemplate.exchange(
                url() + "?page=1&size=10&periodMonths=60",
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                RecommendationsResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getPage(), "Page should be 1");
        assertEquals(10, response.getBody().getSize(), "Size should be 10");
        assertTrue(response.getBody().getRecommendations().size() <= 10, "Should not exceed page size");
    }

    @Test
    void testSortingByNormalizedRange() {
        ResponseEntity<RecommendationsResponse> response = restTemplate.exchange(
                url() + "?page=0&size=50&sortBy=normalizedRange_desc&periodMonths=60",
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                RecommendationsResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getRecommendations());

        // Verify sorted by normalized range descending
        if (response.getBody().getRecommendations().size() > 1) {
            for (int i = 0; i < response.getBody().getRecommendations().size() - 1; i++) {
                CryptoStats current = response.getBody().getRecommendations().get(i);
                CryptoStats next = response.getBody().getRecommendations().get(i + 1);
                assertNotNull(current.getNormalizedRange(), "Current crypto should have normalized range");
                assertNotNull(next.getNormalizedRange(), "Next crypto should have normalized range");
                assertTrue(
                        current.getNormalizedRange().compareTo(next.getNormalizedRange()) >= 0,
                        "Should be sorted in descending order by normalized range"
                );
            }
        }
    }

    @Test
    void testFilterByDateRange() {
        LocalDate from = LocalDate.of(2023, 1, 1);
        LocalDate to = LocalDate.of(2023, 12, 31);

        ResponseEntity<RecommendationsResponse> response = restTemplate.exchange(
                url() + "?page=0&size=50&fromDate=" + from + "&toDate=" + to,
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                RecommendationsResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200");
        assertNotNull(response.getBody(), "Response should not be null");
        assertNotNull(response.getBody().getRecommendations(), "Should have recommendations");
    }

    @Test
    void testGetStatsByCrypto() {
        ResponseEntity<CryptoStats> response = restTemplate.exchange(
                url() + "/BTC?periodMonths=60",
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                CryptoStats.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals("BTC", response.getBody().getName(), "Should return BTC stats");
        assertNotNull(response.getBody().getNormalizedRange(), "Should have normalized range");
        assertNotNull(response.getBody().getMin(), "Should have min price");
        assertNotNull(response.getBody().getMax(), "Should have max price");
    }

    @Test
    void testGetStatsForETH() {
        ResponseEntity<CryptoStats> response = restTemplate.exchange(
                url() + "/ETH?periodMonths=60",
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                CryptoStats.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ETH", response.getBody().getName());
    }

    @Test
    void testGetStatsByDateRange() {
        ResponseEntity<CryptoStats> response = restTemplate.exchange(
                url() + "/BTC?fromDate=2022-01-01&toDate=2022-12-31",
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                CryptoStats.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetTopCrypto() {
        ResponseEntity<CryptoStats> response = restTemplate.exchange(
                url() + "/top?periodMonths=60",
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                CryptoStats.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertNotNull(response.getBody().getName(), "Should have crypto name");
        assertNotNull(response.getBody().getNormalizedRange(), "Should have normalized range");
    }

    @Test
    void testInvalidCrypto() {
        ResponseEntity<String> response = restTemplate.exchange(
                url() + "/INVALID?periodMonths=60",
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Should return 404 for invalid crypto");
    }

    @Test
    void testPaginationValidation() {
        ResponseEntity<RecommendationsResponse> response = restTemplate.exchange(
                url() + "?page=0&size=1&periodMonths=60",
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                RecommendationsResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getSize(), "Should respect size=1");
        assertTrue(response.getBody().getRecommendations().size() <= 1, "Should return max 1 item");
    }

    @Test
    void testPeriodMonthsParameter() {
        ResponseEntity<RecommendationsResponse> response = restTemplate.exchange(
                url() + "?page=0&size=50&periodMonths=12",
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                RecommendationsResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}


