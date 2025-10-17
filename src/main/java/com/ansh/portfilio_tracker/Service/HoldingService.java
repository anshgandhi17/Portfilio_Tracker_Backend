package com.ansh.portfilio_tracker.Service;

import com.ansh.portfilio_tracker.Classes.CreateHoldingRequest;
import com.ansh.portfilio_tracker.Classes.FinnhubQuoteResponse;
import com.ansh.portfilio_tracker.Classes.Holding;
import com.ansh.portfilio_tracker.Repo.HoldingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final FinnhubClient finnhubClient;

    public Holding getHolding(String symbol) {
        Holding holding = holdingRepository.findBySymbol(symbol);

        if (holding == null) {
            // Holding doesn't exist, fetch from Finnhub and create it
            log.info("Holding not found for symbol: {}. Fetching from Finnhub...", symbol);
            Optional<FinnhubQuoteResponse> quoteOpt = finnhubClient.getQuote(symbol);

            if (quoteOpt.isPresent()) {
                FinnhubQuoteResponse quote = quoteOpt.get();
                holding = Holding.builder()
                        .symbol(symbol.toUpperCase())
                        .name(symbol.toUpperCase()) // We don't have company name from quote endpoint
                        .quantity(BigDecimal.ZERO) // Default quantity is 0
                        .avgPriceInBaseCurrency(BigDecimal.ZERO) // No cost basis
                        .marketPrice(quote.getCurrentPrice())
                        .instrumentCurrency("USD")
                        .valueInBaseCurrency(BigDecimal.ZERO) // 0 quantity means 0 value
                        .unrealizedProfitInBaseCurrency(BigDecimal.ZERO)
                        .build();

                holdingRepository.save(holding);
                log.info("Created new holding for {} with current price ${}", symbol, quote.getCurrentPrice());
                return holding;
            } else {
                log.warn("Could not fetch quote from Finnhub for symbol: {}", symbol);
                return null;
            }
        }

        // Holding exists, refresh the market price
        holdingRepository.refreshMarketPrice(symbol);
        return holdingRepository.findBySymbol(symbol);
    }

    public Collection<Holding> getAllHoldings() {
        return holdingRepository.findAll();
    }

    public Holding createHolding(CreateHoldingRequest request) {
        Holding holding = Holding.builder()
                .portfolioId(request.getPortfolioId())
                .symbol(request.getSymbol().toUpperCase())
                .name(request.getName())
                .quantity(request.getQuantity())
                .avgPriceInBaseCurrency(request.getAvgPriceInBaseCurrency())
                .build();

        holdingRepository.save(holding);
        // Fetch live price immediately after creation
        if (request.getPortfolioId() != null) {
            holdingRepository.refreshMarketPrice(request.getPortfolioId(), holding.getSymbol());
            return holdingRepository.findByPortfolioAndSymbol(request.getPortfolioId(), holding.getSymbol());
        } else {
            holdingRepository.refreshMarketPrice(holding.getSymbol());
            return holdingRepository.findBySymbol(holding.getSymbol());
        }
    }
}
