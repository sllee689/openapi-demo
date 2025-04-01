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
 * 订单查询测试类
 */
public class OrderQueryTest {

    private static final Logger log = LoggerFactory.getLogger(OrderQueryTest.class);
    private static ApiClient apiClient;

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

            // 创建查询参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();
            queryParams.put("orderId", orderId.toString());

            // 调用API
            String responseJson = apiClient.sendGetRequest("/spot/v1/u/trade/order/detail", queryParams);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            // 使用TypeReference处理泛型
            ApiResponse<OrderVO> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<>() {
                    }, false);

            if (!apiResponse.isSuccess()) {
                throw new HashExApiException("查询订单失败: " + apiResponse.getMessage());
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
     * 创建限价订单并获取订单ID
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
     * 测试查询订单详情
     */
    private void testQueryOrderDetail() throws HashExApiException {
        log.info("===== 测试查询订单详情 =====");

        // 1. 创建一个限价买单
        String symbol = "BTC_USDT";
        BigDecimal amount = new BigDecimal("0.001");
        BigDecimal price = new BigDecimal("60000");

        log.info("步骤1: 创建限价买单 - 币对:{}, 数量:{}, 价格:{}", symbol, amount, price);
        String orderId = createLimitOrder(symbol, "BUY", amount, price);
        log.info("限价买单创建成功，订单ID: {}", orderId);

        // 等待1秒，确保订单已处理
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 2. 查询订单详情
        log.info("步骤2: 查询订单详情 - 订单ID: {}", orderId);
        OrderVO orderDetail = getOrderDetail(Long.parseLong(orderId));

        // 3. 打印订单详情
        log.info("订单查询成功，详情如下:");
        log.info("订单ID: {}", orderDetail.getOrderId());
        log.info("客户端订单ID: {}", orderDetail.getClientOrderId());
        log.info("币对: {}", orderDetail.getSymbol());
        log.info("订单类型: {}", orderDetail.getOrderType());
        log.info("方向: {}", orderDetail.getOrderSide());
        log.info("价格: {}", orderDetail.getPrice());
        log.info("数量: {}", orderDetail.getOrigQty());
        log.info("已成交数量: {}", orderDetail.getExecutedQty());
        log.info("平均成交价: {}", orderDetail.getAvgPrice());
        log.info("冻结保证金: {}", orderDetail.getMarginFrozen());
        log.info("状态: {} ({})", orderDetail.getState(), orderDetail.getStateText());
        log.info("账户类型: {}", orderDetail.getBalanceType());
        log.info("创建时间: {}", orderDetail.getCreatedTime());
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient("https://open.hashex.vip",
                "0a9970e8986247d6e6d5deadc886a4e558c0a1c4f2047c2a00bc96e2efd24499",
                "yZGQ4OWExMjVmMWViYWE1MmU0ZGQwY2ZmODQ4NDI0ZWI0OWU1MTUyNmUyZDU4NWJmZWRmYmM4ZDA1NWEyYjAxMmE=");
        OrderQueryTest queryTest = new OrderQueryTest();

        // 测试查询订单详情
        queryTest.testQueryOrderDetail();
    }

    /**
     * 订单详情VO
     */
    public static class OrderVO {
        private String orderId;           // 订单ID
        private String clientOrderId;     // 客户端订单ID
        private String symbol;            // 交易对
        private String orderType;         // 订单类型：LIMIT或MARKET
        private String orderSide;         // 买卖方向：BUY或SELL
        private Integer balanceType;      // 账户类型 1.现货账户 2.杠杆账户
        private String timeInForce;       // 有效方式，如GTC (Good Till Cancel)
        private String price;             // 价格
        private String origQty;           // 原始数量
        private String avgPrice;          // 平均成交价
        private String executedQty;       // 已成交数量
        private String marginFrozen;      // 冻结保证金
        private String sourceId;          // 来源ID
        private String forceClose;        // 是否强制平仓
        private String state;             // 订单状态
        private Long createdTime;         // 创建时间戳

        // Getters and Setters
        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getClientOrderId() {
            return clientOrderId;
        }

        public void setClientOrderId(String clientOrderId) {
            this.clientOrderId = clientOrderId;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getOrderType() {
            return orderType;
        }

        public void setOrderType(String orderType) {
            this.orderType = orderType;
        }

        public String getOrderSide() {
            return orderSide;
        }

        public void setOrderSide(String orderSide) {
            this.orderSide = orderSide;
        }

        public Integer getBalanceType() {
            return balanceType;
        }

        public void setBalanceType(Integer balanceType) {
            this.balanceType = balanceType;
        }

        public String getTimeInForce() {
            return timeInForce;
        }

        public void setTimeInForce(String timeInForce) {
            this.timeInForce = timeInForce;
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

        public String getSourceId() {
            return sourceId;
        }

        public void setSourceId(String sourceId) {
            this.sourceId = sourceId;
        }

        public String getForceClose() {
            return forceClose;
        }

        public void setForceClose(String forceClose) {
            this.forceClose = forceClose;
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

        /**
         * 获取订单状态的文字描述
         */
        public String getStateText() {
            if (state == null) return "未知";
            switch (state) {
                case "NEW": return "新建";
                case "PARTIALLY_FILLED": return "部分成交";
                case "FILLED": return "完全成交";
                case "CANCELED": return "已取消";
                case "REJECTED": return "已拒绝";
                case "EXPIRED": return "已过期";
                default: return "未知状态:" + state;
            }
        }
    }
}