package com.ansh.portfilio_tracker.Classes;

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
public class Transaction {
    private UUID id;

    private UUID portfolioId; // Set from path parameter, not required in request body

    @NotBlank(message = "Symbol is required")
    private String instrumentSymbol;

    @NotBlank(message = "Transaction type is required")
    private String type; // BUY or SELL

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "Price per unit is required")
    @Positive(message = "Price per unit must be positive")
    private BigDecimal pricePerUnit;

    private String txnCurrency;

    private LocalDateTime transactionDate;

    private BigDecimal realizedProfit; // Only for SELL transactions
}
