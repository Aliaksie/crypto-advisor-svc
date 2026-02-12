package dev.cryptorec.api.controller;

import com.cryptorec.api.generated.RecommendationsApi;
import com.cryptorec.api.generated.model.CryptoStats;
import com.cryptorec.api.generated.model.RecommendationsResponse;
import dev.cryptorec.api.mapper.RecommendationMapper;
import dev.cryptorec.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller implementing the Recommendations API endpoints.
 * Implements the OpenAPI-generated RecommendationsApi interface for API-first approach.
 * Handles HTTP requests for crypto recommendations and statistics.
 */
@RestController
@Validated
public class RecommendationController implements RecommendationsApi {
    private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);

    private final RecommendationService service;
    private final RecommendationMapper mapper;

    public RecommendationController(final RecommendationService service,
                                    final RecommendationMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    /**
     * GET /recommendations - List all cryptos sorted by normalized range
     *
     * @param authorization Bearer or Token authorization header
     * @param page          zero-based page index
     * @param size          page size
     * @param sortBy        sort field and direction (e.g., normalizedRange_desc)
     * @param fromDate      start date
     * @param toDate        end date
     * @param periodMonths  months to look back
     * @return paginated list of crypto statistics
     */
    @Override
    public ResponseEntity<RecommendationsResponse> getRecommendations(String authorization, Integer page, Integer size,
                                                                      String sortBy, LocalDate fromDate, LocalDate toDate,
                                                                      Integer periodMonths) {
        log.info("GET /recommendations - page={}, size={}, sortBy={}", page, size, sortBy);

        var result = service.getRecommendations(page, size, sortBy, fromDate, toDate, periodMonths);
        return ResponseEntity.ok(new RecommendationsResponse()
                .recommendations(mapper.mapToCryptoStatsDtoList(result.items()))
                .page(page)
                .size(size)
                .totalElements(result.totalElements())
                .totalPages(result.totalPages())
        );
    }

    /**
     * GET /recommendations/{cryptoName} - Get statistics for a specific crypto
     *
     * @param cryptoName    cryptocurrency symbol (e.g., BTC)
     * @param authorization Bearer or Token authorization header
     * @param fromDate      start date
     * @param toDate        end date
     * @param periodMonths  months to look back
     * @return crypto statistics
     */
    @Override
    public ResponseEntity<CryptoStats> getRecommendationsByCrypto(String cryptoName, String authorization,
                                                                  LocalDate fromDate, LocalDate toDate,
                                                                  Integer periodMonths) {
        log.info("GET /recommendations/{} - fromDate={}, toDate={}, periodMonths={}", cryptoName, fromDate, toDate,
                periodMonths);

        dev.cryptorec.model.CryptoStats stats = service.getStats(cryptoName, fromDate, toDate, periodMonths);
        return ResponseEntity.ok(mapper.mapToCryptoStatsDto(stats));
    }

    /**
     * GET /recommendations/top - Get crypto with highest normalized range
     *
     * @param authorization Bearer or Token authorization header
     * @param fromDate      start date
     * @param toDate        end date
     * @param periodMonths  months to look back
     * @return top crypto statistics
     */
    @Override
    public ResponseEntity<CryptoStats> getRecommendationsTopCrypto(String authorization, LocalDate fromDate,
                                                                   LocalDate toDate, Integer periodMonths) {
        log.info("GET /recommendations/top - fromDate={}, toDate={}, periodMonths={}", fromDate, toDate, periodMonths);

        dev.cryptorec.model.CryptoStats topCrypto = service.getTopCrypto(fromDate, toDate, periodMonths);
        return ResponseEntity.ok(mapper.mapToCryptoStatsDto(topCrypto));
    }

}