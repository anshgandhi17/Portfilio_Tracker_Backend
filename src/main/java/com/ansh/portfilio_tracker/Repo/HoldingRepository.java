package com.ansh.portfilio_tracker.Repo;

import com.ansh.portfilio_tracker.Classes.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Holding entity.
 * Extends JpaRepository which provides built-in CRUD operations.
 *
 * Note: Holding uses a composite primary key (portfolioId + symbol)
 * defined by @IdClass(Holding.HoldingId.class)
 */
@Repository
public interface HoldingRepository extends JpaRepository<Holding, Holding.HoldingId> {

    /**
     * Find the first holding with the given symbol (across all portfolios).
     * Useful for lookup but not recommended for updates.
     *
     * @param symbol stock symbol
     * @return optional holding
     */
    Optional<Holding> findFirstBySymbol(String symbol);

    /**
     * Find a specific holding by portfolio and symbol.
     * This is the primary way to query holdings.
     *
     * @param portfolioId portfolio ID
     * @param symbol stock symbol
     * @return optional holding
     */
    Optional<Holding> findByPortfolioIdAndSymbol(UUID portfolioId, String symbol);

    /**
     * Find all holdings for a specific portfolio.
     *
     * @param portfolioId portfolio ID
     * @return list of holdings
     */
    List<Holding> findByPortfolioId(UUID portfolioId);

    /**
     * Find all holdings for a specific symbol (across all portfolios).
     *
     * @param symbol stock symbol
     * @return list of holdings
     */
    List<Holding> findBySymbol(String symbol);

    /**
     * Delete a specific holding by portfolio and symbol.
     *
     * @param portfolioId portfolio ID
     * @param symbol stock symbol
     */
    void deleteByPortfolioIdAndSymbol(UUID portfolioId, String symbol);

    /**
     * Check if a holding exists for a portfolio and symbol.
     *
     * @param portfolioId portfolio ID
     * @param symbol stock symbol
     * @return true if exists
     */
    boolean existsByPortfolioIdAndSymbol(UUID portfolioId, String symbol);
}
