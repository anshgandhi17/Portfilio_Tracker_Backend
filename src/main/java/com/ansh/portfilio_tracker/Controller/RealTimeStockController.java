package com.ansh.portfilio_tracker.Controller;

import com.ansh.portfilio_tracker.Service.RealTimeStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for real-time stock data operations.
 * Provides endpoints to get cached real-time prices.
 */
@RestController
@RequestMapping("/api/stocks/realtime")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class RealTimeStockController {

    private final RealTimeStockService realTimeStockService;

    /**
     * Get the latest cached price for a symbol.
     * This endpoint returns data from the WebSocket cache, providing near-instant responses.
     *
     * @param symbol Stock symbol
     * @return Latest price data or 404 if not available
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<Map<String, Object>> getLatestPrice(@PathVariable String symbol) {
        RealTimeStockService.StockPrice stockPrice = realTimeStockService.getLatestPrice(symbol);

        if (stockPrice != null) {
            return ResponseEntity.ok(Map.of(
                    "symbol", stockPrice.getSymbol(),
                    "price", stockPrice.getPrice(),
                    "timestamp", stockPrice.getTimestamp(),
                    "volume", stockPrice.getVolume()
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Manually subscribe to a symbol's real-time updates.
     *
     * @param symbol Stock symbol
     * @return Success message
     */
    @PostMapping("/subscribe/{symbol}")
    public ResponseEntity<Map<String, String>> subscribe(@PathVariable String symbol) {
        realTimeStockService.subscribe(symbol);
        return ResponseEntity.ok(Map.of(
                "message", "Subscribed to " + symbol,
                "symbol", symbol.toUpperCase()
        ));
    }

    /**
     * Manually unsubscribe from a symbol's real-time updates.
     *
     * @param symbol Stock symbol
     * @return Success message
     */
    @DeleteMapping("/subscribe/{symbol}")
    public ResponseEntity<Map<String, String>> unsubscribe(@PathVariable String symbol) {
        realTimeStockService.unsubscribe(symbol);
        return ResponseEntity.ok(Map.of(
                "message", "Unsubscribed from " + symbol,
                "symbol", symbol.toUpperCase()
        ));
    }
}
