# HashEx 合约OpenAPI 接入文档

## 1. API 概述
HashEx 合约交易平台 API 提供了程序化交易的能力，允许开发者通过 HTTP 请求创建订单、查询市场数据等功能。

## 2. 服务地址

**API 基础 URL**: `https://open.hashex.vip`

## 3. 认证机制

### 3.1 认证参数

| 请求头 | 说明 |
|-------|------|
| X-Access-Key | 您的 API 访问密钥 |
| X-Signature | 使用秘钥对请求参数生成的签名 |
| X-Request-Timestamp | 请求时间戳（毫秒） |
| X-Request-Nonce | 随机字符串，防止重放攻击 |

### 3.2 签名算法

签名算法流程：

1. 将常规请求参数按键名字母顺序排序（使用TreeMap）
2. 构建参数字符串：`key1=value1&key2=value2&...`
3. 在参数字符串末尾附加时间戳：`key1=value1&key2=value2&...&timestamp=xxx`
   （其中`timestamp`值与请求头`X-Request-Timestamp`相同）
4. 使用HMAC-SHA256算法，以secretKey为密钥对最终参数字符串进行签名
5. 将签名结果转换为十六进制字符串

### 3.3 公开接口

以下接口是公开接口，**不需要任何权限验证**，可以直接访问：

```
/fut/v1/public/time        // 系统时间
/fut/v1/public/symbol/detail // 交易对详情
/fut/v1/public/q/ticker    // 单一交易对24小时行情
/fut/v1/public/q/tickers   // 所有交易对行情
/fut/v1/public/q/kline     // K线数据
/fut/v1/public/q/depth     // 深度信息
/fut/v1/public/q/deal      // 最新成交记录
```

## 4. 市场数据接口

### 4.1 获取服务器时间

**请求方法**: GET  
**接口路径**: `/fut/v1/public/time`  
**是否签名**: 否

**请求参数**:
无需任何请求参数

**响应参数**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Long | 服务器时间戳（毫秒） |

**响应格式**:
```json
{
  "code": 0,
  "msg": "success",
  "data": 1689245871234
}
```

### 4.2 获取交易对详情

**请求方法**: GET
**接口路径**: `/fut/v1/public/symbol/detail`
**是否签名**: 否

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|-----|------|
| symbol | String | 是 | 交易对，例如："btc_usdt" |

**响应参数**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Object | 交易对详情 |

**data对象字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| id | Integer | 交易对ID |
| symbol | String | 交易对名称 |
| contractType | String | 合约类型 |
| underlyingType | String | 标的类型 |
| contractSize | String | 合约大小 |
| initLeverage | Integer | 初始杠杆倍数 |
| initPositionType | String | 初始持仓类型 |
| baseCoin | String | 基础资产 |
| quoteCoin | String | 计价资产 |
| quantityPrecision | Integer | 数量精度 |
| pricePrecision | Integer | 价格精度 |
| supportOrderType | String | 支持的订单类型 |
| supportTimeInForce | String | 支持的有效期类型 |
| minQty | String | 最小委托数量 |
| minNotional | String | 最小委托金额 |
| maxNotional | String | 最大委托金额 |
| makerFee | String | 挂单手续费率 |
| takerFee | String | 吃单手续费率 |
| minStepPrice | String | 最小价格步长 |

**响应格式**:
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

### 4.3 获取单个合约行情信息

**请求方法**: GET  
**接口路径**: `/fut/v1/public/q/ticker`  
**是否签名**: 否

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|-----|------|
| symbol | String | 是 | 交易对，例如："BTC_USDT" |

**响应参数**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Object | 行情数据 |

**data对象字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| s | String | 交易对 |
| t | Long | 时间戳 |
| c | String | 最新价格 |
| o | String | 24小时前价格 |
| h | String | 24小时最高价 |
| l | String | 24小时最低价 |
| a | String | 24小时成交量 |
| v | String | 24小时成交额 |
| r | String | 24小时涨跌幅(%) |
| tickerTrendVo | Object | 价格趋势数据，可能为null |

**响应格式**:
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

### 4.4 获取全部合约行情信息

**请求方法**: GET  
**接口路径**: `/fut/v1/public/q/tickers`  
**是否签名**: 否

**请求参数**:
无需任何请求参数

**响应参数**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Array | 所有交易对行情数据列表 |

**data数组中每个对象的字段**:

| 参数名 | 类型 | 说明         |
|-------|-----|------------|
| s | String | 交易对        |
| t | Long | 时间戳        |
| c | String | 最新价格       |
| o | String | 24小时前价格    |
| h | String | 24小时最高价    |
| l | String | 24小时最低价    |
| a | String | 24小时成交量    |
| v | String | 24小时成交额    |
| r | String | 24小时涨跌幅(%) |
| tickerTrendVo | Object | 价格趋势数据     |

**tickerTrendVo对象字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| list | Array | 价格趋势数据列表 |

**list数组中每个对象的字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| symbolId | Integer | 交易对ID |
| symbol | String | 交易对名称 |
| price | Double/Number | 价格 |
| time | Long | 时间戳 |

**响应格式**:
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

### 4.5 获取合约K线数据

**请求方法**: GET  
**接口路径**: `/fut/v1/public/q/kline`  
**是否签名**: 否

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|-----|------|
| symbol | String | 是 | 交易对，例如："BTC_USDT" |
| interval | String | 是 | K线间隔, 例如："1m","5m","15m","30m","1h","4h","1d","1w","1M" |
| limit | Integer | 否 | 获取数量，默认500，最大1500 |
| startTime | Long | 否 | 起始时间（毫秒时间戳） |
| endTime | Long | 否 | 结束时间（毫秒时间戳） |

**响应参数**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Array | K线数据列表 |

**data数组中每个对象的字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| s | String | 交易对 |
| t | Long | 时间戳 |
| o | String | 开盘价 |
| h | String | 最高价 |
| l | String | 最低价 |
| c | String | 收盘价 |
| a | String | 成交量 |
| v | String | 成交额 |

**响应格式**:
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

### 4.6 获取合约深度数据

**请求方法**: GET  
**接口路径**: `/fut/v1/public/q/depth`  
**是否签名**: 否

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|-----|------|
| symbol | String | 是 | 交易对，例如："BTC_USDT" |
| level | Integer | 是 | 档位（1-50） |

**响应参数**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Object | 深度数据 |

**data对象字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| s | String | 交易对 |
| t | Long | 时间戳 |
| u | Long | 深度更新序列号 |
| b | Array | 买单列表 [价格, 数量] |
| a | Array | 卖单列表 [价格, 数量] |

**响应格式**:
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

### 4.7 获取合约最新成交信息

**请求方法**: GET  
**接口路径**: `/fut/v1/public/q/deal`  
**是否签名**: 否

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|-----|------|
| symbol | String | 是 | 交易对，例如："BTC_USDT" |
| num | Integer | 是 | 获取数量，最小值为1 |

**响应参数**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Array | 成交记录列表 |

**data数组中每个对象的字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| s | String | 交易对 |
| p | String | 成交价格 |
| a | String | 成交数量 |
| t | Long | 成交时间（毫秒时间戳） |
| m | String | 买卖方向（"ASK"为卖出，"BID"为买入） |

**响应格式**:
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

## 5. 私有接口

### 5.1 创建订单

**请求方法**: POST  
**接口路径**: `/fut/v1/order/create`  
**是否签名**: 是

**请求参数**:

| 参数名 | 类型 | 是否必须 | 说明 |
|-------|-----|---------|------|
| symbol | String | 是 | 交易对 |
| orderSide | String | 是 | 买卖方向：BUY;SELL |
| orderType | String | 是 | 订单类型：LIMIT;MARKET |
| positionSide | String | 是 | 仓位方向：LONG;SHORT |
| origQty | String | 是 | 原始数量（实际交易量） |
| price | String | 否 | 价格，市价单不需要传 |
| leverage | Integer | 否 | 杠杆倍数 |
| positionId | Long | 否 | 平仓ID |
| triggerProfitPrice | String | 否 | 止盈价 |
| triggerStopPrice | String | 否 | 止损价 |
| sourceType | Integer | 否 | 来源类型（不传默认 `0`） |

**sourceType 参数说明**:

| 值 | 场景说明 |
|---|---|
| 0 | 普通下单 |
| 1 | 计划委托触发下单 |
| 2 | 止盈止损触发下单 |
| 4 | 反手下单 |

**使用说明**:
- 仅支持上述取值；未传时默认按 `0` 处理。
- `sourceType` 表示订单来源类型（整型）。

**响应参数**:

| 参数名 | 类型 | 说明       |
|-------|-----|----------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息     |
| data | Object | 订单ID     |


**响应格式**:
```json
{
   "code": 0,
   "msg": "success",
   "data": "481556897766682496"
}
```

### 5.2 查询订单详情

**请求方法**: GET  
**接口路径**: `/fut/v1/order/detail`  
**是否签名**: 是

**请求参数**:

| 参数名 | 类型 | 是否必须 | 说明 |
|-------|-----|---------|------|
| orderId | Long | 是 | 订单ID |

**响应参数**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Object | 订单详情 |

**data对象字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| orderId | String | 订单ID |
| symbol | String | 交易对名称 |
| contractType | String | 合约类型 |
| orderType | String | 订单类型 |
| orderSide | String | 买卖方向 |
| leverage | Integer | 杠杆倍数 |
| positionSide | String | 仓位方向 |
| closePosition | Boolean | 是否平仓 |
| price | String | 价格 |
| origQty | String | 原始数量 |
| avgPrice | String | 成交均价 |
| executedQty | String | 已成交数量 |
| marginFrozen | String | 冻结保证金 |
| triggerProfitPrice | String | 止盈价格 |
| triggerStopPrice | String | 止损价格 |
| sourceId | String | 来源ID |
| forceClose | Boolean | 是否强平 |
| tradeFee | String | 交易费用 |
| closeProfit | String | 平仓盈亏 |
| state | String | 订单状态 |
| createdTime | Long | 创建时间 |
| updatedTime | Long | 更新时间 |

**响应格式**:
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

### 5.3 查询订单列表

**请求方法**: GET  
**接口路径**: `/fut/v1/order/list`  
**是否签名**: 是

**请求参数**:

| 参数名 | 类型 | 是否必须 | 说明 |
|-------|-----|---------|------|
| symbol | String | 否 | 交易对 |
| state | String | 否 | 订单状态筛选：UNFINISHED(未完成)、OPEN(未完成别名)、HISTORY(历史)、NEW(新建)、FILLED(全部成交)、CANCELED(已撤销) |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页大小，默认10 |
| startTime | Long | 否 | 开始时间 |
| endTime | Long | 否 | 结束时间 |
| contractType | String | 否 | 合约类型 |
| forceClose | Boolean | 否 | 是否强平 |

**state 参数说明**:
- `UNFINISHED` 与 `OPEN` 均表示“未完成订单”聚合筛选。
- `HISTORY` 表示“历史订单”聚合筛选。
- 其余值为精确状态筛选（`NEW`、`FILLED`、`CANCELED`）。
- 状态值区分大小写，建议严格使用上述大写状态字符串。
- 响应中的 `state` 在特定场景可能出现 `PARTIALLY_CANCELED`（仅返回值），该值不作为查询入参。

**响应参数**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Object | 分页数据 |

**data对象字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| page | Integer | 当前页码 |
| ps | Integer | 每页大小 |
| total | Long | 总记录数 |
| items | Array | 订单列表 |

**items数组中每个对象的字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| orderId | String | 订单ID |
| symbol | String | 交易对 |
| contractType | String | 合约类型 |
| orderType | String | 订单类型 |
| orderSide | String | 买卖方向 |
| leverage | Integer | 杠杆倍数 |
| positionSide | String | 仓位方向 |
| closePosition | Boolean | 是否平仓 |
| price | String | 价格 |
| origQty | String | 原始数量 |
| avgPrice | String | 成交均价 |
| executedQty | String | 已成交数量 |
| marginFrozen | String | 冻结保证金 |
| triggerProfitPrice | String | 止盈价 |
| triggerStopPrice | String | 止损价 |
| sourceId | String | 来源ID |
| forceClose | Boolean | 是否强平 |
| tradeFee | String | 交易费 |
| closeProfit | String | 平仓盈亏 |
| state | String | 订单状态 |
| createdTime | Long | 创建时间 |
| updatedTime | Long | 更新时间 |

**响应格式**:
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

### 5.4 查询订单成交明细

**请求方法**: GET  
**接口路径**: `/fut/v1/order/trade-list`  
**是否签名**: 是

**请求参数**:

| 参数名 | 类型 | 是否必须 | 说明        |
|-------|-----|---------|-----------|
| symbol | String | 否 | 交易对       |
| orderId | String | 否 | 订单ID      |
| orderSide | String | 否 | 订单方向：BUY 或 SELL |
| startTime | Long | 否 | 开始时间      |
| endTime | Long | 否 | 结束时间      |
| page | Integer | 否 | 页码，默认1    |
| size | Integer | 否 | 每页大小，默认10 |

**响应参数**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Object | 分页数据 |

**data对象字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| page | Integer | 当前页码 |
| ps | Integer | 每页大小 |
| total | Long | 总记录数 |
| items | Array | 成交明细列表 |

**items数组中每个对象的字段**:

| 参数名 | 类型 | 说明    |
|-------|-----|-------|
| orderId | String | 订单ID  |
| execId | String | 成交ID  |
| symbol | String | 交易对   |
| orderSide | String | 订单方向 |
| positionSide | String | 持仓方向 |
| quantity | String | 成交数量  |
| price | String | 成交价格  |
| fee | String | 手续费   |
| feeCoin | String | 手续费币种 |
| timestamp | Long | 成交时间戳 |

**响应格式**:
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

### 5.5 查询用户资金

**请求方法**: GET  
**接口路径**: `/fut/v1/balance/list`  
**是否签名**: 是

**请求参数**:

| 参数名 | 类型 | 是否必须 | 说明 |
|-------|-----|---------|------|
| balanceType | String | 否 | 资金类型，不传则查询所有类型 |

**响应参数**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Array | 资金信息列表 |

**data数组中每个对象的字段**:

| 参数名 | 类型 | 说明     |
|-------|-----|--------|
| coin | String | 币种     |
| balanceType | String | 资金类型   |
| walletBalance | String | 钱包余额   |
| availableBalance | String | 可用余额   |
| openOrderMarginFrozen | String | 委托冻结金额 |
| isolatedMargin | String | 逐仓保证金  |
| crossedMargin | String | 全仓保证金  |
| bonus | String | 赠送金额   |

**响应格式**:
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

### 5.6 获取持仓列表

**请求方法**: GET
**接口路径**: `/fut/v1/position/list`
**是否签名**: 是

**请求参数**:

| 参数名 | 类型 | 是否必须 | 说明 |
|-------|-----|---------|------|
| symbol | String | 否 | 交易对，例如："btc_usdt" |
| contractType | String | 否 | 合约类型：PERPETUAL(永续) |
| balanceType | String | 否 | 余额类型：CONTRACT(合约)、COPY(跟单) |

**参数说明**:
- `contractType` 当前仅支持 `PERPETUAL`。
- `balanceType` 支持 `CONTRACT`(合约账户) 和 `COPY`(跟单账户)，默认为合约账户。

**响应参数**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Array | 持仓信息列表 |

**data数组中每个对象的字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| symbol | String | 交易对 |
| positionId | String | 持仓ID |
| contractType | String | 合约类型：PERPETUAL(永续) |
| balanceType | String | 余额类型：CONTRACT(合约)、COPY(跟单) |
| positionType | String | 仓位类型：CROSSED(全仓)、ISOLATED(逐仓) |
| positionSide | String | 持仓方向：LONG(多)、SHORT(空) |
| positionModel | String | 持仓模式 |
| underlyingType | String | 标的类型：U_BASED(U本位)、COIN_BASED(币本位) |
| positionSize | String | 持仓数量（张） |
| closeOrderSize | String | 平仓挂单数量（张） |
| availableCloseSize | String | 可平仓数量（张） |
| entryPrice | String | 开仓均价 |
| isolatedMargin | String | 逐仓保证金 |
| bounsMargin | String | 体验金占用比例 |
| openOrderMarginFrozen | String | 开仓订单保证金占用 |
| realizedProfit | String | 已实现盈亏 |
| autoMargin | Boolean | 是否自动追加保证金 |
| leverage | Integer | 杠杆倍数 |
| isLeaderPosition | Boolean | 是否是带单仓位 |
| updatedTime | Long | 更新时间 |
| unsettledProfit | String | 未结算盈亏 |
| profit | Object | 止盈止损信息，可能为null |

**profit对象字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| profitId | String | 止盈止损ID |
| profitPrice | String | 止盈价 |
| stopPrice | String | 止损价 |
| triggerPriceType | String | 触发价格类型：MARK_PRICE(标记价格)、LATEST_PRICE(最新价格) |

**说明**:
- `isLeaderPosition` 为 `true` 时表示该仓位由带单策略创建。
- `profit` 对象仅在设置了止盈止损时返回，未配置时返回 `null`。

**响应格式**:
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

### 5.7 修改仓位类型

**请求方法**: POST  
**接口路径**: `/fut/v1/position/change-type`  
**是否签名**: 是

**请求参数**:

| 参数名 | 类型 | 是否必须 | 说明 |
|-------|-----|---------|------|
| symbol | String | 是 | 交易对，例如："btc_usdt" |
| positionType | String | 是 | 仓位类型：`CROSSED`(全仓)、`ISOLATED`(逐仓) |
| positionModel | String | 是 | 仓位模式：`AGGREGATION`(合仓)、`DISAGGREGATION`(分仓) |


**响应格式**:
```json
{
  "code": 0,
  "msg": "success"
}
```

**常见业务响应示例（存在持仓时）**:
```json
{
  "code": -1,
  "msg": "There are positions currently, and the margin mode cannot be switched",
  "data": null
}
```

### 5.8 调整杠杆倍数

**请求方法**: POST  
**接口路径**: `/fut/v1/position/adjust-leverage`  
**是否签名**: 是

**请求参数**:

| 参数名 | 类型 | 是否必须 | 说明 |
|-------|-----|---------|------|
| symbol | String | 是 | 交易对，例如："btc_usdt" |
| leverage | Integer | 是 | 新的杠杆倍数 |
| positionSide | String | 否 | 指定仓位方向：`LONG` 或 `SHORT`。单向持仓可不传，双向持仓建议显式传值 |

**说明**:
- 建议根据当前持仓与风险策略设置杠杆；若无明确策略，可从较低杠杆开始。

**响应格式**:
```json
{
  "code": 0,
  "msg": "success"
}
```

### 5.9 一键平仓

**请求方法**: POST  
**接口路径**: `/fut/v1/position/close-all`  
**是否签名**: 是

**请求参数**:

| 参数名 | 类型 | 是否必须 | 说明 |
|-------|-----|---------|------|
| symbol | String | 否 | 交易对，例如："btc_usdt"。不传表示平掉所有合约持仓 |
| contractType | String | 否 | 合约类型，示例：`PERPETUAL` |

> ⚠️ 操作会触发实际平仓，请谨慎调用。
>
> 示例参数仅用于演示，请按实际业务场景替换。

**响应格式**:
```json
{
  "code": 0,
  "msg": "success",
  "data": true
}
```

### 5.10 调整逐仓保证金

**请求方法**: POST  
**接口路径**: `/fut/v1/position/margin`  
**是否签名**: 是

**请求参数**:

| 参数名 | 类型 | 是否必须 | 说明 |
|-------|-----|---------|------|
| symbol | String | 是 | 交易对，例如："btc_usdt" |
| positionSide | String | 是 | 仓位方向：`LONG` 或 `SHORT` |
| positionId | String | 是 | 持仓ID，可通过持仓列表获取 |
| margin | String | 是 | 调整数量，支持小数 |
| type | String | 是 | 调整方向：`ADD`(增加逐仓保证金)、`SUB`(减少逐仓保证金) |

**说明**:
- 仅逐仓 (`ISOLATED`) 仓位支持保证金调整；若传入全仓仓位，会返回 `code = -1` 并提示无法调整。
- 建议先小额验证，再按实际风控策略调整保证金。

**响应格式**:
```json
{
  "code": 0,
  "msg": "success"
}
```

**常见业务失败示例（非逐仓仓位）**:
```json
{
  "code": -1,
  "msg": "Only isolated position supports margin adjustment",
  "data": null
}
```

### 5.11 撤销订单

**请求方法**: POST
**接口路径**: `/fut/v1/order/cancel`
**是否签名**: 是

**请求参数**:

| 参数名 | 类型 | 是否必须 | 说明 |
|-------|-----|---------|------|
| orderId | Long | 是 | 订单ID |

**响应参数**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Object | 撤单结果 |

**响应格式**:
```json
{
   "code": 0,
   "msg": "success",
   "data": null
}
```

### 5.12 批量撤销订单

**请求方法**: POST
**接口路径**: `/fut/v1/order/cancel-batch`
**是否签名**: 是

**请求参数**:

| 参数名 | 类型 | 是否必须 | 说明 |
|-------|-----|---------|------|
| orderIds | String | 是 | 订单ID列表，JSON数组格式，例如："[123,456,789]" |

**响应参数**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Object | 批量撤单结果 |

**响应格式**:
```json
{
   "code": 0,
   "msg": "success",
   "data": null
}
```

## 6. 错误码说明

| 错误码 | 说明 |
|-------|------|
| -1 | 通用错误 |
| 0 | 成功 |
| 1001 | 签名错误 |
| 1002 | 签名过期 |
| 1003 | 签名为空 |
| 1004 | 签名参数为空 |
| 1005 | 签名参数错误 |
| 1006 | 签名时间为空 |
| 1007 | 签名时间错误 |
| 1008 | 签名时间过期 |
| 1009 | 签名时间格式错误 |
| 1010 | nonce重复提交 |
| 1011 | 无效的签名信息 |
| 1012 | 无效的请求来源IP |
| 1013 | 无效的访问URL |
