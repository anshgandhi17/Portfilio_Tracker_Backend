package com.ansh.portfilio_tracker.Service;

import com.ansh.portfilio_tracker.Classes.Transaction;
import java.util.Collection;
import java.util.UUID;

public interface TransactionService {
    Transaction addTransaction(UUID portfolioId, Transaction transactionRequest);
    Collection<Transaction> getPortfolioTransactions(UUID portfolioId);
}