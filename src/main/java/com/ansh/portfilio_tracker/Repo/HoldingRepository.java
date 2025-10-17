package com.ansh.portfilio_tracker.Repo;

import com.ansh.portfilio_tracker.Classes.Holding;

import java.util.Collection;
import java.util.UUID;

public interface HoldingRepository {
    Holding findBySymbol(String symbol);
    Holding findByPortfolioAndSymbol(UUID portfolioId, String symbol);
    Collection<Holding> findAll();
    Collection<Holding> findByPortfolioId(UUID portfolioId);
    void save(Holding holding);
    void delete(UUID portfolioId, String symbol);
    void refreshMarketPrice(String symbol);
    void refreshMarketPrice(UUID portfolioId, String symbol);
}
