package com.example.openapi.test.spot.order;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import com.example.openapi.test.spot.query.DepthQueryTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.TreeMap;

/**
 * 做市商自动交易测试类
 *
 * 测试 /u/maker/selfDeal 接口
 * 该接口用于做市商执行自动交易，使用实时深度数据计算交易价格
 */
public class MakerSelfDealTest2 {

    private static final Logger log = LoggerFactory.getLogger(MakerSelfDealTest2.class);
    private static ApiClient apiClient;
    private static DepthQueryTest depthQueryTest;

    // 订单方向常量
    private static final int ORDER_SIDE_BUY = 1;   // 买入
    private static final int ORDER_SIDE_SELL = 2;  // 卖出

    /**
     * 获取买一卖一中间价格
     *
     * @param symbol 交易对
     * @return 中间价格
     * @throws HashExApiException 如果获取深度数据失败
     */
    public BigDecimal getMiddlePrice(String symbol) throws HashExApiException {
        try {
            // 获取深度数据
            DepthQueryTest.DepthVO depth = depthQueryTest.getDepthData(symbol, 1);

            if (depth.getB() == null || depth.getB().isEmpty()) {
                throw new HashExApiException("买盘数据为空");
            }
            if (depth.getA() == null || depth.getA().isEmpty()) {
                throw new HashExApiException("卖盘数据为空");
            }

            // 获取买一价格（最高买价）
            List<String> bestBid = depth.getB().get(0);
            BigDecimal bidPrice = new BigDecimal(bestBid.get(0));

            // 获取卖一价格（最低卖价）
            List<String> bestAsk = depth.getA().get(0);
            BigDecimal askPrice = new BigDecimal(bestAsk.get(0));

            // 计算中间价格
            BigDecimal middlePrice = bidPrice.add(askPrice).divide(new BigDecimal("2"), 8, RoundingMode.HALF_UP);

            log.info("{}深度信息 - 买一价格: {}, 卖一价格: {}, 中间价格: {}",
                    symbol, bidPrice, askPrice, middlePrice);

            return middlePrice;
        } catch (Exception e) {
            throw new HashExApiException("获取中间价格失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据中间价格和偏移量计算交易价格
     *
     * @param symbol 交易对
     * @param orderSide 订单方向
     * @param priceOffset 价格偏移量（相对于中间价格的偏移，正数表示高于中间价，负数表示低于中间价）
     * @return 计算后的交易价格
     * @throws HashExApiException 如果计算失败
     */
    public BigDecimal calculateTradePrice(String symbol, Integer orderSide, BigDecimal priceOffset) throws HashExApiException {
        BigDecimal middlePrice = getMiddlePrice(symbol);
        BigDecimal tradePrice = middlePrice.add(priceOffset);

        // 确保价格为正数
        if (tradePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new HashExApiException("计算出的交易价格必须大于0");
        }

        log.info("{}交易价格计算 - 中间价格: {}, 偏移量: {}, 最终价格: {}",
                symbol, middlePrice, priceOffset, tradePrice);

        return tradePrice;
    }

    /**
     * 执行做市商自动交易（使用实时价格）
     *
     * @param symbol 交易对名字，例如："BTC_USDT"
     * @param orderSide 订单方向（1-买入，2-卖出）
     * @param quantity 交易数量
     * @param priceOffset 价格偏移量（相对于中间价格）
     * @return 交易结果
     * @throws HashExApiException 如果API调用失败
     */
    public Object executeMakerSelfDealWithMarketPrice(String symbol, Integer orderSide, BigDecimal quantity, BigDecimal priceOffset) throws HashExApiException {
        // 计算交易价格
        BigDecimal price = calculateTradePrice(symbol, orderSide, priceOffset);

        return executeMakerSelfDeal(symbol, orderSide, price, quantity);
    }

    /**
     * 执行做市商自动交易
     *
     * @param symbol 交易对名字，例如："BTC_USDT"
     * @param orderSide 订单方向（1-买入，2-卖出）
     * @param price 交易价格
     * @param quantity 交易数量
     * @return 交易结果
     * @throws HashExApiException 如果API调用失败
     */
    public Object executeMakerSelfDeal(String symbol, Integer orderSide, BigDecimal price, BigDecimal quantity) throws HashExApiException {
        try {
            // 验证参数
            if (symbol == null || symbol.trim().isEmpty()) {
                throw new HashExApiException("交易对不能为空");
            }
            if (orderSide == null || (orderSide != ORDER_SIDE_BUY && orderSide != ORDER_SIDE_SELL)) {
                throw new HashExApiException("订单方向必须是1（买入）或2（卖出）");
            }
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new HashExApiException("交易价格必须大于0");
            }
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new HashExApiException("交易数量必须大于0");
            }

            // 构建请求参数
            TreeMap<String, String> params = new TreeMap<>();
            params.put("symbol", symbol);
            params.put("orderSide", String.valueOf(orderSide));
            params.put("price", price.toString());
            params.put("quantity", quantity.toString());

            log.info("执行做市商自动交易: 交易对={}, 方向={}, 价格={}, 数量={}",
                    symbol, orderSide == ORDER_SIDE_BUY ? "买入" : "卖出", price, quantity);

            // 发送POST请求
            String response = apiClient.sendPostRequest("/spot/v1/u/maker/selfDeal", params);
            log.info("做市商自动交易响应: {}", response);

            // 解析响应
            JSONObject jsonResponse = JSONUtil.parseObj(response);
            if (jsonResponse.getInt("code") != 0) {
                throw new HashExApiException("做市商自动交易失败: " + jsonResponse.getStr("msg"));
            }

            return jsonResponse.get("data");
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("执行做市商自动交易时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试BTC_USDT买入交易（使用实时价格）
     */
    private void testBuyOrderWithMarketPrice() throws HashExApiException {
        log.info("===== 测试BTC_USDT买入交易（使用实时价格） =====");

        String symbol = "ETH_USDT";
        Integer orderSide = ORDER_SIDE_SELL;
        BigDecimal quantity = new BigDecimal("1000000000");
        // 买入时使用比中间价低一些的价格
        BigDecimal priceOffset = new BigDecimal("0.01");

        log.info("准备执行买入交易: 交易对={}, 数量={}, 价格偏移={}", symbol, quantity, priceOffset);

        try {
            Object result = executeMakerSelfDealWithMarketPrice(symbol, orderSide, quantity, priceOffset);
            log.info("买入交易成功，结果: {}", result);
        } catch (HashExApiException e) {
            log.error("买入交易失败: {}", e.getMessage());
        }
    }

    /**
     * 测试BTC_USDT卖出交易（使用实时价格）
     */
    private void testSellOrderWithMarketPrice() throws HashExApiException {
        log.info("===== 测试BTC_USDT卖出交易（使用实时价格） =====");

        String symbol = "BTC_USDT";
        Integer orderSide = ORDER_SIDE_SELL;
        BigDecimal quantity = new BigDecimal("0.001");
        // 卖出时使用比中间价高一些的价格
        BigDecimal priceOffset = new BigDecimal("100");

        log.info("准备执行卖出交易: 交易对={}, 数量={}, 价格偏移={}", symbol, quantity, priceOffset);

        try {
            Object result = executeMakerSelfDealWithMarketPrice(symbol, orderSide, quantity, priceOffset);
            log.info("卖出交易成功，结果: {}", result);
        } catch (HashExApiException e) {
            log.error("卖出交易失败: {}", e.getMessage());
        }
    }

    /**
     * 测试ETH_USDT交易（使用实时价格）
     */
    private void testEthTradeWithMarketPrice() throws HashExApiException {
        log.info("===== 测试ETH_USDT交易（使用实时价格） =====");

        String symbol = "ETH_USDT";
        Integer orderSide = ORDER_SIDE_BUY;
        BigDecimal quantity = new BigDecimal("0.01");
        // 买入时使用比中间价低一些的价格
        BigDecimal priceOffset = new BigDecimal("-10");

        log.info("准备执行ETH买入交易: 交易对={}, 数量={}, 价格偏移={}", symbol, quantity, priceOffset);

        try {
            Object result = executeMakerSelfDealWithMarketPrice(symbol, orderSide, quantity, priceOffset);
            log.info("ETH买入交易成功，结果: {}", result);
        } catch (HashExApiException e) {
            log.error("ETH买入交易失败: {}", e.getMessage());
        }
    }

    /**
     * 测试双向挂单（买入和卖出）
     */
    private void testBidirectionalOrders() throws HashExApiException {
        log.info("===== 测试双向挂单 =====");

        String symbol = "BTC_USDT";
        BigDecimal quantity = new BigDecimal("0.001");

        // 买入订单 - 比中间价低200
        BigDecimal buyOffset = new BigDecimal("-200");
        // 卖出订单 - 比中间价高200
        BigDecimal sellOffset = new BigDecimal("200");

        try {
            // 先获取当前中间价格用于日志
            BigDecimal middlePrice = getMiddlePrice(symbol);
            log.info("当前{}中间价格: {}", symbol, middlePrice);

            // 执行买入订单
            log.info("执行买入订单，价格偏移: {}", buyOffset);
            Object buyResult = executeMakerSelfDealWithMarketPrice(symbol, ORDER_SIDE_BUY, quantity, buyOffset);
            log.info("买入订单成功，结果: {}", buyResult);

            // 等待1秒避免请求过于频繁
            Thread.sleep(1000);

            // 执行卖出订单
            log.info("执行卖出订单，价格偏移: {}", sellOffset);
            Object sellResult = executeMakerSelfDealWithMarketPrice(symbol, ORDER_SIDE_SELL, quantity, sellOffset);
            log.info("卖出订单成功，结果: {}", sellResult);

        } catch (Exception e) {
            log.error("双向挂单失败: {}", e.getMessage());
        }
    }

    /**
     * 测试多个交易对的交易
     */
    private void testMultipleSymbols() throws HashExApiException {
        log.info("===== 测试多个交易对的交易 =====");

        String[] symbols = {"BTC_USDT", "ETH_USDT"};
        BigDecimal quantity = new BigDecimal("0.001");
        BigDecimal priceOffset = new BigDecimal("-50");

        for (String symbol : symbols) {
            log.info("开始测试交易对: {}", symbol);
            try {
                Object result = executeMakerSelfDealWithMarketPrice(symbol, ORDER_SIDE_BUY, quantity, priceOffset);
                log.info("{}交易成功，结果: {}", symbol, result);

                // 等待1秒避免请求过于频繁
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error("{}交易失败: {}", symbol, e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws HashExApiException {
        // 初始化API客户端
        apiClient = new ApiClient("https://open.hashex.vip",
                "d5065d3ea7fcda602c126d9c610751ef7b6a5417593708af8a23937b8b35d38d",
                "065d23ab593d9656686c991b2e5a7b3932ba62fb451e48a11b992a7cea9bc7af");

        // 初始化深度查询测试类
        // 初始化深度查询测试类，需要传入ApiClient实例
        depthQueryTest = new DepthQueryTest();
        // 设置DepthQueryTest的ApiClient
        DepthQueryTest.setApiClient(apiClient);

        MakerSelfDealTest2 test = new MakerSelfDealTest2();

        // 测试使用实时价格的交易
        for (long i = 0; i < 10000000000L; i++) {
            test.testBuyOrderWithMarketPrice();
        }

//        test.testSellOrderWithMarketPrice();
//        test.testEthTradeWithMarketPrice();
//        test.testBidirectionalOrders();
//        test.testMultipleSymbols();
    }
}