# MGBX WebSocket API Documentation

## 1. WebSocket Overview

MGBX Trading Platform provides WebSocket interfaces that support real-time subscription to market data and user data, offering lower latency and higher efficiency compared to REST API. WebSocket connections do not require authentication, but subscribing to private user data requires an authentication token.

## 2. Service Address

- WebSocket Base URL: `wss://open.mgbx.com/spot/v1/ws/socket`
- User Authentication Token URL: `https://open.mgbx.com/spot/v1/u/ws/token`

## 3. Authentication Mechanism

### 3.1 User Data Authentication Process

Subscribing to private user data requires obtaining an authentication token first, then providing this token in the subscription message:

1. Call the `/spot/v1/u/ws/token` interface to get the WebSocket authentication token
2. Add the obtained token to the subscription message when subscribing to user data

### 3.2 Obtaining Authentication Token

**Interface Information:**
- **Path**: `/spot/v1/u/ws/token`
- **Method**: `GET`
- **Signature Required**: Yes

**Request Headers:**

| Request Header | Description |
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
| message | String | Response message |
| data | String | WebSocket authentication Token |

## 4. Heartbeat Mechanism

- Client needs to send a `ping` message every 50 seconds
- Server will reply with a `pong` message
- If the server does not receive a heartbeat for more than 60 seconds, it will disconnect

## 5. Subscription Types

### 5.1 Market Data Subscription

#### 5.1.1 Single Trading Pair Market Subscription

**No API permissions required, public interface**

**Request Format**:
```json
{
  "sub": "subSymbol",
  "symbol": "BTC_USDT"
}
```

**Response Data Types**:

1. Single-level Depth Data (qDepth):

Field descriptions:
- `id`: Depth update unique identifier
- `s`: Trading pair
- `p`: Price
- `q`: Quantity
- `m`: Direction (1 buy/2 sell)
- `t`: Timestamp (milliseconds)

```json
{
  "resType": "qDepth",
  "data": {
    "id": "485241469557277058",
    "s": "BTC_USDT",
    "m": 2,
    "p": "93503.08",
    "q": "2.05713",
    "t": 1745392581693
  }
}
```

2. Full Depth Data (qAllDepth):

Field descriptions:
- `s`: Trading pair
- `id`: Update ID
- `a`: Sell orders array [price, quantity]
- `b`: Buy orders array [price, quantity]

```json
{
  "resType": "qAllDepth",
  "data": {
    "s": "BTC_USDT",
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

3. Trade Data (qDeal):

Field descriptions:
- `s`: Trading pair
- `p`: Trade price
- `a`: Trade volume
- `m`: Buy/sell direction, 1 buy/2 sell
- `t`: Timestamp

```json
{
  "resType": "qDeal",
  "data": {
    "s": "BTC_USDT",
    "p": "30050.00",
    "a": "0.25",
    "m": 1,
    "t": 1687245871234
  }
}
```

#### 5.1.2 K-line Data Subscription


**Request Format**:
```json
{
  "sub": "subKline",
  "symbol": "BTC_USDT",
  "type": "1m"
}
```

> Supported K-line periods: 1m, 5m, 15m, 30m, 1h, 4h, 1d, 1w, 1M

**Response Format**:

Field descriptions:
- `s`: Trading pair
- `o`: Open price
- `c`: Close price
- `h`: High price
- `l`: Low price
- `a`: Volume
- `v`: Turnover
- `i`: K-line period
- `t`: Timestamp

```json
{
  "resType": "qKLine",
  "data": {
    "s": "BTC_USDT",
    "o": "30000.00",
    "c": "30100.00",
    "h": "30200.00",
    "l": "29900.00",
    "a": "10.5",
    "v": "315000.50",
    "i": "1m",
    "t": 1687245871234
  }
}
```

#### 5.1.3 Statistics Data Subscription


**Request Format**:
```json
{
  "sub": "subStats"
}
```

**Response Format**:

Field descriptions:
- `s`: Trading pair
- `o`: Open price
- `c`: Close price
- `h`: High price
- `l`: Low price
- `a`: 24h volume
- `v`: 24h turnover
- `r`: Price change percentage (decimal)

```json
{
  "resType": "qStats",
  "data": {
    "s": "BTC_USDT",
    "o": "30000.00",
    "c": "30100.00",
    "h": "30200.00",
    "l": "29900.00",
    "a": "10.5",
    "v": "315000.50",
    "r": "0.0033"
  }
}
```

### 5.2 User Data Subscription

**Requires requesting /spot/v1/u/ws/token to obtain token**

**Request Format**:
```json
{
  "sub": "subUser",
  "token": "Authentication Token obtained"
}
```

**Response Data Types**:

1. User Balance Change (uBalance):

Field descriptions:
- `coin`: Currency
- `balanceType`: Account type (1 spot, 2 margin)
- `balance`: Total balance
- `freeze`: Frozen amount
- `availableBalance`: Available balance
- `estimatedTotalAmount`: Total balance estimated value
- `estimatedCynAmount`: CNY estimated value
- `estimatedAvailableAmount`: Available balance estimated value
- `estimatedCoinType`: Currency used for estimation

```json
{
   "resType": "uBalance",
   "data": {
      "coin": "USDT",
      "balanceType": 1,
      "balance": "76027890.2214756",
      "freeze": "948.5",
      "availableBalance": "76027890.2214756",
      "estimatedTotalAmount": "76028838.72147560",
      "estimatedCynAmount": "547407638.79462432",
      "estimatedAvailableAmount": "76027890.22147560",
      "estimatedCoinType": "USDT"
   }
}
```

2. User Order Update (uOrder):

Field descriptions:
- `orderId`: Order ID
- `balanceType`: Account type (1 spot, 2 margin)
- `orderType`: Order type (1 limit order, 2 market order, 3 stop-limit)
- `symbol`: Trading pair
- `price`: Price
- `direction`: Direction (1 buy, 2 sell)
- `origQty`: Original quantity
- `avgPrice`: Average execution price
- `dealQty`: Executed quantity
- `state`: Status (1 unfilled, 2 partially filled, 3 completely filled, 4 canceled)
- `createTime`: Creation time

```json
{
  "resType": "uOrder",
  "data": {
    "orderId": "475533479170587712",
    "balanceType": 1,
    "orderType": 1,
    "symbol": "BTC_USDT",
    "price": "30000.00",
    "direction": 1,
    "origQty": "0.1",
    "avgPrice": "30050.00",
    "dealQty": "0.05",
    "state": 2,
    "createTime": 1687245871234
  }
}
```

3. User Trade Notification (uTrade):

Field descriptions:
- `orderId`: Order ID
- `price`: Execution price
- `quantity`: Execution quantity
- `marginUnfrozen`: Margin unfrozen
- `timestamp`: Execution timestamp

```json
{
  "resType": "uTrade",
  "data": {
    "orderId": "475533479170587712",
    "price": "30050.00",
    "quantity": "0.05",
    "marginUnfrozen": "15.025",
    "timestamp": 1687245871500
  }
}
```

### 5.3 System Notification Subscription

System notifications are automatically pushed without special subscription. When the system has important notifications (such as price fluctuation alerts), they will be pushed through this channel.

**Response Data Type** (znxMessage):

Field descriptions:
- `id`: Notification message ID
- `tenantId`: Tenant ID
- `title`: Notification title
- `content`: Notification content
- `aggType`: Message aggregation type, e.g., "SYSTEM"
- `detailType`: Message detail type, e.g., "SYSTEM_PRICE" indicates price alert
- `createdTime`: Creation timestamp (milliseconds)
- `allScope`: Whether it's a global scope message
- `userId`: User ID, -1 indicates system message
- `read`: Whether it has been read

```json
{
   "resType": "znxMessage",
   "data": {
      "id": 336912,
      "tenantId": 1,
      "title": "Market Price",
      "content": "VVVUSDT 10-minute price change -3.01%, current price 4.177 USDT",
      "aggType": "SYSTEM",
      "detailType": "SYSTEM_PRICE",
      "createdTime": 1745932297862,
      "allScope": true,
      "userId": -1,
      "read": false
   }
}
```

## 6. Error Codes

| Error Code | Description |
|-------|------|
| invalid param | Invalid parameter, please check subscription format |
| auth fail | Authentication failed, please check authentication parameters |
| sub fail | Subscription failed |
| system error | System error |

## 7. Best Practices

1. **Heartbeat Maintenance**: Send heartbeat messages every 25 seconds to ensure the connection remains active
2. **Reconnection**: Implement automatic reconnection mechanism to handle network fluctuations
3. **Token Management**: Tokens have limited validity periods and need to be updated in a timely manner
4. **Asynchronous Token Retrieval**: Asynchronously obtain tokens after connection is successful, without blocking the main thread
5. **Data Verification**: Critical business data should be double-checked using REST API
6. **Efficient Processing**: Implement appropriate caching and processing strategies for high-frequency data to avoid memory overflow
7. **Error Handling**: Properly handle various exception scenarios, including authentication failures, subscription errors, etc.