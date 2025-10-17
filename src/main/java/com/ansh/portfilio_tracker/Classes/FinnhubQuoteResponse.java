package com.ansh.portfilio_tracker.Classes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Finnhub Quote API response.
 * See: https://finnhub.io/docs/api/quote
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinnhubQuoteResponse {

    @JsonProperty("c")
    private BigDecimal currentPrice;

    @JsonProperty("d")
    private BigDecimal change;

    @JsonProperty("dp")
    private BigDecimal percentChange;

    @JsonProperty("h")
    private BigDecimal highPrice;

    @JsonProperty("l")
    private BigDecimal lowPrice;

    @JsonProperty("o")
    private BigDecimal openPrice;

    @JsonProperty("pc")
    private BigDecimal previousClose;

    @JsonProperty("t")
    private Long timestamp;
}
