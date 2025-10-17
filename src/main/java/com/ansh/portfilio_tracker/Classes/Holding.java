package com.ansh.portfilio_tracker.Classes;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class Holding {
    private UUID portfolioId;
    private String symbol;
    private String name;
    private BigDecimal quantity;
    private BigDecimal avgPriceInBaseCurrency;
    // values provided by market feed or computed elsewhere:
    private BigDecimal marketPrice; // in instrument currency (optional)
    private String instrumentCurrency;
    private BigDecimal valueInBaseCurrency; // quantity * marketPrice (converted if needed)
    private BigDecimal unrealizedProfitInBaseCurrency;
}


