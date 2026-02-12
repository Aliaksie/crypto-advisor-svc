package dev.cryptorec;


import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;

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

    private HttpEntity<Void> request() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token test-user");
        return new HttpEntity<>(headers);
    }

    // todo: add integration tests for various scenarios (pagination, sorting, date filters, error handling)

}
