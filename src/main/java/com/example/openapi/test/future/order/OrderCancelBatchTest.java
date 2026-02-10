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
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * 合约批量撤单测试类
 */
public class OrderCancelBatchTest {

    private static final Logger log = LoggerFactory.getLogger(OrderCancelBatchTest.class);
    private static ApiClient apiClient;

    /**
     * 批量撤销合约订单
     *
     * @param orderIds 订单ID列表
     * @return 撤单结果
     * @throws HashExApiException 如果API调用失败
     */
    public Object cancelOrderBatch(List<Long> orderIds) throws HashExApiException {
        try {
            // 验证参数
            if (orderIds == null || orderIds.isEmpty()) {
                throw new IllegalArgumentException("订单ID列表不能为空");
            }


            String orderIdsStr =  JSONUtil.toJsonStr(orderIds);
            if (orderIdsStr.isEmpty()) {
                throw new IllegalArgumentException("没有有效的订单ID");
            }

            // 创建参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();
            queryParams.put("orderIds", orderIdsStr);

            // 调用API
            String responseJson = apiClient.sendPostRequest("/fut/v1/order/cancel-batch", queryParams);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);
            ApiResponse<Object> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<Object>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("批量撤销订单失败: " + apiResponse.getMsg());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("批量撤销订单时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试批量撤销限价单
     */
    private void testCancelBatchLimitOrders() throws HashExApiException {
        log.info("===== 测试批量撤销合约限价单 =====");

        // 创建订单测试对象
        OrderCreateTest orderCreateTest = new OrderCreateTest(apiClient);
        List<Long> orderIds = new ArrayList<>();

        // 1. 创建多个限价买单（价格设置较低以确保不会立即成交）
        log.info("步骤1: 创建多个限价买单");

        // 创建第一个限价买单
        OrderCreateTest.OrderRequest request1 = new OrderCreateTest.OrderRequest();
        request1.setSymbol("btc_usdt");
        request1.setOrderSide("BUY");
        request1.setOrderType("LIMIT");
        request1.setPositionSide("LONG");
        request1.setOrigQty(new BigDecimal("1"));
        request1.setPrice(new BigDecimal("75000"));
        request1.setTimeInForce("GTC");
        request1.setLeverage(100);

        Object createResult1 = orderCreateTest.createOrder(request1);
        log.info("限价买单1创建结果: {}", createResult1);
        Long orderId1 = extractOrderId(createResult1);
        orderIds.add(orderId1);

        // 创建第二个限价买单
        OrderCreateTest.OrderRequest request2 = new OrderCreateTest.OrderRequest();
        request2.setSymbol("btc_usdt");
        request2.setOrderSide("BUY");
        request2.setOrderType("LIMIT");
        request2.setPositionSide("LONG");
        request2.setOrigQty(new BigDecimal("1"));
        request2.setPrice(new BigDecimal("74000"));
        request2.setTimeInForce("GTC");
        request2.setLeverage(100);

        Object createResult2 = orderCreateTest.createOrder(request2);
        log.info("限价买单2创建结果: {}", createResult2);
        Long orderId2 = extractOrderId(createResult2);
        orderIds.add(orderId2);

        // 等待1秒，确保订单已处理
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 2. 批量撤销这些买单
        log.info("步骤2: 批量撤销买单 - 订单ID列表: {}", orderIds);
        Object result = cancelOrderBatch(orderIds);
        log.info("批量撤销买单成功，结果: {}", result);
    }

    /**
     * 从创建订单结果中提取订单ID
     */
    private Long extractOrderId(Object createResult) {
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



    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient(
                FutureTestConfig.BASE_URL,
                FutureTestConfig.ACCESS_KEY,
                FutureTestConfig.SECRET_KEY);

        OrderCancelBatchTest cancelBatchTest = new OrderCancelBatchTest();

        // 测试批量撤销限价单
        cancelBatchTest.testCancelBatchLimitOrders();

    }
}