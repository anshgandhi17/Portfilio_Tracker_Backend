package com.ansh.portfilio_tracker.Repo;

import com.ansh.portfilio_tracker.Classes.Holding;
import com.ansh.portfilio_tracker.Service.FinnhubClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class HoldingRepo implements HoldingRepository {

    // thread-safe in-memory store: portfolioId:symbol -> Holding
    private final Map<String, Holding> store = new ConcurrentHashMap<>();

    private final FinnhubClient finnhubClient;

    private String buildKey(UUID portfolioId, String symbol) {
        return (portfolioId != null ? portfolioId.toString() : "null") + ":" + symbol.toUpperCase();
    }

    @Override
    public Holding findBySymbol(String symbol) {
        if (symbol == null) return null;
        // Return first holding with this symbol (for backwards compatibility)
        return store.values().stream()
                .filter(h -> h.getSymbol().equalsIgnoreCase(symbol))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Holding findByPortfolioAndSymbol(UUID portfolioId, String symbol) {
        if (symbol == null) return null;
        return store.get(buildKey(portfolioId, symbol));
    }

    @Override
    public Collection<Holding> findAll() {
        return store.values();
    }

    @Override
    public Collection<Holding> findByPortfolioId(UUID portfolioId) {
        if (portfolioId == null) return Collections.emptyList();
        return store.values().stream()
                .filter(h -> portfolioId.equals(h.getPortfolioId()))
                .collect(Collectors.toList());
    }

    @Override
    public void save(Holding holding) {
        if (holding == null || holding.getSymbol() == null) return;
        store.put(buildKey(holding.getPortfolioId(), holding.getSymbol()), holding);
    }

    @Override
    public void delete(UUID portfolioId, String symbol) {
        if (portfolioId == null || symbol == null) return;
        String key = buildKey(portfolioId, symbol);
        Holding removed = store.remove(key);
        if (removed != null) {
            log.info("Deleted holding for portfolioId {} and symbol {}", portfolioId, symbol);
        }
    }

    @Override
    public void refreshMarketPrice(String symbol) {
        if (symbol == null) return;

        Holding holding = findBySymbol(symbol);
        if (holding == null) {
            log.warn("Cannot refresh market price: holding not found for symbol {}", symbol);
            return;
        }

        refreshMarketPriceInternal(holding, symbol);
    }

    @Override
    public void refreshMarketPrice(UUID portfolioId, String symbol) {
        if (symbol == null) return;

        Holding holding = findByPortfolioAndSymbol(portfolioId, symbol);
        if (holding == null) {
            log.warn("Cannot refresh market price: holding not found for portfolioId {} and symbol {}", portfolioId, symbol);
            return;
        }

        refreshMarketPriceInternal(holding, symbol);
    }

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

                log.info("Updated market price for {}: ${}", symbol, price);
            },
            () -> log.warn("Failed to fetch market price for symbol: {}", symbol)
        );
    }
}
