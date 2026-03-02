# MGBX Futures OpenAPI Documentation

## 1. API Overview

> ⚠️ **Important Notice**: Futures trading requires activation by contacting customer service. Please reach out to our online customer service through the official website or send an email to the support mailbox to apply for futures trading access. Once activated, you can use the following API endpoints for futures trading.

The MGBX Futures Trading Platform API provides programmatic trading capabilities, allowing developers to create orders, query market data, and other functions via HTTP requests.

## 2. Service Address

**API Base URL**: `https://open.mgbx.com`

## 3. Authentication Mechanism

### 3.1 Authentication Parameters

| Request Header | Description |
|-------|------|
| X-Access-Key | Your API access key |
| X-Signature | Signature generated using your secret key against request parameters |
| X-Request-Timestamp | Request timestamp (milliseconds) |
| X-Request-Nonce | Random string to prevent replay attacks |

### 3.2 Signature Algorithm

Signature algorithm process:

1. Sort regular request parameters alphabetically by key name (using TreeMap)
2. Build parameter string: `key1=value1&key2=value2&...`
3. Append timestamp at the end of parameter string: `key1=value1&key2=value2&...&timestamp=xxx`
   (where `timestamp` value is the same as the `X-Request-Timestamp` header)
4. Use HMAC-SHA256 algorithm with secretKey as the key to sign the final parameter string, encoding using UTF-8
5. Convert the signature result to a hexadecimal string
6. Send parameter content in the form of application/x-www-form-urlencoded query string

### 3.3 Public Endpoints

The following endpoints are public and **do not require any authentication**. They can be accessed directly:

```
/fut/v1/public/time            // System time
/fut/v1/public/symbol/detail   // Trading pair details
/fut/v1/public/q/ticker        // 24-hour ticker for a single trading pair
/fut/v1/public/q/tickers       // Tickers for all trading pairs
/fut/v1/public/q/kline         // K-line data
/fut/v1/public/q/depth         // Depth information
/fut/v1/public/q/deal          // Latest trade records
```

## 4. Market Data Endpoints

### 4.1 Get Server Time

**Request Method**: GET  
**Endpoint**: `/fut/v1/public/time`  
**Signature Required**: No

**Request Parameters**:
No parameters required

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Long | Server timestamp (milliseconds) |

**Response Format**:
```json
{
  "code": 0,
  "msg": "success",
  "data": 1689245871234
}
```

### 4.2 Get Trading Pair Details

**Request Method**: GET
**Endpoint**: `/fut/v1/public/symbol/detail`
**Signature Required**: No

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|-----|------|
| symbol | String | Yes | Trading pair, e.g., "btc_usdt" |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Object | Trading pair details |

**Data Object Fields**:

| Parameter Name | Type | Description |
|-------|-----|------|
| id | Integer | Trading pair ID |
| symbol | String | Trading pair name |
| contractType | String | Contract type |
| underlyingType | String | Underlying type |
| contractSize | String | Contract size |
| initLeverage | Integer | Initial leverage multiplier |
| initPositionType | String | Initial position type |
| baseCoin | String | Base asset |
| quoteCoin | String | Quote asset |
| quantityPrecision | Integer | Quantity precision |
| pricePrecision | Integer | Price precision |
| supportOrderType | String | Supported order types |
| supportTimeInForce | String | Supported time-in-force types |
| minQty | String | Minimum order quantity |
| minNotional | String | Minimum order notional |
| maxNotional | String | Maximum order notional |
| makerFee | String | Maker fee rate |
| takerFee | String | Taker fee rate |
| minStepPrice | String | Minimum price step |

**Response Format**:
```json
{
   "code": 0,
   "msg": "success",
   "data": {
      "id": 1,
      "symbol": "btc_usdt",
      "contractType": "PERPETUAL",
      "underlyingType": "U_BASED",
      "contractSize": "0.0001",
      "tradeSwitch": true,
      "state": 0,
      "initLeverage": 100,
      "initPositionType": "CROSSED",
      "baseCoin": "btc",
      "quoteCoin": "usdt",
      "baseCoinPrecision": 6,
      "baseCoinDisplayPrecision": 5,
      "quoteCoinPrecision": 4,
      "quoteCoinDisplayPrecision": 4,
      "quantityPrecision": 0,
      "pricePrecision": 1,
      "supportOrderType": "LIMIT,MARKET",
      "supportTimeInForce": "GTC,FOK,IOC,GTX",
      "supportEntrustType": "TAKE_PROFIT,STOP,TAKE_PROFIT_MARKET,STOP_MARKET,TRAILING_STOP_MARKET",
      "supportPositionType": "CROSSED,ISOLATED",
      "minQty": "1",
      "minNotional": "1",
      "maxNotional": "100000000",
      "multiplierDown": "0.1",
      "multiplierUp": "0.1",
      "maxOpenOrders": 1000,
      "maxEntrusts": 20,
      "makerFee": "0.0005",
      "takerFee": "0.0003",
      "liquidationFee": "0.015",
      "marketTakeBound": "0.0003",
      "depthPrecisionMerge": 3,
      "labels": ["HOT"],
      "onboardDate": 1651528801000,
      "enName": "BTCUSDT ",
      "cnName": "BTCUSDT 永续",
      "minStepPrice": "0.1"
   }
}
```

### 4.3 Get Single Contract Ticker

**Request Method**: GET  
**Endpoint**: `/fut/v1/public/q/ticker`  
**Signature Required**: No

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|-----|------|
| symbol | String | Yes | Trading pair, e.g., "BTC_USDT" |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Object | Ticker data |

**Data Object Fields**:

| Parameter Name | Type | Description |
|-------|-----|------|
| s | String | Trading pair |
| t | Long | Timestamp |
| c | String | Latest price |
| o | String | Price 24 hours ago |
| h | String | 24-hour highest price |
| l | String | 24-hour lowest price |
| a | String | 24-hour volume |
| v | String | 24-hour turnover |
| r | String | 24-hour price change percentage (%) |
| tickerTrendVo | Object | Price trend data, may be null |

**Response Format**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "s": "btc_usdt",
    "t": 1744365480681,
    "c": "82296.5",
    "o": "82019.0",
    "h": "82459.1",
    "l": "78594.0",
    "a": "1279195",
    "v": "10353763.15100",
    "r": "0.0033",
    "tickerTrendVo": null
  }
}
```

### 4.4 Get All Contract Tickers

**Request Method**: GET  
**Endpoint**: `/fut/v1/public/q/tickers`  
**Signature Required**: No

**Request Parameters**:
No parameters required

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Array | Ticker data list for all trading pairs |

**Fields for Each Object in the Data Array**:

| Parameter Name | Type | Description |
|-------|-----|------|
| s | String | Trading pair |
| t | Long | Timestamp |
| c | String | Latest price |
| o | String | Price 24 hours ago |
| h | String | 24-hour highest price |
| l | String | 24-hour lowest price |
| a | String | 24-hour volume |
| v | String | 24-hour turnover |
| r | String | 24-hour price change percentage (%) |
| tickerTrendVo | Object | Price trend data |

**tickerTrendVo Object Fields**:

| Parameter Name | Type | Description |
|-------|-----|------|
| list | Array | Price trend data list |

**Fields for Each Object in the list Array**:

| Parameter Name | Type | Description |
|-------|-----|------|
| symbolId | Integer | Trading pair ID |
| symbol | String | Trading pair name |
| price | Double/Number | Price |
| time | Long | Timestamp |

**Response Format**:
```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "s": "BTC_USDT",
      "t": 1744364409830,
      "c": "82274.4",
      "o": "81883.0",
      "h": "82459.1",
      "l": "78594.0",
      "a": "1279533",
      "v": "10356451.01575",
      "r": "0.0047",
      "tickerTrendVo": {
        "list": [
          {
            "symbolId": 1,
            "symbol": "btc_usdt",
            "price": 82019.0,
            "time": 1744275600000
          }
        ]
      }
    }
  ]
}
```

### 4.5 Get Contract K-Line Data

**Request Method**: GET  
**Endpoint**: `/fut/v1/public/q/kline`  
**Signature Required**: No

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|-----|------|
| symbol | String | Yes | Trading pair, e.g., "BTC_USDT" |
| interval | String | Yes | K-line interval, e.g., "1m","5m","15m","30m","1h","4h","1d","1w","1M" |
| limit | Integer | No | Number of records, default 500, maximum 1500 |
| startTime | Long | No | Start timestamp (milliseconds) |
| endTime | Long | No | End timestamp (milliseconds) |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Array | K-line data list |

**Fields for Each Object in the Data Array**:

| Parameter Name | Type | Description |
|-------|-----|------|
| s | String | Trading pair |
| t | Long | Timestamp |
| o | String | Open price |
| h | String | Highest price |
| l | String | Lowest price |
| c | String | Close price |
| a | String | Volume |
| v | String | Turnover |

**Response Format**:
```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "s": "BTC_USDT",
      "t": 1687245871234,
      "o": "62100.00",
      "h": "62500.00",
      "l": "61800.00",
      "c": "62300.00",
      "a": "125.35",
      "v": "7756432.25"
    }
  ]
}
```

### 4.6 Get Contract Depth Data

**Request Method**: GET  
**Endpoint**: `/fut/v1/public/q/depth`  
**Signature Required**: No

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|-----|------|
| symbol | String | Yes | Trading pair, e.g., "BTC_USDT" |
| level | Integer | Yes | Depth level (1-50) |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Object | Depth data |

**Data Object Fields**:

| Parameter Name | Type | Description |
|-------|-----|------|
| s | String | Trading pair |
| t | Long | Timestamp |
| u | Long | Depth update sequence number |
| b | Array | Buy orders [price, quantity] |
| a | Array | Sell orders [price, quantity] |

**Response Format**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "s": "BTC_USDT",
    "t": 1687245871234, 
    "u": 480925789685022816,
    "b": [
      ["62300.00", "0.5"],
      ["62250.00", "1.2"]
    ],
    "a": [
      ["62350.00", "0.3"],
      ["62400.00", "1.0"]
    ]
  }
}
```

### 4.7 Get Latest Contract Trades

**Request Method**: GET  
**Endpoint**: `/fut/v1/public/q/deal`  
**Signature Required**: No

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|-----|------|
| symbol | String | Yes | Trading pair, e.g., "BTC_USDT" |
| num | Integer | No | Number of records, minimum 1, default 50 |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Array | Trade record list |

**Fields for Each Object in the Data Array**:

| Parameter Name | Type | Description |
|-------|-----|------|
| s | String | Trading pair |
| p | String | Trade price |
| a | String | Trade quantity |
| t | Long | Trade timestamp (milliseconds) |
| m | String | Trade direction ("ASK" for sell, "BID" for buy) |

**Response Format**:
```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "s": "BTC_USDT",
      "p": "62350.00",
      "a": "0.05",
      "t": 1687245871234,
      "m": "BID"
    },
    {
      "s": "BTC_USDT",
      "p": "62340.00",
      "a": "0.08",
      "t": 1687245865432,
      "m": "ASK"
    }
  ]
}
```

## 5. Private Endpoints

### 5.1 Create Order

**Request Method**: POST  
**Endpoint**: `/fut/v1/order/create`  
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|---------|------|
| symbol | String | Yes | Trading pair |
| orderSide | String | Yes | Order side: BUY or SELL |
| orderType | String | Yes | Order type: LIMIT or MARKET |
| positionSide | String | Yes | Position side: LONG or SHORT |
| origQty | String | Yes | Original quantity (actual trading volume) |
| price | String | No | Price, not required for market orders |
| leverage | Integer | No | Leverage multiplier |
| positionId | Long | No | Close position ID |
| triggerProfitPrice | String | No | Take profit price |
| triggerStopPrice | String | No | Stop loss price |
| sourceType | Integer | No | Source type (default `0` if not specified) |

**sourceType Parameter Description**:

| Value | Description |
|---|---|
| 0 | Normal order |
| 1 | Plan order trigger |
| 2 | Take profit/Stop loss trigger |
| 4 | Reverse order |

**Usage Notes**:
- Only the above values are supported; defaults to `0` when not provided.
- `sourceType` represents the order source type (integer).

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Object | Order ID |

**Response Format**:
```json
{
   "code": 0,
   "msg": "success",
   "data": "481556897766682496"
}
```

### 5.2 Query Order Details

**Request Method**: GET  
**Endpoint**: `/fut/v1/order/detail`  
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|---------|------|
| orderId | Long | Yes | Order ID |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Object | Order details |

**Data Object Fields**:

| Parameter Name | Type | Description |
|-------|-----|------|
| orderId | String | Order ID |
| symbol | String | Trading pair name |
| contractType | String | Contract type |
| orderType | String | Order type |
| orderSide | String | Order side |
| leverage | Integer | Leverage multiplier |
| positionSide | String | Position side |
| closePosition | Boolean | Whether closing position |
| price | String | Price |
| origQty | String | Original quantity |
| avgPrice | String | Average execution price |
| executedQty | String | Executed quantity |
| marginFrozen | String | Frozen margin |
| triggerProfitPrice | String | Take profit price |
| triggerStopPrice | String | Stop loss price |
| sourceId | String | Source ID |
| forceClose | Boolean | Whether force liquidation |
| tradeFee | String | Trading fee |
| closeProfit | String | Close position P&L |
| state | String | Order status |
| createdTime | Long | Creation time |
| updatedTime | Long | Update time |

**Response Format**:
```json
{
   "code": 0,
   "msg": "success",
   "data": {
      "orderId": "481561679331962752",
      "symbol": "btc_usdt",
      "contractType": "PERPETUAL",
      "orderType": "LIMIT",
      "orderSide": "BUY",
      "leverage": 100,
      "positionSide": "LONG",
      "closePosition": false,
      "price": "70000",
      "origQty": "10000",
      "avgPrice": "0",
      "executedQty": "0",
      "marginFrozen": "700",
      "triggerProfitPrice": null,
      "triggerStopPrice": null,
      "sourceId": null,
      "forceClose": false,
      "tradeFee": "0",
      "closeProfit": null,
      "state": "NEW",
      "createdTime": 1744515251497,
      "updatedTime": 1744515251497
   }
}
```

### 5.3 Query Order List

**Request Method**: GET  
**Endpoint**: `/fut/v1/order/list`  
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|---------|------|
| symbol | String | No | Trading pair |
| state | String | No | Order status filter: NEW, FILLED, CANCELED |
| page | Integer | No | Page number, default 1 |
| size | Integer | No | Page size, default 10 |
| startTime | Long | No | Start time |
| endTime | Long | No | End time |
| contractType | String | No | Contract type |
| forceClose | Boolean | No | Whether force liquidation |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Object | Paginated data |

**Data Object Fields**:

| Parameter Name | Type | Description |
|-------|-----|------|
| page | Integer | Current page number |
| ps | Integer | Page size |
| total | Long | Total records |
| items | Array | Order list |

**Fields for Each Object in the Items Array**:

| Parameter Name | Type | Description |
|-------|-----|------|
| orderId | String | Order ID |
| symbol | String | Trading pair |
| contractType | String | Contract type |
| orderType | String | Order type |
| orderSide | String | Order side |
| leverage | Integer | Leverage multiplier |
| positionSide | String | Position side |
| closePosition | Boolean | Whether closing position |
| price | String | Price |
| origQty | String | Original quantity |
| avgPrice | String | Average execution price |
| executedQty | String | Executed quantity |
| marginFrozen | String | Frozen margin |
| triggerProfitPrice | String | Take profit price |
| triggerStopPrice | String | Stop loss price |
| sourceId | String | Source ID |
| forceClose | Boolean | Whether force liquidation |
| tradeFee | String | Trading fee |
| closeProfit | String | Close position P&L |
| state | String | Order status |
| createdTime | Long | Creation time |
| updatedTime | Long | Update time |

**Response Format**:
```json
{
   "code": 0,
   "msg": "success",
   "data": {
      "page": 1,
      "ps": 10,
      "total": 9,
      "items": [
         {
            "orderId": "481561679331962752",
            "symbol": "btc_usdt",
            "contractType": "PERPETUAL",
            "orderType": "LIMIT",
            "orderSide": "BUY",
            "leverage": 100,
            "positionSide": "LONG",
            "closePosition": false,
            "price": "70000",
            "origQty": "10000",
            "avgPrice": "0",
            "executedQty": "0",
            "marginFrozen": "700",
            "triggerProfitPrice": null,
            "triggerStopPrice": null,
            "sourceId": null,
            "forceClose": false,
            "tradeFee": "0",
            "closeProfit": null,
            "state": "NEW",
            "createdTime": 1744515251497,
            "updatedTime": 1744515251497
         }
      ]
   }
}
```

### 5.4 Query Trade Details

**Request Method**: GET  
**Endpoint**: `/fut/v1/order/trade-list`  
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|---------|------|
| symbol | String | No | Trading pair |
| orderId | String | No | Order ID |
| orderSide | String | No | Order side: BUY or SELL |
| startTime | Long | No | Start time |
| endTime | Long | No | End time |
| page | Integer | No | Page number, default 1 |
| size | Integer | No | Page size, default 10 |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Object | Paginated data |

**Data Object Fields**:

| Parameter Name | Type | Description |
|-------|-----|------|
| page | Integer | Current page number |
| ps | Integer | Page size |
| total | Long | Total records |
| items | Array | Trade detail list |

**Fields for Each Object in the Items Array**:

| Parameter Name | Type | Description |
|-------|-----|------|
| orderId | String | Order ID |
| execId | String | Execution ID |
| symbol | String | Trading pair |
| orderSide | String | Order side |
| positionSide | String | Position side |
| quantity | String | Execution quantity |
| price | String | Execution price |
| fee | String | Fee |
| feeCoin | String | Fee currency |
| timestamp | Long | Execution timestamp |

**Response Format**:
```json
{
   "code": 0,
   "msg": "success",
   "data": {
      "page": 1,
      "ps": 10,
      "total": 144,
      "items": [
         {
            "orderId": "481556897766682496",
            "execId": "481556897976025155",
            "symbol": "btc_usdt",
            "orderSide": "BUY",
            "positionSide": "LONG",
            "quantity": "100",
            "price": "85360.2",
            "fee": "0.0426",
            "feeCoin": "usdt",
            "timestamp": 1744514111372
         }
      ]
   }
}
```

### 5.5 Query User Balance

**Request Method**: GET  
**Endpoint**: `/fut/v1/balance/list`  
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|---------|------|
| balanceType | String | No | Balance type, returns all types if not specified |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Array | Balance information list |

**Fields for Each Object in the Data Array**:

| Parameter Name | Type | Description |
|-------|-----|------|
| coin | String | Currency |
| balanceType | String | Balance type |
| walletBalance | String | Wallet balance |
| availableBalance | String | Available balance |
| openOrderMarginFrozen | String | Open order frozen margin |
| isolatedMargin | String | Isolated margin |
| crossedMargin | String | Cross margin |
| bonus | String | Bonus amount |

**Response Format**:
```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "coin": "USDT",
      "balanceType": "CONTRACT",
      "walletBalance": "10000.00",
      "availableBalance": "9900.00",
      "openOrderMarginFrozen": "100.00",
      "isolatedMargin": "0.00",
      "crossedMargin": "0.00",
      "bonus": "0.00"
    }
  ]
}
```

### 5.6 Get Position List

**Request Method**: GET
**Endpoint**: `/fut/v1/position/list`
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|---------|------|
| symbol | String | No | Trading pair, e.g., "btc_usdt" |
| contractType | String | No | Contract type: PERPETUAL (perpetual) |
| balanceType | String | No | Balance type: CONTRACT (contract), COPY (copy trading) |

**Parameter Notes**:
- `contractType` currently only supports `PERPETUAL`.
- `balanceType` supports `CONTRACT` (contract account) and `COPY` (copy trading account), defaults to contract account.

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Array | Position information list |

**Fields for Each Object in the Data Array**:

| Parameter Name | Type | Description |
|-------|-----|------|
| symbol | String | Trading pair |
| positionId | String | Position ID |
| contractType | String | Contract type: PERPETUAL |
| balanceType | String | Balance type: CONTRACT, COPY |
| positionType | String | Position type: CROSSED (cross), ISOLATED (isolated) |
| positionSide | String | Position side: LONG, SHORT |
| positionModel | String | Position model |
| underlyingType | String | Underlying type: U_BASED, COIN_BASED |
| positionSize | String | Position size (contracts) |
| closeOrderSize | String | Close order size (contracts) |
| availableCloseSize | String | Available close size (contracts) |
| entryPrice | String | Average entry price |
| isolatedMargin | String | Isolated margin |
| bounsMargin | String | Bonus margin ratio |
| openOrderMarginFrozen | String | Open order margin frozen |
| realizedProfit | String | Realized P&L |
| autoMargin | Boolean | Auto-add margin enabled |
| leverage | Integer | Leverage multiplier |
| isLeaderPosition | Boolean | Whether it is a leader position |
| updatedTime | Long | Update time |
| unsettledProfit | String | Unsettled P&L |
| profit | Object | Take profit/Stop loss info, may be null |

**profit Object Fields**:

| Parameter Name | Type | Description |
|-------|-----|------|
| profitId | String | Take profit/Stop loss ID |
| profitPrice | String | Take profit price |
| stopPrice | String | Stop loss price |
| triggerPriceType | String | Trigger price type: MARK_PRICE, LATEST_PRICE |

**Notes**:
- When `isLeaderPosition` is `true`, the position was created by a copy trading strategy.
- The `profit` object is returned only when take profit/stop loss is configured; otherwise `null`.

**Response Format**:
```json
{
   "code": 0,
   "msg": "success",
   "data": [
      {
         "symbol": "btc_usdt",
         "positionId": "481556897766682496",
         "contractType": "PERPETUAL",
         "balanceType": "CONTRACT",
         "positionType": "CROSSED",
         "positionSide": "LONG",
         "positionModel": "AGGREGATION",
         "underlyingType": "U_BASED",
         "positionSize": "100",
         "closeOrderSize": "0",
         "availableCloseSize": "100",
         "entryPrice": "82350.5",
         "isolatedMargin": "0",
         "bounsMargin": "0",
         "openOrderMarginFrozen": "0",
         "realizedProfit": "0",
         "autoMargin": false,
         "leverage": 100,
         "isLeaderPosition": false,
         "updatedTime": 1744515251497,
         "unsettledProfit": "0",
         "profit": null
      }
   ]
}
```

### 5.7 Change Position Type

**Request Method**: POST  
**Endpoint**: `/fut/v1/position/change-type`  
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|---------|------|
| symbol | String | Yes | Trading pair, e.g., "btc_usdt" |
| positionType | String | Yes | Position type: `CROSSED` (cross), `ISOLATED` (isolated) |
| positionModel | String | Yes | Position model: `AGGREGATION` (hedge), `DISAGGREGATION` (one-way) |

**Response Format**:
```json
{
  "code": 0,
  "msg": "success"
}
```

**Common Business Response (when positions exist)**:
```json
{
  "code": -1,
  "msg": "There are positions currently, and the margin mode cannot be switched",
  "data": null
}
```

### 5.8 Adjust Leverage

**Request Method**: POST  
**Endpoint**: `/fut/v1/position/adjust-leverage`  
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|---------|------|
| symbol | String | Yes | Trading pair, e.g., "btc_usdt" |
| leverage | Integer | Yes | New leverage multiplier |
| positionSide | String | No | Position side: `LONG` or `SHORT`. Not required for one-way position mode |

**Notes**:
- It is recommended to set leverage based on your current position and risk strategy; start with lower leverage if you don't have a specific strategy.

**Response Format**:
```json
{
  "code": 0,
  "msg": "success"
}
```

### 5.9 Close All Positions

**Request Method**: POST  
**Endpoint**: `/fut/v1/position/close-all`  
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|---------|------|
| symbol | String | No | Trading pair, e.g., "btc_usdt". If not specified, closes all contract positions |
| contractType | String | No | Contract type, e.g., `PERPETUAL` |

> ⚠️ This operation will trigger actual position closing. Please use with caution.
>
> Example parameters are for demonstration only. Please replace with actual business parameters.

**Response Format**:
```json
{
  "code": 0,
  "msg": "success",
  "data": true
}
```

### 5.10 Adjust Isolated Margin

**Request Method**: POST  
**Endpoint**: `/fut/v1/position/margin`  
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|---------|------|
| symbol | String | Yes | Trading pair, e.g., "btc_usdt" |
| positionSide | String | Yes | Position side: `LONG` or `SHORT` |
| positionId | String | Yes | Position ID, obtained from position list |
| margin | String | Yes | Adjustment amount, supports decimals |
| type | String | Yes | Adjustment direction: `ADD` (increase margin), `SUB` (decrease margin) |

**Notes**:
- Only isolated (`ISOLATED`) positions support margin adjustment; cross positions will return `code = -1`.
- It is recommended to verify with a small amount first, then adjust according to your risk strategy.

**Response Format**:
```json
{
  "code": 0,
  "msg": "success"
}
```

**Common Business Failure (non-isolated position)**:
```json
{
  "code": -1,
  "msg": "Only isolated position supports margin adjustment",
  "data": null
}
```

### 5.11 Cancel Order

**Request Method**: POST
**Endpoint**: `/fut/v1/order/cancel`
**Signature Required**: Yes

> ⚠️ **Note**: This is an asynchronous interface. A successful response only indicates that the cancellation request has been submitted. The actual cancellation result should be confirmed by querying the order status through the order query endpoint.

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|---------|------|
| orderId | Long | Yes | Order ID |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Object | Cancellation result |

**Response Format**:
```json
{
   "code": 0,
   "msg": "success",
   "data": null
}
```

### 5.12 Create Take Profit/Stop Loss Order

**Request Method**: POST
**Endpoint**: `/fut/v1/entrust/create-profit`
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|---------|------|
| symbol | String | Yes | Trading pair, e.g., "btc_usdt" |
| triggerProfitPrice | BigDecimal | No | Take profit trigger price; at least one of `triggerProfitPrice` or `triggerStopPrice` is required |
| triggerStopPrice | BigDecimal | No | Stop loss trigger price; at least one of `triggerProfitPrice` or `triggerStopPrice` is required |
| triggerPriceType | String | No | Trigger price type: `MARK_PRICE` or `LATEST_PRICE`, default `LATEST_PRICE` |
| positionId | Long | No | Position ID |
| origQty | BigDecimal | Yes | Order quantity (contracts), required and must be > 0 |
| expireTime | Long | No | Expiration time (millisecond timestamp) |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | String | Order ID |

**Response Format**:
```json
{
   "code": 0,
   "msg": "success",
   "data": "596507084674713600"
}
```

### 5.13 Query Take Profit/Stop Loss Order List

**Request Method**: GET
**Endpoint**: `/fut/v1/entrust/profit-list`
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|---------|------|
| symbol | String | No | Trading pair |
| contractType | String | No | Contract type: `PERPETUAL`, `CURRENT_MONTH`, `NEXT_MONTH`, `CURRENT_QUARTER`, `NEXT_QUARTER`, etc. |
| positionSide | String | No | Position side: `LONG`, `SHORT` |
| state | String | No | Order status filter: `UNFINISHED` (not triggered, aggregated), `HISTORY` (historical, aggregated), `NOT_TRIGGERED`, `TRIGGERED`, `EXPIRED`, `USER_REVOCATION`, `PLATFORM_REVOCATION` |
| startTime | Long | No | Start time (millisecond timestamp) |
| endTime | Long | No | End time (millisecond timestamp) |
| page | Integer | No | Page number, default 1 |
| size | Integer | No | Page size, default 10 |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Object | Paginated data |

**Data Object Fields**:

| Parameter Name | Type | Description |
|-------|-----|------|
| page | Integer | Current page number |
| ps | Integer | Page size |
| total | Long | Total records |
| items | Array | Order list |

**Fields for Each Object in the Items Array**:

| Parameter Name | Type | Description |
|-------|-----|------|
| profitId | String | Order ID |
| positionId | String | Position ID |
| contractType | String | Contract type |
| symbol | String | Trading pair |
| positionType | String | Position type: `CROSSED`, `ISOLATED` |
| positionSide | String | Position side: `LONG`, `SHORT` |
| origQty | String | Order quantity (contracts) |
| triggerPriceType | String | Trigger price type: `MARK_PRICE`, `LATEST_PRICE` |
| triggerProfitPrice | String | Take profit trigger price |
| triggerStopPrice | String | Stop loss trigger price |
| triggerPriceSide | Integer | Trigger direction |
| entryPrice | String | Average entry price |
| positionSize | String | Position size |
| isolatedMargin | String | Isolated margin |
| triggerPrice | String | Current trigger price |
| executedQty | String | Executed quantity |
| state | String | Order status (`NOT_TRIGGERED`, `TRIGGERED`, `EXPIRED`, `USER_REVOCATION`, `PLATFORM_REVOCATION`) |
| createdTime | Long | Creation time |
| updatedTime | Long | Update time |

**Response Format**:
```json
{
   "code": 0,
   "msg": "success",
   "data": {
      "page": 1,
      "ps": 10,
      "total": 5,
      "items": [
         {
            "profitId": "596521474568374272",
            "positionId": "592447815234371840",
            "contractType": "PERPETUAL",
            "symbol": "btc_usdt",
            "positionType": "CROSSED",
            "positionSide": "LONG",
            "origQty": "3002",
            "triggerPriceType": "LATEST_PRICE",
            "triggerProfitPrice": "100000",
            "triggerStopPrice": "30000",
            "triggerPriceSide": 0,
            "entryPrice": "66591.9",
            "positionSize": "3002",
            "isolatedMargin": "999.544",
            "triggerPrice": "0",
            "executedQty": "0",
            "state": "NOT_TRIGGERED",
            "createdTime": 1771923802386,
            "updatedTime": 1771923802386
         }
      ]
   }
}
```

### 5.14 Query Historical Orders

**Request Method**: GET
**Endpoint**: `/fut/v1/order/list-history`
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|---------|------|
| symbol | String | No | Trading pair |
| orderSide | String | No | Order side: `BUY`, `SELL` |
| orderType | String | No | Order type: `LIMIT`, `MARKET` |
| forceClose | Boolean | No | Whether force liquidation order |
| startTime | Long | No | Start time (millisecond timestamp) |
| endTime | Long | No | End time (millisecond timestamp) |
| page | Integer | No | Page number, default 1 |
| size | Integer | No | Page size, default 10 |
| contractType | String | No | Contract type: `PERPETUAL`, `CURRENT_MONTH`, `NEXT_MONTH`, `CURRENT_QUARTER`, `NEXT_QUARTER`, etc. |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Object | Paginated data |

**Data Object Fields**:

| Parameter Name | Type | Description |
|-------|-----|------|
| hasPrev | Boolean | Whether there is a previous page |
| hasNext | Boolean | Whether there is a next page |
| items | Array | Order list |

**Fields for Each Object in the Items Array**:

| Parameter Name | Type | Description |
|-------|-----|------|
| orderId | String | Order ID |
| clientOrderId | String | Client order ID, may be null |
| symbol | String | Trading pair |
| contractType | String | Contract type |
| orderType | String | Order type: `LIMIT`, `MARKET` |
| orderSide | String | Order side: `BUY`, `SELL` |
| leverage | Integer | Leverage multiplier |
| positionSide | String | Position side: `LONG`, `SHORT` |
| timeInForce | String | Time-in-force: `GTC`, `IOC`, `FOK`, `GTX` (Post-Only) |
| closePosition | Boolean | Whether closing position |
| price | String | Price |
| origQty | String | Original quantity |
| avgPrice | String | Average execution price |
| executedQty | String | Executed quantity |
| marginFrozen | String | Frozen margin |
| triggerProfitPrice | String | Take profit price |
| triggerStopPrice | String | Stop loss price |
| sourceId | String | Source ID |
| forceClose | Boolean | Whether force liquidation |
| tradeFee | String | Trading fee |
| closeProfit | String | Close position P&L |
| state | String | Order status (`NEW`, `PARTIALLY_FILLED`, `FILLED`, `CANCELED`, `PARTIALLY_CANCELED`) |
| createdTime | Long | Creation time |
| updatedTime | Long | Update time |

**Response Format**:
```json
{
   "code": 0,
   "msg": "success",
   "data": {
      "hasPrev": false,
      "hasNext": true,
      "items": [
         {
            "orderId": "592456494457801600",
            "clientOrderId": null,
            "symbol": "btc_usdt",
            "contractType": "PERPETUAL",
            "orderType": "MARKET",
            "orderSide": "BUY",
            "leverage": 20,
            "positionSide": "LONG",
            "timeInForce": "IOC",
            "closePosition": false,
            "price": "0",
            "origQty": "3002",
            "avgPrice": "66591.8",
            "executedQty": "3002",
            "marginFrozen": "999.6826",
            "triggerProfitPrice": null,
            "triggerStopPrice": null,
            "sourceId": null,
            "forceClose": false,
            "tradeFee": "0.399",
            "closeProfit": null,
            "state": "FILLED",
            "createdTime": 1770954635912,
            "updatedTime": 1770954635914
         }
      ]
   }
}
```

### 5.15 Query Balance Bills

**Request Method**: GET
**Endpoint**: `/fut/v1/balance/bills`
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|---------|------|
| coin | String | No | Currency, e.g., "USDT" |
| symbol | String | No | Trading pair, e.g., "btc_usdt" |
| balanceType | String | No | Account type: `CONTRACT`, `COPY` |
| type | String | No | Business type (e.g., `FUND`) |
| startTime | Long | No | Start time (millisecond timestamp) |
| endTime | Long | No | End time (millisecond timestamp) |
| page | Integer | No | Page number, default 1 |
| size | Integer | No | Page size, default 10 |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Object | Paginated data |

**Data Object Fields**:

| Parameter Name | Type | Description |
|-------|-----|------|
| hasPrev | Boolean | Whether there is a previous page |
| hasNext | Boolean | Whether there is a next page |
| items | Array | Bill list |

**Fields for Each Object in the Items Array**:

| Parameter Name | Type | Description |
|-------|-----|------|
| id | String | Bill ID |
| coin | String | Currency |
| balanceType | String | Balance type: `CONTRACT`, `COPY` |
| symbol | String | Trading pair |
| positionId | Long | Position ID |
| type | String | Business type, e.g., `FUND` |
| amount | String | Change amount |
| side | String | Direction, may be null |
| afterAmount | String | Balance after change |
| createdTime | Long | Creation time |

**Response Format**:
```json
{
   "code": 0,
   "msg": "success",
   "data": {
      "hasPrev": false,
      "hasNext": true,
      "items": [
         {
            "id": "596490429839458560",
            "coin": "usdt",
            "balanceType": "CONTRACT",
            "symbol": "btc_usdt",
            "positionId": 592447815234371840,
            "type": "FUND",
            "amount": "1.2121",
            "side": null,
            "afterAmount": "7481.6302",
            "createdTime": 1771916400730
         }
      ]
   }
}
```

### 5.16 Query Position Configuration

**Request Method**: GET
**Endpoint**: `/fut/v1/position/confs`
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|---------|------|
| symbol | String | Yes | Trading pair code, recommended lowercase with underscore (e.g., `btc_usdt`) |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Array | Position configuration list |

**Fields for Each Object in the Data Array**:

| Parameter Name | Type | Description |
|-------|-----|------|
| symbol | String | Trading pair |
| leverage | Integer | Leverage multiplier |
| positionType | String | Position type: `CROSSED` (cross), `ISOLATED` (isolated) |
| positionModel | String | Position model: `AGGREGATION` (hedge), `DISAGGREGATION` (one-way) |
| positionSide | String | Position side: `LONG`, `SHORT` |
| autoMargin | Boolean | Auto-add margin enabled |

**Response Format**:
```json
{
   "code": 0,
   "msg": "success",
   "data": [
      {
         "symbol": "btc_usdt",
         "positionType": "CROSSED",
         "positionSide": "SHORT",
         "positionModel": "AGGREGATION",
         "autoMargin": null,
         "leverage": 20
      },
      {
         "symbol": "btc_usdt",
         "positionType": "CROSSED",
         "positionSide": "LONG",
         "positionModel": "AGGREGATION",
         "autoMargin": null,
         "leverage": 20
      }
   ]
}
```

## 6. Error Code Description

| Error Code | Description |
|-------|------|
| -1 | General error |
| 0 | Success |
| 1001 | Signature error |
| 1002 | Signature expired |
| 1003 | Signature is empty |
| 1004 | Signature parameter is empty |
| 1005 | Signature parameter error |
| 1006 | Signature timestamp is empty |
| 1007 | Signature timestamp error |
| 1008 | Signature timestamp expired |
| 1009 | Signature timestamp format error |
| 1010 | Duplicate nonce submission |
| 1011 | Invalid signature information |
| 1012 | Invalid request source IP |
| 1013 | Invalid access URL |
