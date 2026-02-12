package dev.cryptorec.api;


import dev.cryptorec.api.mapper.RecommendationMapper;
import dev.cryptorec.service.RecommendationService;
import org.mockito.Mockito;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.cryptorec.api")
public class ControllerTestConfig {
    @Bean
    public RecommendationService recommendationService() {
        return Mockito.mock(RecommendationService.class);
    }

    @Bean
    public RecommendationMapper recommendationResponseMapper() {
        return new RecommendationMapper();
    }

}
