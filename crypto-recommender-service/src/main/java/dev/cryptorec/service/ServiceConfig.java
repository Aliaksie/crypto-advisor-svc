package dev.cryptorec.service;

import dev.cryptorec.provider.DataProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for service module.
 * Instantiates and manages beans for service components.
 */
@Configuration
public class ServiceConfig {

    /**
     * Creates a recommendation service bean.
     *
     * @param dataProvider data provider for crypto data
     * @return configured RecommendationService instance
     */
    @Bean
    public RecommendationService recommendationService(DataProvider dataProvider) {
        return new RecommendationService(dataProvider);
    }
}

