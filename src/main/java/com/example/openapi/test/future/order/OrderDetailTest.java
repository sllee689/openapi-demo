package com.example.openapi.test.future.order;

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
 * 合约订单查询测试类
 */
public class OrderDetailTest {

    private static final Logger log = LoggerFactory.getLogger(OrderDetailTest.class);
    private static ApiClient apiClient;

    public OrderDetailTest(ApiClient apiClient) {
        OrderDetailTest.apiClient = apiClient;
    }

    /**
     * 根据订单ID查询订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     * @throws HashExApiException 如果API调用失败
     */
    public OrderVO getOrderDetail(Long orderId) throws HashExApiException {
        try {
            // 验证参数
            if (orderId == null || orderId <= 0) {
                throw new IllegalArgumentException("订单ID无效");
            }

            // 创建参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();
            queryParams.put("orderId", orderId.toString());

            // 调用API
            String responseJson = apiClient.sendGetRequest("/fut/v1/order/detail", queryParams, true);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);
            ApiResponse<OrderVO> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<OrderVO>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("查询订单失败: " + apiResponse.getMsg());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("查询订单时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试查询订单详情
     */
    private void testGetOrderDetail() throws HashExApiException {
        // 创建一个订单
        OrderCreateTest orderCreateTest = new OrderCreateTest(apiClient);

        log.info("===== 步骤1: 创建一个限价订单 =====");
        OrderCreateTest.OrderRequest request = new OrderCreateTest.OrderRequest();
        request.setSymbol("btc_usdt");
        request.setOrderSide("BUY");
        request.setOrderType("LIMIT");
        request.setPositionSide("LONG");
        request.setOrigQty(new BigDecimal("1"));
        request.setPrice(new BigDecimal("70000")); // 设置较低价格确保不会成交
        request.setTimeInForce("GTC");
        request.setLeverage(100);

        Object createResult = orderCreateTest.createOrder(request);
        log.info("限价订单创建结果: {}", createResult);

        // 提取订单ID
        Long orderId = extractOrderId(createResult);
        log.info("获取到订单ID: {}", orderId);

        // 等待1秒，确保订单已处理
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 查询订单详情
        log.info("===== 步骤2: 查询订单详情 =====");
        OrderVO orderDetail = getOrderDetail(orderId);
        log.info("订单详情查询结果: {}", orderDetail);

        // 验证返回的订单ID是否与请求的一致
        if (orderId.equals(orderDetail.getOrderId())) {
            log.info("验证成功: 返回的订单ID与请求的一致");
        } else {
            log.error("验证失败: 返回的订单ID与请求的不一致");
        }
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
        ApiClient client = new ApiClient(
                "https://open.hashex.vip",
                "0a9970e8986247d6e6d5deadc886a4e558c0a1c4f2047c2a00bc96e2efd24499",
                "dd89a125f1ebaa52e4dd0cff848424eb49e51526e2d585bfedfbc8d055a2b01a");

        OrderDetailTest orderDetailTest = new OrderDetailTest(client);
        orderDetailTest.testGetOrderDetail();
    }

    /**
     * 订单详情VO
     */
    public static class OrderVO {
        private Long orderId;              // 订单ID
        private String symbol;             // 交易对
        private String orderSide;          // 买卖方向
        private String orderType;          // 订单类型
        private String positionSide;       // 仓位方向
        private BigDecimal price;          // 价格
        private BigDecimal origQty;        // 原始数量
        private BigDecimal executedQty;    // 已成交数量
        private BigDecimal avgPrice;  // 成交均价
        private String state;             // 订单状态
        private String timeInForce;        // 有效方式
        private Long createdTime;           // 创建时间
        private Long updatedTime;           // 更新时间
        private Integer leverage;          // 杠杆倍数

        // Getters and Setters
        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getOrderSide() {
            return orderSide;
        }

        public void setOrderSide(String orderSide) {
            this.orderSide = orderSide;
        }

        public String getOrderType() {
            return orderType;
        }

        public void setOrderType(String orderType) {
            this.orderType = orderType;
        }

        public String getPositionSide() {
            return positionSide;
        }

        public void setPositionSide(String positionSide) {
            this.positionSide = positionSide;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public BigDecimal getOrigQty() {
            return origQty;
        }

        public void setOrigQty(BigDecimal origQty) {
            this.origQty = origQty;
        }

        public BigDecimal getExecutedQty() {
            return executedQty;
        }

        public void setExecutedQty(BigDecimal executedQty) {
            this.executedQty = executedQty;
        }

        public BigDecimal getAvgPrice() {
            return avgPrice;
        }

        public void setAvgPrice(BigDecimal avgPrice) {
            this.avgPrice = avgPrice;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getTimeInForce() {
            return timeInForce;
        }

        public void setTimeInForce(String timeInForce) {
            this.timeInForce = timeInForce;
        }

        public Long getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(Long createdTime) {
            this.createdTime = createdTime;
        }

        public Long getUpdatedTime() {
            return updatedTime;
        }

        public void setUpdatedTime(Long updatedTime) {
            this.updatedTime = updatedTime;
        }

        public Integer getLeverage() {
            return leverage;
        }

        public void setLeverage(Integer leverage) {
            this.leverage = leverage;
        }

        @Override
        public String toString() {
            return "订单详情 {\n" +
                    "  订单ID: " + orderId + "\n" +
                    "  交易对: " + symbol + "\n" +
                    "  买卖方向: " + orderSide + "\n" +
                    "  订单类型: " + orderType + "\n" +
                    "  仓位方向: " + positionSide + "\n" +
                    "  价格: " + price + "\n" +
                    "  数量: " + origQty + "\n" +
                    "  已成交数量: " + (executedQty != null ? executedQty : "0") + "\n" +
                    "  成交均价: " + (avgPrice == null ? "未成交" : avgPrice) + "\n" +
                    "  订单状态: " + (state == null || "null".equals(state) ? "未知" : state) + "\n" +
                    "  有效方式: " + timeInForce + "\n" +
                    "  创建时间: " + (createdTime == null ? "未知" : createdTime) + "\n" +
                    "  更新时间: " + (updatedTime == null ? "未知" : updatedTime) + "\n" +
                    "  杠杆倍数: " + leverage + "\n" +
                    "}";
        }
    }
}