package com.example.openapi.test.future.websocket;

import cn.hutool.core.lang.Console;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.test.future.FutureTestConfig;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 合约交易市场数据WebSocket客户端
 */
public class FutureMarketWebSocketTest {
    private static final String WS_HOST = FutureTestConfig.WS_BASE_URL;
    private static final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final int RECONNECT_INTERVAL = 5000;

    public static void main(String[] args) {
        try {
            // 构建合约市场WebSocket URL
            String wsUrl = String.format("%s/fut/v1/ws/market", WS_HOST);
            Console.log("正在连接合约市场WebSocket: {}", wsUrl);

            // 创建客户端
            FutureMarketWSClient client = new FutureMarketWSClient(new URI(wsUrl));

            // 连接服务器
            client.connect();

            // 保持程序运行
            long runMillis = getRunMillis(args, 60000);
            Thread.sleep(runMillis);

            // 关闭连接
            client.close();
        } catch (Exception e) {
            Console.error("合约市场WebSocket连接失败: {}", e.getMessage());
        }
    }

    private static long getRunMillis(String[] args, long defaultMillis) {
        if (args != null && args.length > 0) {
            try {
                return Long.parseLong(args[0]);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        String env = System.getenv("HASHEX_WS_RUN_MS");
        if (env != null && !env.trim().isEmpty()) {
            try {
                return Long.parseLong(env.trim());
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return defaultMillis;
    }


    private static class FutureMarketWSClient extends WebSocketClient {
        private Timer heartbeatTimer;
        private String symbol = "btc_usdt"; // 合约交易对格式使用下划线分隔

        public FutureMarketWSClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Console.log("合约市场WebSocket连接已建立: {}", handshakedata.getHttpStatus());
            reconnectAttempts.set(0);

            // 发送心跳
            sendHeartbeat();

            // 设置定时心跳
            startHeartbeatTimer();

            // 发送合约市场订阅请求
            sendSymbolSubscription(symbol);
            sendMarkPriceSubscription();
            sendTickerSubscription();
            sendKlineSubscription(symbol, "1h");
        }

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
            }, 25000, 25000);
        }

        private void sendHeartbeat() {
            if (isOpen()) {
                this.send("ping");
                Console.log("已发送心跳");
            }
        }

        // 订阅交易对
        private void sendSymbolSubscription(String symbol) {
            try {
                JSONObject subscribeMsg = JSONUtil.createObj();
                subscribeMsg.set("req", "sub_symbol");
                subscribeMsg.set("symbol", symbol);

                String subscribeText = subscribeMsg.toString();
                this.send(subscribeText);
                Console.log("已发送交易对订阅请求: {}", subscribeText);
            } catch (Exception e) {
                Console.error("发送交易对订阅失败: {}", e.getMessage());
            }
        }

        // 取消订阅交易对
        @SuppressWarnings("unused")
        private void unsubscribeSymbol() {
            try {
                JSONObject unsubscribeMsg = JSONUtil.createObj();
                unsubscribeMsg.set("req", "unsub_symbol");

                String unsubscribeText = unsubscribeMsg.toString();
                this.send(unsubscribeText);
                Console.log("已发送取消交易对订阅请求: {}", unsubscribeText);
            } catch (Exception e) {
                Console.error("发送取消交易对订阅失败: {}", e.getMessage());
            }
        }

        // 订阅标记价格
        private void sendMarkPriceSubscription() {
            try {
                JSONObject subscribeMsg = JSONUtil.createObj();
                subscribeMsg.set("req", "sub_mark_price");

                String subscribeText = subscribeMsg.toString();
                this.send(subscribeText);
                Console.log("已发送标记价格订阅请求: {}", subscribeText);
            } catch (Exception e) {
                Console.error("发送标记价格订阅失败: {}", e.getMessage());
            }
        }

        // 取消订阅标记价格
        @SuppressWarnings("unused")
        private void unsubscribeMarkPrice() {
            try {
                JSONObject unsubscribeMsg = JSONUtil.createObj();
                unsubscribeMsg.set("req", "unsub_mark_price");

                String unsubscribeText = unsubscribeMsg.toString();
                this.send(unsubscribeText);
                Console.log("已发送取消标记价格订阅请求: {}", unsubscribeText);
            } catch (Exception e) {
                Console.error("发送取消标记价格订阅失败: {}", e.getMessage());
            }
        }

        // 订阅行情
        private void sendTickerSubscription() {
            try {
                JSONObject subscribeMsg = JSONUtil.createObj();
                subscribeMsg.set("req", "sub_ticker");

                String subscribeText = subscribeMsg.toString();
                this.send(subscribeText);
                Console.log("已发送行情订阅请求: {}", subscribeText);
            } catch (Exception e) {
                Console.error("发送行情订阅失败: {}", e.getMessage());
            }
        }

        // 取消订阅行情
        @SuppressWarnings("unused")
        private void unsubscribeTicker() {
            try {
                JSONObject unsubscribeMsg = JSONUtil.createObj();
                unsubscribeMsg.set("req", "unsub_ticker");

                String unsubscribeText = unsubscribeMsg.toString();
                this.send(unsubscribeText);
                Console.log("已发送取消行情订阅请求: {}", unsubscribeText);
            } catch (Exception e) {
                Console.error("发送取消行情订阅失败: {}", e.getMessage());
            }
        }

        // 订阅K线数据
        private void sendKlineSubscription(String symbol, String type) {
            try {
                JSONObject subscribeMsg = JSONUtil.createObj();
                subscribeMsg.set("req", "sub_kline");
                subscribeMsg.set("symbol", symbol);
                subscribeMsg.set("type", type);

                String subscribeText = subscribeMsg.toString();
                this.send(subscribeText);
                Console.log("已发送K线订阅请求: {}", subscribeText);
            } catch (Exception e) {
                Console.error("发送K线订阅失败: {}", e.getMessage());
            }
        }

        // 取消订阅K线
        @SuppressWarnings("unused")
        private void unsubscribeKline() {
            try {
                JSONObject unsubscribeMsg = JSONUtil.createObj();
                unsubscribeMsg.set("req", "unsub_kline");

                String unsubscribeText = unsubscribeMsg.toString();
                this.send(unsubscribeText);
                Console.log("已发送取消K线订阅请求: {}", unsubscribeText);
            } catch (Exception e) {
                Console.error("发送取消K线订阅失败: {}", e.getMessage());
            }
        }

        @Override
        public void onMessage(String message) {
            // 处理心跳响应
            if ("pong".equals(message)) {
                Console.log("收到心跳响应");
                return;
            }

            // 解析响应数据
            try {
                if (message.startsWith("{")) {
                    JSONObject json = JSONUtil.parseObj(message);

                    // 根据响应中的字段判断消息类型
                    if (json.containsKey("channel")) {
                        String channel = json.getStr("channel");
                        switch (channel) {
                            case "push.deal":
                                processDealData(json);
                                break;
                            case "push.deep":
                                processDeepData(json);
                                break;
                            case "push.mark.price":
                                processMarkPriceData(json);
                                break;
                            case "push.ticker":
                                processTickerData(json);
                                break;
                            case "push.index.price":
                                processIndexPriceData(json);
                                break;
                            case "push.kline":
                                processKlineData(json);
                                break;
                            case "push.agg.ticker":
                                processAggTickerData(json);
                                break;
                            case "push.deep.full":
                                processDeepFullData(json);
                                break;
                            default:
                                Console.log("收到未知频道消息: {}", message);
                        }
                    } else if (json.containsKey("status")) {
                        // 处理订阅/取消订阅响应
                        processStatusResponse(json);
                    } else {
                        Console.log("收到未分类消息: {}", message);
                    }
                } else {
                    Console.log("收到非JSON格式消息: {}", message);
                }
            } catch (Exception e) {
                Console.log("消息解析失败: {}, 内容: {}", e.getMessage(), message);
            }
        }

        // 处理订阅/取消订阅响应
        private void processStatusResponse(JSONObject json) {
            String status = json.getStr("status");
            String reqType = json.getStr("req");

            if ("ok".equalsIgnoreCase(status)) {
                Console.log("{}请求成功", reqType);
            } else {
                Console.error("{}请求失败: {}", reqType, json);
            }
        }


        // 处理成交推送数据
        private void processDealData(JSONObject json) {
            JSONObject data = json.getJSONObject("data");
            String symbol = data.getStr("s");
            String price = data.getStr("p");
            String amount = data.getStr("a");
            String side = "ASK".equals(data.getStr("m")) ? "卖出" : "买入";
            long timestamp = data.getLong("t");

            Console.log("收到{}成交推送: {} {}@{} 时间:{}",
                    symbol, side, amount, price, new Date(timestamp));
        }

        // 处理深度推送数据
        private void processDeepData(JSONObject json) {
            JSONObject data = json.getJSONObject("data");
            String id = data.getStr("id");
            String symbol = data.getStr("s");
            int buyOrSell = data.getInt("ba");  // 1买 2卖
            String price = data.getStr("p");
            String quantity = data.getStr("q");
            long timestamp = data.getLong("t");

            String side = buyOrSell == 1 ? "买盘" : "卖盘";
            Console.log("收到{}深度推送: {} ID:{} 价格:{} 数量:{} 时间:{}",
                    symbol, side, id, price, quantity, new Date(timestamp));
        }


        // 处理标记价格推送数据
        private void processMarkPriceData(JSONObject json) {
            JSONObject data = json.getJSONObject("data");
            String symbol = data.getStr("s");
            String price = data.getStr("p");
            long timestamp = data.getLong("t");

            Console.log("收到{}标记价格推送: 标记价:{} 时间:{}",
                    symbol, price, new Date(timestamp));
        }


        // 处理行情推送数据
        private void processTickerData(JSONObject json) {
            JSONObject data = json.getJSONObject("data");
            String symbol = data.getStr("s");
            String open = data.getStr("o");
            String close = data.getStr("c");
            String high = data.getStr("h");
            String low = data.getStr("l");
            String amount = data.getStr("a");
            String volume = data.getStr("v");
            String rate = data.getStr("r");
            long timestamp = data.getLong("t");

            // 显示格式化的涨跌幅
            String ratePercent = rate;
            if (!rate.startsWith("-") && !rate.startsWith("+")) {
                ratePercent = "+" + rate;
            }

            Console.log("收到{}行情推送: 最新价:{}, 开盘价:{}, 最高:{}, 最低:{}, 涨跌幅:{}%, 成交量:{}, 成交额:{}, 时间:{}",
                    symbol, close, open, high, low, ratePercent,
                    amount, volume, new Date(timestamp));
        }


        // 处理指数价格推送数据
        private void processIndexPriceData(JSONObject json) {
            JSONObject data = json.getJSONObject("data");
            String symbol = data.getStr("s");
            String price = data.getStr("p");
            long timestamp = data.getLong("t");

            Console.log("收到{}指数价格推送: 指数价:{} 时间:{}",
                    symbol, price, new Date(timestamp));
        }


        // 处理K线推送数据
        private void processKlineData(JSONObject json) {
            JSONObject data = json.getJSONObject("data");
            String symbol = data.getStr("s");
            String open = data.getStr("o");
            String close = data.getStr("c");
            String high = data.getStr("h");
            String low = data.getStr("l");
            String amount = data.getStr("a");
            String volume = data.getStr("v");
            String interval = data.getStr("i");
            long timestamp = data.getLong("t");

            // 计算涨跌幅
            double openValue = Double.parseDouble(open);
            double closeValue = Double.parseDouble(close);
            double changeRate = ((closeValue - openValue) / openValue) * 100;

            Console.log("收到{}K线数据 [{}]: 时间:{}, 开:{}, 收:{}, 高:{}, 低:{}, 量:{}, 额:{}, 涨跌:{}%",
                    symbol, interval, new Date(timestamp),
                    open, close, high, low,
                    amount, volume, String.format("%.2f", changeRate));
        }

        // 处理聚合行情推送数据
        private void processAggTickerData(JSONObject json) {
            JSONObject data = json.getJSONObject("data");
            String symbol = data.getStr("s");
            String open = data.getStr("o");
            String close = data.getStr("c");
            String high = data.getStr("h");
            String low = data.getStr("l");
            String amount = data.getStr("a");
            String volume = data.getStr("v");
            String rate = data.getStr("r");
            String indexPrice = data.getStr("i");
            String markPrice = data.getStr("m");
            String bestBidPrice = data.getStr("bp");
            String bestAskPrice = data.getStr("ap");
            long timestamp = data.getLong("t");

            // 显示格式化的涨跌幅
            String ratePercent = rate;
            if (!rate.startsWith("-") && !rate.startsWith("+")) {
                ratePercent = "+" + rate;
            }

                Console.log("收到{}聚合行情推送: 开:{}, 收:{}, 高:{}, 低:{}, 量:{}, 额:{}, 指数价:{}, 标记价:{}, 最优买价:{}, 最优卖价:{}, 涨跌幅:{}%, 时间:{}",
                    symbol, open, close, high, low, amount, volume, indexPrice, markPrice, bestBidPrice, bestAskPrice, ratePercent,
                    new Date(timestamp));
        }

        // 处理全量深度推送数据
        private void processDeepFullData(JSONObject json) {
            JSONObject data = json.getJSONObject("data");
            String symbol = data.getStr("s");
            String id = data.getStr("id");
            JSONArray asks = data.getJSONArray("a");
            JSONArray bids = data.getJSONArray("b");

            int askCount = asks.size();
            int bidCount = bids.size();

            Console.log("收到{}全量深度推送: ID:{}, 卖盘数量:{}, 买盘数量:{}",
                    symbol, id, askCount, bidCount);

            // 只打印前5档卖盘
            Console.log("卖盘前5档:");
            for (int i = 0; i < Math.min(5, askCount); i++) {
                JSONArray level = asks.getJSONArray(i);
                Console.log("  价格:{}, 数量:{}", level.get(0), level.get(1));
            }

            // 只打印前5档买盘
            Console.log("买盘前5档:");
            for (int i = 0; i < Math.min(5, bidCount); i++) {
                JSONArray level = bids.getJSONArray(i);
                Console.log("  价格:{}, 数量:{}", level.get(0), level.get(1));
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Console.log("合约市场WebSocket连接已关闭, 状态码: {}, 原因: {}, 由远端发起: {}",
                    code, reason, remote);

            if (heartbeatTimer != null) {
                heartbeatTimer.cancel();
                heartbeatTimer = null;
            }

            // 实现重连逻辑
            if (code != 1000) {  // 非正常关闭
                int attempts = reconnectAttempts.incrementAndGet();
                if (attempts <= MAX_RECONNECT_ATTEMPTS) {
                    long delay = RECONNECT_INTERVAL * attempts;
                    Console.log("将在 {}ms 后尝试第 {} 次重连...", delay, attempts);

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            reconnect();
                        }
                    }, delay);
                }
            }
        }

        @Override
        public void onError(Exception ex) {
            Console.error("合约市场WebSocket错误: {}", ex.getMessage());
        }
    }
}