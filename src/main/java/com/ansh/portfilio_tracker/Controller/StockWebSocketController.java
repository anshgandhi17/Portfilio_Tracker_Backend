package com.ansh.portfilio_tracker.Controller;

import com.ansh.portfilio_tracker.Service.RealTimeStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket controller for real-time stock price updates.
 * Clients can subscribe to stock symbols and receive live price updates.
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class StockWebSocketController {

    private final RealTimeStockService realTimeStockService;
    private final SimpMessagingTemplate messagingTemplate;

    // Track active subscriptions per symbol
    private final Map<String, Integer> subscriptionCounts = new ConcurrentHashMap<>();

    /**
     * Handle subscription requests from clients.
     * Endpoint: /app/subscribe/{symbol}
     *
     * @param symbol Stock symbol to subscribe to
     */
    @MessageMapping("/subscribe/{symbol}")
    public void subscribeToSymbol(@DestinationVariable String symbol) {
        String upperSymbol = symbol.toUpperCase();
        log.info("Client subscribing to symbol: {}", upperSymbol);

        // Increment subscription count
        subscriptionCounts.merge(upperSymbol, 1, Integer::sum);

        // Add listener for this symbol
        realTimeStockService.addPriceListener(upperSymbol, stockPrice -> {
            // Broadcast to all subscribers of this symbol
            messagingTemplate.convertAndSend(
                    "/topic/stock/" + upperSymbol,
                    Map.of(
                            "symbol", stockPrice.getSymbol(),
                            "price", stockPrice.getPrice(),
                            "timestamp", stockPrice.getTimestamp(),
                            "volume", stockPrice.getVolume()
                    )
            );
        });

        // Send current price if available
        RealTimeStockService.StockPrice currentPrice = realTimeStockService.getLatestPrice(upperSymbol);
        if (currentPrice != null) {
            messagingTemplate.convertAndSend(
                    "/topic/stock/" + upperSymbol,
                    Map.of(
                            "symbol", currentPrice.getSymbol(),
                            "price", currentPrice.getPrice(),
                            "timestamp", currentPrice.getTimestamp(),
                            "volume", currentPrice.getVolume()
                    )
            );
        }
    }

    /**
     * Handle unsubscription requests from clients.
     * Endpoint: /app/unsubscribe/{symbol}
     *
     * @param symbol Stock symbol to unsubscribe from
     */
    @MessageMapping("/unsubscribe/{symbol}")
    public void unsubscribeFromSymbol(@DestinationVariable String symbol) {
        String upperSymbol = symbol.toUpperCase();
        log.info("Client unsubscribing from symbol: {}", upperSymbol);

        // Decrement subscription count
        subscriptionCounts.compute(upperSymbol, (k, v) -> {
            if (v == null || v <= 1) {
                // No more subscribers, unsubscribe from Finnhub
                realTimeStockService.unsubscribe(upperSymbol);
                return null;
            }
            return v - 1;
        });
    }
}
