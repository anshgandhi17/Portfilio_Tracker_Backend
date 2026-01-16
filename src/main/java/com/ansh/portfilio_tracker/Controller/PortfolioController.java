package com.ansh.portfilio_tracker.Controller;

import com.ansh.portfilio_tracker.Classes.*;
import com.ansh.portfilio_tracker.Service.PortfolioService;
import com.ansh.portfilio_tracker.Service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portfolios")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class PortfolioController {

    // TODO: Remove this hardcoded user ID once authentication is implemented
    // Currently using a fixed user ID for all operations
    private static final UUID DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final PortfolioService portfolioService;
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Portfolio> createPortfolio(
            @RequestParam(required = false) UUID userId,
            @Valid @RequestBody CreatePortfolioRequest request) {
        // TODO: Replace with authenticated user ID from security context
        UUID effectiveUserId = (userId != null) ? userId : DEFAULT_USER_ID;
        Portfolio portfolio = portfolioService.createPortfolio(effectiveUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolio);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Portfolio>> getUserPortfolios(@PathVariable UUID userId) {
        List<Portfolio> portfolios = portfolioService.getUserPortfolios(userId);
        return ResponseEntity.ok(portfolios);
    }

    // TODO: Add endpoint to get current user's portfolios without userId parameter
    // @GetMapping("/my-portfolios")
    // public ResponseEntity<List<Portfolio>> getMyPortfolios() {
    //     UUID userId = getCurrentAuthenticatedUserId(); // From security context
    //     List<Portfolio> portfolios = portfolioService.getUserPortfolios(userId);
    //     return ResponseEntity.ok(portfolios);
    // }

    @GetMapping("/{portfolioId}")
    public ResponseEntity<Portfolio> getPortfolio(@PathVariable UUID portfolioId) {
        Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
        if (portfolio != null) {
            return ResponseEntity.ok(portfolio);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{portfolioId}/holdings")
    public ResponseEntity<List<Holding>> getPortfolioHoldings(@PathVariable UUID portfolioId) {
        List<Holding> holdings = portfolioService.getHoldingsByPortfolio(portfolioId);
        return ResponseEntity.ok(holdings);
    }

    @GetMapping("/{portfolioId}/summary")
    public ResponseEntity<PortfolioSummary> getPortfolioSummary(@PathVariable UUID portfolioId) {
        PortfolioSummary summary = portfolioService.getPortfolioSummary(portfolioId);
        if (summary != null) {
            return ResponseEntity.ok(summary);
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/{portfolioId}/transactions")
    public ResponseEntity<Collection<Transaction>> getPortfolioTransactions(@PathVariable UUID portfolioId) {
        Collection<Transaction> transactions = transactionService.getPortfolioTransactions(portfolioId);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/{portfolioId}/transactions")
    public ResponseEntity<Transaction> executeTransaction(
            @PathVariable UUID portfolioId,
            @Valid @RequestBody Transaction transaction) {
        transaction.setPortfolioId(portfolioId);
        Transaction executedTransaction = transactionService.addTransaction(portfolioId, transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(executedTransaction);
    }


}
