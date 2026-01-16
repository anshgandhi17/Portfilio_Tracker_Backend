package com.ansh.portfilio_tracker.Service;

import com.ansh.portfilio_tracker.Classes.CreateHoldingRequest;
import com.ansh.portfilio_tracker.Classes.Holding;
import com.ansh.portfilio_tracker.Repo.HoldingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final FinnhubClient finnhubClient;

    /**
     * Get holding by symbol (first match across all portfolios).
     *
     * @param symbol stock symbol
     * @return holding or null if not found
     */
    public Holding getHolding(String symbol) {
        Optional<Holding> holdingOpt = holdingRepository.findFirstBySymbol(symbol);

        if (holdingOpt.isEmpty()) {
            log.warn("Holding not found for symbol: {}", symbol);
            return null;
        }

        Holding holding = holdingOpt.get();
        // Refresh market price
        refreshMarketPrice(holding.getPortfolioId(), symbol);

        return holdingRepository.findByPortfolioIdAndSymbol(holding.getPortfolioId(), symbol).orElse(null);
    }

    /**
     * Get all holdings.
     *
     * @return list of all holdings
     */
    public List<Holding> getAllHoldings() {
        return holdingRepository.findAll();
    }

    /**
     * Create a new holding.
     *
     * @param request holding creation request
     * @return created holding
     */
    public Holding createHolding(CreateHoldingRequest request) {
        Holding holding = Holding.builder()
                .portfolioId(request.getPortfolioId())
                .symbol(request.getSymbol().toUpperCase())
                .name(request.getName())
                .quantity(request.getQuantity())
                .avgPriceInBaseCurrency(request.getAvgPriceInBaseCurrency())
                .build();

        holdingRepository.save(holding);
        log.info("Created holding for {} in portfolio {}", holding.getSymbol(), holding.getPortfolioId());

        // Fetch live price immediately after creation
        refreshMarketPrice(request.getPortfolioId(), holding.getSymbol());

        return holdingRepository.findByPortfolioIdAndSymbol(request.getPortfolioId(), holding.getSymbol()).orElse(holding);
    }

    /**
     * Refresh market price for a holding.
     * Fetches current price from Finnhub and updates calculated fields.
     *
     * @param portfolioId portfolio ID
     * @param symbol stock symbol
     */
    public void refreshMarketPrice(UUID portfolioId, String symbol) {
        Optional<Holding> holdingOpt = holdingRepository.findByPortfolioIdAndSymbol(portfolioId, symbol);

        if (holdingOpt.isEmpty()) {
            log.warn("Cannot refresh market price: holding not found for portfolioId {} and symbol {}", portfolioId, symbol);
            return;
        }

        Holding holding = holdingOpt.get();
        refreshMarketPriceInternal(holding, symbol);
    }

    /**
     * Refresh market price for all holdings with a given symbol.
     *
     * @param symbol stock symbol
     */
    public void refreshMarketPriceForSymbol(String symbol) {
        List<Holding> holdings = holdingRepository.findBySymbol(symbol);

        if (holdings.isEmpty()) {
            log.warn("No holdings found for symbol: {}", symbol);
            return;
        }

        holdings.forEach(holding -> refreshMarketPriceInternal(holding, symbol));
    }

    /**
     * Internal method to refresh market price and update calculated fields.
     *
     * @param holding the holding to update
     * @param symbol stock symbol
     */
    private void refreshMarketPriceInternal(Holding holding, String symbol) {
        finnhubClient.getCurrentPrice(symbol).ifPresentOrElse(
                price -> {
                    holding.setMarketPrice(price);
                    holding.setInstrumentCurrency("USD"); // Finnhub typically returns USD prices

                    // Calculate value in base currency (assuming base currency is USD for now)
                    if (holding.getQuantity() != null) {
                        BigDecimal valueInBaseCurrency = price.multiply(holding.getQuantity());
                        holding.setValueInBaseCurrency(valueInBaseCurrency);

                        // Calculate unrealized profit
                        if (holding.getAvgPriceInBaseCurrency() != null) {
                            BigDecimal costBasis = holding.getAvgPriceInBaseCurrency().multiply(holding.getQuantity());
                            BigDecimal unrealizedProfit = valueInBaseCurrency.subtract(costBasis);
                            holding.setUnrealizedProfitInBaseCurrency(unrealizedProfit);
                        }
                    }

                    holdingRepository.save(holding);
                    log.info("Updated market price for {}: ${}", symbol, price);
                },
                () -> log.warn("Failed to fetch market price for symbol: {}", symbol)
        );
    }
}
