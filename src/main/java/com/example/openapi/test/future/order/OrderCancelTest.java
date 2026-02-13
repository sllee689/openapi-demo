package com.example.openapi.test.future.order;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import com.example.openapi.test.future.FutureTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.TreeMap;

/**
 * 合约订单撤销测试类
 */
public class OrderCancelTest {

    private static final Logger log = LoggerFactory.getLogger(OrderCancelTest.class);
    private static ApiClient apiClient;

    /**
     * 撤销合约订单
     *
     * @param orderId 订单ID
     * @return 撤单结果
     * @throws HashExApiException 如果API调用失败
     */
    public Object cancelOrder(Long orderId) throws HashExApiException {
        try {
            // 验证参数
            if (orderId == null || orderId <= 0) {
                throw new IllegalArgumentException("订单ID无效");
            }

            // 创建参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();
            queryParams.put("orderId", orderId.toString());

            // 调用API
            String responseJson = apiClient.sendPostRequest("/fut/v1/order/cancel", queryParams);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);
            ApiResponse<Object> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<Object>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("撤销订单失败: " + apiResponse.getMsg());
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
     * 测试撤销限价买单
     */
    private void testCancelLimitOrder() throws HashExApiException {
        log.info("===== 测试撤销合约限价单 =====");

        // 创建订单测试对象
        OrderCreateTest orderCreateTest = new OrderCreateTest(apiClient);

        // 1. 创建一个限价买单（价格设置较低以确保不会立即成交）
        log.info("步骤1: 创建限价买单");
        OrderCreateTest.OrderRequest request = new OrderCreateTest.OrderRequest();
        request.setSymbol("btc_usdt");
        request.setOrderSide("BUY");
        request.setOrderType("LIMIT");
        request.setPositionSide("LONG");
        request.setOrigQty(new BigDecimal("0.01"));
        request.setPrice(new BigDecimal("60000")); // 设置较低价格确保不会成交
        request.setLeverage(20);

        Object createResult = orderCreateTest.createOrder(request);
        log.info("限价买单创建结果: {}", createResult);

        // 提取订单ID
        Long orderId = extractOrderId(createResult);
        log.info("获取到订单ID: {}", orderId);

        // 等待1秒，确保订单已处理
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 2. 撤销该买单
        log.info("步骤2: 撤销买单 - 订单ID: {}", orderId);
        Object result = cancelOrder(orderId);
        log.info("撤销买单成功，结果: {}", result);
    }

    /**
     * 从创建订单结果中提取订单ID
     * 注意：根据实际返回的数据结构调整此方法
     */
    private Long extractOrderId(Object createResult) {
        // 这里需要根据实际返回结构调整提取逻辑
        if (createResult instanceof JSONObject) {
            JSONObject json = (JSONObject) createResult;
            return json.getLong("orderId");
        } else if (createResult instanceof String) {
            try {
                return Long.parseLong(createResult.toString());
            } catch (NumberFormatException e) {
                log.error("无法解析订单ID: {}", createResult);
                throw new RuntimeException("无法从创建订单结果中提取订单ID");
            }
        } else {
            log.error("未知的创建订单返回格式: {}", createResult);
            throw new RuntimeException("未知的创建订单返回格式");
        }
    }

    /**
     * 测试撤销不存在的订单（异常情况）
     */
    @SuppressWarnings("unused")
    private void testCancelNonExistentOrder() {
        log.info("===== 测试撤销不存在的订单 =====");

        try {
            Long fakeOrderId = 9999999999L;
            log.info("尝试撤销不存在的订单ID: {}", fakeOrderId);

            Object result = cancelOrder(fakeOrderId);
            log.info("操作结果: {}", result); // 这里应该不会执行到，因为预期会抛出异常
        } catch (HashExApiException e) {
            log.info("预期的异常: {}", e.getMessage());
        }
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient(
                FutureTestConfig.BASE_URL,
                FutureTestConfig.ACCESS_KEY,
                FutureTestConfig.SECRET_KEY);
        OrderCancelTest cancelTest = new OrderCancelTest();
        // 测试撤销限价单
        cancelTest.testCancelLimitOrder();

        // 测试异常情况
        // cancelTest.testCancelNonExistentOrder();
    }
}
