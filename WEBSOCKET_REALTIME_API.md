# Real-Time Stock Data with WebSocket

This application now supports real-time stock price updates using Finnhub's WebSocket API. This provides significantly faster data updates compared to polling the REST API.

## Architecture Overview

The real-time data system consists of several components:

1. **FinnhubWebSocketClient** - Manages the WebSocket connection to Finnhub
2. **RealTimeStockService** - Manages subscriptions, caches prices, and broadcasts updates
3. **StockWebSocketController** - WebSocket endpoint for frontend clients
4. **RealTimeStockController** - REST endpoints for cached real-time data

## How It Works

### Backend to Finnhub Connection

1. The application connects to `wss://ws.finnhub.io` using your API key
2. When a stock symbol is requested, it subscribes to real-time trades
3. Finnhub sends trade data as it happens in the market
4. The service caches the latest price for each subscribed symbol

### Frontend to Backend Connection

Clients can connect via:
- **WebSocket (STOMP)** - For real-time push updates
- **REST API** - For cached real-time data (near-instant response)

## WebSocket API Usage (Frontend)

### Connecting to WebSocket

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

// Create WebSocket connection
const socket = new SockJS('http://localhost:8080/ws/stocks');
const stompClient = new Client({
    webSocketFactory: () => socket,
    onConnect: () => {
        console.log('Connected to WebSocket');

        // Subscribe to a stock symbol
        stompClient.subscribe('/topic/stock/AAPL', (message) => {
            const data = JSON.parse(message.body);
            console.log('Price update:', data);
            // data = { symbol, price, timestamp, volume }
        });

        // Request subscription
        stompClient.publish({
            destination: '/app/subscribe/AAPL'
        });
    }
});

stompClient.activate();
```

### Unsubscribing from Updates

```javascript
// Unsubscribe from a symbol
stompClient.publish({
    destination: '/app/unsubscribe/AAPL'
});
```

### Message Format

Updates received on `/topic/stock/{SYMBOL}`:
```json
{
    "symbol": "AAPL",
    "price": 178.45,
    "timestamp": 1699564800000,
    "volume": 1234.5
}
```

## REST API Usage

### Get Latest Cached Price

```http
GET /api/stocks/realtime/{symbol}
```

**Example:**
```bash
curl http://localhost:8080/api/stocks/realtime/AAPL
```

**Response:**
```json
{
    "symbol": "AAPL",
    "price": 178.45,
    "timestamp": 1699564800000,
    "volume": 1234.5
}
```

Returns `404 Not Found` if the symbol is not currently subscribed or no data is cached.

### Manual Subscribe

```http
POST /api/stocks/realtime/subscribe/{symbol}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/stocks/realtime/subscribe/AAPL
```

### Manual Unsubscribe

```http
DELETE /api/stocks/realtime/subscribe/{symbol}
```

## Enhanced FinnhubClient

The existing `FinnhubClient.getQuote()` method now automatically uses the real-time cache when available, providing faster responses:

1. First checks the real-time cache (instant response if data exists)
2. Falls back to REST API if cache miss
3. Automatically subscribes to real-time updates for future requests

This means existing code benefits from WebSocket performance without changes!

## Configuration

Add to `application.properties`:

```properties
# Enable/disable real-time cache integration
finnhub.use-realtime-cache=true
```

## Performance Benefits

### Before (REST API polling)
- Request time: ~200-500ms per request
- Data freshness: Depends on polling interval
- Rate limits: 60 calls/minute (free tier)

### After (WebSocket)
- Initial connection: ~200ms
- Update latency: Real-time (milliseconds)
- Cached read time: <1ms
- No rate limit for receiving updates

## Example Integration

### React Component Example

```jsx
import { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

function StockPrice({ symbol }) {
    const [price, setPrice] = useState(null);
    const [client, setClient] = useState(null);

    useEffect(() => {
        const socket = new SockJS('http://localhost:8080/ws/stocks');
        const stompClient = new Client({
            webSocketFactory: () => socket,
            onConnect: () => {
                // Subscribe to price updates
                stompClient.subscribe(`/topic/stock/${symbol}`, (message) => {
                    const data = JSON.parse(message.body);
                    setPrice(data.price);
                });

                // Request subscription
                stompClient.publish({
                    destination: `/app/subscribe/${symbol}`
                });
            }
        });

        stompClient.activate();
        setClient(stompClient);

        return () => {
            if (stompClient) {
                stompClient.publish({
                    destination: `/app/unsubscribe/${symbol}`
                });
                stompClient.deactivate();
            }
        };
    }, [symbol]);

    return <div>Current price: ${price}</div>;
}
```

## Monitoring and Debugging

The WebSocket implementation includes comprehensive logging:

- Connection status
- Subscription/unsubscription events
- Price updates (debug level)
- Error handling and auto-reconnect

Check application logs for WebSocket activity:
```
[FinnhubWebSocketClient] Connected to Finnhub WebSocket
[RealTimeStockService] Subscribed to real-time updates for: AAPL
[RealTimeStockService] Updated price for AAPL: $178.45 (volume: 1234.5)
```

## Limitations

1. **Finnhub Free Tier**: Limited to US stocks only on free tier
2. **Connection Limits**: WebSocket connection is shared across all symbols
3. **Market Hours**: Real-time data only available during market hours

## Troubleshooting

### WebSocket Connection Fails

- Verify `finnhub.api.key` is set in application.properties
- Check firewall allows WebSocket connections
- Ensure Finnhub API key is valid and not rate-limited

### No Price Updates

- Verify market is open for the stock
- Check symbol is valid (must be exact ticker, e.g., "AAPL" not "Apple")
- Review application logs for subscription confirmation

### High Memory Usage

The service caches prices for all subscribed symbols. If monitoring many symbols, consider:
- Implementing a max cache size
- Adding TTL (time-to-live) for cached entries
- Unsubscribing from inactive symbols
