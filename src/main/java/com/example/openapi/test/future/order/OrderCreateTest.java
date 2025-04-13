package com.example.openapi.test.future.order;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import com.example.openapi.test.future.query.ServerTimeTest.*;
import com.example.openapi.test.future.symbol.SymbolDetailTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.TreeMap;

public class OrderCreateTest {

    private static final Logger log = LoggerFactory.getLogger(OrderCreateTest.class);
    private static ApiClient apiClient;

    public OrderCreateTest() {
        // 默认构造函数
    }
    public OrderCreateTest(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 获取币对详情信息
     */
    private SymbolDetailTest.SymbolDetailVO getSymbolDetail(String symbol) throws HashExApiException {
        try {
            TreeMap<String, String> queryParams = new TreeMap<>();
            queryParams.put("symbol", symbol);

            String responseJson = apiClient.sendGetRequest("/fut/v1/public/symbol/detail", queryParams, false);
            JSONObject jsonObject = new JSONObject(responseJson);

            ApiResponse<SymbolDetailTest.SymbolDetailVO> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<SymbolDetailTest.SymbolDetailVO>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("获取币对详情失败: " + apiResponse.getMsg());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            throw new HashExApiException("获取币对详情时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 合约下单
     */
    public Object createOrder(OrderRequest orderRequest) throws HashExApiException {
        try {
            // 验证必填参数
            if (orderRequest.getSymbol() == null || orderRequest.getSymbol().isEmpty()) {
                throw new HashExApiException("交易对不能为空");
            }
            if (orderRequest.getOrderSide() == null || orderRequest.getOrderSide().isEmpty()) {
                throw new HashExApiException("买卖方向不能为空");
            }
            if (orderRequest.getOrderType() == null || orderRequest.getOrderType().isEmpty()) {
                throw new HashExApiException("订单类型不能为空");
            }
            if (orderRequest.getPositionSide() == null || orderRequest.getPositionSide().isEmpty()) {
                throw new HashExApiException("仓位方向不能为空");
            }
            if (orderRequest.getOrigQty() == null || orderRequest.getOrigQty().compareTo(BigDecimal.ZERO) <= 0) {
                throw new HashExApiException("数量必须大于0");
            }
            if ("LIMIT".equals(orderRequest.getOrderType()) && orderRequest.getPrice() == null) {
                throw new HashExApiException("限价单价格不能为空");
            }

            // 获取币对详情，计算实际数量
            SymbolDetailTest.SymbolDetailVO symbolDetail = getSymbolDetail(orderRequest.getSymbol());
            BigDecimal contractSize = new BigDecimal(symbolDetail.getContractSize());

            // 计算实际下单数量，除以合约面值并确保整数
            BigDecimal actualQty = orderRequest.getOrigQty().divide(contractSize, 0, BigDecimal.ROUND_DOWN);

            // 如果计算结果为0，提示错误
            if (actualQty.compareTo(BigDecimal.ZERO) <= 0) {
                throw new HashExApiException("计算后的数量必须大于0，请增加下单数量");
            }

            log.info("原始数量: {}, 合约面值: {}, 实际下单数量: {}",
                    orderRequest.getOrigQty(), contractSize, actualQty);

            // 创建参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();
            queryParams.put("symbol", orderRequest.getSymbol());
            queryParams.put("orderSide", orderRequest.getOrderSide());
            queryParams.put("orderType", orderRequest.getOrderType());
            queryParams.put("positionSide", orderRequest.getPositionSide());
            queryParams.put("origQty", actualQty.toString()); // 使用计算后的数量

            // 添加可选参数
            if (orderRequest.getClientOrderId() != null) {
                queryParams.put("clientOrderId", orderRequest.getClientOrderId());
            }
            if (orderRequest.getPrice() != null) {
                queryParams.put("price", orderRequest.getPrice().toString());
            }
            if (orderRequest.getLeverage() != null) {
                queryParams.put("leverage", orderRequest.getLeverage().toString());
            }
            if (orderRequest.getTimeInForce() != null) {
                queryParams.put("timeInForce", orderRequest.getTimeInForce());
            }
            if (orderRequest.getMarketOrderLevel() != null) {
                queryParams.put("marketOrderLevel", orderRequest.getMarketOrderLevel().toString());
            }
            if (orderRequest.getPositionId() != null) {
                queryParams.put("positionId", orderRequest.getPositionId().toString());
            }
            if (orderRequest.getReduceOnly() != null) {
                queryParams.put("reduceOnly", orderRequest.getReduceOnly().toString());
            }
            if (orderRequest.getTriggerProfitPrice() != null) {
                queryParams.put("triggerProfitPrice", orderRequest.getTriggerProfitPrice().toString());
            }
            if (orderRequest.getTriggerStopPrice() != null) {
                queryParams.put("triggerStopPrice", orderRequest.getTriggerStopPrice().toString());
            }
            if (orderRequest.getCopyTrade() != null) {
                queryParams.put("copyTrade", orderRequest.getCopyTrade().toString());
            }
            if (orderRequest.getSourceType() != null) {
                queryParams.put("sourceType", orderRequest.getSourceType().toString());
            }

            // 调用API
            String responseJson = apiClient.sendPostRequest("/fut/v1/order/create", queryParams);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            // 处理响应
            ApiResponse<Object> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<Object>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("下单失败: " + apiResponse.getMsg());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("下单时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试创建限价单
     */
    private void testCreateLimitOrder() throws HashExApiException {
        log.info("===== 创建限价单测试 =====");

        // 获取币对信息，查看合约面值
        String symbol = "btc_usdt";
        SymbolDetailTest.SymbolDetailVO symbolDetail = getSymbolDetail(symbol);
        log.info("交易对: {}, 合约面值: {}", symbol, symbolDetail.getContractSize());

        // 设置原始数量（实际交易量）
        BigDecimal origAmount = new BigDecimal("0.01"); // 比如要买入0.01 BTC

        OrderRequest request = new OrderRequest();
        request.setSymbol(symbol);
        request.setOrderSide("BUY");
        request.setOrderType("LIMIT");
        request.setPositionSide("LONG");
        request.setOrigQty(origAmount); // 设置原始数量，系统会自动根据合约面值转换
        request.setPrice(new BigDecimal("85000"));
        request.setTimeInForce("GTC");
        request.setLeverage(100);

        Object result = createOrder(request);
        log.info("限价单创建结果: {}", result);
    }

    /**
     * 测试创建市价单
     */
    private void testCreateMarketOrder() throws HashExApiException {
        log.info("===== 创建市价单测试 =====");

        // 获取币对信息，查看合约面值
        String symbol = "btc_usdt";
        SymbolDetailTest.SymbolDetailVO symbolDetail = getSymbolDetail(symbol);
        log.info("交易对: {}, 合约面值: {}", symbol, symbolDetail.getContractSize());

        // 设置原始数量（实际交易量）
        BigDecimal origAmount = new BigDecimal("0.01");

        OrderRequest request = new OrderRequest();
        request.setSymbol(symbol);
        request.setOrderSide("BUY");
        request.setOrderType("MARKET");
        request.setPositionSide("LONG");
        request.setOrigQty(origAmount); // 设置原始数量，系统会自动转换
        request.setMarketOrderLevel(1); // 对手价
        request.setLeverage(100);

        Object result = createOrder(request);
        log.info("市价单创建结果: {}", result);
    }

    /**
     * 测试创建止盈止损单
     */
    private void testCreateOrderWithTP_SL() throws HashExApiException {
        log.info("===== 创建带止盈止损的订单测试 =====");

        // 获取币对信息，查看合约信息
        String symbol = "btc_usdt";
        SymbolDetailTest.SymbolDetailVO symbolDetail = getSymbolDetail(symbol);
        log.info("交易对: {}, 合约面值: {}", symbol, symbolDetail.getContractSize());

        // 设置原始数量（实际交易量）
        BigDecimal origAmount = new BigDecimal("0.01");

        OrderRequest request = new OrderRequest();
        request.setSymbol(symbol);
        request.setOrderSide("BUY");
        request.setOrderType("LIMIT");
        request.setPositionSide("LONG");
        request.setOrigQty(origAmount); // 设置原始数量，系统会自动转换
        request.setPrice(new BigDecimal("85000"));
        request.setTimeInForce("GTC");
        request.setLeverage(100);
        request.setTriggerProfitPrice(new BigDecimal("89000")); // 止盈价
        request.setTriggerStopPrice(new BigDecimal("82000"));   // 止损价

        Object result = createOrder(request);
        log.info("带止盈止损的订单创建结果: {}", result);
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient(
                "https://open.hashex.vip",
                "0a9970e8986247d6e6d5deadc886a4e558c0a1c4f2047c2a00bc96e2efd24499",
                "dd89a125f1ebaa52e4dd0cff848424eb49e51526e2d585bfedfbc8d055a2b01a");
        OrderCreateTest orderTest = new OrderCreateTest();

        // 选择一个测试方法执行
        //orderTest.testCreateLimitOrder();
         orderTest.testCreateMarketOrder();
        // orderTest.testCreateOrderWithTP_SL();
    }

    /**
     * 订单请求参数类
     */
    public static class OrderRequest {
        private String symbol;             // 交易对
        private String orderSide;          // 买卖方向：BUY;SELL
        private String orderType;          // 订单类型：LIMIT；MARKET
        private String positionSide;       // 仓位方向：LONG;SHORT
        private BigDecimal origQty;        // 原始数量（实际交易量）
        private BigDecimal price;          // 价格
        private String clientOrderId;      // 自定义orderId
        private Integer leverage;          // 杠杆倍数
        private String timeInForce;        // 有效方式：GTC;IOC;FOK;GTX
        private Integer marketOrderLevel;  // 市价最优档：1(对手价)；5,10,15档
        private Long positionId;           // 平仓Id
        private Boolean reduceOnly;        // 只减仓
        private BigDecimal triggerProfitPrice; // 止盈价
        private BigDecimal triggerStopPrice;   // 止损价
        private Boolean copyTrade;         // 是否复制交易
        private Integer sourceType;        // 来源类型

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

        public BigDecimal getOrigQty() {
            return origQty;
        }

        public void setOrigQty(BigDecimal origQty) {
            this.origQty = origQty;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getClientOrderId() {
            return clientOrderId;
        }

        public void setClientOrderId(String clientOrderId) {
            this.clientOrderId = clientOrderId;
        }

        public Integer getLeverage() {
            return leverage;
        }

        public void setLeverage(Integer leverage) {
            this.leverage = leverage;
        }

        public String getTimeInForce() {
            return timeInForce;
        }

        public void setTimeInForce(String timeInForce) {
            this.timeInForce = timeInForce;
        }

        public Integer getMarketOrderLevel() {
            return marketOrderLevel;
        }

        public void setMarketOrderLevel(Integer marketOrderLevel) {
            this.marketOrderLevel = marketOrderLevel;
        }

        public Long getPositionId() {
            return positionId;
        }

        public void setPositionId(Long positionId) {
            this.positionId = positionId;
        }

        public Boolean getReduceOnly() {
            return reduceOnly;
        }

        public void setReduceOnly(Boolean reduceOnly) {
            this.reduceOnly = reduceOnly;
        }

        public BigDecimal getTriggerProfitPrice() {
            return triggerProfitPrice;
        }

        public void setTriggerProfitPrice(BigDecimal triggerProfitPrice) {
            this.triggerProfitPrice = triggerProfitPrice;
        }

        public BigDecimal getTriggerStopPrice() {
            return triggerStopPrice;
        }

        public void setTriggerStopPrice(BigDecimal triggerStopPrice) {
            this.triggerStopPrice = triggerStopPrice;
        }

        public Boolean getCopyTrade() {
            return copyTrade;
        }

        public void setCopyTrade(Boolean copyTrade) {
            this.copyTrade = copyTrade;
        }

        public Integer getSourceType() {
            return sourceType;
        }

        public void setSourceType(Integer sourceType) {
            this.sourceType = sourceType;
        }
    }
}