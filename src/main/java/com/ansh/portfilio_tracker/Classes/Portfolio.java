package com.ansh.portfilio_tracker.Classes;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class Portfolio {
    private UUID id;
    private String name;
    private String baseCurrency;
    private BigDecimal realizedProfitInBaseCurrency;
}
