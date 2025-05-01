# MGBX OpenAPI 接入文档

## 1. API 概述

MGBX 交易平台 API 提供了程序化交易的能力，允许开发者通过 HTTP 请求创建订单、查询市场数据等功能。

## 2. 服务地址

**API 基础 URL**: `https://open.mgbx.com`

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
4. 使用HMAC-SHA256算法，以secretKey为密钥对最终参数字符串进行签名，编码使用 UTF-8
5. 将签名结果转换为十六进制字符串
6. 参数内容形式为application/x-www-form-urlencoded的 query string中发送

### 3.3 公开接口

以下接口是公开接口，**不需要任何权限验证**，可以直接访问：

```
/spot/v1/p/quotation/kline    // K线数据
/spot/v1/p/quotation/ticker   // 单一交易对24小时行情
/spot/v1/p/quotation/tickers  // 所有交易对行情
/spot/v1/p/quotation/depth    // 深度信息
/spot/v1/p/quotation/deal     // 最新成交记录
/spot/v1/p/time               // 系统时间
```

## 4. 交易接口

### 4.1 创建订单

**请求方法**: POST  
**接口路径**: `/spot/v1/u/trade/order/create`
**是否签名**: 是

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|-----|------|
| symbol | String | 是 | 交易对，如 "BTC_USDT" |
| direction | String | 是 | 交易方向："BUY"买入，"SELL"卖出 |
| tradeType | String | 是 | 订单类型："LIMIT"限价单，"MARKET"市价单 |
| totalAmount | String | 是 | 委托数量 |
| price | String | 条件必填 | 委托价格，限价单必填 |
| clientOrderId | String | 否 | 客户自定义订单ID |

**响应参数**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | String | 订单ID |

**响应格式**:
```json
{
  "code": 0,
  "msg": "success",
  "data": "123456789012345678"
}
```
**订单创建注意事项**:

1. 订单创建功能为异步处理，返回的订单ID可能在稍后时间才会在系统中可见。

### 4.2 查询订单详情

**请求方法**: GET  
**接口路径**: `/spot/v1/u/trade/order/detail`
**是否签名**: 是

**请求参数**:

| 参数名 | 类型 | 必填 | 说明                           |
|-------|-----|-----|------------------------------|
| orderId | Long | 是 | 订单ID,用户创建订单时返回的 ID 或者查询到的 ID |

**响应参数**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Object | 订单详情 |

**data对象字段**:

| 参数名 | 类型 | 说明                            |
|-------|-----|-------------------------------|
| orderId | String | 订单ID                          |
| clientOrderId | String | 客户端订单ID                       |
| symbol | String | 交易对                           |
| orderType | String | 订单类型：LIMIT或MARKET             |
| orderSide | String | 买卖方向：BUY或SELL                 |
| balanceType | Integer | 账户类型：1.现货账户，2.杠杆账户            |
| timeInForce | String | 订单有效方式，如GTC                   |
| price | String | 委托价格                          |
| origQty | String | 原始委托数量                        |
| avgPrice | String | 平均成交价                         |
| executedQty | String | 已成交数量                         |
| marginFrozen | String | 冻结保证金                         |
| state | String | 订单状态，详见下表                     |
| createdTime | Long | 创建时间戳                         |
| sourceId | String | 订单来源ID，可能为null                |
| forceClose | String | 是否强平标志，true-是，false-否，可能为null |

**订单状态说明**:

| 状态代码 | 状态说明 |
|---------|---------|
| `NEW` | 新建订单（未成交） |
| `PARTIALLY_FILLED` | 部分成交 |
| `PARTIALLY_CANCELED` | 部分撤销 |
| `FILLED` | 全部成交 |
| `CANCELED` | 已撤销 |
| `REJECTED` | 下单失败 |
| `EXPIRED` | 已过期 |

**响应格式**:
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

### 4.3 查询未完成订单列表

**请求方法**: `GET`  
**接口路径**: `/spot/v1/u/trade/order/list`  
**是否签名**: 是

**请求参数**:

| 参数名 | 类型 | 必填 | 说明                                                                |
|-------|-----|-----|-------------------------------------------------------------------|
| symbol | String | 否 | 交易对，如"BTC_USDT"                                                   |
| clientOrderId | String | 否 | 客户自定义订单ID                                                         |
| startTime | Long | 否 | 查询开始时间戳(毫秒)                                                       |
| endTime | Long | 否 | 查询结束时间戳(毫秒)                                                       | 
| state | Integer | 否 | 订单状态筛选：1-新建订单(未成交)、2-部分成交、3-全部成交、4-已撤销、5-下单失败、6-已过期、9-未完成、10-历史订单 |
| page | Integer | 否 | 页码，从1开始，默认1                                                       |
| size | Integer | 否 | 每页条数，最大100，默认10                                                   |

**响应参数**:  

| 参数名  | 类型    | 说明        |
|--------|---------|-------------|
| code   | Integer | 状态码，0表示成功 |
| msg    | String  | 响应信息      |
| data   | Object  | 分页结果      |

**data对象字段**:  

| 参数名      | 类型    | 说明           |
|-------------|---------|----------------|
| page        | Integer | 当前页码         |
| ps          | Integer | 每页条数         |
| total       | Long    | 总记录数         |
| items       | Array   | 订单列表         |

**items数组中每个对象的字段**:  

| 参数名       | 类型    | 说明                    |
|-------------|---------|-----------------------|
| orderId     | String  | 订单ID                  |
| clientOrderId | String  | 客户端订单ID               |
| symbol      | String  | 交易对                   |
| orderType   | String  | 订单类型: LIMIT or MARKET |
| orderSide   | String  | 买卖方向: BUY或SELL        |
| balanceType | Integer | 账户类型                  |
| timeInForce | String  | 订单有效方式，如GTC           |
| price       | String  | 委托价格                  |
| origQty     | String  | 原始委托数量                |
| avgPrice    | String  | 平均成交价                 |
| executedQty | String  | 已成交数量                 |
| marginFrozen | String | 冻结保证金                 |
| sourceId | String | 订单来源ID，可能为null        |
| forceClose | String | 是否强平标志，可能为null        |
| state       | String  | 订单状态参考订单详情接口          |
| createdTime | Long    | 创建时间戳                 |

**响应示例**:
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

### 4.4 查询历史订单

**请求方法**: GET  
**接口路径**: `/spot/v1/u/trade/order/history`  
**是否签名**: 是

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|-----|------|
| symbol | String | 否 | 交易对，如 "BTC_USDT" |
| startTime | Long | 否 | 查询起始时间（毫秒时间戳） |
| endTime | Long | 否 | 查询结束时间（毫秒时间戳） |
| balanceType | Integer | 否 | 账户类型：1.现货账户(默认)，2.杠杆账户 |
| id | Long | 否 | 分页标识，来自上一次请求结果 |
| direction | String | 否 | 翻页方向："NEXT"(下一页)，"PREV"(上一页) |
| limit | Integer | 否 | 每页记录数，默认值根据系统配置 |

**响应参数**:  

| 参数名 | 类型    | 说明           |
|-------|---------|----------------|
| code  | Integer | 状态码，0表示成功 |
| msg   | String  | 响应信息         |
| data  | Object  | 分页结果         |

**data对象字段**:  

| 参数名  | 类型   | 说明          |
|--------|--------|---------------|
| hasPrev | Boolean | 是否有上一页   |
| hasNext | Boolean | 是否有下一页   |
| items   | Array   | 历史订单列表   |

**items数组中每个对象的字段**:  

| 参数名       | 类型   | 说明                 |
|-------------|--------|--------------------|
| orderId     | String | 订单ID               |
| clientOrderId | String | 客户端订单ID            |
| symbol      | String | 交易对                |
| orderType   | String | 订单类型: LIMIT或MARKET |
| orderSide   | String | 买卖方向: BUY或SELL     |
| balanceType | Integer | 账户类型               |
| timeInForce | String  | 订单有效方式，如GTC        |
| price       | String | 委托价格               |
| origQty     | String | 原始委托数量             |
| avgPrice    | String | 平均成交价              |
| executedQty | String | 已成交数量              |
| marginFrozen | String | 冻结保证金              |
| sourceId    | String | 订单来源ID，可为空         |
| forceClose  | String | 是否强平标志，可为空         |
| state       | String | 订单状态参考订单详情接口       |
| createdTime | Long   | 创建时间戳              |

**响应示例**:
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

### 4.5 查询订单成交明细

**请求方法**: GET  
**接口路径**: `/spot/v1/u/trade/order/deal`
**是否签名**: 是

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|-----|------|
| orderId | Long | 否 | 订单ID |
| symbol | String | 否 | 交易对，如 "BTC_USDT" |
| page | Integer | 否 | 页码，从1开始 |
| size | Integer | 否 | 每页条数 |

**响应参数**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Object | 分页结果 |

**data对象字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| page | Integer | 当前页码 |
| ps | Integer | 每页条数 |
| total | Long | 总记录数 |
| items | Array | 成交记录列表 |

**items数组中每个对象的字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| orderId | String | 订单ID |
| execId | String | 成交ID |
| symbol | String | 交易对 |
| quantity | String | 成交数量 |
| price | String | 成交价格 |
| fee | String | 手续费 |
| orderSide | String | 买卖方向：BUY或SELL |
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

## 4.6 批量创建订单

**请求方法**: POST  
**接口路径**: `/spot/v1/u/trade/order/batch/create`
**是否签名**: 是

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|-----|------|
| ordersJsonStr | String | 是 | 订单列表的JSON字符串 |

**ordersJsonStr包含的订单对象字段**:

| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|-----|------|
| symbol | String | 是 | 交易对，如 "BTC_USDT" |
| direction | String | 是 | 交易方向："BUY"买入，"SELL"卖出 |
| tradeType | String | 是 | 订单类型："LIMIT"限价单，"MARKET"市价单 |
| totalAmount | String | 是 | 委托数量 |
| price | String | 条件必填 | 委托价格，限价单必填 |
| clientOrderId | String | 否 | 客户自定义订单ID |

**请求示例**:
```json
{
  "ordersJsonStr": "[{\"symbol\":\"BTC_USDT\",\"direction\":\"BUY\",\"tradeType\":\"LIMIT\",\"totalAmount\":\"0.001\",\"price\":\"80000\",\"clientOrderId\":\"BATCH_LIMIT_BUY_123456789\"},{\"symbol\":\"BTC_USDT\",\"direction\":\"SELL\",\"tradeType\":\"LIMIT\",\"totalAmount\":\"0.001\",\"price\":\"90000\",\"clientOrderId\":\"BATCH_LIMIT_SELL_123456789\"}]"
}
```

**响应参数**:
| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Array | 每个订单的创建结果 |

**data数组中每个对象的字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 订单创建状态码，0表示成功 |
| msg | String | 订单创建响应信息 |
| data | String | 订单ID |

**响应示例**:
```json
{
"code": 0,
"msg": "success",
"data": [
{"code": 0, "msg": "success", "data": "484636297488037056"},
{"code": 0, "msg": "success", "data": "484636297496425664"}
]
}
```

**批量创建订单接口注意事项**:

1. 支持不同交易对的订单一次性提交。
2. 批量操作遵循"尽力而为"原则，部分订单失败不会影响其他订单处理
3. 响应仅包含成功和失败的订单数量，具体订单状态需要通过订单查询接口获取
4. 撤单接口每次最多支持20个订单，超过限制会返回错误

## 4.7 批量撤销订单

**请求方法**: POST  
**接口路径**: `/spot/v1/u/trade/order/batch/cancel`
**是否签名**: 是

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|-----|------|
| orderIdsJson | String | 是 | 订单ID列表的JSON字符串 |

**请求示例**:
```json
{
  "orderIdsJson": "[\"475534805480815680\",\"475534266177207360\"]"
}
```

**响应参数**:

| 参数名 | 类型 | 说明        |
|-------|-----|-----------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息      |
| data | Boolean | 撤销是否成功信息  |

**响应示例**:
```json
{"code":0,"msg":"success","data":[{"code":0,"msg":"success","data":"477292986250590464"},{"code":0,"msg":"success","data":"477292986254784768"}]}
```

**批量撤销订单注意事项**:

1. 批量撤单接口每次最多支持撤销20个订单，超过限制会返回错误
2. 批量撤单操作为异步处理，返回成功仅表示请求已受理
3. 部分订单可能因已成交或已撤销等原因无法被撤销，不影响其他订单的撤销
4. 撤单后建议通过订单查询接口确认所有订单的最终状态
5. 高频交易场景下，建议合理安排撤单请求间隔，避免频繁调用

# 4.8 撤销订单

## 接口信息
- **接口路径**: `/spot/v1/u/trade/order/cancel`
- **请求方法**: `POST`
- **是否签名**: 是
- **接口类型**: 私有接口

## 接口说明
此接口用于撤销指定的未成交或部分成交订单。订单一旦撤销成功，剩余未成交部分将不再继续执行。

## 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|-----|------|
| orderId | String | 是 | 要撤销的订单ID |

## 响应参数

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | String | 被撤销的订单ID |

## 响应示例
```json
{
  "code": 0,
  "msg": "success",
  "data": "480910202607429120"
}
```

## 4.9 查询现货账户余额

### 接口信息
- **接口路径**: `/spot/v1/u/balance/spot`
- **请求方法**: `GET`
- **是否签名**: 是
- **接口类型**: 私有接口

### 接口说明
此接口用于查询用户现货账户中所有币种或指定币种的余额信息，包含可用余额、冻结余额及估值等数据。

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|-----|------|
| coin | String | 否 | 币种名称，不填则返回所有币种余额 |

### 响应参数

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Array | 余额数据列表 |

**data数组中每个对象的字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| coin | String | 币种 |
| balance | String | 总余额 |
| freeze | String | 冻结金额 |
| availableBalance | String | 可用余额 |
| estimatedTotalAmount | String | 估值(USDT) |
| estimatedCynAmount | String | 估值(CNY) |
| estimatedAvailableAmount | String | 可用金额估值 |
| estimatedFreeze | String | 冻结金额估值 |
| estimatedCoinType | String | 估值使用的币种 |

### 响应示例
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

## 5. 市场数据接口

### 5.1 获取系统时间接口

## 接口信息
- **接口路径**: `/spot/v1/p/time`
- **请求方法**: `GET`
- **是否签名**: 否
- **接口类型**: 公开接口

## 接口说明
此接口用于获取交易平台服务器的当前系统时间戳，可用于客户端与服务器时间同步。服务器时间对于API请求签名验证至关重要，因为签名中包含时间戳参数。

## 请求参数
无需任何请求参数

## 响应数据

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

# 5.2 获取K线数据

## 接口信息
- **接口路径**: `/spot/v1/p/quotation/kline`
- **请求方法**: `GET`
- **是否签名**: 否
- **接口类型**: 公开接口

## 接口说明
此接口用于获取指定交易对的K线(蜡烛图)数据，支持多种时间周期，可用于技术分析和交易策略研究。

## 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|-----|------|
| symbol | String | 是 | 交易对，如 "BTC_USDT" |
| interval | String | 是 | K线间隔:"1m","5m","15m","30m","1h","4h","1d","1w","1M" |
| limit | Integer | 否 | 获取数量，默认500，最大1500 |
| startTime | Long | 否 | 起始时间（毫秒时间戳） |
| endTime | Long | 否 | 结束时间（毫秒时间戳） |

## 响应数据

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
      "o": "30000.00",
      "h": "31000.00",
      "l": "29800.00",
      "c": "30500.00",
      "a": "10.5",
      "v": "315000.50"
    }
  ]
}
```
### 5.3 获取24小时行情(单一交易对)

### 接口信息
- **接口路径**: `/spot/v1/p/quotation/trend/ticker`
- **请求方法**: `GET`
- **是否签名**: 否
- **接口类型**: 公开接口

### 接口说明
此接口提供指定交易对的24小时价格变动统计信息，包括价格、成交量、涨跌幅等关键市场数据。

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|-----|------|
| symbol | String | 是 | 交易对，如 "BTC_USDT" |

### 响应数据

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Object | 交易对行情数据 |

**data对象字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| t | Long | 时间戳 |
| s | String | 交易对 |
| c | String | 最新价格 |
| h | String | 24小时最高价 |
| l | String | 24小时最低价 |
| a | String | 成交量 |
| v | String | 成交额 |
| o | String | 开盘价 |
| r | String | 24小时涨跌幅（百分比） |
| tickerTrendVo | Object | 趋势数据，包含历史价格点列表，可能为null |

**tickerTrendVo对象字段** (当不为null时):

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| list | Array | 趋势数据点列表 |

**list数组中每个对象的字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| symbolId | Long | 交易对ID |
| symbol | String | 交易对名称 |
| price | String | 价格 |
| time | Long | 时间戳 |

**响应格式**:
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

# 5.4 获取所有交易对行情

## 接口信息
- **接口路径**: `/spot/v1/p/quotation/tickers`
- **请求方法**: `GET`
- **是否签名**: 否
- **接口类型**: 公开接口

## 接口说明
此接口提供平台上所有交易对的24小时价格统计信息，适用于需要监控整个市场概况的场景。

## 请求参数
无需任何请求参数

## 响应数据

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | Array | 所有交易对行情数据列表 |

**data数组中每个对象的字段**:

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| t | Long | 时间戳 |
| s | String | 交易对 |
| c | String | 最新价格 |
| h | String | 24小时最高价 |
| l | String | 24小时最低价 |
| a | String | 成交量 |
| v | String | 成交额 |
| o | String | 开盘价 |
| r | String | 24小时涨跌幅（百分比） |

**响应格式**:
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

# 5.5 获取最新成交记录

## 接口信息
- **接口路径**: `/spot/v1/p/quotation/deal`
- **请求方法**: `GET`
- **是否签名**: 否
- **接口类型**: 公开接口

## 接口说明
此接口提供指定交易对的最新成交记录，包括成交价格、数量、时间和买卖方向等信息，用于了解市场最新交易活动。

## 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|-----|------|
| symbol | String | 是 | 交易对，如 "BTC_USDT" |
| num | Integer | 是 | 获取数量，必须大于等于1 |

## 响应数据

| 参数名  | 类型 | 说明 |
|------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg  | String | 响应信息 |
| data | Array | 成交记录列表 |

**data数组中每个对象的字段**:

| 字段名 | 类型 | 说明 |
|-------|-----|------|
| t | Long | 成交时间戳 |
| s | String | 交易对 |
| p | String | 成交价格 |
| a | String | 成交数量 |
| m | String | 成交方向：BID-买入，ASK-卖出 |

**响应格式**:
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

# 5.6 获取市场深度

## 接口信息
- **接口路径**: `/spot/v1/p/quotation/depth`
- **请求方法**: `GET`
- **是否签名**: 否
- **接口类型**: 公开接口

## 接口说明
此接口提供指定交易对的市场深度（订单簿）数据，包括买卖双方的价格和数量信息，用于分析市场供需关系。

## 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|-------|-----|-----|------|
| symbol | String | 是 | 交易对，如 "BTC_USDT" |
| level | Integer | 是 | 档位，范围：1-50 |

## 响应数据

| 参数名  | 类型 | 说明 |
|------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg  | String | 响应信息 |
| data | Object | 深度数据 |

| 字段名 | 类型 | 说明 |
|-------|-----|------|
| t | Long | 时间戳 |
| s | String | 交易对 |
| u | Long | 更新ID |
| b | Array | 买盘 [价格, 数量] |
| a | Array | 卖盘 [价格, 数量] |

**响应格式**:
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