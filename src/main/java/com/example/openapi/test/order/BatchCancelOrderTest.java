package com.example.openapi.test.order;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * 批量撤单测试类
 */
public class BatchCancelOrderTest {

    private static final Logger log = LoggerFactory.getLogger(BatchCancelOrderTest.class);
    private static ApiClient apiClient;

    /**
     * 批量撤单
     *
     * @param orderIds 订单ID列表
     * @return 是否撤单成功
     * @throws HashExApiException 如果API调用失败
     */
    public boolean batchCancelOrders(List<String> orderIds) throws HashExApiException {
        try {
            // 验证必填参数
            if (orderIds == null || orderIds.isEmpty()) {
                throw new HashExApiException("订单ID列表不能为空");
            }

            // 创建请求参数Map
            TreeMap<String, String> params = new TreeMap<>();

            // 将订单ID列表转为JSON字符串
            String orderIdsJson = JSONUtil.toJsonStr(orderIds);
            params.put("orderIdsJson", orderIdsJson);

            // 调用API - 批量撤单
            String responseJson = apiClient.sendPostRequest("/spot/v1/u/trade/order/batch/cancel", params);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);
            ApiResponse<?> apiResponse = JSONUtil.toBean(jsonObject, ApiResponse.class, false);

            if (apiResponse.getCode() != 0) {
                log.error("批量撤单失败: {}", apiResponse.getMessage());
                return false;
            }

            return true;
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("批量撤单时发生错误: " + e.getMessage(), e);
        }
    }

    /**
     * 先查询未完成订单，然后批量撤销测试
     */
    private void testBatchCancelOrders() throws HashExApiException {
        log.info("===== 测试批量撤单 =====");

        // 先查询当前未完成的订单
        UnfinishedOrderQueryTest orderQueryTest = new UnfinishedOrderQueryTest();
        UnfinishedOrderQueryTest.PageResult<UnfinishedOrderQueryTest.OrderVO> orders = orderQueryTest.getUnfinishedOrders(
                "BTC_USDT", 1, null, null, null, 9, 1, 10);

        if (orders.getItems() == null || orders.getItems().isEmpty()) {
            log.info("当前没有未完成的订单，无法测试批量撤单");
            return;
        }

        // 获取订单ID列表
        List<String> orderIds = new ArrayList<>();
        for (UnfinishedOrderQueryTest.OrderVO order : orders.getItems()) {
            orderIds.add(order.getOrderId());
            log.info("准备撤销订单: ID={}, 类型={}, 方向={}, 价格={}, 数量={}",
                    order.getOrderId(),
                    order.getOrderType(),
                    order.getOrderSide(),
                    order.getPrice(),
                    order.getOrigQty());
        }

        // 批量撤单
        log.info("开始批量撤单，共 {} 个订单", orderIds.size());
        boolean success = batchCancelOrders(orderIds);

        if (success) {
            log.info("批量撤单成功");
        } else {
            log.info("批量撤单失败");
        }
    }

    /**
     * 测试撤销特定订单
     */
    private void testCancelSpecificOrders() throws HashExApiException {
        log.info("===== 测试撤销特定订单 =====");

        // 这里可以指定要撤销的订单ID列表
        List<String> orderIds = new ArrayList<>();
        orderIds.add("475534805480815680");
        orderIds.add("475534266177207360");

        log.info("准备撤销 {} 个指定订单", orderIds.size());
        for (String orderId : orderIds) {
            log.info("订单ID: {}", orderId);
        }

        boolean success = batchCancelOrders(orderIds);

        if (success) {
            log.info("指定订单撤销成功");
        } else {
            log.info("指定订单撤销失败");
        }
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient("https://open.hashex.vip",
                "0a9970e8986247d6e6d5deadc886a4e558c0a1c4f2047c2a00bc96e2efd24499",
                "yZGQ4OWExMjVmMWViYWE1MmU0ZGQwY2ZmODQ4NDI0ZWI0OWU1MTUyNmUyZDU4NWJmZWRmYmM4ZDA1NWEyYjAxMmE=");
        BatchCancelOrderTest cancelTest = new BatchCancelOrderTest();
        UnfinishedOrderQueryTest.setApiClient(apiClient);
        // 测试批量撤单
        cancelTest.testBatchCancelOrders();

        // 测试撤销特定订单
        cancelTest.testCancelSpecificOrders();
    }
}