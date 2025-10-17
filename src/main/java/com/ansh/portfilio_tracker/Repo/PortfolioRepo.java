package com.ansh.portfilio_tracker.Repo;

import com.ansh.portfilio_tracker.Classes.Portfolio;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class PortfolioRepo implements PortfolioRepository {

    // In-memory store: portfolioId -> Portfolio
    private final Map<UUID, Portfolio> portfolioStore = new ConcurrentHashMap<>();

    // In-memory store: userId -> Set of portfolioIds
    private final Map<UUID, Collection<UUID>> userPortfoliosStore = new ConcurrentHashMap<>();

    @Override
    public Portfolio findById(UUID portfolioId) {
        if (portfolioId == null) return null;
        return portfolioStore.get(portfolioId);
    }

    @Override
    public Collection<Portfolio> findByUserId(UUID userId) {
        if (userId == null) return Collections.emptyList();

        Collection<UUID> portfolioIds = userPortfoliosStore.get(userId);
        if (portfolioIds == null) return Collections.emptyList();

        return portfolioIds.stream()
                .map(portfolioStore::get)
                .filter(p -> p != null)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Portfolio> findAll() {
        return portfolioStore.values();
    }

    @Override
    public void save(Portfolio portfolio) {
        if (portfolio == null || portfolio.getId() == null) return;
        portfolioStore.put(portfolio.getId(), portfolio);
    }

    public void saveUserPortfolio(UUID userId, UUID portfolioId) {
        if (userId == null || portfolioId == null) return;
        userPortfoliosStore.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(portfolioId);
    }

    @Override
    public void delete(UUID portfolioId) {
        if (portfolioId == null) return;
        portfolioStore.remove(portfolioId);
        // Also remove from user associations
        userPortfoliosStore.values().forEach(set -> set.remove(portfolioId));
    }
}
