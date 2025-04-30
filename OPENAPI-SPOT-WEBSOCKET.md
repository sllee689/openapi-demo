# HashEx WebSocket API接入文档

## 1. WebSocket 概述

HashEx交易平台提供WebSocket接口，支持实时订阅行情数据和用户数据，相比REST API具有更低的延迟和更高的效率。WebSocket连接不需要认证，但订阅用户私有数据时需要提供认证token。

## 2. 服务地址

- WebSocket基础URL: `wss://open.hashex.vip/spot/v1/ws/socket`
- 获取用户认证Token URL: `https://open.hashex.vip/spot/v1/u/ws/token`

## 3. 认证机制

### 3.1 用户数据认证流程

订阅用户私有数据需要先获取认证token，然后在订阅消息中提供该token：

1. 调用 `/spot/v1/u/ws/token` 接口获取WebSocket认证token
2. 在订阅用户数据时将获取的token添加到订阅消息中

### 3.2 获取认证Token

**接口信息:**
- **路径**: `/spot/v1/u/ws/token`
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
| message | String | 响应信息 |
| data | String | WebSocket认证Token |

## 4. 心跳机制

- 客户端需要每50秒发送一次`ping`消息
- 服务端会回复`pong`消息
- 如果服务端超过60秒未收到心跳，将断开连接

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
- `id`: 深度更新唯一标识
- `s`: 交易对
- `p`: 价格
- `q`: 数量
- `m`: 方向(1买入/2卖出)
- `t`: 时间戳(毫秒)

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
- `m`: 买卖方向，1买/2卖
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

**需要请求/spot/v1/u/ws/token获取 token**

**请求格式**:
```json
{
  "sub": "subUser",
  "token": "获取到的认证Token"
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
- `estimatedTotalAmount`: 总余额估值
- `estimatedCynAmount`: CNY估值
- `estimatedAvailableAmount`: 可用余额估值
- `estimatedCoinType`: 估值使用的币种

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

2. 用户订单更新 (uOrder):

字段说明:
- `orderId`: 订单ID
- `balanceType`: 账户类型（1现货，2杠杆）
- `orderType`: 订单类型（1限价单，2市价单，3止盈止损）
- `symbol`: 交易对
- `price`: 价格
- `direction`: 方向（1买入，2卖出）
- `origQty`: 原始数量
- `avgPrice`: 成交均价
- `dealQty`: 已成交数量
- `state`: 状态（1未成交，2部分成交，3完全成交，4已撤单）
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

### 5.3 系统通知订阅

系统通知会自动推送，无需特殊订阅。当系统有重要通知（如价格波动提醒）时，会通过此通道推送。

**响应数据类型** (znxMessage):

字段说明:
- `id`: 通知消息ID
- `tenantId`: 租户ID
- `title`: 通知标题
- `content`: 通知内容
- `aggType`: 消息聚合类型，如"SYSTEM"
- `detailType`: 消息详细类型，如"SYSTEM_PRICE"表示价格提醒
- `createdTime`: 创建时间戳(毫秒)
- `allScope`: 是否全局范围消息
- `userId`: 用户ID，-1表示系统消息
- `read`: 是否已读

```json
{
   "resType": "znxMessage",
   "data": {
      "id": 336912,
      "tenantId": 1,
      "title": "行情价格",
      "content": "VVVUSDT 10分钟涨跌幅 -3.01%，现价 4.177 USDT",
      "aggType": "SYSTEM",
      "detailType": "SYSTEM_PRICE",
      "createdTime": 1745932297862,
      "allScope": true,
      "userId": -1,
      "read": false
   }
}

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
// 1. 连接WebSocket (不需要认证)
String wsUrl = "wss://open.hashex.vip/spot/v1/ws/socket";
WebSocketClient client = new WebSocketClient(new URI(wsUrl)) {
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        // 连接成功后订阅公共数据
        send("{\"sub\":\"subSymbol\",\"symbol\":\"BTC_USDT\"}");
        send("{\"sub\":\"subKline\",\"symbol\":\"BTC_USDT\",\"type\":\"1m\"}");
        
        // 设置定时发送心跳
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                send("ping");
            }
        }, 0, 25000); // 每25秒发送一次心跳
        
        // 获取token后订阅用户数据
        new Thread(() -> {
            String token = getWebSocketToken();
            if (token != null) {
                send("{\"sub\":\"subUser\",\"token\":\"" + token + "\"}");
            }
        }).start();
    }

    @Override
    public void onMessage(String message) {
        // 处理接收到的消息
        System.out.println("收到消息: " + message);
    }
};

// 2. 获取WebSocket认证Token
private String getWebSocketToken() {
    try {
        String url = "https://open.hashex.vip/spot/v1/u/ws/token";
        long timestamp = System.currentTimeMillis();
        String nonce = UUID.randomUUID().toString();

        // 准备签名参数
        TreeMap<String, String> sortedParams = new TreeMap<>();
        String signature = generateSignature(SECRET_KEY, sortedParams, String.valueOf(timestamp));

        // 发送请求获取Token
        HttpRequest request = HttpRequest.get(url)
            .header("X-Access-Key", ACCESS_KEY)
            .header("X-Request-Timestamp", String.valueOf(timestamp))
            .header("X-Request-Nonce", nonce)
            .header("X-Signature", signature);
        
        String response = request.execute().body();
        JSONObject json = JSONUtil.parseObj(response);
        
        if (json.getInt("code") == 0) {
            return json.getStr("data");
        }
        return null;
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}

// 连接服务器
client.connect();
```

## 8. 最佳实践

1. **心跳维护**: 每25秒发送一次心跳消息，确保连接不断开
2. **断线重连**: 实现自动重连机制，处理网络波动情况
3. **Token管理**: Token有效期有限，需要及时更新
4. **异步获取Token**: 连接成功后异步获取Token，不阻塞主线程
5. **数据验证**: 关键业务数据建议同时使用REST API进行二次确认
6. **高效处理**: 针对高频数据实现合理的缓存和处理策略，避免内存溢出
7. **错误处理**: 妥善处理各类异常情况，包括认证失败、订阅错误等