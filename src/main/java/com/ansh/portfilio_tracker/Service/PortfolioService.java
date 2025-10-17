package com.ansh.portfilio_tracker.Service;

import com.ansh.portfilio_tracker.Classes.CreatePortfolioRequest;
import com.ansh.portfilio_tracker.Classes.Portfolio;
import com.ansh.portfilio_tracker.Classes.Holding;
import com.ansh.portfilio_tracker.Classes.PortfolioSummary;
import com.ansh.portfilio_tracker.Repo.HoldingRepository;
import com.ansh.portfilio_tracker.Repo.PortfolioRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final PortfolioRepo portfolioRepo;
    private final HoldingRepository holdingRepository;

    public Portfolio createPortfolio(UUID userId, CreatePortfolioRequest request) {
        Portfolio portfolio = Portfolio.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .baseCurrency(request.getBaseCurrency())
                .build();

        portfolioRepo.save(portfolio);
        if (userId != null) {
            portfolioRepo.saveUserPortfolio(userId, portfolio.getId());
        }

        log.info("Created portfolio {} for user {}", portfolio.getId(), userId);
        return portfolio;
    }

    public List<Portfolio> getUserPortfolios(UUID userId) {
        if (userId == null) {
            log.warn("Cannot get portfolios for null userId");
            return List.of();
        }

        Collection<Portfolio> portfolios = portfolioRepo.findByUserId(userId);
        return portfolios.stream().collect(Collectors.toList());
    }

    public Portfolio getPortfolioById(UUID portfolioId) {
        return portfolioRepo.findById(portfolioId);
    }

    public List<Holding> getHoldingsByPortfolio(UUID portfolioId) {
        if (portfolioId == null) {
            log.warn("Cannot get holdings for null portfolioId");
            return List.of();
        }

        Collection<Holding> holdings = holdingRepository.findByPortfolioId(portfolioId);

        // Refresh market prices for all holdings
        holdings.forEach(holding -> {
            if (holding.getSymbol() != null) {
                holdingRepository.refreshMarketPrice(portfolioId, holding.getSymbol());
            }
        });

        return holdings.stream().collect(Collectors.toList());
    }

    public PortfolioSummary getPortfolioSummary(UUID portfolioId) {
        if (portfolioId == null) {
            log.warn("Cannot get summary for null portfolioId");
            return null;
        }

        Portfolio portfolio = portfolioRepo.findById(portfolioId);
        if (portfolio == null) {
            log.warn("Portfolio not found: {}", portfolioId);
            return null;
        }

        List<Holding> holdings = getHoldingsByPortfolio(portfolioId);

        BigDecimal totalCost = holdings.stream()
                .map(h -> {
                    if (h.getAvgPriceInBaseCurrency() != null && h.getQuantity() != null) {
                        return h.getAvgPriceInBaseCurrency().multiply(h.getQuantity());
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalValue = holdings.stream()
                .map(h -> h.getValueInBaseCurrency() != null ? h.getValueInBaseCurrency() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal unrealizedProfit = totalValue.subtract(totalCost);

        BigDecimal realizedProfit = portfolio.getRealizedProfitInBaseCurrency() != null
                ? portfolio.getRealizedProfitInBaseCurrency()
                : BigDecimal.ZERO;

        BigDecimal totalProfit = unrealizedProfit.add(realizedProfit);

        return PortfolioSummary.builder()
                .portfolioId(portfolioId.toString())
                .baseCurrency(portfolio.getBaseCurrency())
                .totalCostInBase(totalCost)
                .totalValueInBase(totalValue)
                .unrealizedProfitInBase(unrealizedProfit)
                .realizedProfitInBase(realizedProfit)
                .totalProfitInBase(totalProfit)
                .build();
    }
}
