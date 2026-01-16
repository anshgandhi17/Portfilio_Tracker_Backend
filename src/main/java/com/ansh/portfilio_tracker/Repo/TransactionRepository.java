package com.ansh.portfilio_tracker.Repo;

import com.ansh.portfilio_tracker.Classes.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for Transaction entity.
 * Extends JpaRepository which provides built-in CRUD operations.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Find all transactions for a specific portfolio.
     * Orders by transaction date descending (newest first).
     *
     * @param portfolioId portfolio ID
     * @return list of transactions
     */
    List<Transaction> findByPortfolioIdOrderByTransactionDateDesc(UUID portfolioId);

    /**
     * Find all transactions for a specific portfolio and symbol.
     *
     * @param portfolioId portfolio ID
     * @param instrumentSymbol stock symbol
     * @return list of transactions
     */
    List<Transaction> findByPortfolioIdAndInstrumentSymbol(UUID portfolioId, String instrumentSymbol);

    /**
     * Find all transactions for a specific portfolio, symbol, and type.
     *
     * @param portfolioId portfolio ID
     * @param instrumentSymbol stock symbol
     * @param type transaction type (BUY or SELL)
     * @return list of transactions
     */
    List<Transaction> findByPortfolioIdAndInstrumentSymbolAndType(UUID portfolioId, String instrumentSymbol, String type);

    /**
     * Find all BUY or SELL transactions for a portfolio.
     *
     * @param portfolioId portfolio ID
     * @param type transaction type (BUY or SELL)
     * @return list of transactions
     */
    List<Transaction> findByPortfolioIdAndType(UUID portfolioId, String type);
}
