package dev.cryptorec.api.mapper;

import dev.cryptorec.model.PriceData;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for converting domain models to OpenAPI-generated model classes.
 * Handles conversion between internal CryptoStats and API response objects.
 */
public class RecommendationMapper {

    /**
     * Converts domain CryptoStats to API model.
     *
     * @param stats domain model
     * @return API model
     */
    public com.cryptorec.api.generated.model.CryptoStats mapToCryptoStatsDto(dev.cryptorec.model.CryptoStats stats) {
        if (stats == null) {
            return null;
        }

        com.cryptorec.api.generated.model.CryptoStats dto = new com.cryptorec.api.generated.model.CryptoStats();
        dto.setName(stats.symbol());
        dto.setNormalizedRange(stats.normalizedRange());
        dto.setMin(mapToPricePointDto(stats.min()));
        dto.setMax(mapToPricePointDto(stats.max()));
        dto.setOldest(mapToPricePointDto(stats.oldest()));
        dto.setNewest(mapToPricePointDto(stats.newest()));

        return dto;
    }

    /**
     * Converts domain PriceData to API model.
     * PriceData contains price in USD, this constant is part of PriceData.
     *
     * @param priceData domain model (price in USD)
     * @return API model with USD currency
     */
    public com.cryptorec.api.generated.model.PricePoint mapToPricePointDto(PriceData priceData) {
        if (priceData == null) {
            return null;
        }

        com.cryptorec.api.generated.model.PricePoint dto = new com.cryptorec.api.generated.model.PricePoint();
        dto.setTimestamp(priceData.timestamp());
        dto.setCurrency(PriceData.CURRENCY); // Always USD
        dto.setPrice(priceData.price().doubleValue()); // Convert BigDecimal to double for API

        return dto;
    }

    /**
     * Converts list of CryptoStats to list of models.
     *
     * @param statsList list of domain models
     * @return list of API models
     */
    public List<com.cryptorec.api.generated.model.CryptoStats> mapToCryptoStatsDtoList(List<dev.cryptorec.model.CryptoStats> statsList) {
        if (statsList == null) {
            return new ArrayList<>();
        }

        return statsList.stream()
                .map(this::mapToCryptoStatsDto)
                .toList();
    }
}
