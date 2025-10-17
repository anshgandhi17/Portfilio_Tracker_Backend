package com.ansh.portfilio_tracker.Classes;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PortfolioSummary {
    private String portfolioId;
    private String baseCurrency;
    private BigDecimal totalCostInBase;
    private BigDecimal totalValueInBase;
    private BigDecimal unrealizedProfitInBase;
    private BigDecimal realizedProfitInBase;
    private BigDecimal totalProfitInBase; // unrealized + realized
}