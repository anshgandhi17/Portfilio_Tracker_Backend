package com.ansh.portfilio_tracker.Repo;

import com.ansh.portfilio_tracker.Classes.UserPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for UserPortfolio join table.
 * Manages the relationship between users and portfolios.
 */
@Repository
public interface UserPortfolioRepository extends JpaRepository<UserPortfolio, Long> {

    /**
     * Find all user-portfolio relationships for a specific user.
     *
     * @param userId the user ID
     * @return list of user-portfolio relationships
     */
    List<UserPortfolio> findByUserId(UUID userId);

    /**
     * Find all user-portfolio relationships for a specific portfolio.
     *
     * @param portfolioId the portfolio ID
     * @return list of user-portfolio relationships
     */
    List<UserPortfolio> findByPortfolioId(UUID portfolioId);

    /**
     * Find a specific user-portfolio relationship.
     *
     * @param userId the user ID
     * @param portfolioId the portfolio ID
     * @return optional user-portfolio relationship
     */
    Optional<UserPortfolio> findByUserIdAndPortfolioId(UUID userId, UUID portfolioId);

    /**
     * Delete all relationships for a specific portfolio.
     *
     * @param portfolioId the portfolio ID
     */
    void deleteByPortfolioId(UUID portfolioId);
}
