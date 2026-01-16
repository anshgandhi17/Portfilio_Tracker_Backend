package com.ansh.portfilio_tracker.Repo;

import com.ansh.portfilio_tracker.Classes.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for Portfolio entity.
 * Extends JpaRepository which provides built-in CRUD operations:
 * - save(Portfolio)
 * - findById(UUID)
 * - findAll()
 * - deleteById(UUID)
 * - count()
 * - existsById(UUID)
 * And many more...
 */
@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {

    /**
     * Find all portfolios belonging to a specific user.
     * Joins with user_portfolios table to get the relationship.
     *
     * @param userId the user ID
     * @return list of portfolios owned by the user
     */
    @Query("SELECT p FROM Portfolio p WHERE p.id IN " +
           "(SELECT up.portfolioId FROM UserPortfolio up WHERE up.userId = :userId)")
    List<Portfolio> findByUserId(@Param("userId") UUID userId);

    /**
     * Find portfolio by name (optional - for searching).
     *
     * @param name portfolio name
     * @return list of portfolios with matching name
     */
    List<Portfolio> findByName(String name);
}
