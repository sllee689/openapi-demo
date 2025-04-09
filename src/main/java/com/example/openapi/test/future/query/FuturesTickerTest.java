package com.example.openapi.test.future.query;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

public class FuturesTickerTest {

    private static final Logger log = LoggerFactory.getLogger(FuturesTickerTest.class);
    private static ApiClient apiClient;

    /**
     * 获取合约行情信息
     *
     * @param symbol 交易对，例如："BTC_USDT"
     * @return 行情数据
     * @throws HashExApiException 如果API调用失败
     */
    public FuturesTickerVO getFuturesTickerData(String symbol) throws HashExApiException {
        try {
            // 验证必填参数
            if (symbol == null || symbol.isEmpty()) {
                throw new HashExApiException("交易对不能为空");
            }

            // 创建查询参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();
            queryParams.put("symbol", symbol);

            // 调用API
            String responseJson = apiClient.sendGetRequest("/fut/v1/public/q/ticker", queryParams, false);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            // 使用适合实际返回结构的映射
            ApiResponse<FuturesTickerVO> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<FuturesTickerVO>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("获取合约行情数据失败: " + apiResponse.getMessage());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("获取合约行情数据时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试获取合约行情数据
     */
    private void testGetFuturesTickerData() throws HashExApiException {
        log.info("===== 获取合约行情数据测试 =====");

        // 获取BTC_USDT的行情数据
        FuturesTickerVO tickerData = getFuturesTickerData("btc_usdt");

        log.info("交易对: {}, 最新价: {}, 24小时涨跌幅: {}%, 24小时最高价: {}, 24小时最低价: {}, 24小时成交量: {}, 24小时成交额: {}",
                tickerData.getS(), tickerData.getC(), tickerData.getP(), tickerData.getH(),
                tickerData.getL(), tickerData.getV(), tickerData.getQ());
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient(
                "https://open.hashex.vip",
                "0a9970e8986247d6e6d5deadc886a4e558c0a1c4f2047c2a00bc96e2efd24499",
                "dd89a125f1ebaa52e4dd0cff848424eb49e51526e2d585bfedfbc8d055a2b01a");
        FuturesTickerTest tickerTest = new FuturesTickerTest();

        tickerTest.testGetFuturesTickerData();
    }

    /**
     * 合约行情数据模型类 - 根据实际API返回结构定义
     */
    public static class FuturesTickerVO {
        private String s;      // 交易对
        private String c;      // 最新价格
        private String o;      // 24小时前价格
        private String h;      // 24小时最高价
        private String l;      // 24小时最低价
        private String v;      // 24小时成交量
        private String q;      // 24小时成交额
        private String p;      // 24小时涨跌幅(%)
        private String t;      // 时间戳

        // Getters and Setters
        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }

        public String getC() {
            return c;
        }

        public void setC(String c) {
            this.c = c;
        }

        public String getO() {
            return o;
        }

        public void setO(String o) {
            this.o = o;
        }

        public String getH() {
            return h;
        }

        public void setH(String h) {
            this.h = h;
        }

        public String getL() {
            return l;
        }

        public void setL(String l) {
            this.l = l;
        }

        public String getV() {
            return v;
        }

        public void setV(String v) {
            this.v = v;
        }

        public String getQ() {
            return q;
        }

        public void setQ(String q) {
            this.q = q;
        }

        public String getP() {
            return p;
        }

        public void setP(String p) {
            this.p = p;
        }

        public String getT() {
            return t;
        }

        public void setT(String t) {
            this.t = t;
        }
    }
}