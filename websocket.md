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
| `X-Access-Key` | 您的API访问密钥 |
| `X-Signature` | 使用秘钥对请求参数生成的签名 |
| `X-Request-Timestamp` | 请求时间戳（毫秒） |
| `X-Request-Nonce` | 随机字符串，防止重放攻击 |

### 3.2 签名算法

签名算法流程：

1. 将常规请求参数按键名字母顺序排序（使用TreeMap）
2. 构建参数字符串：`key1=value1&key2=value2&...`
3. 在参数字符串末尾附加时间戳：`key1=value1&key2=value2&...&timestamp=xxx`  
   （其中`timestamp`值与请求头`X-Request-Timestamp`相同）
4. 使用HMAC-SHA256算法，以secretKey为密钥对最终参数字符串进行签名
5. 将签名结果转换为十六进制字符串

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

字段说明:
- `s`: 交易对
- `p`: 价格
- `q`: 数量
- `m`: 方向，1买/0卖

```json
{
  "resType": "qDepth",
  "data": {
    "s": "BTC_USDT",
    "p": "30000.00",
    "q": "0.1",
    "m": 1
  }
}
```

2. 全量深度数据 (qAllDepth):

字段说明:
- `s`: 交易对
- `id`: 更新ID
- `a`: 卖单数组 [价格, 数量]
- `b`: 买单数组 [价格, 数量]

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

3. 成交数据 (qDeal):

字段说明:
- `s`: 交易对
- `p`: 成交价
- `a`: 成交量
- `m`: 买卖方向，1买/0卖
- `t`: 时间戳

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

#### 5.1.2 K线数据订阅

**无需API权限，公共接口**

**请求格式**:
```json
{
  "sub": "subKline",
  "symbol": "BTC_USDT",
  "type": "1m"
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

#### 5.1.3 统计数据订阅

**无需API权限，公共接口**

**请求格式**:
```json
{
  "sub": "subStats"
}
```

**响应格式**:

字段说明:
- `s`: 交易对
- `o`: 开盘价
- `c`: 收盘价
- `h`: 最高价
- `l`: 最低价
- `a`: 24h成交量
- `v`: 24h成交额
- `r`: 涨跌幅(小数表示)

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

字段说明:
- `coin`: 币种
- `balanceType`: 账户类型（1现货，2杠杆）
- `balance`: 总余额
- `freeze`: 冻结金额
- `availableBalance`: 可用余额
- `estimatedTotalAmount`: USDT估值
- `estimatedCynAmount`: CNY估值

```json
{
  "resType": "uBalance",
  "data": {
    "coin": "BTC",
    "balanceType": 1,
    "balance": "1.5",
    "freeze": "0.5",
    "availableBalance": "1.0",
    "estimatedTotalAmount": "45000",
    "estimatedCynAmount": "315000"
  }
}
```

2. 用户订单更新 (uOrder):

字段说明:
- `orderId`: 订单ID
- `balanceType`: 账户类型（1现货，2杠杆）
- `orderType`: 订单类型（1限价单，2市价单，3止盈止损）
- `symbol`: 交易对
- `price`: 价格
- `direction`: 方向（1买入，0卖出）
- `origQty`: 原始数量
- `avgPrice`: 成交均价
- `dealQty`: 已成交数量
- `state`: 状态（1未成交，2部分成交，3完全成交，4已撤单，5撤单中）
- `createTime`: 创建时间

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

3. 用户成交通知 (uTrade):

字段说明:
- `orderId`: 订单ID
- `price`: 成交价格
- `quantity`: 成交数量
- `marginUnfrozen`: 解冻保证金
- `timestamp`: 成交时间戳

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
// 生成签名时将timestamp添加在末尾
String signature = generateSignature(SECRET_KEY, signParams, timestamp);

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
        
        // 设置定时发送心跳
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                send("ping");
            }
        }, 0, 25000); // 每25秒发送一次心跳
    }

    @Override
    public void onMessage(String message) {
        // 处理接收到的消息
        System.out.println("收到消息: " + message);
    }
};

// 连接服务器
client.connect();
```

## 8. 最佳实践

1. **心跳维护**: 每25秒发送一次心跳消息，确保连接不断开
2. **断线重连**: 实现自动重连机制，处理网络波动情况
3. **认证优先**: 建立连接后先完成认证，再进行数据订阅
4. **数据验证**: 关键业务数据建议同时使用REST API进行二次确认
5. **高效处理**: 针对高频数据实现合理的缓存和处理策略，避免内存溢出
6. **错误处理**: 妥善处理各类异常情况，包括认证失败、订阅错误等