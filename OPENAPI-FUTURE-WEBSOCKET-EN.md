# MGBX Futures WebSocket API Documentation

## 1. WebSocket Overview

> ⚠️ **Important Notice**: Futures trading requires activation by contacting customer service. Please reach out to our online customer service through the official website or send an email to the support mailbox to apply for futures trading access. Once activated, you can use the following WebSocket endpoints for real-time futures data subscriptions.

The MGBX trading platform provides futures WebSocket interfaces that support real-time subscription to market data and user data. Compared to REST API, WebSocket offers lower latency and higher efficiency. WebSocket connections do not require authentication to subscribe to market data, but authentication via listenKey is required for subscribing to private user data.

## 2. Service Address

- Market Data WebSocket Base URL: `wss://open.mgbx.com/fut/v1/ws/market`
- User Data WebSocket Base URL: `wss://open.mgbx.com/fut/v1/ws/user`
- Get User listenKey URL: `https://open.mgbx.com/fut/v1/user/listen-key`

## 3. Authentication Mechanism

### 3.1 User Data Authentication Flow

Subscribing to private user data requires obtaining a listenKey first, then providing it in the subscription message:

1. Call the `/fut/v1/user/listen-key` endpoint to obtain a WebSocket authentication listenKey
2. Include the obtained listenKey in the subscription message when subscribing to user data

### 3.2 Obtain Authentication listenKey

**Endpoint Information:**
- **Path**: `/fut/v1/user/listen-key`
- **Method**: `GET`
- **Signature Required**: Yes

**Request Headers:**

| Header | Description |
|-------|------|
| `X-Access-Key` | Your API access key |
| `X-Signature` | Signature generated using your secret key against request parameters |
| `X-Request-Timestamp` | Request timestamp (milliseconds) |
| `X-Request-Nonce` | Random string to prevent replay attacks |

**Signature Algorithm:**

Signature algorithm process:

1. Sort regular request parameters alphabetically by key name (using TreeMap)
2. Build parameter string: `key1=value1&key2=value2&...`
3. Append timestamp at the end of parameter string: `key1=value1&key2=value2&...&timestamp=xxx`
   (where `timestamp` value is the same as the `X-Request-Timestamp` header)
4. Use HMAC-SHA256 algorithm with secretKey as the key to sign the final parameter string, encoding using UTF-8
5. Convert the signature result to a hexadecimal string

**Response Data:**

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | String | WebSocket authentication listenKey |

## 4. Heartbeat Mechanism

- Client needs to send a `ping` message every 25 seconds
- Server will reply with a `pong` message
- If the server does not receive a heartbeat for more than 30 seconds, the connection will be disconnected

## 5. Subscription Types

### 5.1 Market Data Subscriptions

#### 5.1.1 Trading Pair Subscription

**No API permissions required, public endpoint**

**Request Format:**
```json
{
  "req": "sub_symbol",
  "symbol": "btc_usdt"
}
```

**Response Data Types:**

1. Trade Push (push.deal):

Field descriptions:
- `s`: Trading pair
- `p`: Trade price
- `a`: Trade quantity
- `m`: Trade direction, ASK=sell, BID=buy
- `t`: Timestamp

```json
{
  "channel": "push.deal",
  "data": {
    "s": "btc_usdt",
    "p": "30050.00",
    "a": "0.25",
    "m": "ASK",
    "t": 1687245871234
  }
}
```

2. Depth Push (push.deep):

Field descriptions:
- `s`: Trading pair
- `id`: Update ID
- `ba`: Side, 1=buy / 2=sell
- `p`: Price
- `q`: Quantity
- `t`: Timestamp

```json
{
  "channel": "push.deep",
  "data": {
    "s": "btc_usdt",
    "id": "12345678",
    "ba": 1,
    "p": "29900.00",
    "q": "0.3",
    "t": 1687245871234
  }
}
```

3. Full Depth Snapshot Push (push.deep.full):

Field descriptions:
- `s`: Trading pair
- `id`: Update ID
- `a`: Sell orders array [price, quantity]
- `b`: Buy orders array [price, quantity]

```json
{
  "channel": "push.deep.full",
  "data": {
    "s": "btc_usdt",
    "id": "12345678",
    "a": [
      ["30100.00", "0.1"],
      ["30200.00", "0.5"]
    ],
    "b": [
      ["29900.00", "0.3"],
      ["29800.00", "0.6"]
    ]
  }
}
```

#### 5.1.2 Mark Price Subscription

**Request Format:**
```json
{
  "req": "sub_mark_price"
}
```

**Response Format:**

Field descriptions:
- `s`: Trading pair
- `p`: Mark price
- `t`: Timestamp

```json
{
  "channel": "push.mark.price",
  "data": {
    "s": "btc_usdt",
    "p": "30000.00",
    "t": 1687245871234
  }
}
```

#### 5.1.3 Ticker Subscription

**Request Format:**
```json
{
  "req": "sub_ticker"
}
```

**Response Format:**

Field descriptions:
- `s`: Trading pair
- `o`: Open price
- `c`: Close price
- `h`: High price
- `l`: Low price
- `a`: Volume
- `v`: Turnover
- `r`: Price change ratio
- `t`: Timestamp

```json
{
  "channel": "push.ticker",
  "data": {
    "s": "btc_usdt",
    "o": "30000.00",
    "c": "30100.00",
    "h": "30200.00",
    "l": "29900.00",
    "a": "10.5",
    "v": "315000.50",
    "r": "0.0033",
    "t": 1687245871234
  }
}
```

#### 5.1.4 Index Price Push (May Be Delivered with Ticker Subscription)

**Response Format:**

Field descriptions:
- `s`: Trading pair
- `p`: Index price
- `t`: Timestamp

```json
{
  "channel": "push.index.price",
  "data": {
    "s": "btc_usdt",
    "p": "30000.00",
    "t": 1687245871234
  }
}
```

#### 5.1.5 Aggregated Ticker Push (May Be Delivered with Ticker Subscription)

**Response Format:**

Field descriptions:
- `s`: Trading pair
- `o`: Open price
- `c`: Close price
- `h`: High price
- `l`: Low price
- `a`: Volume
- `v`: Turnover
- `r`: Price change ratio
- `i`: Index price
- `m`: Mark price
- `bp`: Best bid price
- `ap`: Best ask price
- `t`: Timestamp

```json
{
  "channel": "push.agg.ticker",
  "data": {
    "s": "btc_usdt",
    "o": "30000.00",
    "c": "30100.00",
    "h": "30200.00",
    "l": "29900.00",
    "a": "10.5",
    "v": "315000.50",
    "r": "0.0033",
    "i": "30050.00",
    "m": "30020.00",
    "bp": "30100.00",
    "ap": "30120.00",
    "t": 1687245871234
  }
}
```

#### 5.1.6 K-Line Data Subscription

**Request Format:**
```json
{
  "req": "sub_kline",
  "symbol": "btc_usdt",
  "type": "1h"
}
```

> Supported K-line intervals: 1m, 5m, 15m, 30m, 1h, 4h, 1d, 1w, 1M

**Response Format:**

Field descriptions:
- `s`: Trading pair
- `o`: Open price
- `c`: Close price
- `h`: High price
- `l`: Low price
- `a`: Volume
- `v`: Turnover
- `i`: K-line interval
- `t`: Timestamp

```json
{
  "channel": "push.kline",
  "data": {
    "s": "btc_usdt",
    "o": "30000.00",
    "c": "30100.00",
    "h": "30200.00",
    "l": "29900.00",
    "a": "10.5",
    "v": "315000.50",
    "i": "1h",
    "t": 1687245871234
  }
}
```

### 5.2 User Data Subscriptions

**Requires listenKey obtained from /fut/v1/user/listen-key**

**Request Format:**
```json
{
  "req": "sub_user",
  "listenKey": "your_authentication_listenKey"
}
```

> Upon successful subscription, a plain text `succeed` response (non-JSON) may be returned. This can be ignored or used only as a success indicator.

**Response Data Types:**

1. User Balance Update (user.balance):

Field descriptions:
- `coin`: Currency
- `balanceType`: Account type
- `underlyingType`: Underlying type (1=coin-based, 2=USDT-based)
- `walletBalance`: Wallet balance
- `openOrderMarginFrozen`: Open order frozen margin
- `isolatedMargin`: Isolated margin
- `crossedMargin`: Cross margin
- `availableBalance`: Available balance
- `bonus`: Bonus amount

```json
{
  "channel": "user.balance",
  "data": {
    "coin": "BTC",
    "balanceType": "FUTURES",
    "underlyingType": 1,
    "walletBalance": "1.5",
    "openOrderMarginFrozen": "0.5",
    "isolatedMargin": "0.3",
    "crossedMargin": "0.2",
    "availableBalance": "0.5",
    "bonus": "0.0"
  }
}
```

2. User Position Update (user.position):

Field descriptions:
- `symbol`: Trading pair
- `positionId`: Position ID
- `contractType`: Contract type
- `positionType`: Position type (CROSSED=cross, ISOLATED=isolated)
- `balanceType`: Balance type
- `positionModel`: Position model (AGGREGATION=hedge, INDEPENDENT=one-way)
- `positionSide`: Position side (LONG, SHORT)
- `positionSize`: Position size
- `closeOrderSize`: Close order quantity
- `availableCloseSize`: Available close size (= positionSize - closeOrderSize)
- `realizedProfit`: Realized P&L
- `entryPrice`: Average entry price
- `isolatedMargin`: Isolated margin
- `bounsMargin`: Bonus margin
- `openOrderMarginFrozen`: Open order frozen margin
- `underlyingType`: Underlying type (1=coin-based, 2=USDT-based)
- `unsettledProfit`: Unsettled P&L
- `autoMargin`: Whether auto-add margin is enabled
- `leverage`: Leverage multiplier
- `work`: Whether position is active

```json
{
  "channel": "user.position",
  "data": {
    "symbol": "btc_usdt",
    "positionId": "123456789",
    "contractType": "PERPETUAL",
    "positionType": "CROSSED",
    "balanceType": "CONTRACT",
    "positionModel": "AGGREGATION",
    "positionSide": "LONG",
    "positionSize": "0.5",
    "closeOrderSize": "0.0",
    "availableCloseSize": "0.5",
    "realizedProfit": "0.0",
    "entryPrice": "30000.00",
    "isolatedMargin": "0.0",
    "bounsMargin": "0.0",
    "openOrderMarginFrozen": "0.0",
    "underlyingType": "2",
    "unsettledProfit": "0.01",
    "autoMargin": false,
    "leverage": 10,
    "work": true
  }
}
```

3. User Position Configuration (user.position.conf):

Field descriptions:
- `symbol`: Trading pair
- `positionType`: Position type (CROSSED=cross, ISOLATED=isolated)
- `positionModel`: Position model (AGGREGATION=hedge, INDEPENDENT=one-way)
- `positionSide`: Position side (LONG, SHORT)
- `autoMargin`: Whether auto-add margin is enabled
- `leverage`: Leverage multiplier

```json
{
  "channel": "user.position.conf",
  "data": {
    "symbol": "btc_usdt",
    "positionType": "CROSSED",
    "positionModel": "AGGREGATION",
    "positionSide": "LONG",
    "autoMargin": false,
    "leverage": 10
  }
}
```

4. User Order Update (user.order):

Field descriptions:
- `symbol`: Trading pair
- `contractType`: Contract type
- `orderId`: Order ID
- `origQty`: Original quantity
- `avgPrice`: Average execution price
- `price`: Order price
- `executedQty`: Executed quantity
- `orderSide`: Order side (BUY, SELL)
- `positionSide`: Position side (LONG, SHORT)
- `marginFrozen`: Frozen margin
- `sourceType`: Source type
- `sourceId`: Source ID (may be null, e.g., source order ID for TP/SL triggered orders)
- `state`: Order status
- `createTime`: Creation timestamp (ms)

```json
{
  "channel": "user.order",
  "data": {
    "symbol": "btc_usdt",
    "contractType": "PERPETUAL",
    "orderId": "475533479170587712",
    "origQty": "0.1",
    "avgPrice": "30050.00",
    "price": "30000.00",
    "executedQty": "0.05",
    "orderSide": "BUY",
    "positionSide": "LONG",
    "marginFrozen": "150.25",
    "sourceType": "WEB",
    "sourceId": null,
    "state": "PARTIALLY_FILLED",
    "createTime": 1687245871234
  }
}
```

5. User Trade Notification (user.trade):

Field descriptions:
- `orderId`: Order ID
- `price`: Trade price
- `quantity`: Trade quantity
- `marginUnfrozen`: Unfrozen margin
- `timestamp`: Trade timestamp

```json
{
  "channel": "user.trade",
  "data": {
    "orderId": "475533479170587712",
    "price": "30050.00",
    "quantity": "0.05",
    "marginUnfrozen": "15.025",
    "timestamp": 1687245871500
  }
}
```

## 6. Best Practices

1. **Heartbeat Maintenance**: Send a heartbeat message every 25 seconds to ensure the connection stays alive
2. **Auto Reconnection**: Implement automatic reconnection to handle network fluctuations
3. **listenKey Management**: listenKey has a limited validity period; update it in a timely manner
4. **Async listenKey Retrieval**: Retrieve listenKey asynchronously after connection is established; do not block the main thread
5. **Data Verification**: For critical business data, use REST API for secondary confirmation
6. **Efficient Processing**: Implement proper caching and processing strategies for high-frequency data to avoid memory overflow
