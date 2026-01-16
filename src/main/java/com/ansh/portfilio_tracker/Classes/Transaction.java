package com.ansh.portfilio_tracker.Classes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId; // Set from path parameter, not required in request body

    @NotBlank(message = "Symbol is required")
    @Column(name = "instrument_symbol", nullable = false, length = 10)
    private String instrumentSymbol;

    @NotBlank(message = "Transaction type is required")
    @Column(name = "type", nullable = false, length = 10)
    private String type; // BUY or SELL

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Column(name = "quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

    @NotNull(message = "Price per unit is required")
    @Positive(message = "Price per unit must be positive")
    @Column(name = "price_per_unit", nullable = false, precision = 19, scale = 2)
    private BigDecimal pricePerUnit;

    @Column(name = "txn_currency", length = 3)
    private String txnCurrency;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "realized_profit", precision = 19, scale = 2)
    private BigDecimal realizedProfit; // Only for SELL transactions
}
