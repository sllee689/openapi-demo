package com.example.openapi.test.websocket;

import cn.hutool.core.lang.Console;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.utils.HashexApiUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket 测试客户端
 */
public class WebsocketTest {
    // 服务器地址
    private static final String WS_HOST = "wss://open.hashex.vip";
    // API凭证
    private static final String ACCESS_KEY = "0a9970e8986247d6e6d5deadc886a4e558c0a1c4f2047c2a00bc96e2efd24499";
    private static final String SECRET_KEY = "yZGQ4OWExMjVmMWViYWE1MmU0ZGQwY2ZmODQ4NDI0ZWI0OWU1MTUyNmUyZDU4NWJmZWRmYmM4ZDA1NWEyYjAxMmE=";

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final int RECONNECT_INTERVAL = 5000; // 5秒

    public static void main(String[] args) {
        try {
            // 构建客户端实例
            HashexWebSocketClient client = createWebSocketClient();

            // 连接服务器
            client.connect();

            // 保持程序运行
            Thread.sleep(600000); // 10分钟

            // 关闭连接
            client.close();
        } catch (Exception e) {
            Console.error("WebSocket 测试失败: {}", e.getMessage());
        }
    }

    /**
     * 创建 WebSocketClient 实例
     */
    private static HashexWebSocketClient createWebSocketClient() throws URISyntaxException {
        // 生成时间戳和随机数
        String timestamp = HashexApiUtils.generateTimestamp();
        String nonce = HashexApiUtils.generateNonce();

        // 准备签名参数（必须使用 TreeMap 保证顺序）
        TreeMap<String, String> signParams = new TreeMap<>();

        // 生成签名
        String signature = HashexApiUtils.generateSignature(SECRET_KEY, signParams, timestamp);

        // 构建请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Access-Key", ACCESS_KEY);
        headers.put("X-Signature", signature);
        headers.put("X-Request-Timestamp", timestamp);
        headers.put("X-Request-Nonce", nonce);

        // 构建 WebSocket URL
        String wsUrl = String.format("%s/spot/v1/ws/socket", WS_HOST);
        Console.log("正在连接 WebSocket: {}", wsUrl);

        return new HashexWebSocketClient(new URI(wsUrl), headers);
    }

    /**
     * 自定义 WebSocketClient 实现
     */
    private static class HashexWebSocketClient extends WebSocketClient {
        private Timer heartbeatTimer;
        private String symbol = "BTC_USDT";

        public HashexWebSocketClient(URI serverUri, Map<String, String> httpHeaders) {
            super(serverUri, httpHeaders);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Console.log("WebSocket 连接已建立: {}", handshakedata.getHttpStatus());
            reconnectAttempts.set(0); // 重置重连计数

            // 发送心跳
            sendHeartbeat();

            // 设置定时心跳
            startHeartbeatTimer();

            // 发送订阅请求
            sendSubscription(symbol);

            // 发送用户订阅请求
            sendUserSubscription();
            // 发送K线订阅请求
            sendKlineSubscription(symbol, "1m");
            // 发送统计数据订阅请求
            sendStatsSubscription();


        }

        /**
         * 发送心跳消息并设置定时器
         */
        private void startHeartbeatTimer() {
            if (heartbeatTimer != null) {
                heartbeatTimer.cancel();
            }

            heartbeatTimer = new Timer();
            heartbeatTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isOpen()) {
                        sendHeartbeat();
                    }
                }
            }, 25000, 25000); // 25秒发送一次
        }

        /**
         * 发送心跳消息
         */
        private void sendHeartbeat() {
            if (isOpen()) {
                this.send("ping");
                Console.log("已发送心跳");
            }
        }

        /**
         * 发送订阅请求
         */
        private void sendSubscription(String symbol) {
            try {
                JSONObject subscribeMsg = JSONUtil.createObj();
                subscribeMsg.set("sub", "subSymbol");  // 必须匹配WsReqType定义的值
                subscribeMsg.set("symbol", symbol);

                String subscribeText = subscribeMsg.toString();
                this.send(subscribeText);
                Console.log("已发送订阅请求: {}", subscribeText);
            } catch (Exception e) {
                Console.error("发送订阅消息失败: {}", e.getMessage());
            }
        }

        private void sendUserSubscription() {
            try {
                JSONObject subscribeMsg = JSONUtil.createObj();
                subscribeMsg.set("sub", "subUser");  // 必须匹配WsReqType定义的值

                String subscribeText = subscribeMsg.toString();
                this.send(subscribeText);
                Console.log("已发送订阅请求: {}", subscribeText);
            } catch (Exception e) {
                Console.error("发送订阅消息失败: {}", e.getMessage());
            }
        }

        /**
         * 发送K线订阅请求
         */
        private void sendKlineSubscription(String symbol, String period) {
            try {
                JSONObject subscribeMsg = JSONUtil.createObj();
                subscribeMsg.set("sub", "subKline");
                subscribeMsg.set("symbol", symbol);
                subscribeMsg.set("type", period);

                String subscribeText = subscribeMsg.toString();
                this.send(subscribeText);
                Console.log("已发送K线订阅请求: {}", subscribeText);
            } catch (Exception e) {
                Console.error("发送K线订阅消息失败: {}", e.getMessage());
            }
        }

        /**
         * 发送统计数据订阅请求
         */
        private void sendStatsSubscription() {
            try {
                JSONObject subscribeMsg = JSONUtil.createObj();
                subscribeMsg.set("sub", "subStats");  // 必须匹配WsReqType定义的值

                String subscribeText = subscribeMsg.toString();
                this.send(subscribeText);
                Console.log("已发送统计数据订阅请求: {}", subscribeText);
            } catch (Exception e) {
                Console.error("发送统计数据订阅消息失败: {}", e.getMessage());
            }
        }

        @Override
        public void onMessage(String message) {
            // 处理系统消息
            if ("pong".equals(message)) {
                Console.log("收到心跳响应");
                return;
            }

            if ("succeed".equals(message)) {
                Console.log("订阅操作成功");
                return;
            }

            if ("invalid param".equals(message)) {
                Console.log("参数无效，请检查订阅格式");
                return;
            }

            // 解析行情数据
            try {
                JSONObject json = JSONUtil.parseObj(message);
                String resType = json.getStr("resType");
                JSONObject data = json.getJSONObject("data");

                switch (resType) {
                    case "qDepth":
                        // 单档深度数据
                        processDepthData(data);
                        break;
                    case "qAllDepth":
                        // 全量深度数据
                        processAllDepthData(data);
                        break;
                    case "qDeal":
                        // 成交数据
                        processDealData(data);
                        break;
                    case "qStats":
                        // 统计数据
                        processStatsData(data);
                        break;
                    case "qKLine":
                        // K线数据
                        processKlineData(data);
                        break;
                    case "uBalance":
                        // 用户余额信息
                        processUserBalanceData(data);
                        break;
                    case "uOrder":
                        // 用户订单信息
                        processUserOrderData(data);
                        break;
                    case "uTrade":
                        // 用户交易信息
                        processUserTradeData(data);
                        break;
                    default:
                        Console.error("收到未知类型消息: {}, 内容: {}", resType, message);
                }
            } catch (Exception e) {
                Console.log("消息解析失败: {}, 内容: {}", e.getMessage(), message);
            }
        }

        /**
         * 处理单档深度数据
         */
        private void processDepthData(JSONObject data) {
            String symbol = data.getStr("s");
            String price = data.getStr("p");
            String quantity = data.getStr("q");
            int direction = data.getInt("m"); // 1买/0卖

            Console.log("收到{}单档深度: {} {} {}@{}",
                    symbol,
                    direction == 1 ? "买入" : "卖出",
                    quantity,
                    price);
        }

        /**
         * 处理全量深度数据
         */
        private void processAllDepthData(JSONObject data) {
            String symbol = data.getStr("s");
            String id = data.getStr("id");
            JSONArray asks = data.getJSONArray("a");
            JSONArray bids = data.getJSONArray("b");

            Console.log("收到{}全量深度 ID:{}", symbol, id);
            Console.log("卖盘前5档: {}",
                    asks.size() >= 5 ? asks.subList(0, 5) : asks);
            Console.log("买盘前5档: {}",
                    bids.size() >= 5 ? bids.subList(0, 5) : bids);
        }

        /**
         * 处理成交数据
         */
        private void processDealData(JSONObject data) {
            String symbol = data.getStr("s");
            String price = data.getStr("p");
            String amount = data.getStr("a");
            int direction = data.getInt("m"); // 买卖方向
            long timestamp = data.getLong("t");

            Console.log("收到{}成交: {} {} {}@{} 时间:{}",
                    symbol,
                    direction == 1 ? "买入" : "卖出",
                    amount,
                    price,
                    timestamp);
        }

        /**
         * 处理统计数据
         */
        private void processStatsData(JSONObject data) {
            String symbol = data.getStr("s");
            String open = data.getStr("o");
            String close = data.getStr("c");
            String high = data.getStr("h");
            String low = data.getStr("l");
            String amount = data.getStr("a");
            String volume = data.getStr("v");
            String ratio = data.getStr("r");

            Console.log("收到{}统计数据: 开:{} 收:{} 高:{} 低:{} 量:{} 额:{} 涨跌幅:{}%",
                    symbol, open, close, high, low, amount, volume,
                    Float.parseFloat(ratio) * 100);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Console.log("WebSocket 连接已关闭, 状态码: {}, 原因: {}, 由远端发起: {}",
                    code, reason, remote);

            // 取消心跳定时器
            if (heartbeatTimer != null) {
                heartbeatTimer.cancel();
                heartbeatTimer = null;
            }

            // 实现重连逻辑
            if (shouldReconnect(code)) {
                int attempts = reconnectAttempts.incrementAndGet();
                if (attempts <= MAX_RECONNECT_ATTEMPTS) {
                    long delay = RECONNECT_INTERVAL * attempts;
                    Console.log("将在 {}ms 后尝试第 {} 次重连...", delay, attempts);

                    Timer reconnectTimer = new Timer();
                    reconnectTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Console.log("正在尝试重新连接...");
                            reconnect();
                        }
                    }, delay);
                } else {
                    Console.error("重连次数达到上限({})，放弃重连", MAX_RECONNECT_ATTEMPTS);
                }
            }
        }


        /**
         * 处理用户余额数据
         */
        private void processUserBalanceData(JSONObject data) {
            String coin = data.getStr("coin");
            int balanceType = data.getInt("balanceType");
            String balance = data.getStr("balance");
            String freeze = data.getStr("freeze");
            String availableBalance = data.getStr("availableBalance");
            String estimatedTotalAmount = data.getStr("estimatedTotalAmount");
            String estimatedCynAmount = data.getStr("estimatedCynAmount");

            Console.log("收到余额变动通知: 币种:{}, 余额类型:{}, 总余额:{}, 冻结:{}, 可用:{}, 估值(USDT):{}, 估值(CNY):{}",
                    coin, balanceType == 1 ? "现货" : "其他",
                    balance, freeze, availableBalance,
                    estimatedTotalAmount, estimatedCynAmount);
        }

        /**
         * 处理用户订单数据
         */
        private void processUserOrderData(JSONObject data) {
            String orderId = data.getStr("orderId");
            int balanceType = data.getInt("balanceType");
            int orderType = data.getInt("orderType");
            String symbol = data.getStr("symbol");
            String price = data.getStr("price");
            int direction = data.getInt("direction");
            String origQty = data.getStr("origQty");
            String avgPrice = data.getStr("avgPrice");
            String dealQty = data.getStr("dealQty");
            int state = data.getInt("state");
            long createTime = data.getLong("createTime");

            String orderTypeStr = "";
            switch (orderType) {
                case 1: orderTypeStr = "限价单"; break;
                case 2: orderTypeStr = "市价单"; break;
                case 3: orderTypeStr = "止盈止损"; break;
                default: orderTypeStr = "未知类型";
            }

            String stateStr = "";
            switch (state) {
                case 1: stateStr = "未成交"; break;
                case 2: stateStr = "部分成交"; break;
                case 3: stateStr = "完全成交"; break;
                case 4: stateStr = "已撤单"; break;
                case 5: stateStr = "撤单中"; break;
                default: stateStr = "未知状态";
            }

            Console.log("收到订单更新: 订单ID:{}, 交易对:{}, 类型:{}, {}:{}, 原始数量:{}, 成交数量:{}, 均价:{}, 状态:{}",
                    orderId, symbol, orderTypeStr,
                    direction == 1 ? "买入" : "卖出", price,
                    origQty, dealQty, avgPrice, stateStr);
        }

        /**
         * 处理用户成交数据
         */
        private void processUserTradeData(JSONObject data) {
            String orderId = data.getStr("orderId");
            String price = data.getStr("price");
            String quantity = data.getStr("quantity");
            String marginUnfrozen = data.getStr("marginUnfrozen");
            long timestamp = data.getLong("timestamp");

            Console.log("收到成交通知: 订单ID:{}, 成交价:{}, 成交量:{}, 解冻金额:{}, 时间:{}",
                    orderId, price, quantity, marginUnfrozen,
                    new Date(timestamp));
        }


        /**
         * 处理K线数据
         */
        private void processKlineData(JSONObject data) {
            String symbol = data.getStr("s");    // 交易对
            String open = data.getStr("o");       // 开盘价
            String close = data.getStr("c");      // 收盘价
            String high = data.getStr("h");       // 最高价
            String low = data.getStr("l");        // 最低价
            String amount = data.getStr("a");     // 成交量
            String volume = data.getStr("v");     // 成交额
            String interval = data.getStr("i");   // K线周期
            long timestamp = data.getLong("t");   // 时间戳

            // 计算涨跌幅
            double openValue = Double.parseDouble(open);
            double closeValue = Double.parseDouble(close);
            double changeRate = ((closeValue - openValue) / openValue) * 100;

            Console.log("收到{}K线数据 [{}]: 时间:{}, 开:{}, 收:{}, 高:{}, 低:{}, 量:{}, 额:{}, 涨跌:{:.2f}%",
                    symbol, interval, new Date(timestamp),
                    open, close, high, low,
                    amount, volume, changeRate);
        }

        /**
         * 判断是否应该尝试重连
         */
        private boolean shouldReconnect(int code) {
            // 1000是正常关闭，不需要重连
            return code != 1000;
        }

        @Override
        public void onError(Exception ex) {
            Console.error("WebSocket 连接错误: {}", ex.getMessage());
        }
    }
}