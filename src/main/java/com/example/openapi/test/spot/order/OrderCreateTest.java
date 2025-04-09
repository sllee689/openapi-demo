package com.example.openapi.test.spot.order;

import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.TreeMap;

public class OrderCreateTest {

    private static final Logger log = LoggerFactory.getLogger(OrderCreateTest.class);
    private static ApiClient apiClient;

    /**
     * 测试场景：卖出500 USDT价值的BTC
     */
    private void testSellBtcOrder() throws HashExApiException {
        // 执行卖出操作（使用市价单）
        String result = sellBtcForUsdt(new BigDecimal("0.0005"), null, true);

        // 打印结果
        log.info(result);
    }

    /**
     * 创建订单
     *
     * @param orderRequest 订单请求对象
     * @return 订单响应对象
     * @throws HashExApiException 如果API调用失败
     */
    public String createOrder(OrderRequest orderRequest) throws HashExApiException {
        try {
            // 参数校验
            orderRequest.validate();

            // 创建排序的参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();

            // 添加必需参数
            queryParams.put("symbol", orderRequest.getSymbol());
            queryParams.put("direction", orderRequest.getDirection());
            queryParams.put("totalAmount", orderRequest.getTotalAmount().toPlainString());
            queryParams.put("tradeType", orderRequest.getTradeType());

            // 添加可选参数
            if (orderRequest.getPrice() != null) {
                queryParams.put("price", orderRequest.getPrice().toPlainString());
            }

            if (orderRequest.getClientOrderId() != null) {
                queryParams.put("clientOrderId", orderRequest.getClientOrderId());
            }

            // 账户类型默认为1（现货账户）
            queryParams.put("balanceType", orderRequest.getBalanceType().toString());

            // 调用API
            String responseJson = apiClient.sendPostRequest("/spot/v1/u/trade/order/create", queryParams);

            // 解析响应JSON
            cn.hutool.json.JSONObject jsonObject = new cn.hutool.json.JSONObject(responseJson);
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
     * 卖出指定USDT价值的BTC
     *
     * @param btcAmount BTC数量
     * @param btcPrice BTC当前价格
     * @param isMarketOrder 是否为市价单
     * @return 订单响应对象
     * @throws HashExApiException 如果API调用失败
     */
    public String sellBtcForUsdt(BigDecimal btcAmount, BigDecimal btcPrice, boolean isMarketOrder)
            throws HashExApiException {
        try {
            // 创建订单请求对象
            OrderRequest request = isMarketOrder ?
                    OrderRequest.marketSell("ETH_USDT", btcAmount) :
                    OrderRequest.limitSell("ETH_USDT", btcAmount, btcPrice);

            // 发送订单请求
            return createOrder(request);
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("卖出BTC时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 买入BTC
     *
     * @param btcAmount BTC数量
     * @param btcPrice BTC当前价格(限价单使用)
     * @param isMarketOrder 是否为市价单
     * @return 订单响应对象
     * @throws HashExApiException 如果API调用失败
     */
    public String buyBtcWithUsdt(BigDecimal btcAmount, BigDecimal btcPrice, boolean isMarketOrder)
            throws HashExApiException {
        try {
            // 创建订单请求对象，使用静态工厂方法
            OrderRequest request = isMarketOrder ?
                    OrderRequest.marketBuy("ETH_USDT", btcAmount) :
                    OrderRequest.limitBuy("ETH_USDT", btcAmount, btcPrice);

            // 发送订单请求
            return createOrder(request);
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("买入BTC时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试场景：买入BTC
     */
    private void testBuyBtcOrder() throws HashExApiException {
        log.info("===== 买入BTC测试 =====");

        // 定义要使用的USDT金额
        BigDecimal usdtAmount = new BigDecimal("0.005");
        log.info("使用USDT金额: " + usdtAmount);

        // 执行买入操作（使用市价单）
        String result = buyBtcWithUsdt(usdtAmount, null, true);

        log.info("订单创建成功: identifier: " + result);
    }


    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient("https://open.hashex.vip",
                "0a9970e8986247d6e6d5deadc886a4e558c0a1c4f2047c2a00bc96e2efd24499",
                "dd89a125f1ebaa52e4dd0cff848424eb49e51526e2d585bfedfbc8d055a2b01a");
        OrderCreateTest orderCreateTest = new OrderCreateTest();

        orderCreateTest.testSellBtcOrder();
        orderCreateTest.testBuyBtcOrder();

    }

    /**
     * 订单请求参数封装类
     */
    public static class OrderRequest {
        private String symbol;           // 交易对
        private String direction;        // 买卖方向：BUY或SELL
        private String tradeType;        // 订单类型：LIMIT或MARKET
        private BigDecimal totalAmount;  // 数量
        private BigDecimal price;        // 价格（市价单可为null）
        private Integer balanceType = 1; // 账户类型 1.现货账户 2.杠杆账户
        private String clientOrderId;    // 自定义订单ID

        // 默认构造函数
        public OrderRequest() {
        }

        // 带参数的构造函数
        public OrderRequest(String symbol, String direction, String tradeType, BigDecimal totalAmount) {
            this.symbol = symbol;
            this.direction = direction;
            this.tradeType = tradeType;
            this.totalAmount = totalAmount;
        }

        // 完整参数构造函数
        public OrderRequest(String symbol, String direction, String tradeType,
                            BigDecimal totalAmount, BigDecimal price,
                            Integer balanceType, String clientOrderId) {
            this.symbol = symbol;
            this.direction = direction;
            this.tradeType = tradeType;
            this.totalAmount = totalAmount;
            this.price = price;
            this.balanceType = balanceType == null ? 1 : balanceType;
            this.clientOrderId = clientOrderId;
        }

        /**
         * 创建限价买单
         *
         * @param symbol 交易对
         * @param amount 数量
         * @param price 价格
         * @return 订单请求对象
         */
        public static OrderRequest limitBuy(String symbol, BigDecimal amount, BigDecimal price) {
            OrderRequest request = new OrderRequest();
            request.setSymbol(symbol);
            request.setDirection("BUY");
            request.setTradeType("LIMIT");
            request.setTotalAmount(amount);
            request.setPrice(price);
            request.setClientOrderId("LIMIT_BUY_" + System.currentTimeMillis());
            return request;
        }

        /**
         * 创建限价卖单
         *
         * @param symbol 交易对
         * @param amount 数量
         * @param price 价格
         * @return 订单请求对象
         */
        public static OrderRequest limitSell(String symbol, BigDecimal amount, BigDecimal price) {
            OrderRequest request = new OrderRequest();
            request.setSymbol(symbol);
            request.setDirection("SELL");
            request.setTradeType("LIMIT");
            request.setTotalAmount(amount);
            request.setPrice(price);
            request.setClientOrderId("LIMIT_SELL_" + System.currentTimeMillis());
            return request;
        }

        /**
         * 创建市价买单
         *
         * @param symbol 交易对
         * @param amount 数量（USDT金额）
         * @return 订单请求对象
         */
        public static OrderRequest marketBuy(String symbol, BigDecimal amount) {
            OrderRequest request = new OrderRequest();
            request.setSymbol(symbol);
            request.setDirection("BUY");
            request.setTradeType("MARKET");
            request.setTotalAmount(amount);
            request.setClientOrderId("MARKET_BUY_" + System.currentTimeMillis());
            return request;
        }

        /**
         * 创建市价卖单
         *
         * @param symbol 交易对
         * @param amount 数量（币种数量）
         * @return 订单请求对象
         */
        public static OrderRequest marketSell(String symbol, BigDecimal amount) {
            OrderRequest request = new OrderRequest();
            request.setSymbol(symbol);
            request.setDirection("SELL");
            request.setTradeType("MARKET");
            request.setTotalAmount(amount);
            request.setClientOrderId("MARKET_SELL_" + System.currentTimeMillis());
            return request;
        }

        /**
         * 参数校验
         *
         * @throws IllegalArgumentException 如果参数无效
         */
        public void validate() throws IllegalArgumentException {
            if (symbol == null || symbol.isEmpty()) {
                throw new IllegalArgumentException("交易对不能为空");
            }

            if (direction == null || direction.isEmpty() ||
                    (!direction.equals("BUY") && !direction.equals("SELL"))) {
                throw new IllegalArgumentException("买卖方向必须为BUY或SELL");
            }

            if (tradeType == null || tradeType.isEmpty() ||
                    (!tradeType.equals("LIMIT") && !tradeType.equals("MARKET"))) {
                throw new IllegalArgumentException("订单类型必须为LIMIT或MARKET");
            }

            if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("数量必须大于0");
            }

            if (tradeType.equals("LIMIT") && (price == null || price.compareTo(BigDecimal.ZERO) <= 0)) {
                throw new IllegalArgumentException("限价单价格必须大于0");
            }
        }

        // Getters and Setters
        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public String getTradeType() {
            return tradeType;
        }

        public void setTradeType(String tradeType) {
            this.tradeType = tradeType;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getBalanceType() {
            return balanceType;
        }

        public void setBalanceType(Integer balanceType) {
            this.balanceType = balanceType == null ? 1 : balanceType;
        }

        public String getClientOrderId() {
            return clientOrderId;
        }

        public void setClientOrderId(String clientOrderId) {
            this.clientOrderId = clientOrderId;
        }
    }
}