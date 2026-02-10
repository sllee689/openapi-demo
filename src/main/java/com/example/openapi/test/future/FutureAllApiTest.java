package com.example.openapi.test.future;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.example.openapi.client.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 合约所有API综合测试类
 * 用于测试所有合约接口并验证文档正确性
 */
public class FutureAllApiTest {

    private static final Logger log = LoggerFactory.getLogger(FutureAllApiTest.class);
    private final ApiClient apiClient;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 测试结果统计
    private int totalTests = 0;
    private int passedTests = 0;
    private int failedTests = 0;
    private final List<String> failedTestNames = new ArrayList<>();

    public FutureAllApiTest(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static void main(String[] args) {
        ApiClient apiClient = new ApiClient(
            FutureTestConfig.BASE_URL,
            FutureTestConfig.ACCESS_KEY,
            FutureTestConfig.SECRET_KEY);

        FutureAllApiTest test = new FutureAllApiTest(apiClient);
        test.runAllTests();
    }

    /**
     * 运行所有测试
     */
    public void runAllTests() {
        log.info("========================================");
        log.info("开始测试所有合约API接口");
        log.info("========================================");

        // 公共接口测试
        log.info("\n========== 公共接口测试 ==========\n");
        testServerTime();
        testSymbolDetail();
        testTicker();
        testTickers();
        testKline();
        testDepth();
        testDeals();

        // 私有接口测试
        log.info("\n========== 私有接口测试 ==========\n");
        testUserBalance();
        testOrderCreate();
        testOrderDetail();
        testOrderList();
        testOrderTradeList();
        testOrderCancel();

        // 打印测试结果总结
        printTestSummary();
    }

    /**
     * 打印测试结果总结
     */
    private void printTestSummary() {
        log.info("\n========================================");
        log.info("测试结果总结");
        log.info("========================================");
        log.info("总测试数: {}", totalTests);
        log.info("通过: {}", passedTests);
        log.info("失败: {}", failedTests);

        if (!failedTestNames.isEmpty()) {
            log.info("\n失败的测试:");
            for (String name : failedTestNames) {
                log.info("  - {}", name);
            }
        }

        log.info("========================================");
    }

    /**
     * 记录测试结果
     */
    private void recordTestResult(String testName, boolean passed, String message) {
        totalTests++;
        if (passed) {
            passedTests++;
            log.info("✅ [PASS] {} - {}", testName, message);
        } else {
            failedTests++;
            failedTestNames.add(testName);
            log.error("❌ [FAIL] {} - {}", testName, message);
        }
    }

    // ==================== 公共接口测试 ====================

    /**
     * 测试获取服务器时间
     * 文档接口: GET /fut/v1/public/time
     */
    private void testServerTime() {
        String testName = "获取服务器时间 - /fut/v1/public/time";
        log.info("\n----- {} -----", testName);

        try {
            TreeMap<String, String> params = new TreeMap<>();
            String response = apiClient.sendGetRequest("/fut/v1/public/time", params, false);
            JSONObject json = new JSONObject(response);

            int code = json.getInt("code");
            if (code == 0) {
                Long serverTime = json.getLong("data");
                log.info("服务器时间戳: {}", serverTime);
                log.info("格式化时间: {}", sdf.format(new Date(serverTime)));

                // 验证返回字段
                boolean hasAllFields = json.containsKey("code") && json.containsKey("msg") && json.containsKey("data");
                recordTestResult(testName, hasAllFields, "返回格式正确，时间戳: " + serverTime);
            } else {
                recordTestResult(testName, false, "错误码: " + code + ", 消息: " + json.getStr("msg"));
            }
        } catch (Exception e) {
            recordTestResult(testName, false, "异常: " + e.getMessage());
        }
    }

    /**
     * 测试获取交易对详情
     * 文档接口: GET /fut/v1/public/symbol/detail
     */
    private void testSymbolDetail() {
        String testName = "获取交易对详情 - /fut/v1/public/symbol/detail";
        log.info("\n----- {} -----", testName);

        try {
            TreeMap<String, String> params = new TreeMap<>();
            params.put("symbol", "btc_usdt");
            String response = apiClient.sendGetRequest("/fut/v1/public/symbol/detail", params, false);
            JSONObject json = new JSONObject(response);

            int code = json.getInt("code");
            if (code == 0) {
                JSONObject data = json.getJSONObject("data");
                log.info("交易对: {}", data.getStr("symbol"));
                log.info("合约类型: {}", data.getStr("contractType"));
                log.info("合约面值: {}", data.getStr("contractSize"));
                log.info("价格精度: {}", data.getInt("pricePrecision"));
                log.info("数量精度: {}", data.getInt("quantityPrecision"));
                log.info("最小委托数量: {}", data.getStr("minQty"));
                log.info("支持订单类型: {}", data.getStr("supportOrderType"));

                // 验证关键字段
                boolean valid = data.containsKey("symbol") && data.containsKey("contractType") &&
                               data.containsKey("contractSize") && data.containsKey("pricePrecision");
                recordTestResult(testName, valid, "交易对信息获取成功");
            } else {
                recordTestResult(testName, false, "错误码: " + code);
            }
        } catch (Exception e) {
            recordTestResult(testName, false, "异常: " + e.getMessage());
        }
    }

    /**
     * 测试获取单个合约行情
     * 文档接口: GET /fut/v1/public/q/ticker
     */
    private void testTicker() {
        String testName = "获取单个合约行情 - /fut/v1/public/q/ticker";
        log.info("\n----- {} -----", testName);

        try {
            TreeMap<String, String> params = new TreeMap<>();
            params.put("symbol", "btc_usdt");
            String response = apiClient.sendGetRequest("/fut/v1/public/q/ticker", params, false);
            JSONObject json = new JSONObject(response);

            int code = json.getInt("code");
            if (code == 0) {
                JSONObject data = json.getJSONObject("data");
                log.info("交易对: {}", data.getStr("s"));
                log.info("最新价格: {}", data.getStr("c"));
                log.info("24小时最高价: {}", data.getStr("h"));
                log.info("24小时最低价: {}", data.getStr("l"));
                log.info("24小时成交量: {}", data.getStr("a"));
                log.info("24小时成交额: {}", data.getStr("v"));
                log.info("24小时涨跌幅: {}", data.getStr("r"));

                boolean valid = data.containsKey("s") && data.containsKey("c") &&
                               data.containsKey("h") && data.containsKey("l");
                recordTestResult(testName, valid, "行情数据获取成功");
            } else {
                recordTestResult(testName, false, "错误码: " + code);
            }
        } catch (Exception e) {
            recordTestResult(testName, false, "异常: " + e.getMessage());
        }
    }

    /**
     * 测试获取全部合约行情
     * 文档接口: GET /fut/v1/public/q/tickers
     */
    private void testTickers() {
        String testName = "获取全部合约行情 - /fut/v1/public/q/tickers";
        log.info("\n----- {} -----", testName);

        try {
            TreeMap<String, String> params = new TreeMap<>();
            String response = apiClient.sendGetRequest("/fut/v1/public/q/tickers", params, false);
            JSONObject json = new JSONObject(response);

            int code = json.getInt("code");
            if (code == 0) {
                JSONArray data = json.getJSONArray("data");
                log.info("获取到 {} 个交易对的行情数据", data.size());

                // 打印前3个
                for (int i = 0; i < Math.min(3, data.size()); i++) {
                    JSONObject ticker = data.getJSONObject(i);
                    log.info("  交易对: {}, 最新价: {}, 涨跌幅: {}",
                            ticker.getStr("s"), ticker.getStr("c"), ticker.getStr("r"));
                }

                recordTestResult(testName, data.size() > 0, "获取到 " + data.size() + " 个交易对行情");
            } else {
                recordTestResult(testName, false, "错误码: " + code);
            }
        } catch (Exception e) {
            recordTestResult(testName, false, "异常: " + e.getMessage());
        }
    }

    /**
     * 测试获取K线数据
     * 文档接口: GET /fut/v1/public/q/kline
     */
    private void testKline() {
        String testName = "获取K线数据 - /fut/v1/public/q/kline";
        log.info("\n----- {} -----", testName);

        try {
            TreeMap<String, String> params = new TreeMap<>();
            params.put("symbol", "btc_usdt");
            params.put("interval", "1h");
            params.put("limit", "10");
            String response = apiClient.sendGetRequest("/fut/v1/public/q/kline", params, false);
            JSONObject json = new JSONObject(response);

            int code = json.getInt("code");
            if (code == 0) {
                JSONArray data = json.getJSONArray("data");
                log.info("获取到 {} 条K线数据", data.size());

                // 打印最新的K线
                if (data.size() > 0) {
                    JSONObject kline = data.getJSONObject(data.size() - 1);
                    log.info("最新K线 - 时间: {}, 开: {}, 高: {}, 低: {}, 收: {}, 量: {}",
                            sdf.format(new Date(kline.getLong("t"))),
                            kline.getStr("o"), kline.getStr("h"),
                            kline.getStr("l"), kline.getStr("c"), kline.getStr("a"));
                }

                recordTestResult(testName, data.size() > 0, "获取到 " + data.size() + " 条K线数据");
            } else {
                recordTestResult(testName, false, "错误码: " + code);
            }
        } catch (Exception e) {
            recordTestResult(testName, false, "异常: " + e.getMessage());
        }
    }

    /**
     * 测试获取深度数据
     * 文档接口: GET /fut/v1/public/q/depth
     */
    private void testDepth() {
        String testName = "获取深度数据 - /fut/v1/public/q/depth";
        log.info("\n----- {} -----", testName);

        try {
            TreeMap<String, String> params = new TreeMap<>();
            params.put("symbol", "btc_usdt");
            params.put("level", "10");
            String response = apiClient.sendGetRequest("/fut/v1/public/q/depth", params, false);
            JSONObject json = new JSONObject(response);

            int code = json.getInt("code");
            if (code == 0) {
                JSONObject data = json.getJSONObject("data");
                JSONArray bids = data.getJSONArray("b");
                JSONArray asks = data.getJSONArray("a");

                log.info("交易对: {}", data.getStr("s"));
                log.info("买单档数: {}", bids.size());
                log.info("卖单档数: {}", asks.size());

                // 打印前3档
                log.info("买单前3档:");
                for (int i = 0; i < Math.min(3, bids.size()); i++) {
                    JSONArray bid = bids.getJSONArray(i);
                    log.info("  价格: {}, 数量: {}", bid.get(0), bid.get(1));
                }

                log.info("卖单前3档:");
                for (int i = 0; i < Math.min(3, asks.size()); i++) {
                    JSONArray ask = asks.getJSONArray(i);
                    log.info("  价格: {}, 数量: {}", ask.get(0), ask.get(1));
                }

                boolean valid = bids.size() > 0 && asks.size() > 0;
                recordTestResult(testName, valid, "买单: " + bids.size() + "档, 卖单: " + asks.size() + "档");
            } else {
                recordTestResult(testName, false, "错误码: " + code);
            }
        } catch (Exception e) {
            recordTestResult(testName, false, "异常: " + e.getMessage());
        }
    }

    /**
     * 测试获取最新成交
     * 文档接口: GET /fut/v1/public/q/deal
     */
    private void testDeals() {
        String testName = "获取最新成交 - /fut/v1/public/q/deal";
        log.info("\n----- {} -----", testName);

        try {
            TreeMap<String, String> params = new TreeMap<>();
            params.put("symbol", "btc_usdt");
            params.put("num", "10");
            String response = apiClient.sendGetRequest("/fut/v1/public/q/deal", params, false);
            JSONObject json = new JSONObject(response);

            int code = json.getInt("code");
            if (code == 0) {
                JSONArray data = json.getJSONArray("data");
                log.info("获取到 {} 条成交记录", data.size());

                // 打印前3条
                for (int i = 0; i < Math.min(3, data.size()); i++) {
                    JSONObject deal = data.getJSONObject(i);
                    log.info("  价格: {}, 数量: {}, 方向: {}, 时间: {}",
                            deal.getStr("p"), deal.getStr("a"),
                            deal.getStr("m"), sdf.format(new Date(deal.getLong("t"))));
                }

                recordTestResult(testName, data.size() > 0, "获取到 " + data.size() + " 条成交记录");
            } else {
                recordTestResult(testName, false, "错误码: " + code);
            }
        } catch (Exception e) {
            recordTestResult(testName, false, "异常: " + e.getMessage());
        }
    }

    // ==================== 私有接口测试 ====================

    /**
     * 测试获取用户资金
     * 文档接口: GET /fut/v1/balance/list
     */
    private void testUserBalance() {
        String testName = "获取用户资金 - /fut/v1/balance/list";
        log.info("\n----- {} -----", testName);

        try {
            TreeMap<String, String> params = new TreeMap<>();
            String response = apiClient.sendGetRequest("/fut/v1/balance/list", params, true);
            JSONObject json = new JSONObject(response);

            int code = json.getInt("code");
            if (code == 0) {
                JSONArray data = json.getJSONArray("data");
                log.info("获取到 {} 条资金记录", data.size());

                for (int i = 0; i < data.size(); i++) {
                    JSONObject balance = data.getJSONObject(i);
                    log.info("  币种: {}, 类型: {}, 钱包余额: {}, 可用余额: {}",
                            balance.getStr("coin"), balance.getStr("balanceType"),
                            balance.getStr("walletBalance"), balance.getStr("availableBalance"));
                }

                recordTestResult(testName, true, "获取到 " + data.size() + " 条资金记录");
            } else {
                recordTestResult(testName, false, "错误码: " + code + ", 消息: " + json.getStr("msg"));
            }
        } catch (Exception e) {
            recordTestResult(testName, false, "异常: " + e.getMessage());
        }
    }

    /**
     * 测试创建订单
     * 文档接口: POST /fut/v1/order/create
     */
    private Long testOrderCreate() {
        String testName = "创建订单 - /fut/v1/order/create";
        log.info("\n----- {} -----", testName);

        Long orderId = null;
        try {
            // 先获取合约面值
            TreeMap<String, String> symbolParams = new TreeMap<>();
            symbolParams.put("symbol", "btc_usdt");
            String symbolResponse = apiClient.sendGetRequest("/fut/v1/public/symbol/detail", symbolParams, false);
            JSONObject symbolJson = new JSONObject(symbolResponse);
            String contractSize = symbolJson.getJSONObject("data").getStr("contractSize");

            // 计算下单数量 (0.001 BTC / 合约面值)
            BigDecimal origQty = new BigDecimal("0.001").divide(new BigDecimal(contractSize), 0, RoundingMode.DOWN);
            if (origQty.compareTo(BigDecimal.ZERO) <= 0) {
                origQty = BigDecimal.ONE; // 至少1张合约
            }

            TreeMap<String, String> params = new TreeMap<>();
            params.put("symbol", "btc_usdt");
            params.put("orderSide", "BUY");
            params.put("orderType", "LIMIT");
            params.put("positionSide", "LONG");
            params.put("origQty", origQty.toString());
            params.put("price", "70000"); // 较低价格，确保不会成交
            params.put("timeInForce", "GTC");
            params.put("leverage", "5");

            String response = apiClient.sendPostRequest("/fut/v1/order/create", params);
            JSONObject json = new JSONObject(response);

            int code = json.getInt("code");
            if (code == 0) {
                String orderIdStr = json.getStr("data");
                orderId = Long.parseLong(orderIdStr);
                log.info("创建订单成功，订单ID: {}", orderId);
                recordTestResult(testName, true, "订单ID: " + orderId);
            } else {
                recordTestResult(testName, false, "错误码: " + code + ", 消息: " + json.getStr("msg"));
            }
        } catch (Exception e) {
            recordTestResult(testName, false, "异常: " + e.getMessage());
        }

        return orderId;
    }

    /**
     * 测试查询订单详情
     * 文档接口: GET /fut/v1/order/detail
     */
    private void testOrderDetail() {
        String testName = "查询订单详情 - /fut/v1/order/detail";
        log.info("\n----- {} -----", testName);

        try {
            // 先创建一个订单
            Long orderId = createTestOrder();
            if (orderId == null) {
                recordTestResult(testName, false, "无法创建测试订单");
                return;
            }

            // 等待订单处理
            Thread.sleep(500);

            TreeMap<String, String> params = new TreeMap<>();
            params.put("orderId", orderId.toString());
            String response = apiClient.sendGetRequest("/fut/v1/order/detail", params, true);
            JSONObject json = new JSONObject(response);

            int code = json.getInt("code");
            if (code == 0) {
                JSONObject data = json.getJSONObject("data");
                log.info("订单ID: {}", data.getStr("orderId"));
                log.info("交易对: {}", data.getStr("symbol"));
                log.info("订单类型: {}", data.getStr("orderType"));
                log.info("买卖方向: {}", data.getStr("orderSide"));
                log.info("价格: {}", data.getStr("price"));
                log.info("数量: {}", data.getStr("origQty"));
                log.info("状态: {}", data.getStr("state"));
                log.info("杠杆: {}", data.getInt("leverage"));

                // 检查 clientOrderId 字段（文档中说此字段已废弃）
                if (data.containsKey("clientOrderId")) {
                    log.warn("注意: 响应中包含 clientOrderId 字段: {}", data.getStr("clientOrderId"));
                }

                recordTestResult(testName, true, "订单详情获取成功");
            } else {
                recordTestResult(testName, false, "错误码: " + code);
            }

            // 清理: 取消订单
            cancelTestOrder(orderId);
        } catch (Exception e) {
            recordTestResult(testName, false, "异常: " + e.getMessage());
        }
    }

    /**
     * 测试查询订单列表
     * 文档接口: GET /fut/v1/order/list
     */
    private void testOrderList() {
        String testName = "查询订单列表 - /fut/v1/order/list";
        log.info("\n----- {} -----", testName);

        try {
            TreeMap<String, String> params = new TreeMap<>();
            params.put("symbol", "btc_usdt");
            params.put("page", "1");
            params.put("size", "10");

            String response = apiClient.sendGetRequest("/fut/v1/order/list", params, true);
            JSONObject json = new JSONObject(response);

            int code = json.getInt("code");
            if (code == 0) {
                JSONObject data = json.getJSONObject("data");
                log.info("当前页: {}", data.getInt("page"));
                log.info("每页大小: {}", data.getInt("ps"));
                log.info("总记录数: {}", data.getLong("total"));

                JSONArray items = data.getJSONArray("items");
                log.info("本页订单数: {}", items.size());

                // 打印前3条订单
                for (int i = 0; i < Math.min(3, items.size()); i++) {
                    JSONObject order = items.getJSONObject(i);
                    log.info("  订单ID: {}, 类型: {}, 方向: {}, 价格: {}, 状态: {}",
                            order.getStr("orderId"), order.getStr("orderType"),
                            order.getStr("orderSide"), order.getStr("price"), order.getStr("state"));

                    // 检查 clientOrderId 字段
                    if (order.containsKey("clientOrderId")) {
                        log.warn("  注意: 订单列表中包含 clientOrderId 字段: {}", order.getStr("clientOrderId"));
                    }
                }

                recordTestResult(testName, true, "获取到 " + items.size() + " 条订单记录");
            } else {
                recordTestResult(testName, false, "错误码: " + code);
            }
        } catch (Exception e) {
            recordTestResult(testName, false, "异常: " + e.getMessage());
        }
    }

    /**
     * 测试查询订单成交明细
     * 文档接口: GET /fut/v1/order/trade-list
     */
    private void testOrderTradeList() {
        String testName = "查询订单成交明细 - /fut/v1/order/trade-list";
        log.info("\n----- {} -----", testName);

        try {
            TreeMap<String, String> params = new TreeMap<>();
            params.put("symbol", "btc_usdt");
            params.put("page", "1");
            params.put("size", "10");

            String response = apiClient.sendGetRequest("/fut/v1/order/trade-list", params, true);
            JSONObject json = new JSONObject(response);

            int code = json.getInt("code");
            if (code == 0) {
                JSONObject data = json.getJSONObject("data");
                log.info("当前页: {}", data.getInt("page"));
                log.info("每页大小: {}", data.getInt("ps"));
                log.info("总记录数: {}", data.getLong("total"));

                JSONArray items = data.getJSONArray("items");
                log.info("本页成交明细数: {}", items.size());

                // 打印前3条成交明细
                for (int i = 0; i < Math.min(3, items.size()); i++) {
                    JSONObject trade = items.getJSONObject(i);
                    log.info("  订单ID: {}, 成交ID: {}, 价格: {}, 数量: {}, 手续费: {}",
                            trade.getStr("orderId"), trade.getStr("execId"),
                            trade.getStr("price"), trade.getStr("quantity"), trade.getStr("fee"));
                }

                recordTestResult(testName, true, "获取到 " + items.size() + " 条成交明细");
            } else {
                recordTestResult(testName, false, "错误码: " + code);
            }
        } catch (Exception e) {
            recordTestResult(testName, false, "异常: " + e.getMessage());
        }
    }

    /**
     * 测试取消订单
     * 文档接口: POST /fut/v1/order/cancel
     */
    private void testOrderCancel() {
        String testName = "取消订单 - /fut/v1/order/cancel";
        log.info("\n----- {} -----", testName);

        try {
            // 先创建一个订单
            Long orderId = createTestOrder();
            if (orderId == null) {
                recordTestResult(testName, false, "无法创建测试订单");
                return;
            }

            // 等待订单处理
            Thread.sleep(500);

            TreeMap<String, String> params = new TreeMap<>();
            params.put("orderId", orderId.toString());
            String response = apiClient.sendPostRequest("/fut/v1/order/cancel", params);
            JSONObject json = new JSONObject(response);

            int code = json.getInt("code");
            if (code == 0) {
                log.info("取消订单成功，订单ID: {}", orderId);
                recordTestResult(testName, true, "订单取消成功");
            } else {
                recordTestResult(testName, false, "错误码: " + code + ", 消息: " + json.getStr("msg"));
            }
        } catch (Exception e) {
            recordTestResult(testName, false, "异常: " + e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试订单
     */
    private Long createTestOrder() {
        try {
            // 获取合约面值
            TreeMap<String, String> symbolParams = new TreeMap<>();
            symbolParams.put("symbol", "btc_usdt");
            String symbolResponse = apiClient.sendGetRequest("/fut/v1/public/symbol/detail", symbolParams, false);
            JSONObject symbolJson = new JSONObject(symbolResponse);
            String contractSize = symbolJson.getJSONObject("data").getStr("contractSize");

            // 计算下单数量
            BigDecimal origQty = new BigDecimal("0.001").divide(new BigDecimal(contractSize), 0, RoundingMode.DOWN);
            if (origQty.compareTo(BigDecimal.ZERO) <= 0) {
                origQty = BigDecimal.ONE;
            }

            TreeMap<String, String> params = new TreeMap<>();
            params.put("symbol", "btc_usdt");
            params.put("orderSide", "BUY");
            params.put("orderType", "LIMIT");
            params.put("positionSide", "LONG");
            params.put("origQty", origQty.toString());
            params.put("price", "70000");
            params.put("timeInForce", "GTC");
            params.put("leverage", "5");

            String response = apiClient.sendPostRequest("/fut/v1/order/create", params);
            JSONObject json = new JSONObject(response);

            if (json.getInt("code") == 0) {
                return Long.parseLong(json.getStr("data"));
            }
        } catch (Exception e) {
            log.error("创建测试订单失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 取消测试订单
     */
    private void cancelTestOrder(Long orderId) {
        try {
            TreeMap<String, String> params = new TreeMap<>();
            params.put("orderId", orderId.toString());
            apiClient.sendPostRequest("/fut/v1/order/cancel", params);
        } catch (Exception e) {
            log.warn("取消测试订单失败: {}", e.getMessage());
        }
    }

}
