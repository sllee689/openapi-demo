package com.example.openapi.test.spot.order;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
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
     * 批量订单创建结果对象
     */
    public static class BatchOrderResult {
        private int code;
        private String msg;
        private String orderId;

        public BatchOrderResult(int code, String msg, String orderId) {
            this.code = code;
            this.msg = msg;
            this.orderId = orderId;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        public String getOrderId() {
            return orderId;
        }

        public boolean isSuccess() {
            return code == 0;
        }

        @Override
        public String toString() {
            return "BatchOrderResult{" +
                    "code=" + code +
                    ", msg='" + msg + '\'' +
                    ", orderId='" + orderId + '\'' +
                    '}';
        }
    }

    /**
     * 批量创建订单
     *
     * @param orderRequestList 订单请求列表
     * @return 批量创建结果列表
     * @throws HashExApiException 如果API调用失败
     */
    public List<BatchOrderResult> batchCreateOrders(List<OrderCreateTest.OrderRequest> orderRequestList) throws HashExApiException {
        try {
            // 验证参数
            if (orderRequestList == null || orderRequestList.isEmpty()) {
                throw new IllegalArgumentException("订单列表不能为空");
            }

            if (orderRequestList.size() > 20) {
                throw new IllegalArgumentException("批量订单接口最多支持20个订单");
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
            JSONObject jsonObject = JSONUtil.parseObj(responseJson);

            // 检查API响应是否成功
            int code = jsonObject.getInt("code");
            String message = jsonObject.getStr("msg");

            if (code != 0) {
                throw new HashExApiException("批量创建订单失败: " + message);
            }

            // 提取订单结果数组
            JSONArray dataArray = jsonObject.getJSONArray("data");
            List<BatchOrderResult> resultList = new ArrayList<>();

            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject orderResult = dataArray.getJSONObject(i);
                int orderCode = orderResult.getInt("code");
                String orderMsg = orderResult.getStr("msg");
                String orderId = orderResult.getStr("data");

                resultList.add(new BatchOrderResult(orderCode, orderMsg, orderId));
            }

            return resultList;
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
        List<BatchOrderResult> results = batchCreateOrders(orderList);

        // 输出每个订单的创建结果
        for (int i = 0; i < results.size(); i++) {
            BatchOrderResult result = results.get(i);
            if (result.isSuccess()) {
                log.info("订单 {} 创建成功，订单ID: {}", i + 1, result.getOrderId());
            } else {
                log.warn("订单 {} 创建失败，错误码: {}，错误信息: {}", i + 1, result.getCode(), result.getMsg());
            }
        }
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
        List<BatchOrderResult> results = batchCreateOrders(orderList);

        // 输出每个订单的创建结果
        for (int i = 0; i < results.size(); i++) {
            BatchOrderResult result = results.get(i);
            if (result.isSuccess()) {
                log.info("{}交易对订单创建成功，订单ID: {}",
                        orderList.get(i).getSymbol(), result.getOrderId());
            } else {
                log.warn("{}交易对订单创建失败，错误码: {}，错误信息: {}",
                        orderList.get(i).getSymbol(), result.getCode(), result.getMsg());
            }
        }
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient("https://open.hashex.vip",
                "0a9970e8986247d6e6d5deadc886a4e558c0a1c4f2047c2a00bc96e2efd24499",
                "dd89a125f1ebaa52e4dd0cff848424eb49e51526e2d585bfedfbc8d055a2b01a");
        BatchOrderCreateTest batchTest = new BatchOrderCreateTest();

        // 测试批量创建订单
        batchTest.testBatchCreateOrders();

        // 测试多交易对批量下单
        // batchTest.testMultiSymbolBatchOrders();
    }
}