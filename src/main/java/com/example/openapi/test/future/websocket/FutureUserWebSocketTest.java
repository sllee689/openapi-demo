package com.example.openapi.test.future.websocket;

import cn.hutool.core.lang.Console;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.test.future.FutureTestConfig;
import com.example.openapi.utils.HashexApiUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 合约交易用户数据WebSocket客户端
 */
public class FutureUserWebSocketTest {
    // 服务器地址
    private static final String HOST = FutureTestConfig.BASE_URL;
    private static final String WS_HOST = FutureTestConfig.WS_BASE_URL;
    // API凭证
    private static final String API_KEY = FutureTestConfig.ACCESS_KEY;
    private static final String SECRET_KEY = FutureTestConfig.SECRET_KEY;

    private static final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final int RECONNECT_INTERVAL = 5000;

    public static void main(String[] args) {
        try {
            // 获取listenKey
            String listenKey = getListenKey();
            if (listenKey == null) {
                Console.error("获取listenKey失败，无法订阅用户数据");
                return;
            }

            // 构建合约用户WebSocket URL
            String wsUrl = String.format("%s/fut/v1/ws/user", WS_HOST);
            Console.log("正在连接合约用户WebSocket: {}", wsUrl);

            // 创建客户端
            FutureUserWSClient client = new FutureUserWSClient(new URI(wsUrl), listenKey);

            // 连接服务器
            client.connect();

            // 保持程序运行
            long runMillis = getRunMillis(args, 60000);
            Thread.sleep(runMillis);

            // 关闭连接
            client.close();
        } catch (Exception e) {
            Console.error("合约用户WebSocket连接失败: {}", e.getMessage());
        }
    }

    /**
     * 获取listenKey
     */
    private static String getListenKey() {
        try {
            // 构建请求URL
            String url = HOST + "/fut/v1/user/listen-key";

            // 获取当前时间戳和随机数
            long timestamp = System.currentTimeMillis();
            String nonce = UUID.randomUUID().toString();

            // 构建签名参数（按照API要求）
            TreeMap<String, String> params = new TreeMap<>();

            // 计算签名
            String signature = HashexApiUtils.generateSignature(SECRET_KEY, params, String.valueOf(timestamp));

            // 发送请求
            String response = HttpRequest.get(url)
                    .header("X-Access-Key", API_KEY)
                    .header("X-Request-Timestamp", String.valueOf(timestamp))
                    .header("X-Request-Nonce", nonce)
                    .header("X-Signature", signature)
                    .execute()
                    .body();

            // 解析响应
            JSONObject json = JSONUtil.parseObj(response);

            if (json.getInt("code") == 0) {
                String listenKey = json.getStr("data");
                Console.log("获取listenKey成功: {}", listenKey);
                return listenKey;
            } else {
                Console.error("获取listenKey失败: {}", json);
                return null;
            }
        } catch (Exception e) {
            Console.error("请求listenKey异常: {}", e.getMessage());
            return null;
        }
    }

    private static class FutureUserWSClient extends WebSocketClient {
        private Timer heartbeatTimer;
        private final String listenKey;

        public FutureUserWSClient(URI serverUri, String listenKey) {
            super(serverUri);
            this.listenKey = listenKey;
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Console.log("合约用户WebSocket连接已建立: {}", handshakedata.getHttpStatus());
            reconnectAttempts.set(0);

            // 发送心跳
            sendHeartbeat();

            // 设置定时心跳
            startHeartbeatTimer();

            // 使用listenKey订阅用户数据
            subscribeUserData();
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

        private void subscribeUserData() {
            try {
                JSONObject subscribeMsg = JSONUtil.createObj();
                subscribeMsg.set("req", "sub_user");
                subscribeMsg.set("listenKey", listenKey);

                String subscribeText = subscribeMsg.toString();
                this.send(subscribeText);
                Console.log("已发送用户数据订阅请求: {}", subscribeText);
            } catch (Exception e) {
                Console.error("发送用户数据订阅失败: {}", e.getMessage());
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

                    // 处理订阅响应
                    if (json.containsKey("status")) {
                        processStatusResponse(json);
                        return;
                    }

                    // 处理推送数据
                    if (json.containsKey("channel")) {
                        String channel = json.getStr("channel");

                        switch (channel) {
                            case "user.balance":
                                processBalanceData(json);
                                break;
                            case "user.position":
                                processPositionData(json);
                                break;
                            case "user.position.conf":
                                processPositionConfData(json);
                                break;
                            case "user.order":
                                processOrderData(json);
                                break;
                            case "user.trade":
                                processTradeData(json);
                                break;
                            default:
                                Console.log("收到未知频道消息: {}", message);
                                break;
                        }
                    } else {
                        Console.log("收到未识别消息: {}", message);
                    }
                } else {
                    Console.log("收到非JSON格式消息: {}", message);
                }
            } catch (Exception e) {
                Console.log("消息解析失败: {}, 内容: {}", e.getMessage(), message);
            }
        }

        /**
         * 处理用户余额数据
         */
        private void processBalanceData(JSONObject json) {
            JSONObject data = json.getJSONObject("data");
            String coin = data.getStr("coin");
            String balanceType = data.getStr("balanceType");
            int underlyingType = data.getInt("underlyingType");
            String walletBalance = data.getStr("walletBalance");
            String openOrderMarginFrozen = data.getStr("openOrderMarginFrozen");
            String isolatedMargin = data.getStr("isolatedMargin");
            String crossedMargin = data.getStr("crossedMargin");
            String availableBalance = data.getStr("availableBalance");

            String underlyingTypeStr = underlyingType == 1 ? "币本位" : "U本位";

            Console.log("收到余额更新: 币种:{}, 账户类型:{}, 类型:{}, 钱包余额:{}, 委托冻结:{}, " +
                            "逐仓保证金:{}, 全仓保证金:{}, 可用余额:{}",
                    coin, balanceType, underlyingTypeStr, walletBalance, openOrderMarginFrozen,
                    isolatedMargin, crossedMargin, availableBalance);
        }

        /**
         * 处理用户持仓数据
         */
        private void processPositionData(JSONObject json) {
            JSONObject data = json.getJSONObject("data");
            String symbol = data.getStr("symbol");
            String positionId = data.getStr("positionId");
            String contractType = data.getStr("contractType");
            String positionType = data.getStr("positionType");
            String positionModel = data.getStr("positionModel");
            String positionSide = data.getStr("positionSide");
            String positionSize = data.getStr("positionSize");
            String availableCloseSize = data.getStr("availableCloseSize");
            String entryPrice = data.getStr("entryPrice");
            String isolatedMargin = data.getStr("isolatedMargin");
            String openOrderMarginFrozen = data.getStr("openOrderMarginFrozen");
            String leverage = data.getStr("leverage");
            String unsettledProfit = data.getStr("unsettledProfit");
            boolean work = data.getBool("work", true);

            // 转换持仓方向为更友好的显示
            String directionStr = "LONG".equals(positionSide) ? "多" : "空";

            // 转换保证金类型为更友好的显示
            String positionTypeStr = "CROSSED".equals(positionType) ? "全仓" : "逐仓";

            // 转换持仓模式为更友好的显示
            String positionModelStr = "AGGREGATION".equals(positionModel) ? "聚合" : "单向";

            Console.log("收到持仓更新: 交易对:{}, 仓位ID:{}, 合约类型:{}, 保证金类型:{}, 持仓模式:{}, " +
                            "方向:{}, 持仓量:{}, 可平量:{}, 开仓均价:{}, 保证金:{}, 委托冻结:{}, " +
                            "杠杆:{}, 未结盈亏:{}, 有效:{}",
                    symbol, positionId, contractType, positionTypeStr, positionModelStr,
                    directionStr, positionSize, availableCloseSize, entryPrice, isolatedMargin,
                    openOrderMarginFrozen, leverage, unsettledProfit, work ? "是" : "否");
        }

        /**
         * 处理用户持仓配置数据
         */
        private void processPositionConfData(JSONObject json) {
            JSONObject data = json.getJSONObject("data");
            String symbol = data.getStr("symbol");
            String positionType = data.getStr("positionType");
            String positionModel = data.getStr("positionModel");
            String positionSide = data.getStr("positionSide");
            String leverage = data.getStr("leverage");

            // 转换为友好显示
            String positionTypeStr = "CROSSED".equals(positionType) ? "全仓" : "逐仓";
            String positionModelStr = "AGGREGATION".equals(positionModel) ? "聚合" : "单向";
            String directionStr = "LONG".equals(positionSide) ? "多" : "空";

            Console.log("收到持仓配置: 交易对:{}, 保证金类型:{}, 持仓模式:{}, 方向:{}, 杠杆:{}",
                    symbol, positionTypeStr, positionModelStr, directionStr, leverage);
        }

        /**
         * 处理用户订单数据
         */
        private void processOrderData(JSONObject json) {
            JSONObject data = json.getJSONObject("data");
            String symbol = data.getStr("symbol");
            String contractType = data.getStr("contractType");
            String orderId = data.getStr("orderId");
            String origQty = data.getStr("origQty");
            String avgPrice = data.getStr("avgPrice");
            String price = data.getStr("price");
            String executedQty = data.getStr("executedQty");
            String orderSide = data.getStr("orderSide");
            String positionSide = data.getStr("positionSide");
            String marginFrozen = data.getStr("marginFrozen");
            String state = data.getStr("state");
            String sourceType = data.getStr("sourceType");
            long createTime = data.getLong("createTime");

            // 转换为友好显示
            String sideStr = "BUY".equals(orderSide) ? "买入" : "卖出";
            String positionSideStr = "LONG".equals(positionSide) ? "做多" : "做空";
            String stateStr;
            switch (state) {
                case "NEW": stateStr = "未成交"; break;
                case "PARTIALLY_FILLED": stateStr = "部分成交"; break;
                case "FILLED": stateStr = "完全成交"; break;
                case "CANCELED": stateStr = "已撤单"; break;
                case "REJECTED": stateStr = "已拒绝"; break;
                case "EXPIRED": stateStr = "已过期"; break;
                default: stateStr = state;
            }

            Console.log("收到订单更新: 交易对:{}, 合约:{}, 订单ID:{}, {}{}, " +
                            "价格:{}, 数量:{}, 已成交:{}, 成交均价:{}, " +
                            "冻结保证金:{}, 来源:{}, 状态:{}, 时间:{}",
                    symbol, contractType, orderId, sideStr, positionSideStr,
                    price, origQty, executedQty, avgPrice,
                    marginFrozen, sourceType, stateStr, new Date(createTime));
        }

        /**
         * 处理用户成交数据
         */
        private void processTradeData(JSONObject json) {
            JSONObject data = json.getJSONObject("data");
            String orderId = data.getStr("orderId");
            String price = data.getStr("price");
            String quantity = data.getStr("quantity");
            String marginUnfrozen = data.getStr("marginUnfrozen");
            long timestamp = data.getLong("timestamp");

            Console.log("收到成交更新: 订单ID:{}, 成交价格:{}, 成交数量:{}, 释放保证金:{}, 时间:{}",
                    orderId, price, quantity, marginUnfrozen, new Date(timestamp));
        }

        /**
         * 处理订阅响应
         */
        private void processStatusResponse(JSONObject json) {
            String status = json.getStr("status");
            String reqType = json.getStr("req");

            if ("ok".equals(status)) {
                Console.log("订阅请求 [{}] 成功", reqType);
            } else {
                Console.error("订阅请求 [{}] 失败: {}", reqType, json);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Console.log("合约用户WebSocket连接已关闭, 状态码: {}, 原因: {}, 由远端发起: {}",
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
                } else {
                    Console.error("已达到最大重连次数 ({}), 停止重连", MAX_RECONNECT_ATTEMPTS);
                }
            }
        }

        @Override
        public void onError(Exception ex) {
            Console.error("合约用户WebSocket错误: {}", ex.getMessage());
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
}