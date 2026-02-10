package com.example.openapi.test.spot.maker;

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
 * 做市商自动交易测试类
 *
 * 测试 /u/maker/selfDeal 接口
 * 该接口用于做市商执行自动交易
 */
public class MakerSelfDealTest {

    private static final Logger log = LoggerFactory.getLogger(MakerSelfDealTest.class);
    private static ApiClient apiClient;

    // 订单方向常量
    private static final int ORDER_SIDE_BUY = 1;   // 买入
    private static final int ORDER_SIDE_SELL = 2;  // 卖出

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
            String response = apiClient.sendPostRequest("/spot/v1/u/market/maker/selfDeal", params);
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
     * 测试BTC_USDT买入交易
     */
    private void testBuyOrder() throws HashExApiException {
        log.info("===== 测试BTC_USDT买入交易 =====");

        String symbol = "ETH_USDT";
        Integer orderSide = ORDER_SIDE_BUY;
        BigDecimal price = new BigDecimal("1.00");
        BigDecimal quantity = new BigDecimal("1");

        log.info("准备执行买入交易: 交易对={}, 价格={}, 数量={}", symbol, price, quantity);

        try {
            Object result = executeMakerSelfDeal(symbol, orderSide, price, quantity);
            log.info("买入交易成功，结果: {}", result);
        } catch (HashExApiException e) {
            log.error("买入交易失败: {}", e.getMessage());
        }
    }

    /**
     * 测试BTC_USDT卖出交易
     */
    private void testSellOrder() throws HashExApiException {
        log.info("===== 测试BTC_USDT卖出交易 =====");

        String symbol = "BTC_USDT";
        Integer orderSide = ORDER_SIDE_SELL;
        BigDecimal price = new BigDecimal("51000.00");
        BigDecimal quantity = new BigDecimal("1");

        log.info("准备执行卖出交易: 交易对={}, 价格={}, 数量={}", symbol, price, quantity);

        try {
            Object result = executeMakerSelfDeal(symbol, orderSide, price, quantity);
            log.info("卖出交易成功，结果: {}", result);
        } catch (HashExApiException e) {
            log.error("卖出交易失败: {}", e.getMessage());
        }
    }

    /**
     * 测试ETH_USDT交易
     */
    private void testEthTrade() throws HashExApiException {
        log.info("===== 测试ETH_USDT交易 =====");

        String symbol = "ETH_USDT";
        Integer orderSide = ORDER_SIDE_BUY;
        BigDecimal price = new BigDecimal("3000.00");
        BigDecimal quantity = new BigDecimal("0.01");

        log.info("准备执行ETH买入交易: 交易对={}, 价格={}, 数量={}", symbol, price, quantity);

        try {
            Object result = executeMakerSelfDeal(symbol, orderSide, price, quantity);
            log.info("ETH买入交易成功，结果: {}", result);
        } catch (HashExApiException e) {
            log.error("ETH买入交易失败: {}", e.getMessage());
        }
    }

    /**
     * 测试小数量交易
     */
    private void testSmallQuantityTrade() throws HashExApiException {
        log.info("===== 测试小数量交易 =====");

        String symbol = "BTC_USDT";
        Integer orderSide = ORDER_SIDE_BUY;
        BigDecimal price = new BigDecimal("50000.00");
        BigDecimal quantity = new BigDecimal("0.0001");

        log.info("准备执行小数量买入交易: 交易对={}, 价格={}, 数量={}", symbol, price, quantity);

        try {
            Object result = executeMakerSelfDeal(symbol, orderSide, price, quantity);
            log.info("小数量买入交易成功，结果: {}", result);
        } catch (HashExApiException e) {
            log.error("小数量买入交易失败: {}", e.getMessage());
        }
    }

    /**
     * 测试参数验证
     */
    private void testParameterValidation() {
        log.info("===== 测试参数验证 =====");

        // 测试空交易对
        try {
            executeMakerSelfDeal("", ORDER_SIDE_BUY, new BigDecimal("50000"), new BigDecimal("0.001"));
            log.error("应该抛出异常：交易对不能为空");
        } catch (HashExApiException e) {
            log.info("正确捕获异常：{}", e.getMessage());
        }

        // 测试无效订单方向
        try {
            executeMakerSelfDeal("XRP_USDT", 3, new BigDecimal("50000"), new BigDecimal("0.001"));
            log.error("应该抛出异常：无效的订单方向");
        } catch (HashExApiException e) {
            log.info("正确捕获异常：{}", e.getMessage());
        }

        // 测试负价格
        try {
            executeMakerSelfDeal("XRP_USDT", ORDER_SIDE_BUY, new BigDecimal("-50000"), new BigDecimal("0.001"));
            log.error("应该抛出异常：价格不能为负");
        } catch (HashExApiException e) {
            log.info("正确捕获异常：{}", e.getMessage());
        }

        // 测试零数量
        try {
            executeMakerSelfDeal("XRP_USDT", ORDER_SIDE_BUY, new BigDecimal("50000"), BigDecimal.ZERO);
            log.error("应该抛出异常：数量不能为零");
        } catch (HashExApiException e) {
            log.info("正确捕获异常：{}", e.getMessage());
        }
    }

    /**
     * 测试批量交易
     */
    private void testBatchTrades() throws HashExApiException {
        log.info("===== 测试批量交易 =====");

        String symbol = "XRP_USDT";
        BigDecimal basePrice = new BigDecimal("1");
        BigDecimal baseQuantity = new BigDecimal("0.001");

        // 执行多个买入订单
        for (int i = 1; i <= 3; i++) {
            BigDecimal price = basePrice.subtract(new BigDecimal(i * 100));
            BigDecimal quantity = baseQuantity.multiply(new BigDecimal(i));

            log.info("执行第{}个买入订单: 价格={}, 数量={}", i, price, quantity);

            try {
                Object result = executeMakerSelfDeal(symbol, ORDER_SIDE_BUY, price, quantity);
                log.info("第{}个买入订单成功，结果: {}", i, result);

                // 避免请求过于频繁
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error("第{}个买入订单失败: {}", i, e.getMessage());
            }
        }

        // 执行多个卖出订单
        for (int i = 1; i <= 3; i++) {
            BigDecimal price = basePrice.add(new BigDecimal(i * 100));
            BigDecimal quantity = baseQuantity.multiply(new BigDecimal(i));

            log.info("执行第{}个卖出订单: 价格={}, 数量={}", i, price, quantity);

            try {
                Object result = executeMakerSelfDeal(symbol, ORDER_SIDE_SELL, price, quantity);
                log.info("第{}个卖出订单成功，结果: {}", i, result);

                // 避免请求过于频繁
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error("第{}个卖出订单失败: {}", i, e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws HashExApiException {
        // 替换为您的 accessKey 和 secretKey
        apiClient = new ApiClient("https://open.mgbx.com",
                "a4d91d508e98fbcdd2b8d0cd3bc2dfcfb3b611020fe44d119ccb221464271d39",
                "3b3fbb99a5a3543d7b94471afe8dfac0ee151b7bb7f7c7797d4d6a7213cbea28");
//        apiClient = new ApiClient("https://open.hashex.vip",
//                "d5065d3ea7fcda602c126d9c610751ef7b6a5417593708af8a23937b8b35d38d",
//                "065d23ab593d9656686c991b2e5a7b3932ba62fb451e48a11b992a7cea9bc7af");

        MakerSelfDealTest test = new MakerSelfDealTest();

        // 测试参数验证
       // test.testParameterValidation();

        // 测试单个买入交易
        test.testBuyOrder();

    }
}