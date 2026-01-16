package com.ansh.portfilio_tracker.Config;

import com.ansh.portfilio_tracker.Classes.Holding;
import com.ansh.portfilio_tracker.Classes.Portfolio;
import com.ansh.portfilio_tracker.Classes.UserPortfolio;
import com.ansh.portfilio_tracker.Repo.HoldingRepository;
import com.ansh.portfilio_tracker.Repo.PortfolioRepository;
import com.ansh.portfilio_tracker.Repo.UserPortfolioRepository;
import com.ansh.portfilio_tracker.Service.HoldingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PortfolioRepository portfolioRepository;
    private final UserPortfolioRepository userPortfolioRepository;
    private final HoldingRepository holdingRepository;
    private final HoldingService holdingService;

    // TODO: Remove these hardcoded UUIDs once user authentication is implemented
    // Currently using fixed UUIDs for development/testing purposes
    // Fixed UUID for portfolio ID = 1 (using a simple UUID)
    private static final UUID PORTFOLIO_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing default portfolio and holdings...");

        // Check if portfolio already exists
        if (portfolioRepository.existsById(PORTFOLIO_ID)) {
            log.info("Portfolio already exists with ID: {}. Skipping initialization.", PORTFOLIO_ID);
            return;
        }

        // Create portfolio with ID = 1
        Portfolio portfolio = Portfolio.builder()
                .id(PORTFOLIO_ID)
                .name("Tech Portfolio")
                .baseCurrency("USD")
                .realizedProfitInBaseCurrency(BigDecimal.ZERO)
                .build();

        portfolioRepository.save(portfolio);

        // Create user-portfolio relationship
        UserPortfolio userPortfolio = UserPortfolio.builder()
                .userId(USER_ID)
                .portfolioId(PORTFOLIO_ID)
                .build();
        userPortfolioRepository.save(userPortfolio);

        log.info("Created portfolio with ID: {}", PORTFOLIO_ID);

        // Add 5 tech stocks to the portfolio
        createHolding("AAPL", "Apple Inc.", new BigDecimal("10"), new BigDecimal("150.00"));
        createHolding("MSFT", "Microsoft Corporation", new BigDecimal("15"), new BigDecimal("300.00"));
        createHolding("GOOGL", "Alphabet Inc.", new BigDecimal("5"), new BigDecimal("140.00"));
        createHolding("TSLA", "Tesla Inc.", new BigDecimal("8"), new BigDecimal("250.00"));
        createHolding("NVDA", "NVIDIA Corporation", new BigDecimal("12"), new BigDecimal("450.00"));

        log.info("Successfully initialized portfolio with 5 holdings");
    }

    private void createHolding(String symbol, String name, BigDecimal quantity, BigDecimal avgPrice) {
        Holding holding = Holding.builder()
                .portfolioId(PORTFOLIO_ID)
                .symbol(symbol)
                .name(name)
                .quantity(quantity)
                .avgPriceInBaseCurrency(avgPrice)
                .build();

        holdingRepository.save(holding);
        log.info("Added holding: {} - {} shares at ${}", symbol, quantity, avgPrice);

        // Refresh market price from Finnhub
        holdingService.refreshMarketPrice(PORTFOLIO_ID, symbol);
    }
}
