package com.ansh.portfilio_tracker.Classes;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CreatePortfolioRequest {
    @NotBlank
    private String name;
    // USD/CAD/INR
    private String baseCurrency = "USD";
}