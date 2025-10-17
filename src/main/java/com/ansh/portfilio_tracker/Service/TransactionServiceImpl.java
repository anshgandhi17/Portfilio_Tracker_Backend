package com.ansh.portfilio_tracker.Service;

import com.ansh.portfilio_tracker.Classes.Holding;
import com.ansh.portfilio_tracker.Classes.Portfolio;
import com.ansh.portfilio_tracker.Classes.Transaction;
import com.ansh.portfilio_tracker.Repo.HoldingRepository;
import com.ansh.portfilio_tracker.Repo.PortfolioRepo;
import com.ansh.portfilio_tracker.Repo.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final HoldingRepository holdingRepository;
    private final PortfolioRepo portfolioRepo;

    @Override
    public Transaction addTransaction(UUID portfolioId, Transaction transactionRequest) {
        transactionRequest.setPortfolioId(portfolioId);
        return executeTransaction(transactionRequest);
    }

    public Transaction executeTransaction(Transaction transaction) {
        transaction.setId(UUID.randomUUID());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTxnCurrency(transaction.getTxnCurrency() != null ? transaction.getTxnCurrency() : "USD");

        String type = transaction.getType().toUpperCase();

        if ("BUY".equals(type)) {
            executeBuy(transaction);
        } else if ("SELL".equals(type)) {
            executeSell(transaction);
        } else {
            throw new IllegalArgumentException("Invalid transaction type: " + transaction.getType() + ". Must be BUY or SELL");
        }

        transactionRepository.save(transaction);
        log.info("Executed {} transaction for {} shares of {} at ${}",
                type, transaction.getQuantity(), transaction.getInstrumentSymbol(), transaction.getPricePerUnit());

        return transaction;
    }

    private void executeBuy(Transaction transaction) {
        UUID portfolioId = transaction.getPortfolioId();
        String symbol = transaction.getInstrumentSymbol().toUpperCase();

        Holding holding = holdingRepository.findByPortfolioAndSymbol(portfolioId, symbol);

        if (holding == null) {
            // Create new holding
            holding = Holding.builder()
                    .portfolioId(portfolioId)
                    .symbol(symbol)
                    .name(symbol)
                    .quantity(transaction.getQuantity())
                    .avgPriceInBaseCurrency(transaction.getPricePerUnit())
                    .build();
            log.info("Created new holding for {}", symbol);
        } else {
            // Update existing holding with weighted average cost
            BigDecimal currentValue = holding.getQuantity().multiply(holding.getAvgPriceInBaseCurrency());
            BigDecimal newValue = transaction.getQuantity().multiply(transaction.getPricePerUnit());
            BigDecimal totalQuantity = holding.getQuantity().add(transaction.getQuantity());
            BigDecimal newAvgPrice = currentValue.add(newValue).divide(totalQuantity, 2, RoundingMode.HALF_UP);

            holding.setQuantity(totalQuantity);
            holding.setAvgPriceInBaseCurrency(newAvgPrice);
            log.info("Updated holding for {}: new quantity={}, new avg price={}", symbol, totalQuantity, newAvgPrice);
        }

        holdingRepository.save(holding);
        holdingRepository.refreshMarketPrice(portfolioId, symbol);
    }

    private void executeSell(Transaction transaction) {
        UUID portfolioId = transaction.getPortfolioId();
        String symbol = transaction.getInstrumentSymbol().toUpperCase();

        Holding holding = holdingRepository.findByPortfolioAndSymbol(portfolioId, symbol);

        if (holding == null) {
            throw new IllegalStateException("Cannot sell " + symbol + ": No holding found in portfolio");
        }

        if (holding.getQuantity().compareTo(transaction.getQuantity()) < 0) {
            throw new IllegalStateException("Cannot sell " + transaction.getQuantity() + " shares of " + symbol +
                    ": Only " + holding.getQuantity() + " shares available");
        }

        // Calculate realized profit
        BigDecimal costBasis = holding.getAvgPriceInBaseCurrency().multiply(transaction.getQuantity());
        BigDecimal saleProceeds = transaction.getPricePerUnit().multiply(transaction.getQuantity());
        BigDecimal realizedProfit = saleProceeds.subtract(costBasis);

        transaction.setRealizedProfit(realizedProfit);

        // Update portfolio's realized profit
        Portfolio portfolio = portfolioRepo.findById(portfolioId);
        if (portfolio != null) {
            BigDecimal currentRealizedProfit = portfolio.getRealizedProfitInBaseCurrency() != null
                    ? portfolio.getRealizedProfitInBaseCurrency()
                    : BigDecimal.ZERO;
            portfolio.setRealizedProfitInBaseCurrency(currentRealizedProfit.add(realizedProfit));
            portfolioRepo.save(portfolio);
            log.info("Updated portfolio realized profit: {} (added {})",
                    portfolio.getRealizedProfitInBaseCurrency(), realizedProfit);
        }

        // Update holding quantity
        BigDecimal newQuantity = holding.getQuantity().subtract(transaction.getQuantity());

        if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
            // Delete holding when quantity reaches zero
            holdingRepository.delete(portfolioId, symbol);
            log.info("Deleted holding for {} as quantity reached zero", symbol);
        } else {
            // Only save and refresh if holding still exists
            holding.setQuantity(newQuantity);
            holdingRepository.save(holding);
            holdingRepository.refreshMarketPrice(portfolioId, symbol);
        }

        log.info("Sold {} shares of {} at ${}, realized profit: {}",
                transaction.getQuantity(), symbol, transaction.getPricePerUnit(), realizedProfit);
    }

    public Collection<Transaction> getPortfolioTransactions(UUID portfolioId) {
        return transactionRepository.findByPortfolioId(portfolioId);
    }

    public Collection<Transaction> getTransactionsBySymbol(UUID portfolioId, String symbol) {
        return transactionRepository.findByPortfolioIdAndSymbol(portfolioId, symbol);
    }

    public Transaction getTransaction(UUID transactionId) {
        return transactionRepository.findById(transactionId);
    }
}
