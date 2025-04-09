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

public class FuturesTickersTest {

    private static final Logger log = LoggerFactory.getLogger(FuturesTickersTest.class);
    private static ApiClient apiClient;

    /**
     * 获取全部合约交易对行情信息
     *
     * @return 所有交易对的行情数据列表
     * @throws HashExApiException 如果API调用失败
     */
    public List<FuturesTickerVO> getAllFuturesTickers() throws HashExApiException {
        try {
            // 创建空的查询参数Map（此接口不需要参数）
            TreeMap<String, String> queryParams = new TreeMap<>();

            // 调用API
            String responseJson = apiClient.sendGetRequest("/fut/v1/public/q/tickers", queryParams, false);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            // 使用适合实际返回结构的映射
            ApiResponse<List<FuturesTickerVO>> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<List<FuturesTickerVO>>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("获取全部合约行���数据失败: " + apiResponse.getMessage());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("获取全部合约行情数据时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试获取全部合约行情数据
     */
    private void testGetAllFuturesTickers() throws HashExApiException {
        log.info("===== 获取全部合约行情数据测试 =====");

        // 获取所有交易对的行情数据
        List<FuturesTickerVO> tickersList = getAllFuturesTickers();

        log.info("共获取到 {} 个交易对的行情数据", tickersList.size());

        // 打印前5个交易对的行情数据（如果有的话）
        int count = 0;
        for (FuturesTickerVO ticker : tickersList) {
            log.info("交易对: {}, 最新价: {}, 24小时涨跌幅: {}%, 24小时最高价: {}, 24小时最低价: {}, 24小时成交量: {}, 24小时成交额: {}",
                    ticker.getS(), ticker.getC(), ticker.getP(), ticker.getH(),
                    ticker.getL(), ticker.getV(), ticker.getQ());

            count++;
            if (count >= 5) break; // 只打印前5个
        }
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient(
                "https://open.hashex.vip",
                "0a9970e8986247d6e6d5deadc886a4e558c0a1c4f2047c2a00bc96e2efd24499",
                "dd89a125f1ebaa52e4dd0cff848424eb49e51526e2d585bfedfbc8d055a2b01a");
        FuturesTickersTest tickersTest = new FuturesTickersTest();

        tickersTest.testGetAllFuturesTickers();
    }

    /**
     * 合约行情数据模型类 - 与单个Ticker接口使用相同的模型
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