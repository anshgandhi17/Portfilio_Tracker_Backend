package com.ansh.portfilio_tracker.Classes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "holdings")
@IdClass(Holding.HoldingId.class)
public class Holding {
    @Id
    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;

    @Id
    @Column(name = "symbol", nullable = false, length = 10)
    private String symbol;

    @Column(name = "name")
    private String name;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

    @Column(name = "avg_price_in_base_currency", nullable = false, precision = 19, scale = 2)
    private BigDecimal avgPriceInBaseCurrency;

    // values provided by market feed or computed elsewhere:
    @Column(name = "market_price", precision = 19, scale = 2)
    private BigDecimal marketPrice; // in instrument currency (optional)

    @Column(name = "instrument_currency", length = 3)
    private String instrumentCurrency;

    @Column(name = "value_in_base_currency", precision = 19, scale = 2)
    private BigDecimal valueInBaseCurrency; // quantity * marketPrice (converted if needed)

    @Column(name = "unrealized_profit_in_base_currency", precision = 19, scale = 2)
    private BigDecimal unrealizedProfitInBaseCurrency;

    // Composite Primary Key Class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HoldingId implements Serializable {
        private UUID portfolioId;
        private String symbol;
    }
}


