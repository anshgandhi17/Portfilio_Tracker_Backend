package com.ansh.portfilio_tracker.Repo;

import com.ansh.portfilio_tracker.Classes.Portfolio;

import java.util.Collection;
import java.util.UUID;

public interface PortfolioRepository {
    Portfolio findById(UUID portfolioId);
    Collection<Portfolio> findByUserId(UUID userId);
    Collection<Portfolio> findAll();
    void save(Portfolio portfolio);
    void delete(UUID portfolioId);
}
