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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

/**
 * 合约订单查询测试类
 */
public class OrderDetailTest {

    private static final Logger log = LoggerFactory.getLogger(OrderDetailTest.class);
    private static ApiClient apiClient;

    /**
     * 根据订单ID查询订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     * @throws HashExApiException 如果API调用失败
     */
    public OrderVO getOrderDetail(String orderId) throws HashExApiException {
        try {
            // 验证参数
            if (orderId == null || orderId.isEmpty()) {
                throw new IllegalArgumentException("订单ID无效");
            }

            // 创建参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();
            queryParams.put("orderId", orderId);

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
        request.setPrice(new BigDecimal("60000")); // 设置较低价格确保不会成交
        request.setLeverage(20);

        Object createResult = orderCreateTest.createOrder(request);
        log.info("限价订单创建结果: {}", createResult);

        // 提取订单ID
        String orderId = extractOrderId(createResult);
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
    private String extractOrderId(Object createResult) {
        if (createResult instanceof String) {
            return createResult.toString();
        } else if (createResult instanceof JSONObject) {
            JSONObject json = (JSONObject) createResult;
            return json.getStr("orderId");
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
        OrderDetailTest orderDetailTest = new OrderDetailTest();
        orderDetailTest.testGetOrderDetail();
    }

    /**
     * 订单详情VO
     */
    public static class OrderVO {
        private String orderId;              // 订单ID
        private String symbol;             // 交易对
        private String contractType;       // 合约类型
        private String orderSide;          // 买卖方向
        private String orderType;          // 订单类型
        private String positionSide;       // 仓位方向
        private Boolean closePosition;     // 是否平仓
        private String price;              // 价格
        private String origQty;            // 原始数量
        private String avgPrice;           // 成交均价
        private String executedQty;        // 已成交数量
        private String marginFrozen;       // 冻结保证金
        private String triggerProfitPrice; // 止盈价格
        private String triggerStopPrice;   // 止损价格
        private String sourceId;           // 来源ID
        private Boolean forceClose;        // 是否强平
        private String tradeFee;           // 交易费用
        private String closeProfit;        // 平仓盈亏
        private String state;             // 订单状态
        private Long createdTime;           // 创建时间
        private Long updatedTime;           // 更新时间
        private Integer leverage;          // 杠杆倍数

        // Getters and Setters
        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getContractType() {
            return contractType;
        }

        public void setContractType(String contractType) {
            this.contractType = contractType;
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

        public Boolean getClosePosition() {
            return closePosition;
        }

        public void setClosePosition(Boolean closePosition) {
            this.closePosition = closePosition;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getOrigQty() {
            return origQty;
        }

        public void setOrigQty(String origQty) {
            this.origQty = origQty;
        }

        public String getAvgPrice() {
            return avgPrice;
        }

        public void setAvgPrice(String avgPrice) {
            this.avgPrice = avgPrice;
        }

        public String getExecutedQty() {
            return executedQty;
        }

        public void setExecutedQty(String executedQty) {
            this.executedQty = executedQty;
        }

        public String getMarginFrozen() {
            return marginFrozen;
        }

        public void setMarginFrozen(String marginFrozen) {
            this.marginFrozen = marginFrozen;
        }

        public String getTriggerProfitPrice() {
            return triggerProfitPrice;
        }

        public void setTriggerProfitPrice(String triggerProfitPrice) {
            this.triggerProfitPrice = triggerProfitPrice;
        }

        public String getTriggerStopPrice() {
            return triggerStopPrice;
        }

        public void setTriggerStopPrice(String triggerStopPrice) {
            this.triggerStopPrice = triggerStopPrice;
        }

        public String getSourceId() {
            return sourceId;
        }

        public void setSourceId(String sourceId) {
            this.sourceId = sourceId;
        }

        public Boolean getForceClose() {
            return forceClose;
        }

        public void setForceClose(Boolean forceClose) {
            this.forceClose = forceClose;
        }

        public String getTradeFee() {
            return tradeFee;
        }

        public void setTradeFee(String tradeFee) {
            this.tradeFee = tradeFee;
        }

        public String getCloseProfit() {
            return closeProfit;
        }

        public void setCloseProfit(String closeProfit) {
            this.closeProfit = closeProfit;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return "订单详情 {\n" +
                    "  订单ID: " + orderId + "\n" +
                    "  交易对: " + symbol + "\n" +
                    "  合约类型: " + contractType + "\n" +
                    "  买卖方向: " + orderSide + "\n" +
                    "  订单类型: " + orderType + "\n" +
                    "  仓位方向: " + positionSide + "\n" +
                    "  价格: " + ("0".equals(price) ? "市价" : price) + "\n" +
                    "  数量: " + origQty + "\n" +
                    "  已成交数量: " + (executedQty != null ? executedQty : "0") + "\n" +
                    "  成交均价: " + ("0".equals(avgPrice) ? "未成交" : avgPrice) + "\n" +
                    "  冻结保证金: " + marginFrozen + "\n" +
                    "  订单状态: " + state + "\n" +
                    "  杠杆倍数: " + leverage + "\n" +
                    "  交易费: " + tradeFee + "\n" +
                    "  平仓盈亏: " + (closeProfit == null ? "无" : closeProfit) + "\n" +
                    "  止盈价: " + (triggerProfitPrice == null ? "未设置" : triggerProfitPrice) + "\n" +
                    "  止损价: " + (triggerStopPrice == null ? "未设置" : triggerStopPrice) + "\n" +
                    "  是否强平: " + (forceClose != null && forceClose ? "是" : "否") + "\n" +
                    "  是否平仓: " + (closePosition != null && closePosition ? "是" : "否") + "\n" +
                    "  创建时间: " + (createdTime == null ? "未知" : sdf.format(new Date(createdTime))) + "\n" +
                    "  更新时间: " + (updatedTime == null ? "未知" : sdf.format(new Date(updatedTime))) + "\n" +
                    "}";
        }
    }
}
