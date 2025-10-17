package com.ansh.portfilio_tracker.Repo;

import com.ansh.portfilio_tracker.Classes.Transaction;

import java.util.Collection;
import java.util.UUID;

public interface TransactionRepository {
    Transaction findById(UUID transactionId);
    Collection<Transaction> findByPortfolioId(UUID portfolioId);
    Collection<Transaction> findByPortfolioIdAndSymbol(UUID portfolioId, String symbol);
    Collection<Transaction> findAll();
    void save(Transaction transaction);
}
