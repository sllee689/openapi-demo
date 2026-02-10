# HashEx 合约WebSocket API接入文档

## 1. WebSocket 概述

HashEx交易平台提供合约WebSocket接口，支持实时订阅行情数据和用户数据，相比REST API具有更低的延迟和更高的效率。WebSocket连接不需要认证即可订阅市场数据，但订阅用户私有数据时需要提供认证listenKey。

## 2. 服务地址

- 市场数据WebSocket基础URL: `wss://open.hashex.vip/fut/v1/ws/market`
- 用户数据WebSocket基础URL: `wss://open.hashex.vip/fut/v1/ws/user`
- 获取用户listenKey URL: `https://open.hashex.vip/fut/v1/user/listen-key`

## 3. 认证机制

### 3.1 用户数据认证流程

订阅用户私有数据需要先获取listenKey，然后在订阅消息中提供该listenKey：

1. 调用 `/fut/v1/user/listen-key` 接口获取WebSocket认证listenKey
2. 在订阅用户数据时将获取的listenKey添加到订阅消息中

### 3.2 获取认证listenKey

**接口信息:**
- **路径**: `/fut/v1/user/listen-key`
- **方法**: `GET`
- **是否签名**: 是

**请求头:**

| 请求头 | 说明 |
|-------|------|
| `X-Access-Key` | 您的API访问密钥 |
| `X-Signature` | 使用秘钥对请求参数生成的签名 |
| `X-Request-Timestamp` | 请求时间戳（毫秒） |
| `X-Request-Nonce` | 随机字符串，防止重放攻击 |

**签名算法:**

签名算法流程：

1. 将常规请求参数按键名字母顺序排序（使用TreeMap）
2. 构建参数字符串：`key1=value1&key2=value2&...`
3. 在参数字符串末尾附加时间戳：`key1=value1&key2=value2&...&timestamp=xxx`  
   （其中`timestamp`值与请求头`X-Request-Timestamp`相同）
4. 使用HMAC-SHA256算法，以secretKey为密钥对最终参数字符串进行签名
5. 将签名结果转换为十六进制字符串

**响应数据:**

| 参数名 | 类型 | 说明 |
|-------|-----|------|
| code | Integer | 状态码，0表示成功 |
| msg | String | 响应信息 |
| data | String | WebSocket认证listenKey |

## 4. 心跳机制

- 客户端需要每25秒发送一次`ping`消息
- 服务端会回复`pong`消息
- 如果服务端超过30秒未收到心跳，将断开连接

## 5. 订阅类型

### 5.1 市场数据订阅

#### 5.1.1 交易对行情订阅

**无需API权限，公共接口**

**请求格式**:
```json
{
  "req": "sub_symbol",
  "symbol": "btc_usdt"
}
```

**响应数据类型**:

1. 成交推送 (push.deal):

字段说明:
- `s`: 交易对
- `p`: 成交价
- `a`: 成交量
- `m`: 买卖方向，ASK=卖，BID=买
- `t`: 时间戳

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

2. 深度推送 (push.deep):

字段说明:
- `s`: 交易对
- `id`: 更新ID
- `ba`: 买卖方向，1买/2卖
- `p`: 价格
- `q`: 数量
- `t`: 时间戳

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

3. 全量深度推送 (push.deep.full):

字段说明:
- `s`: 交易对
- `id`: 更新ID
- `a`: 卖单数组 [价格, 数量]
- `b`: 买单数组 [价格, 数量]

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

#### 5.1.2 标记价格订阅

**请求格式**:
```json
{
  "req": "sub_mark_price"
}
```

**响应格式**:

字段说明:
- `s`: 交易对
- `p`: 标记价格
- `t`: 时间戳

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

#### 5.1.3 行情订阅

**请求格式**:
```json
{
  "req": "sub_ticker"
}
```

**响应格式**:

字段说明:
- `s`: 交易对
- `o`: 开盘价
- `c`: 收盘价
- `h`: 最高价
- `l`: 最低价
- `a`: 成交量
- `v`: 成交额
- `r`: 涨跌幅
- `t`: 时间戳

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

#### 5.1.4 指数价格推送（可能随行情订阅下发）

**响应格式**:

字段说明:
- `s`: 交易对
- `p`: 指数价格
- `t`: 时间戳

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

#### 5.1.5 聚合行情推送（可能随行情订阅下发）

**响应格式**:

字段说明:
- `s`: 交易对
- `o`: 开盘价
- `c`: 收盘价
- `h`: 最高价
- `l`: 最低价
- `a`: 成交量
- `v`: 成交额
- `r`: 涨跌幅
- `i`: 指数价
- `m`: 标记价
- `bp`: 最优买价
- `ap`: 最优卖价
- `t`: 时间戳

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

#### 5.1.6 K线数据订阅

**请求格式**:
```json
{
  "req": "sub_kline",
  "symbol": "btc_usdt",
  "type": "1h"
}
```

> 支持的K线周期：1m, 5m, 15m, 30m, 1h, 4h, 1d, 1w, 1M

**响应格式**:

字段说明:
- `s`: 交易对
- `o`: 开盘价
- `c`: 收盘价
- `h`: 最高价
- `l`: 最低价
- `a`: 成交量
- `v`: 成交额
- `i`: K线周期
- `t`: 时间戳

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

### 5.2 用户数据订阅

**需要请求/fut/v1/user/listen-key获取listenKey**

**请求格式**:
```json
{
  "req": "sub_user",
  "listenKey": "获取到的认证listenKey"
}
```

> 订阅成功时可能返回纯文本 `succeed`（非JSON），可忽略或仅用于成功提示。

**响应数据类型**:

1. 用户余额变动 (user.balance):

字段说明:
- `coin`: 币种
- `balanceType`: 账户类型
- `underlyingType`: 标的类型（1币本位，2U本位）
- `walletBalance`: 钱包余额
- `openOrderMarginFrozen`: 委托冻结保证金
- `isolatedMargin`: 逐仓保证金
- `crossedMargin`: 全仓保证金
- `availableBalance`: 可用余额
- `bonus`: 赠金

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

2. 用户持仓更新 (user.position):

字段说明:
- `symbol`: 交易对
- `positionId`: 持仓ID
- `contractType`: 合约类型
- `positionType`: 持仓类型（CROSSED全仓，ISOLATED逐仓）
- `positionModel`: 持仓模式（AGGREGATION聚合，INDEPENDENT单向）
- `positionSide`: 持仓方向（LONG多，SHORT空）
- `positionSize`: 持仓量
- `availableCloseSize`: 可平数量
- `entryPrice`: 开仓均价
- `isolatedMargin`: 逐仓保证金
- `openOrderMarginFrozen`: 委托冻结
- `leverage`: 杠杆
- `unsettledProfit`: 未结盈亏
- `work`: 是否有效

```json
{
  "channel": "user.position",
  "data": {
    "symbol": "btc_usdt",
    "positionId": "123456789",
    "contractType": "perpetual",
    "positionType": "CROSSED",
    "positionModel": "AGGREGATION",
    "positionSide": "LONG",
    "positionSize": "0.5",
    "availableCloseSize": "0.5",
    "entryPrice": "30000.00",
    "isolatedMargin": "0.0",
    "openOrderMarginFrozen": "0.0",
    "leverage": "10",
    "unsettledProfit": "0.01",
    "work": true
  }
}
```

3. 用户持仓配置 (user.position.conf):

字段说明:
- `symbol`: 交易对
- `positionType`: 持仓类型（CROSSED全仓，ISOLATED逐仓）
- `positionModel`: 持仓模式（AGGREGATION聚合，INDEPENDENT单向）
- `positionSide`: 持仓方向（LONG多，SHORT空）
- `leverage`: 杠杆

```json
{
  "channel": "user.position.conf",
  "data": {
    "symbol": "btc_usdt",
    "positionType": "CROSSED",
    "positionModel": "AGGREGATION",
    "positionSide": "LONG",
    "leverage": "10"
  }
}
```

4. 用户订单更新 (user.order):

字段说明:
- `symbol`: 交易对
- `contractType`: 合约类型
- `orderId`: 订单ID
- `origQty`: 原始数量
- `avgPrice`: 平均成交价
- `price`: 委托价格
- `executedQty`: 已成交数量
- `orderSide`: 买卖方向（BUY买入，SELL卖出）
- `positionSide`: 持仓方向（LONG做多，SHORT做空）
- `marginFrozen`: 冻结保证金
- `state`: 订单状态
- `sourceType`: 来源类型
- `createTime`: 创建时间

```json
{
  "channel": "user.order",
  "data": {
    "symbol": "btc_usdt",
    "contractType": "perpetual",
    "orderId": "475533479170587712",
    "origQty": "0.1",
    "avgPrice": "30050.00",
    "price": "30000.00",
    "executedQty": "0.05",
    "orderSide": "BUY",
    "positionSide": "LONG",
    "marginFrozen": "150.25",
    "state": "PARTIALLY_FILLED",
    "sourceType": "WEB",
    "createTime": 1687245871234
  }
}
```

5. 用户成交通知 (user.trade):

字段说明:
- `orderId`: 订单ID
- `price`: 成交价格
- `quantity`: 成交数量
- `marginUnfrozen`: 解冻保证金
- `timestamp`: 成交时间戳

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

## 6. 最佳实践

1. **心跳维护**: 每25秒发送一次心跳消息，确保连接不断开
2. **断线重连**: 实现自动重连机制，处理网络波动情况
3. **listenKey管理**: listenKey有效期有限，需要及时更新
4. **异步获取listenKey**: 连接成功后异步获取listenKey，不阻塞主线程
5. **数据验证**: 关键业务数据建议同时使用REST API进行二次确认
6. **高效处理**: 针对高频数据实现合理的缓存和处理策略，避免内存溢出
7. **错误处理**: 妥善处理各类异常情况，包括认证失败、订阅错误等
8. **请求限频**: 合理控制订阅请求频率，避免被服务端限制