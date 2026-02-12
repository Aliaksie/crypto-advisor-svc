package dev.cryptorec.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for provider module.
 * Instantiates and manages beans for data provider components.
 */
@Configuration
public class ProviderConfig {

    /**
     * Creates a CSV data provider bean.
     *
     * @param csvDirectory path to directory containing CSV files
     * @return configured CsvDataProvider instance
     */
    @Bean
    public DataProvider dataProvider(@Value("${crypto.csv.directory:prices}") String csvDirectory) {
        return new CsvDataProvider(csvDirectory);
    }
}

