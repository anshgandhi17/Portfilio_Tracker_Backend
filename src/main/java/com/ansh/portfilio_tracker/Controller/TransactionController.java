package com.ansh.portfilio_tracker.Controller;

import com.ansh.portfilio_tracker.Classes.Transaction;
import com.ansh.portfilio_tracker.Service.TransactionService;
import com.ansh.portfilio_tracker.Service.TransactionServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionServiceImpl transactionServiceImpl;

    @PostMapping("/portfolio/{portfolioId}")
    public ResponseEntity<Transaction> executeTransaction(
            @PathVariable UUID portfolioId,
            @Valid @RequestBody Transaction transaction) {
        transaction.setPortfolioId(portfolioId);
        Transaction executedTransaction = transactionService.addTransaction(portfolioId, transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(executedTransaction);
    }

    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<Collection<Transaction>> getPortfolioTransactions(@PathVariable UUID portfolioId) {
        Collection<Transaction> transactions = transactionServiceImpl.getPortfolioTransactions(portfolioId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/portfolio/{portfolioId}/symbol/{symbol}")
    public ResponseEntity<Collection<Transaction>> getTransactionsBySymbol(
            @PathVariable UUID portfolioId,
            @PathVariable String symbol) {
        Collection<Transaction> transactions = transactionServiceImpl.getTransactionsBySymbol(portfolioId, symbol);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable UUID transactionId) {
        Transaction transaction = transactionServiceImpl.getTransaction(transactionId);
        if (transaction != null) {
            return ResponseEntity.ok(transaction);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
