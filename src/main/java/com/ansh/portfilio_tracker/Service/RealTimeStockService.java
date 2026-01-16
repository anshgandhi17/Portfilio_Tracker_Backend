package com.ansh.portfilio_tracker.Service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Service to manage real-time stock price updates from Finnhub WebSocket.
 * Handles subscriptions, caches latest prices, and broadcasts to listeners.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RealTimeStockService {

    private final FinnhubWebSocketClient webSocketClient;

    // Cache of latest prices for each symbol
    private final Map<String, StockPrice> latestPrices = new ConcurrentHashMap<>();

    // Set of symbols currently subscribed
    private final Set<String> subscribedSymbols = new CopyOnWriteArraySet<>();

    // Listeners for price updates
    private final Map<String, Set<StockPriceListener>> priceListeners = new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize() {
        log.info("Initializing Real-Time Stock Service");

        // Register message handler
        webSocketClient.registerMessageHandler("realTimeStockService", this::handleWebSocketMessage);

        // Connect to Finnhub WebSocket
        try {
            webSocketClient.connect();
            log.info("Connected to Finnhub WebSocket");
        } catch (Exception e) {
            log.error("Error connecting to Finnhub WebSocket", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up Real-Time Stock Service");
        try {
            webSocketClient.close();
        } catch (Exception e) {
            log.error("Error closing WebSocket connection", e);
        }
    }

    /**
     * Subscribe to real-time updates for a symbol.
     *
     * @param symbol Stock symbol
     */
    public void subscribe(String symbol) {
        String upperSymbol = symbol.toUpperCase();
        if (!subscribedSymbols.contains(upperSymbol)) {
            webSocketClient.subscribe(upperSymbol);
            subscribedSymbols.add(upperSymbol);
            log.info("Subscribed to real-time updates for: {}", upperSymbol);
        }
    }

    /**
     * Unsubscribe from real-time updates for a symbol.
     *
     * @param symbol Stock symbol
     */
    public void unsubscribe(String symbol) {
        String upperSymbol = symbol.toUpperCase();
        if (subscribedSymbols.contains(upperSymbol)) {
            webSocketClient.unsubscribe(upperSymbol);
            subscribedSymbols.remove(upperSymbol);
            latestPrices.remove(upperSymbol);
            log.info("Unsubscribed from real-time updates for: {}", upperSymbol);
        }
    }

    /**
     * Get the latest cached price for a symbol.
     *
     * @param symbol Stock symbol
     * @return Latest price or null if not available
     */
    public StockPrice getLatestPrice(String symbol) {
        return latestPrices.get(symbol.toUpperCase());
    }

    /**
     * Register a listener for price updates of a specific symbol.
     *
     * @param symbol Stock symbol
     * @param listener Listener to be notified of price updates
     */
    public void addPriceListener(String symbol, StockPriceListener listener) {
        String upperSymbol = symbol.toUpperCase();
        priceListeners.computeIfAbsent(upperSymbol, k -> new CopyOnWriteArraySet<>()).add(listener);
        // Auto-subscribe when first listener is added
        subscribe(upperSymbol);
    }

    /**
     * Remove a price listener.
     *
     * @param symbol Stock symbol
     * @param listener Listener to remove
     */
    public void removePriceListener(String symbol, StockPriceListener listener) {
        String upperSymbol = symbol.toUpperCase();
        Set<StockPriceListener> listeners = priceListeners.get(upperSymbol);
        if (listeners != null) {
            listeners.remove(listener);
            // Auto-unsubscribe when no listeners remain
            if (listeners.isEmpty()) {
                priceListeners.remove(upperSymbol);
                unsubscribe(upperSymbol);
            }
        }
    }

    /**
     * Handle incoming WebSocket messages from Finnhub.
     */
    private void handleWebSocketMessage(JsonNode message) {
        try {
            String type = message.has("type") ? message.get("type").asText() : null;

            if ("trade".equals(type) && message.has("data")) {
                JsonNode data = message.get("data");
                if (data.isArray()) {
                    for (JsonNode trade : data) {
                        processTradeData(trade);
                    }
                }
            } else if ("ping".equals(type)) {
                log.debug("Received ping from Finnhub");
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
        }
    }

    /**
     * Process individual trade data and update cache.
     */
    private void processTradeData(JsonNode trade) {
        try {
            String symbol = trade.get("s").asText();
            BigDecimal price = new BigDecimal(trade.get("p").asText());
            long timestamp = trade.get("t").asLong();
            double volume = trade.get("v").asDouble();

            StockPrice stockPrice = StockPrice.builder()
                    .symbol(symbol)
                    .price(price)
                    .timestamp(timestamp)
                    .volume(volume)
                    .build();

            // Update cache
            latestPrices.put(symbol, stockPrice);

            // Notify listeners
            notifyListeners(symbol, stockPrice);

            log.debug("Updated price for {}: ${} (volume: {})", symbol, price, volume);
        } catch (Exception e) {
            log.error("Error processing trade data: {}", trade, e);
        }
    }

    /**
     * Notify all listeners of a price update.
     */
    private void notifyListeners(String symbol, StockPrice stockPrice) {
        Set<StockPriceListener> listeners = priceListeners.get(symbol);
        if (listeners != null) {
            listeners.forEach(listener -> {
                try {
                    listener.onPriceUpdate(stockPrice);
                } catch (Exception e) {
                    log.error("Error notifying listener for symbol: {}", symbol, e);
                }
            });
        }
    }

    /**
     * Listener interface for price updates.
     */
    @FunctionalInterface
    public interface StockPriceListener {
        void onPriceUpdate(StockPrice stockPrice);
    }

    /**
     * Data class for stock price information.
     */
    @lombok.Data
    @lombok.Builder
    public static class StockPrice {
        private String symbol;
        private BigDecimal price;
        private long timestamp;
        private double volume;
    }
}
