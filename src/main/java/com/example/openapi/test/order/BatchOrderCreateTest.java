package com.example.openapi.test.order;

import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class BatchOrderCreateTest {

    private static final Logger log = LoggerFactory.getLogger(BatchOrderCreateTest.class);
    private static ApiClient apiClient;

    /**
     * 批量创建订单
     *
     * @param orderRequestList 订单请求列表
     * @return 批量创建结果
     * @throws HashExApiException 如果API调用失败
     */
    public String batchCreateOrders(List<OrderCreateTest.OrderRequest> orderRequestList) throws HashExApiException {
        try {
            // 验证参数
            if (orderRequestList == null || orderRequestList.isEmpty()) {
                throw new IllegalArgumentException("订单列表不能为空");
            }

            // 验证每个订单参数
            for (OrderCreateTest.OrderRequest request : orderRequestList) {
                request.validate();
            }

            // 创建参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();

            // 将订单列表转换为JSON字符串
            String ordersJsonStr = JSONUtil.toJsonStr(orderRequestList);
            queryParams.put("ordersJsonStr", ordersJsonStr);

            // 调用API
            String responseJson = apiClient.sendPostRequest("/spot/v1/u/trade/order/batch/create", queryParams);

            // 解析响应JSON
            cn.hutool.json.JSONObject jsonObject = new cn.hutool.json.JSONObject(responseJson);
            ApiResponse<String> apiResponse = JSONUtil.toBean(jsonObject, ApiResponse.class);

            if (!apiResponse.isSuccess()) {
                throw new HashExApiException("批量创建订单失败: " + apiResponse.getMessage());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("批量创建订单时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试批量创建订单
     */
    private void testBatchCreateOrders() throws HashExApiException {
        log.info("===== 批量创建订单测试 =====");

        // 创建订单列表
        List<OrderCreateTest.OrderRequest> orderList = new ArrayList<>();

        // 添加限价买单
        OrderCreateTest.OrderRequest buyOrder = OrderCreateTest.OrderRequest.limitBuy(
                "BTC_USDT",
                new BigDecimal("0.001"),
                new BigDecimal("80000")
        );
        buyOrder.setClientOrderId("BATCH_LIMIT_BUY_" + System.currentTimeMillis());
        orderList.add(buyOrder);

        // 添加限价卖单
        OrderCreateTest.OrderRequest sellOrder = OrderCreateTest.OrderRequest.limitSell(
                "BTC_USDT",
                new BigDecimal("0.001"),
                new BigDecimal("90000")
        );
        sellOrder.setClientOrderId("BATCH_LIMIT_SELL_" + System.currentTimeMillis());
        orderList.add(sellOrder);

        // 批量创建订单
        String result = batchCreateOrders(orderList);
        log.info("批量创建订单成功，结果: {}", result);
    }

    /**
     * 测试同时买卖不同交易对
     */
    private void testMultiSymbolBatchOrders() throws HashExApiException {
        log.info("===== 多交易对批量下单测试 =====");

        List<OrderCreateTest.OrderRequest> orderList = new ArrayList<>();

        // 添加BTC_USDT买单
        orderList.add(OrderCreateTest.OrderRequest.marketBuy("BTC_USDT", new BigDecimal("10")));

        // 添加ETH_USDT卖单
        orderList.add(OrderCreateTest.OrderRequest.marketSell("ETH_USDT", new BigDecimal("0.01")));

        // 批量创建订单
        String result = batchCreateOrders(orderList);
        log.info("多交易对批量下单成功，结果: {}", result);
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient("https://open.hashex.vip",
                "0a9970e8986247d6e6d5deadc886a4e558c0a1c4f2047c2a00bc96e2efd24499",
                "yZGQ4OWExMjVmMWViYWE1MmU0ZGQwY2ZmODQ4NDI0ZWI0OWU1MTUyNmUyZDU4NWJmZWRmYmM4ZDA1NWEyYjAxMmE=");
        BatchOrderCreateTest batchTest = new BatchOrderCreateTest();

        // 测试批量创建订单
        batchTest.testBatchCreateOrders();

        // 测试多交易对批量下单
        // batchTest.testMultiSymbolBatchOrders();
    }
}