package com.ansh.portfilio_tracker.Service;

import com.ansh.portfilio_tracker.Classes.FinnhubQuoteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
public class FinnhubClient {

    @Value("${finnhub.api.key}")
    private String apiKey;

    @Value("${finnhub.api.base-url:https://finnhub.io/api/v1}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public FinnhubClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Fetches the current market price for a given stock symbol from Finnhub API.
     *
     * @param symbol Stock symbol (e.g., "AAPL", "TSLA")
     * @return Optional containing the current price, or empty if the request fails
     */
    public Optional<FinnhubQuoteResponse> getQuote(String symbol) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/quote")
                    .queryParam("symbol", symbol.toUpperCase())
                    .queryParam("token", apiKey)
                    .toUriString();

            log.info("Fetching quote for symbol: {}", symbol);
            FinnhubQuoteResponse response = restTemplate.getForObject(url, FinnhubQuoteResponse.class);

            if (response != null && response.getCurrentPrice() != null) {
                log.info("Successfully fetched quote for {}: ${}", symbol, response.getCurrentPrice());
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
