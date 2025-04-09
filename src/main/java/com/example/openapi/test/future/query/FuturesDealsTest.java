package com.example.openapi.test.future.query;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TreeMap;

public class FuturesDealsTest {

    private static final Logger log = LoggerFactory.getLogger(FuturesDealsTest.class);
    private static ApiClient apiClient;

    /**
     * 获取合约交易对最新成交信息
     *
     * @param symbol 交易对，例如："BTC_USDT"
     * @param num 获取数量，最小值为1
     * @return 成交数据列表
     * @throws HashExApiException 如果API调用失败
     */
    public List<FuturesDealVO> getFuturesDealsData(String symbol, Integer num) throws HashExApiException {
        try {
            // 验证必填参数
            if (symbol == null || symbol.isEmpty()) {
                throw new HashExApiException("交易对不能为空");
            }

            if (num == null || num < 1) {
                throw new HashExApiException("获取数量必须大于等于1");
            }

            // 创建查询参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();
            queryParams.put("symbol", symbol);
            queryParams.put("num", num.toString());

            // 调用API
            String responseJson = apiClient.sendGetRequest("/fut/v1/public/q/deal", queryParams, false);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            // 使用适合实际返回结构的映射
            ApiResponse<List<FuturesDealVO>> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<List<FuturesDealVO>>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("获取合约成交数据失败: " + apiResponse.getMessage());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("获取合约成交数据时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试获取合约成交数据
     */
    private void testGetFuturesDealsData() throws HashExApiException {
        log.info("===== 获取合约成交数据测试 =====");

        // 获取BTC_USDT的最新10条成交记录
        List<FuturesDealVO> dealsData = getFuturesDealsData("btc_usdt", 10);

        log.info("共获取到 {} 条成交记录", dealsData.size());

        // 打印成交记录
        // 打印成交记录
        for (FuturesDealVO deal : dealsData) {
            log.info("交易对: {}, 价格: {}, 数量: {}, 时间: {}, 买卖方向: {}",
                    deal.getS(), deal.getP(), deal.getA(), deal.getT(),
                    "ASK".equals(deal.getM()) ? "卖出" : "买入");
        }
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient(
                "https://open.hashex.vip",
                "0a9970e8986247d6e6d5deadc886a4e558c0a1c4f2047c2a00bc96e2efd24499",
                "dd89a125f1ebaa52e4dd0cff848424eb49e51526e2d585bfedfbc8d055a2b01a");
        FuturesDealsTest dealsTest = new FuturesDealsTest();

        dealsTest.testGetFuturesDealsData();
    }

    /**
     * 合约成交数据模型类 - 按照实际返回结构修改
     */
    public static class FuturesDealVO {
        private String s;      // 交易对
        private String p;      // 成交价格
        private String a;      // 成交数量（API返回的是a而不是v）
        private Long t;        // 成交时间（毫秒时间戳）
        private String m;      // 买卖方向（API返回的是m而不是sd，值为"ASK"或"BID"）

        // Getters and Setters
        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }

        public String getP() {
            return p;
        }

        public void setP(String p) {
            this.p = p;
        }

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public Long getT() {
            return t;
        }

        public void setT(Long t) {
            this.t = t;
        }

        public String getM() {
            return m;
        }

        public void setM(String m) {
            this.m = m;
        }
    }
}