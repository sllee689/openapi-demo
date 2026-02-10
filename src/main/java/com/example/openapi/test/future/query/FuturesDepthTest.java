package com.example.openapi.test.future.query;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import com.example.openapi.test.future.FutureTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TreeMap;

public class FuturesDepthTest {

    private static final Logger log = LoggerFactory.getLogger(FuturesDepthTest.class);
    private static ApiClient apiClient;

    /**
     * 获取合约交易对深度信息
     *
     * @param symbol 交易对，例如："BTC_USDT"
     * @param level 档位（1-50）
     * @return 深度数据
     * @throws HashExApiException 如果API调用失败
     */
    public FuturesDepthVO getFuturesDepthData(String symbol, Integer level) throws HashExApiException {
        try {
            // 验证必填参数
            if (symbol == null || symbol.isEmpty()) {
                throw new HashExApiException("交易对不能为空");
            }

            if (level == null || level < 1 || level > 50) {
                throw new HashExApiException("档位必须在1-50范围内");
            }

            // 创建查询参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();
            queryParams.put("symbol", symbol);
            queryParams.put("level", level.toString());

            // 调用API
            String responseJson = apiClient.sendGetRequest("/fut/v1/public/q/depth", queryParams, false);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            // 使用适合实际返回结构的映射
            ApiResponse<FuturesDepthVO> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<FuturesDepthVO>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("获取合约深度数据失败: " + apiResponse.getMsg());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("获取合约深度数据时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试获取合约深度数据
     */
    private void testGetFuturesDepthData() throws HashExApiException {
        log.info("===== 获取合约深度数据测试 =====");

        // 获取BTC_USDT的深度数据，10档
        FuturesDepthVO depthData = getFuturesDepthData("btc_usdt", 10);

        log.info("交易对: {}, 时间戳: {}", depthData.getS(), depthData.getT());

        log.info("===== 买单信息（前5档）=====");
        List<List<String>> bids = depthData.getB();
        for (int i = 0; i < Math.min(5, bids.size()); i++) {
            List<String> bid = bids.get(i);
            log.info("价格: {}, 数量: {}", bid.get(0), bid.get(1));
        }

        log.info("===== 卖单信息（前5档）=====");
        List<List<String>> asks = depthData.getA();
        for (int i = 0; i < Math.min(5, asks.size()); i++) {
            List<String> ask = asks.get(i);
            log.info("价格: {}, 数量: {}", ask.get(0), ask.get(1));
        }
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient(
                FutureTestConfig.BASE_URL,
                FutureTestConfig.ACCESS_KEY,
                FutureTestConfig.SECRET_KEY);
        FuturesDepthTest depthTest = new FuturesDepthTest();

        depthTest.testGetFuturesDepthData();
    }

    /**
     * 合约深度数据模型类
     */
    public static class FuturesDepthVO {
        private String s;               // 交易对
        private Long t;                 // 时间戳
        private List<List<String>> b;   // 买单列表 [价格, 数量]
        private List<List<String>> a;   // 卖单列表 [价格, 数量]

        // Getters and Setters
        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }

        public Long getT() {
            return t;
        }

        public void setT(Long t) {
            this.t = t;
        }

        public List<List<String>> getB() {
            return b;
        }

        public void setB(List<List<String>> b) {
            this.b = b;
        }

        public List<List<String>> getA() {
            return a;
        }

        public void setA(List<List<String>> a) {
            this.a = a;
        }
    }
}