package com.ansh.portfilio_tracker.Repo;

import com.ansh.portfilio_tracker.Classes.Transaction;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class TransactionRepo implements TransactionRepository {

    // In-memory store: transactionId -> Transaction
    private final Map<UUID, Transaction> transactionStore = new ConcurrentHashMap<>();

    @Override
    public Transaction findById(UUID transactionId) {
        if (transactionId == null) return null;
        return transactionStore.get(transactionId);
    }

    @Override
    public Collection<Transaction> findByPortfolioId(UUID portfolioId) {
        if (portfolioId == null) return Collections.emptyList();
        return transactionStore.values().stream()
                .filter(t -> portfolioId.equals(t.getPortfolioId()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Transaction> findByPortfolioIdAndSymbol(UUID portfolioId, String symbol) {
        if (portfolioId == null || symbol == null) return Collections.emptyList();
        return transactionStore.values().stream()
                .filter(t -> portfolioId.equals(t.getPortfolioId())
                        && symbol.equalsIgnoreCase(t.getInstrumentSymbol()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Transaction> findAll() {
        return transactionStore.values();
    }

    @Override
    public void save(Transaction transaction) {
        if (transaction == null || transaction.getId() == null) return;
        transactionStore.put(transaction.getId(), transaction);
    }
}
