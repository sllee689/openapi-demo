package com.example.openapi.test.spot.order;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.TreeMap;

/**
 * 订单撤销测试类
 */
public class OrderCancelTest {

    private static final Logger log = LoggerFactory.getLogger(OrderCancelTest.class);
    private static ApiClient apiClient;

    /**
     * 撤销订单
     *
     * @param orderId 订单ID
     * @return 撤单结果
     * @throws HashExApiException 如果API调用失败
     */
    public String cancelOrder(Long orderId) throws HashExApiException {
        try {
            // 验证参数
            if (orderId == null || orderId <= 0) {
                throw new IllegalArgumentException("订单ID无效");
            }

            // 创建参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();
            queryParams.put("orderId", orderId.toString());

            // 调用API
            String responseJson = apiClient.sendPostRequest("/spot/v1/u/trade/order/cancel", queryParams);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);
            ApiResponse<String> apiResponse = JSONUtil.toBean(jsonObject, ApiResponse.class);

            if (!apiResponse.isSuccess()) {
                throw new HashExApiException("撤销订单失败: " + apiResponse.getMessage());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("撤销订单时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 创建限价订单并获取订单ID
     *
     * @param symbol 交易对
     * @param direction 买卖方向
     * @param amount 数量
     * @param price 价格
     * @return 订单ID
     * @throws HashExApiException 如果API调用失败
     */
    private String createLimitOrder(String symbol, String direction, BigDecimal amount, BigDecimal price) throws HashExApiException {
        try {
            // 创建订单请求对象
            OrderCreateTest.OrderRequest orderRequest = null;

            if ("BUY".equals(direction)) {
                orderRequest = OrderCreateTest.OrderRequest.limitBuy(symbol, amount, price);
            } else {
                orderRequest = OrderCreateTest.OrderRequest.limitSell(symbol, amount, price);
            }

            // 创建参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();
            queryParams.put("symbol", orderRequest.getSymbol());
            queryParams.put("direction", orderRequest.getDirection());
            queryParams.put("totalAmount", orderRequest.getTotalAmount().toPlainString());
            queryParams.put("tradeType", orderRequest.getTradeType());
            queryParams.put("price", orderRequest.getPrice().toPlainString());

            if (orderRequest.getClientOrderId() != null) {
                queryParams.put("clientOrderId", orderRequest.getClientOrderId());
            }

            queryParams.put("balanceType", orderRequest.getBalanceType().toString());

            // 调用API
            String responseJson = apiClient.sendPostRequest("/spot/v1/u/trade/order/create", queryParams);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);
            ApiResponse<String> apiResponse = JSONUtil.toBean(jsonObject, ApiResponse.class);

            if (!apiResponse.isSuccess()) {
                throw new HashExApiException("创建订单失败: " + apiResponse.getMessage());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("创建订单时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试撤销买单
     */
    private void testCancelBuyOrder() throws HashExApiException {
        log.info("===== 测试撤销买单 =====");

        // 1. 创建一个限价买单 (价格设置较低以确保不会立即成交)
        String symbol = "BTC_USDT";
        BigDecimal amount = new BigDecimal("0.001");  // 买入0.001个BTC
        BigDecimal price = new BigDecimal("50000");   // 价格设置较低

        log.info("步骤1: 创建限价买单 - 币对:{}, 数量:{}, 价格:{}", symbol, amount, price);
        String orderId = createLimitOrder(symbol, "BUY", amount, price);
        log.info("限价买单创建成功，订单ID: {}", orderId);

        // 等待1秒，确保订单已处理
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 2. 撤销该买单
        log.info("步骤2: 撤销买单 - 订单ID: {}", orderId);
        String result = cancelOrder(Long.parseLong(orderId));
        log.info("撤销买单成功，结果: {}", result);
    }

    /**
     * 测试撤销卖单
     */
    private void testCancelSellOrder() throws HashExApiException {
        log.info("===== 测试撤销卖单 =====");

        // 1. 创建一个限价卖单 (价格设置较高以确保不会立即成交)
        String symbol = "BTC_USDT";
        BigDecimal amount = new BigDecimal("0.001");  // 卖出0.001个BTC
        BigDecimal price = new BigDecimal("100000");  // 价格设置较高

        log.info("步骤1: 创建限价卖单 - 币对:{}, 数量:{}, 价格:{}", symbol, amount, price);
        String orderId = createLimitOrder(symbol, "SELL", amount, price);
        log.info("限价卖单创建成功，订单ID: {}", orderId);

        // 等待1秒，确保订单已处理
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 2. 撤销该卖单
        log.info("步骤2: 撤销卖单 - 订单ID: {}", orderId);
        String result = cancelOrder(Long.parseLong(orderId));
        log.info("撤销卖单成功，结果: {}", result);
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient("https://open.hashex.vip",
                "0a9970e8986247d6e6d5deadc886a4e558c0a1c4f2047c2a00bc96e2efd24499",
                "yZGQ4OWExMjVmMWViYWE1MmU0ZGQwY2ZmODQ4NDI0ZWI0OWU1MTUyNmUyZDU4NWJmZWRmYmM4ZDA1NWEyYjAxMmE=");
        OrderCancelTest cancelTest = new OrderCancelTest();

        // 测试撤销买单
        cancelTest.testCancelBuyOrder();

        // 等待2秒
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 测试撤销卖单
        cancelTest.testCancelSellOrder();
    }
}