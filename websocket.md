# HashEx WebSocket API接入文档

## 1. WebSocket 概述

HashEx交易平台提供WebSocket接口，支持实时订阅行情数据和用户数据，相比REST API具有更低的延迟和更高的效率。

## 2. 服务地址

- WebSocket基础URL: `wss://open.hashex.vip/spot/v1/ws/socket`

## 3. 认证机制

### 3.1 认证参数

连接WebSocket时需要在请求头中包含以下认证信息：

| 请求头 | 说明 |
|-------|------|
| X-Access-Key | 您的API访问密钥 |
| X-Signature | 使用秘钥对请求参数生成的签名 |
| X-Request-Timestamp | 请求时间戳（毫秒） |
| X-Request-Nonce | 随机字符串，防止重放攻击 |

### 3.2 签名算法

与REST API使用相同的签名算法：

1. 将所有请求参数按键名字母顺序排序（使用TreeMap）
2. 构建参数字符串：`key1=value1&key2=value2&...`
3. 使用HMAC-SHA256算法，以secretKey为密钥对参数字符串进行签名
4. 将结果转换为十六进制字符串

## 4. 心跳机制

- 客户端需要每25秒发送一次`ping`消息
- 服务端会回复`pong`消息
- 如果服务端超过30秒未收到心跳，将断开连接

## 5. 订阅类型

### 5.1 行情数据订阅

#### 5.1.1 单交易对行情订阅

**无需API权限，公共接口**

**请求格式**:
```json
{
  "sub": "subSymbol",
  "symbol": "BTC_USDT"
}
```

**响应数据类型**:

1. 单档深度数据 (qDepth):
```json
{
  "resType": "qDepth",
  "data": {
    "s": "BTC_USDT",  // 交易对
    "p": "30000.00",  // 价格
    "q": "0.1",       // 数量
    "m": 1            // 方向，1买/0卖
  }
}
```

2. 全量深度数据 (qAllDepth):
```json
{
  "resType": "qAllDepth",
  "data": {
    "s": "BTC_USDT",         // 交易对
    "id": "12345678",        // 更新ID
    "a": [                   // 卖单数组 [价格, 数量]
      ["30100.00", "0.1"],
      ["30200.00", "0.5"]
    ],
    "b": [                   // 买单数组 [价格, 数量] 
      ["29900.00", "0.3"],
      ["29800.00", "0.6"]
    ]
  }
}
```

3. 成交数据 (qDeal):
```json
{
  "resType": "qDeal",
  "data": {
    "s": "BTC_USDT",     // 交易对
    "p": "30050.00",     // 成交价
    "a": "0.25",         // 成交量
    "m": 1,              // 买卖方向，1买/0卖
    "t": 1687245871234   // 时间戳
  }
}
```

#### 5.1.2 K线数据订阅

**无需API权限，公共接口**

**请求格式**:
```json
{
  "sub": "subKline",
  "symbol": "BTC_USDT",
  "type": "1m"           // K线周期：1m,5m,15m,30m,1h,4h,1d,1w,1M
}
```

**响应格式**:
```json
{
  "resType": "qKLine",
  "data": {
    "s": "BTC_USDT",     // 交易对
    "o": "30000.00",     // 开盘价
    "c": "30100.00",     // 收盘价
    "h": "30200.00",     // 最高价
    "l": "29900.00",     // 最低价
    "a": "10.5",         // 成交量
    "v": "315000.50",    // 成交额
    "i": "1m",           // K线周期
    "t": 1687245871234   // 时间戳
  }
}
```

#### 5.1.3 统计数据订阅

**无需API权限，公共接口**

**请求格式**:
```json
{
  "sub": "subStats"
}
```

**响应格式**:
```json
{
  "resType": "qStats",
  "data": {
    "s": "BTC_USDT",     // 交易对
    "o": "30000.00",     // 开盘价
    "c": "30100.00",     // 收盘价
    "h": "30200.00",     // 最高价 
    "l": "29900.00",     // 最低价
    "a": "10.5",         // 24h成交量
    "v": "315000.50",    // 24h成交额
    "r": "0.0033"        // 涨跌幅(小数表示)
  }
}
```

### 5.2 用户数据订阅

**需要API权限，私有接口**

**请求格式**:
```json
{
  "sub": "subUser"
}
```

**响应数据类型**:

1. 用户余额变动 (uBalance):
```json
{
  "resType": "uBalance",
  "data": {
    "coin": "BTC",           // 币种
    "balanceType": 1,        // 账户类型：1现货，2杠杆
    "balance": "1.5",        // 总余额
    "freeze": "0.5",         // 冻结金额
    "availableBalance": "1.0", // 可用余额
    "estimatedTotalAmount": "45000", // USDT估值
    "estimatedCynAmount": "315000"   // CNY估值
  }
}
```

2. 用户订单更新 (uOrder):
```json
{
  "resType": "uOrder",
  "data": {
    "orderId": "475533479170587712",
    "balanceType": 1,        // 账户类型：1现货，2杠杆
    "orderType": 1,          // 订单类型：1限价单，2市价单，3止盈止损
    "symbol": "BTC_USDT",
    "price": "30000.00",
    "direction": 1,          // 方向：1买入，0卖出
    "origQty": "0.1",        // 原始数量
    "avgPrice": "30050.00",  // 成交均价
    "dealQty": "0.05",       // 已成交数量
    "state": 2,              // 状态：1未成交，2部分成交，3完全成交，4已撤单，5撤单中
    "createTime": 1687245871234
  }
}
```

3. 用户成交通知 (uTrade):
```json
{
  "resType": "uTrade",
  "data": {
    "orderId": "475533479170587712",
    "price": "30050.00",      // 成交价格
    "quantity": "0.05",       // 成交数量
    "marginUnfrozen": "15.025", // 解冻保证金
    "timestamp": 1687245871500
  }
}
```

## 6. 错误码

| 错误码 | 描述 |
|-------|------|
| invalid param | 参数无效，请检查订阅格式 |
| auth fail | 认证失败，请检查认证参数 |
| sub fail | 订阅失败 |
| system error | 系统错误 |

## 7. WebSocket客户端示例

Java客户端示例：

```java
// 构建WebSocket URL和请求头
String wsUrl = "wss://open.hashex.vip/spot/v1/ws/socket";
String timestamp = generateTimestamp();
String nonce = generateNonce();

// 准备签名参数（必须使用TreeMap保证顺序）
TreeMap<String, String> signParams = new TreeMap<>();
String signature = generateSignature(SECRET_KEY, signParams);

// 构建请求头
Map<String, String> headers = new HashMap<>();
headers.put("X-Access-Key", ACCESS_KEY);
headers.put("X-Signature", signature);
headers.put("X-Request-Timestamp", timestamp);
headers.put("X-Request-Nonce", nonce);

// 创建WebSocketClient
WebSocketClient client = new WebSocketClient(new URI(wsUrl), headers) {
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        // 发送订阅请求
        send("{\"sub\":\"subSymbol\",\"symbol\":\"BTC_USDT\"}");
    }
    
    @Override
    public void onMessage(String message) {
        // 处理接收到的消息
    }
};

// 连接服务器
client.connect();
```

## 8. 最佳实践

1. 建议每25秒发送一次心跳消息
2. 处理连接断开时的自动重连机制
3. 订阅前先进行认证
4. 对于重要数据，建议同时使用REST API进行确认
5. 处理好高频数据的缓存和处理逻辑，避免内存溢出