package dev.cryptorec.api.controller;

import dev.cryptorec.api.ControllerTestConfig;
import dev.cryptorec.api.error.GlobalExceptionHandler;
import dev.cryptorec.api.mapper.RecommendationMapper;
import dev.cryptorec.model.PaginatedResult;
import dev.cryptorec.model.PriceData;
import dev.cryptorec.model.exception.CryptoNotFoundException;
import dev.cryptorec.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RecommendationController.class)
@ContextConfiguration(classes = {
        ControllerTestConfig.class,
        RecommendationController.class,
        GlobalExceptionHandler.class
})
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecommendationService service;

    @Autowired
    private RecommendationMapper mapper;

    private dev.cryptorec.model.CryptoStats createMockStats(String symbol) {
        PriceData pricePoint = new PriceData(1641009600000L, new BigDecimal("46813.21"));
        return new dev.cryptorec.model.CryptoStats(
                symbol,
                new BigDecimal("0.25"),
                pricePoint,
                pricePoint,
                pricePoint,
                pricePoint,
                LocalDate.of(2022, 1, 1),
                LocalDate.of(2022, 1, 31)
        );
    }

    @Test
    void testGetRecommendations() throws Exception {
        dev.cryptorec.model.CryptoStats stats1 = createMockStats("BTC");
        dev.cryptorec.model.CryptoStats stats2 = createMockStats("ETH");

        var serviceResult = new PaginatedResult<>(List.of(stats1, stats2), 0, 50, 2, 1);

        when(service.getRecommendations(
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any()))
                .thenReturn(serviceResult);

        mockMvc.perform(get("/recommendations")
                        .header("Authorization", "Token user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", equalTo(0)))
                .andExpect(jsonPath("$.size", equalTo(50)))
                .andExpect(jsonPath("$.totalElements", equalTo(2)))
                .andExpect(jsonPath("$.totalPages", equalTo(1)))
                .andExpect(jsonPath("$.recommendations", hasSize(2)))
                .andExpect(jsonPath("$.recommendations[0].name", equalTo("BTC")))
                .andExpect(jsonPath("$.recommendations[1].name", equalTo("ETH")));
    }

    @Test
    void testGetRecommendationsWithPagination() throws Exception {
        dev.cryptorec.model.CryptoStats stats1 = createMockStats("BTC");

        var serviceResult = new PaginatedResult<>(List.of(stats1), 1, 10, 5, 1);

        when(service.getRecommendations(
                ArgumentMatchers.eq(1),
                ArgumentMatchers.eq(10),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any()))
                .thenReturn(serviceResult);

        mockMvc.perform(get("/recommendations?page=1&size=10")
                        .header("Authorization", "Token user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", equalTo(1)))
                .andExpect(jsonPath("$.size", equalTo(10)))
                .andExpect(jsonPath("$.recommendations", hasSize(1)))
                .andExpect(jsonPath("$.recommendations[0].name", equalTo("BTC")));
    }

    @Test
    void testGetRecommendationsByCrypto() throws Exception {
        dev.cryptorec.model.CryptoStats stats = createMockStats("BTC");

        when(service.getStats(
                ArgumentMatchers.eq("BTC"),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any()))
                .thenReturn(stats);


        mockMvc.perform(get("/recommendations/BTC")
                        .header("Authorization", "Token user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("BTC")));
    }

    @Test
    void testGetRecommendationsByCryptoNotFound() throws Exception {
        when(service.getStats(
                ArgumentMatchers.eq("INVALID"),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any()))
                .thenThrow(new CryptoNotFoundException("Crypto not found"));

        mockMvc.perform(get("/recommendations/INVALID")
                        .header("Authorization", "Token user-123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", equalTo(404)))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void testGetTopCrypto() throws Exception {
        dev.cryptorec.model.CryptoStats stats = createMockStats("BTC");

        when(service.getTopCrypto(
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any())).thenReturn(stats);


        mockMvc.perform(get("/recommendations/top")
                        .header("Authorization", "Token user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("BTC")));
    }

    @Test
    void testGetRecommendationsWithTimeframe() throws Exception {
        dev.cryptorec.model.CryptoStats stats = createMockStats("BTC");

        var serviceResult = new PaginatedResult<>(List.of(stats), 0, 50, 1, 1);

        when(service.getRecommendations(
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.eq(LocalDate.of(2022, 1, 1)),
                ArgumentMatchers.eq(LocalDate.of(2022, 1, 31)),
                ArgumentMatchers.isNull()))
                .thenReturn(serviceResult);

        mockMvc.perform(get("/recommendations?fromDate=2022-01-01&toDate=2022-01-31")
                        .header("Authorization", "Token user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations", hasSize(1)))
                .andExpect(jsonPath("$.recommendations[0].name", equalTo("BTC")));
    }
}