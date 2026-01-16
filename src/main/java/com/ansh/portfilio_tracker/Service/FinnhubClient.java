package com.ansh.portfilio_tracker.Service;

import com.ansh.portfilio_tracker.Classes.FinnhubQuoteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinnhubClient {

    @Value("${finnhub.api.key}")
    private String apiKey;

    @Value("${finnhub.api.base-url:https://finnhub.io/api/v1}")
    private String baseUrl;

    @Value("${finnhub.use-realtime-cache:true}")
    private boolean useRealtimeCache;

    private final RestTemplate restTemplate;
    private final RealTimeStockService realTimeStockService;

    /**
     * Fetches the current market price for a given stock symbol.
     * First checks real-time cache if enabled, then falls back to REST API.
     *
     * @param symbol Stock symbol (e.g., "AAPL", "TSLA")
     * @return Optional containing the current price, or empty if the request fails
     */
    public Optional<FinnhubQuoteResponse> getQuote(String symbol) {
        // Try real-time cache first for faster response
        if (useRealtimeCache) {
            RealTimeStockService.StockPrice cachedPrice = realTimeStockService.getLatestPrice(symbol);
            if (cachedPrice != null) {
                log.debug("Using cached real-time price for {}: ${}", symbol, cachedPrice.getPrice());
                // Convert to FinnhubQuoteResponse format
                FinnhubQuoteResponse response = FinnhubQuoteResponse.builder()
                        .currentPrice(cachedPrice.getPrice())
                        .timestamp(cachedPrice.getTimestamp())
                        .build();
                return Optional.of(response);
            }
        }

        // Fallback to REST API
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/quote")
                    .queryParam("symbol", symbol.toUpperCase())
                    .queryParam("token", apiKey)
                    .toUriString();

            log.info("Fetching quote from REST API for symbol: {}", symbol);
            FinnhubQuoteResponse response = restTemplate.getForObject(url, FinnhubQuoteResponse.class);

            if (response != null && response.getCurrentPrice() != null) {
                log.info("Successfully fetched quote for {}: ${}", symbol, response.getCurrentPrice());
                // Subscribe to real-time updates for future requests
                if (useRealtimeCache) {
                    realTimeStockService.subscribe(symbol);
                }
                return Optional.of(response);
            } else {
                log.warn("No quote data available for symbol: {}", symbol);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error fetching quote for symbol: {}", symbol, e);
            return Optional.empty();
        }
    }

    /**
     * Gets just the current price for a symbol.
     *
     * @param symbol Stock symbol
     * @return Optional containing the current price
     */
    public Optional<BigDecimal> getCurrentPrice(String symbol) {
        return getQuote(symbol).map(FinnhubQuoteResponse::getCurrentPrice);
    }
}
