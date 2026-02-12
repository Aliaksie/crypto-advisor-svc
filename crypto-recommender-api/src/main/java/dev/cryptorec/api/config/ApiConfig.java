package dev.cryptorec.api.config;

import dev.cryptorec.api.mapper.RecommendationMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for API module.
 * Instantiates and manages beans for API components.
 */
@Configuration
public class ApiConfig {

    /**
     * Creates a recommendation mapper bean.
     *
     * @return configured RecommendationMapper instance
     */
    @Bean
    public RecommendationMapper recommendationMapper() {
        return new RecommendationMapper();
    }
}

