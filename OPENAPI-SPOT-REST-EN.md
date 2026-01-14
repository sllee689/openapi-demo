# OPENAPI-SPOT-REST

## 1. API Overview

The MGBX trading platform API provides programmatic trading capabilities, allowing developers to create orders, query market data, and other functions via HTTP requests.

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
/spot/v1/p/quotation/kline    // K-line data
/spot/v1/p/quotation/ticker   // 24-hour ticker for a single trading pair
/spot/v1/p/quotation/tickers  // Tickers for all trading pairs
/spot/v1/p/quotation/depth    // Depth information
/spot/v1/p/quotation/deal     // Latest trade records
/spot/v1/p/time               // System time
```

## 4. Trading Endpoints

### 4.1 Create Order

**Request Method**: POST  
**Endpoint**: `/spot/v1/u/trade/order/create`  
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|-----|------|
| symbol | String | Yes | Trading pair, e.g., "BTC_USDT" |
| direction | String | Yes | Trade direction: "BUY" for buying, "SELL" for selling |
| tradeType | String | Yes | Order type: "LIMIT" for limit orders, "MARKET" for market orders |
| totalAmount | String | Yes | Order quantity |
| price | String | Conditional | Order price, required for limit orders |
| timeInForce | String | No | Time-in-force, e.g., "GTC" (default), "GTX" (post only) |

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
  "data": "123456789012345678"
}
```
**Order Creation Notes**:

1. Order creation is processed asynchronously. The returned order ID may not be visible in the system until a later time.

### 4.2 Query Order Details

**Request Method**: GET  
**Endpoint**: `/spot/v1/u/trade/order/detail`  
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|-----|------|
| orderId | Long | Yes | Order ID, returned when creating an order or queried |

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
| clientOrderId | String | Client order ID |
| symbol | String | Trading pair |
| orderType | String | Order type: LIMIT or MARKET |
| orderSide | String | Buy or sell direction: BUY or SELL |
| balanceType | Integer | Account type: 1.Spot account, 2.Margin account |
| timeInForce | String | Time-in-force, e.g., GTC |
| price | String | Order price |
| origQty | String | Original order quantity |
| avgPrice | String | Average execution price |
| executedQty | String | Executed quantity |
| marginFrozen | String | Frozen margin |
| state | String | Order status, see table below |
| createdTime | Long | Creation timestamp |
| sourceId | String | Order source ID, may be null |
| forceClose | String | Forced closing flag, true-yes, false-no, may be null |

**Order Status Description**:

| Status Code | Description |
|---------|---------|
| `NEW` | New order (unfilled) |
| `PARTIALLY_FILLED` | Partially filled |
| `PARTIALLY_CANCELED` | Partially canceled |
| `FILLED` | Filled completely |
| `CANCELED` | Canceled |
| `REJECTED` | Order rejected |
| `EXPIRED` | Expired |

**Response Format**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "orderId": "475533479170587712",
    "clientOrderId": "LIMIT_BUY_1684896521234",
    "symbol": "BTC_USDT",
    "orderType": "LIMIT",
    "orderSide": "BUY",
    "balanceType": 1,
    "timeInForce": "GTC",
    "price": "60000.00",
    "origQty": "0.001",
    "avgPrice": "0",
    "executedQty": "0",
    "marginFrozen": "60.00",
    "sourceId": null,
    "forceClose": null,
    "state": "NEW",
    "createdTime": 1684896521234
  }
}
```

### 4.3 Query Open Orders

**Request Method**: GET  
**Endpoint**: `/spot/v1/u/trade/order/list`  
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|-----|------|
| symbol | String | No | Trading pair, e.g., "BTC_USDT" |
| startTime | Long | No | Start timestamp for the query (milliseconds) |
| endTime | Long | No | End timestamp for the query (milliseconds) | 
| state | Integer | No | Order status filter: 1-New order(unfilled), 2-Partially filled, 3-Completely filled, 4-Canceled, 5-Order failed, 6-Expired, 9-Open orders, 10-Historical orders |
| page | Integer | No | Page number, starting from 1, default 1 |
| size | Integer | No | Records per page, maximum 100, default 10 |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Object | Paged results |

**Data Object Fields**:

| Parameter Name | Type | Description |
|-------|-----|------|
| page | Integer | Current page number |
| ps | Integer | Records per page |
| total | Long | Total records |
| items | Array | Order list |

**Fields for Each Object in the Items Array**:

| Parameter Name | Type | Description |
|-------|-----|------|
| orderId | String | Order ID |
| clientOrderId | String | Client order ID |
| symbol | String | Trading pair |
| orderType | String | Order type: LIMIT or MARKET |
| orderSide | String | Buy or sell direction: BUY or SELL |
| balanceType | Integer | Account type |
| timeInForce | String | Time-in-force, e.g., GTC |
| price | String | Order price |
| origQty | String | Original order quantity |
| avgPrice | String | Average execution price |
| executedQty | String | Executed quantity |
| marginFrozen | String | Frozen margin |
| sourceId | String | Order source ID, may be null |
| forceClose | String | Forced closing flag, may be null |
| state | String | Order status, refer to order details endpoint |
| createdTime | Long | Creation timestamp |

**Response Format**:
```json
{
   "code": 0,
   "msg": "success",
   "data": {
      "page": 1,
      "ps": 5,
      "total": 3,
      "items": [
         {
            "orderId": "480897844870833664",
            "clientOrderId": "LIMIT_BUY_1744356980647",
            "symbol": "BTC_USDT",
            "orderType": "LIMIT",
            "orderSide": "BUY",
            "balanceType": 1,
            "timeInForce": "GTC",
            "price": "60000",
            "origQty": "0.001",
            "avgPrice": "0",
            "executedQty": "0",
            "marginFrozen": "60",
            "state": "NEW",
            "createdTime": 1744356981011
         }
      ]
   }
}
```

### 4.4 Query Historical Orders

**Request Method**: GET  
**Endpoint**: `/spot/v1/u/trade/order/history`  
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|-----|------|
| symbol | String | No | Trading pair, e.g., "BTC_USDT" |
| startTime | Long | No | Start timestamp for the query (milliseconds) |
| endTime | Long | No | End timestamp for the query (milliseconds) |
| balanceType | Integer | No | Account type: 1.Spot account(default), 2.Margin account |
| id | Long | No | Pagination identifier from previous query results |
| direction | String | No | Page direction: "NEXT" (next page), "PREV" (previous page) |
| limit | Integer | No | Records per page, default based on system configuration |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Object | Paged results |

**Data Object Fields**:

| Parameter Name | Type | Description |
|--------|--------|---------------|
| hasPrev | Boolean | Whether there is a previous page |
| hasNext | Boolean | Whether there is a next page |
| items | Array | Historical order list |

**Fields for Each Object in the Items Array**:

| Parameter Name | Type | Description |
|-------------|--------|--------------------|
| orderId | String | Order ID |
| clientOrderId | String | Client order ID |
| symbol | String | Trading pair |
| orderType | String | Order type: LIMIT or MARKET |
| orderSide | String | Buy or sell direction: BUY or SELL |
| balanceType | Integer | Account type |
| timeInForce | String | Time-in-force, e.g., GTC |
| price | String | Order price |
| origQty | String | Original order quantity |
| avgPrice | String | Average execution price |
| executedQty | String | Executed quantity |
| marginFrozen | String | Frozen margin |
| sourceId | String | Order source ID, may be null |
| forceClose | String | Forced closing flag, may be null |
| state | String | Order status, refer to order details endpoint |
| createdTime | Long | Creation timestamp |

**Response Example**:
```json
{
   "code": 0,
   "msg": "success",
   "data": {
      "hasPrev": true,
      "hasNext": true,
      "items": [
         {
            "orderId": "477292215853414656",
            "clientOrderId": "BATCH_LIMIT_SELL_1743497331250",
            "symbol": "BTC_USDT",
            "orderType": "LIMIT",
            "orderSide": "SELL",
            "balanceType": 1,
            "timeInForce": "GTC",
            "price": "90000",
            "origQty": "0.001",
            "avgPrice": "0",
            "executedQty": "0",
            "marginFrozen": "0.001",
            "sourceId": null,
            "forceClose": null,
            "state": "CANCELED",
            "createdTime": 1743497332075
         }
      ]
   }
}
```

### 4.5 Query Order Trade Details

**Request Method**: GET  
**Endpoint**: `/spot/v1/u/trade/order/deal`  
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|-----|------|
| orderId | Long | No | Order ID |
| symbol | String | No | Trading pair, e.g., "BTC_USDT" |
| page | Integer | No | Page number, starting from 1 |
| size | Integer | No | Records per page |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Object | Paged results |

**Data Object Fields**:

| Parameter Name | Type | Description |
|-------|-----|------|
| page | Integer | Current page number |
| ps | Integer | Records per page |
| total | Long | Total records |
| items | Array | Trade record list |

**Fields for Each Object in the Items Array**:

| Parameter Name | Type | Description |
|-------|-----|------|
| orderId | String | Order ID |
| execId | String | Execution ID |
| symbol | String | Trading pair |
| quantity | String | Executed quantity |
| price | String | Execution price |
| fee | String | Fee |
| orderSide | String | Buy or sell direction: BUY or SELL |
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
    "total": 5,
    "items": [
      {
        "orderId": "475533479170587712",
        "execId": "475533479170587713",
        "symbol": "BTC_USDT",
        "quantity": "0.001",
        "price": "60000.00",
        "fee": "0.06",
        "feeCoin": "USDT",
        "timestamp": 1684896521500
      }
    ]
  }
}
```

### 4.6 Batch Create Orders

**Request Method**: POST  
**Endpoint**: `/spot/v1/u/trade/order/batch/create`  
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|-----|------|
| ordersJsonStr | String | Yes | JSON string of order list |

**Fields of Each Order Object in ordersJsonStr**:

| Parameter Name | Type | Required | Description |
|-------|-----|-----|------|
| symbol | String | Yes | Trading pair, e.g., "BTC_USDT" |
| direction | String | Yes | Trade direction: "BUY" for buying, "SELL" for selling |
| tradeType | String | Yes | Order type: "LIMIT" for limit orders, "MARKET" for market orders |
| totalAmount | String | Yes | Order quantity |
| price | String | Conditional | Order price, required for limit orders |

**Request Example**:
```json
{
  "ordersJsonStr": "[{\"symbol\":\"BTC_USDT\",\"direction\":\"BUY\",\"tradeType\":\"LIMIT\",\"totalAmount\":\"0.001\",\"price\":\"80000\"},{\"symbol\":\"BTC_USDT\",\"direction\":\"SELL\",\"tradeType\":\"LIMIT\",\"totalAmount\":\"0.001\",\"price\":\"90000\"}]"
}
```

**Response Parameters**:
| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Array | Creation result for each order |

**Fields for Each Object in the Data Array**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Order creation status code, 0 indicates success |
| msg | String | Order creation response message |
| data | String | Order ID |

**Response Format**:
```json
{
   "code": 0,
   "msg": "success",
   "data": [
      {
         "code": 0,
         "msg": "success",
         "data": "481556897766682496"
      },
      {
         "code": 0,
         "msg": "success",
         "data": "481556897766682497"
      }
   ]
}
```

**Batch Order Creation Notes**:

1. Supports submitting orders for different trading pairs in a single request.
2. Batch operations follow a "best-effort" principle—failure of some orders will not affect the processing of others.
3. The response only includes the count of successful and failed orders. To obtain the status of specific orders, use the Order Query API.
4. The API supports a maximum of 20 orders per request. Exceeding this limit will return an error.

### 4.7 Batch Cancel Orders

**Request Method**: POST  
**Endpoint**: `/spot/v1/u/trade/order/batch/cancel`  
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|-----|------|
| orderIdsJson | String | Yes | JSON string of order IDs |

**Request Example**:
```json
{
  "orderIdsJson": "[\"475534805480815680\",\"475534266177207360\"]"
}
```

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|-----------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Boolean | Cancellation success information |

**Response Example**:
```json
{"code":0,"msg":"success","data":[{"code":0,"msg":"success","data":"477292986250590464"},{"code":0,"msg":"success","data":"477292986254784768"}]}
```

**Batch Order Cancellation Notes**:

1. The batch cancellation API supports cancelling up to 20 orders per request. Requests exceeding this limit will return an error.
2. Batch cancellation is processed asynchronously. A successful response only indicates that the request has been accepted.
3. Some orders may not be canceled due to reasons such as already being filled or previously canceled. This does not affect the cancellation of other orders.
4. It is recommended to confirm the final status of all orders through the Order Query API after cancellation.
5. In high-frequency trading scenarios, it is advisable to space out cancellation requests appropriately to avoid excessive calls.

### 4.8 Cancel Order

**Request Method**: POST  
**Endpoint**: `/spot/v1/u/trade/order/cancel`  
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|-----|------|
| orderId | Long | Yes | Order ID |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | String | Canceled order ID |

**Response Format**:
```json
{
  "code": 0,
  "msg": "success",
  "data": "480910202607429120"
}
```

### 4.9 Query Spot Account Balance

**Request Method**: GET  
**Endpoint**: `/spot/v1/u/balance/spot`  
**Signature Required**: Yes

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|-----|------|
| coin | String | No | Currency name, returns all currency balances if not specified |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Array | Balance data list |

**Fields for Each Object in the Data Array**:

| Parameter Name | Type | Description |
|-------|-----|------|
| coin | String | Currency |
| balance | String | Total balance |
| freeze | String | Frozen amount |
| availableBalance | String | Available balance |
| estimatedTotalAmount | String | Estimated value (USDT) |
| estimatedCynAmount | String | Estimated value (CNY) |
| estimatedAvailableAmount | String | Estimated value of available amount |
| estimatedFreeze | String | Estimated value of frozen amount |
| estimatedCoinType | String | Currency used for estimation |

**Response Format**:
```json
{
   "code": 0,
   "msg": "success",
   "data": [
      {
         "coin": "BTC",
         "balance": "6.002",
         "freeze": "0.001",
         "availableBalance": "6.001",
         "estimatedTotalAmount": "489470.36242",
         "estimatedCynAmount": "3524186.609424",
         "estimatedAvailableAmount": "489388.81121",
         "estimatedFreeze": "81.55121",
         "estimatedCoinType": "USDT"
      }
   ]
}
```

## 5. Market Data Endpoints

### 5.1 Get System Time

**Request Method**: GET  
**Endpoint**: `/spot/v1/p/time`  
**Signature Required**: No  
**API Type**: Public Endpoint

**Description**:
This endpoint is used to obtain the current system timestamp from the trading platform server. It can be used for time synchronization between the client and the server. The server time is critical for API request signature verification, as the timestamp parameter is included in the signature.

**Request Parameters**: No parameters required

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
  "data": 1744356981011
}
```

### 5.2 Get K-Line Data

**Request Method**: GET  
**Endpoint**: `/spot/v1/p/quotation/kline`  
**Signature Required**: No  
**API Type**: Public Endpoint

**Description**:
This endpoint is used to retrieve candlestick (K-line) data for a specified trading pair. It supports multiple time intervals and can be used for technical analysis and trading strategy research.

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|-----|------|
| symbol | String | Yes | Trading pair, e.g., "BTC_USDT" |
| interval | String | Yes | K-line interval: "1m","5m","15m","30m","1h","4h","1d","1w","1M" |
| limit | Integer | No | Number of data points, default 500, maximum 1500 |
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
      "t": 1744354800000,
      "o": "60150.32",
      "h": "60220.45",
      "l": "60100.01",
      "c": "60180.25",
      "a": "12.5431",
      "v": "753247.65"
    }
  ]
}
```

### 5.3 Get 24h Ticker (Single Trading Pair)

**Request Method**: GET  
**Endpoint**: `/spot/v1/p/quotation/trend/ticker`  
**Signature Required**: No  
**API Type**: Public Endpoint

**Description**:
This endpoint provides 24-hour price change statistics for a specified trading pair, including price, volume, change percentage, and other key market data.

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|-----|------|
| symbol | String | Yes | Trading pair, e.g., "BTC_USDT" |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Object | Trading pair market data |

**Data Object Fields**:

| Parameter Name | Type | Description |
|-------|-----|------|
| t | Long | Timestamp |
| s | String | Trading pair |
| c | String | Latest price |
| h | String | 24-hour highest price |
| l | String | 24-hour lowest price |
| a | String | Volume |
| v | String | Turnover |
| o | String | Open price |
| r | String | 24-hour price change percentage |
| tickerTrendVo | Object | Trend data, contains a list of historical price points, may be null |

**tickerTrendVo Object Fields** (when not null):

| Parameter Name | Type | Description |
|-------|-----|------|
| list | Array | List of trend data points |

**Fields for Each Object in the list Array**:

| Parameter Name | Type | Description |
|-------|-----|------|
| symbolId | Long | Trading pair ID |
| symbol | String | Trading pair name |
| price | String | Price |
| time | Long | Timestamp |

**Response Format**:
```json
{
   "code": 0,
   "msg": "success",
   "data": {
      "t": 1745408378184,
      "s": "BTC_USDT",
      "c": "93512.38",
      "h": "94507.96",
      "l": "88482.64",
      "a": "6845.64559",
      "v": "18558027109.7755458",
      "o": "88581.60",
      "r": "0.0556",
      "tickerTrendVo": {
         "list": [
            {"symbolId": 6, "symbol": "BTC_USDT", "price": 88981.13, "time": 1745323200000},
            {"symbolId": 6, "symbol": "BTC_USDT", "price": 90236.17, "time": 1745326800000},
            {"symbolId": 6, "symbol": "BTC_USDT", "price": 93755.31, "time": 1745402400000}
         ]
      }
   }
}
```

### 5.4 Get All Trading Pairs Tickers

**Request Method**: GET  
**Endpoint**: `/spot/v1/p/quotation/tickers`  
**Signature Required**: No  
**API Type**: Public Endpoint

**Description**:
This endpoint provides 24-hour price statistics for all trading pairs on the platform, suitable for scenarios that require monitoring the entire market overview.

**Request Parameters**: No parameters required

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Array | Market data list for all trading pairs |

**Fields for Each Object in the Data Array**:

| Parameter Name | Type | Description |
|-------|-----|------|
| t | Long | Timestamp |
| s | String | Trading pair |
| c | String | Latest price |
| h | String | 24-hour highest price |
| l | String | 24-hour lowest price |
| a | String | Volume |
| v | String | Turnover |
| o | String | Open price |
| r | String | 24-hour price change percentage |

**Response Format**:
```json
{
   "code": 0,
   "msg": "success",
   "data": [
      {
         "t": 1687245871234,
         "s": "BTC_USDT",
         "c": "30500.00",
         "h": "31000.00",
         "l": "29800.00",
         "a": "10.5",
         "v": "315000.50",
         "o": "30000.00",
         "r": "1.67"
      }
   ]
}
```

### 5.5 Get Latest Trade Records

**Request Method**: GET  
**Endpoint**: `/spot/v1/p/quotation/deal`  
**Signature Required**: No  
**API Type**: Public Endpoint

**Description**:
This endpoint provides the latest trade records for a specified trading pair, including trade price, quantity, time, and buy/sell direction, used to understand the latest market trading activity.

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|-----|------|
| symbol | String | Yes | Trading pair, e.g., "BTC_USDT" |
| num | Integer | Yes | Number of records to retrieve, must be greater than or equal to 1 |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Array | Trade record list |

**Fields for Each Object in the Data Array**:

| Field Name | Type | Description |
|-------|-----|------|
| t | Long | Trade timestamp |
| s | String | Trading pair |
| p | String | Trade price |
| a | String | Trade quantity |
| m | String | Trade direction: BID-buy, ASK-sell |

**Response Format**:
```json
{
   "code": 0,
   "msg": "success",
   "data": [
      {
         "t": 1687245871234,
         "s": "BTC_USDT",
         "p": "30500.00",
         "a": "0.01",
         "m": "BID"
      }
   ]
}
```

### 5.6 Get Market Depth

**Request Method**: GET  
**Endpoint**: `/spot/v1/p/quotation/depth`  
**Signature Required**: No  
**API Type**: Public Endpoint

**Description**:
This endpoint provides market depth (order book) data for a specified trading pair, including price and quantity information from both buyers and sellers, used to analyze market supply and demand relationships.

**Request Parameters**:

| Parameter Name | Type | Required | Description |
|-------|-----|-----|------|
| symbol | String | Yes | Trading pair, e.g., "BTC_USDT" |
| level | Integer | Yes | Depth level, range: 1-50 |

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Object | Depth data |

**Data Object Fields**:

| Field Name | Type | Description |
|-------|-----|------|
| t | Long | Timestamp |
| s | String | Trading pair |
| u | Long | Update ID |
| b | Array | Buy orders [price, quantity] |
| a | Array | Sell orders [price, quantity] |

**Response Format**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "t": 1687245871234,
    "s": "BTC_USDT",
    "u": 12345678,
    "b": [
      ["30400.00", "0.5"],
      ["30300.00", "1.2"]
    ],
    "a": [
      ["30500.00", "0.3"],
      ["30600.00", "1.0"]
    ]
  }
}
```

### 5.7 Get All Trading Pair Configuration Information

**Request Method**: GET
**Endpoint**: `/spot/v1/p/symbol/configs`
**Signature Required**: No
**API Type**: Public Endpoint

**Description**:
This endpoint provides configuration information for all trading pairs on the platform, including trading pair names, base assets, quote assets, precision configurations, fee settings, and other important parameters. It is suitable for scenarios that require understanding the detailed configuration of trading pairs.

**Request Parameters**: No parameters required

**Response Parameters**:

| Parameter Name | Type | Description |
|-------|-----|------|
| code | Integer | Status code, 0 indicates success |
| msg | String | Response message |
| data | Array | Trading pair configuration information list |

**Fields for Each Object in the Data Array**:

| Parameter Name | Type | Description |
|-------|-----|------|
| symbol | String | Trading pair name, e.g., "BTC_USDT" |
| baseAsset | String | Base asset, e.g., "BTC" |
| baseAssetPrecision | Integer | Base asset precision (decimal places) |
| quoteAsset | String | Quote asset, e.g., "USDT" |
| quoteAssetPrecision | Integer | Quote asset precision (decimal places) |
| pricePrecision | Integer | Price precision (decimal places) |
| quantityPrecision | Integer | Quantity precision (decimal places) |
| makerFee | String | Maker fee rate |
| takerFee | String | Taker fee rate |

**Response Format**:
```json
{
   "code": 0,
   "msg": "success",
   "data": [
      {
         "symbol": "BTC_USDT",
         "baseAsset": "BTC",
         "baseAssetPrecision": 5,
         "quoteAsset": "USDT",
         "quoteAssetPrecision": 4,
         "pricePrecision": 2,
         "quantityPrecision": 5,
         "makerFee": "0.001",
         "takerFee": "0.001"
      },
      {
         "symbol": "ETH_USDT",
         "baseAsset": "ETH",
         "baseAssetPrecision": 4,
         "quoteAsset": "USDT",
         "quoteAssetPrecision": 4,
         "pricePrecision": 2,
         "quantityPrecision": 4,
         "makerFee": "0.001",
         "takerFee": "0.001"
      }
   ]
}
```

## 6. Error Code Description

| Error Code | Description |
|-------|------|
| -1 | indicates a system message |
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