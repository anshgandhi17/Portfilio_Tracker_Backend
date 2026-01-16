package com.ansh.portfilio_tracker.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * WebSocket client for Finnhub real-time stock data.
 * Connects to Finnhub's WebSocket API and streams live trade data.
 */
@Slf4j
@Component
public class FinnhubWebSocketClient extends WebSocketClient {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Consumer<JsonNode>> messageHandlers = new ConcurrentHashMap<>();
    private final String apiKey;

    public FinnhubWebSocketClient(@Value("${finnhub.api.key}") String apiKey) {
        super(URI.create("wss://ws.finnhub.io?token=" + apiKey));
        this.apiKey = apiKey;
        log.info("Finnhub WebSocket client initialized");
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("Finnhub WebSocket connection opened");
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String type = jsonNode.has("type") ? jsonNode.get("type").asText() : "unknown";

            log.debug("Received WebSocket message - Type: {}", type);

            // Notify all registered handlers
            messageHandlers.values().forEach(handler -> {
                try {
                    handler.accept(jsonNode);
                } catch (Exception e) {
                    log.error("Error in message handler", e);
                }
            });
        } catch (Exception e) {
            log.error("Error processing WebSocket message: {}", message, e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.warn("Finnhub WebSocket connection closed - Code: {}, Reason: {}, Remote: {}",
                code, reason, remote);

        // Attempt to reconnect after 5 seconds
        if (remote) {
            scheduleReconnect();
        }
    }

    @Override
    public void onError(Exception ex) {
        log.error("Finnhub WebSocket error", ex);
    }

    /**
     * Subscribe to real-time trades for a stock symbol.
     *
     * @param symbol Stock symbol (e.g., "AAPL")
     */
    public void subscribe(String symbol) {
        try {
            String subscribeMessage = String.format("{\"type\":\"subscribe\",\"symbol\":\"%s\"}",
                    symbol.toUpperCase());
            send(subscribeMessage);
            log.info("Subscribed to symbol: {}", symbol);
        } catch (Exception e) {
            log.error("Error subscribing to symbol: {}", symbol, e);
        }
    }

    /**
     * Unsubscribe from real-time trades for a stock symbol.
     *
     * @param symbol Stock symbol (e.g., "AAPL")
     */
    public void unsubscribe(String symbol) {
        try {
            String unsubscribeMessage = String.format("{\"type\":\"unsubscribe\",\"symbol\":\"%s\"}",
                    symbol.toUpperCase());
            send(unsubscribeMessage);
            log.info("Unsubscribed from symbol: {}", symbol);
        } catch (Exception e) {
            log.error("Error unsubscribing from symbol: {}", symbol, e);
        }
    }

    /**
     * Register a message handler to process incoming WebSocket messages.
     *
     * @param handlerId Unique identifier for the handler
     * @param handler Consumer function to process messages
     */
    public void registerMessageHandler(String handlerId, Consumer<JsonNode> handler) {
        messageHandlers.put(handlerId, handler);
        log.info("Registered message handler: {}", handlerId);
    }

    /**
     * Remove a message handler.
     *
     * @param handlerId Handler identifier to remove
     */
    public void removeMessageHandler(String handlerId) {
        messageHandlers.remove(handlerId);
        log.info("Removed message handler: {}", handlerId);
    }

    /**
     * Schedule a reconnection attempt.
     */
    private void scheduleReconnect() {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                log.info("Attempting to reconnect to Finnhub WebSocket...");
                this.reconnect();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Reconnection interrupted", e);
            }
        }).start();
    }
}
