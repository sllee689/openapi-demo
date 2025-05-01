package com.example.openapi.test.spot.query;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

public class TickerQueryTest {

    private static final Logger log = LoggerFactory.getLogger(TickerQueryTest.class);
    private static ApiClient apiClient;

    /**
     * 获取交易对的ticker信息
     *
     * @param symbol 交易对，例如："BTC_USDT"
     * @return Ticker数据
     * @throws HashExApiException 如果API调用失败
     */
    public TickerVO getTickerData(String symbol) throws HashExApiException {
        try {
            // 验证必填参数
            if (symbol == null || symbol.isEmpty()) {
                throw new HashExApiException("交易对不能为空");
            }

            // 创建查询参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();
            queryParams.put("symbol", symbol);

            // 调用API - 使用正确的API路径(注意添加/spot/v1前缀)
            String responseJson = apiClient.sendGetRequest("/spot/v1/p/quotation/trend/ticker", queryParams,false);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            // 使用适合实际返回结构的映射
            ApiResponse<TickerVO> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<TickerVO>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("获取Ticker数据失败: " + apiResponse.getMsg());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("获取Ticker数据时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试获取ticker数据
     */
    private void testGetTickerData() throws HashExApiException {
        log.info("===== 获取Ticker数据测试 =====");

        // 获取BTC_USDT的ticker数据
        TickerVO ticker = getTickerData("BTC_USDT");

        log.info("交易对: {}", ticker.getS());
        log.info("当前时间戳: {}", ticker.getT());
        log.info("最新价格: {}", ticker.getC());
        log.info("开盘价: {}", ticker.getO());
        log.info("24小时最高价: {}", ticker.getH());
        log.info("24小时最低价: {}", ticker.getL());
        log.info("24小时成交量: {}", ticker.getA());
        log.info("24小时成交额: {}", ticker.getV());
        log.info("24小时涨跌幅: {}%", ticker.getR());
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient("https://open.mgbx.com");
        TickerQueryTest tickerQueryTest = new TickerQueryTest();

        tickerQueryTest.testGetTickerData();
    }

    /**
     * Ticker数据模型类 - 根据实际API返回结构定义
     */
    public static class TickerVO {
        private Long t;       // 时间戳
        private String s;     // 交易对
        private String c;     // 最新价格
        private String h;     // 24小时最高价
        private String l;     // 24小时最低价
        private String a;     // 成交量
        private String v;     // 成交额
        private String o;     // 开盘价
        private String r;     // 涨跌幅（百分比）
        private Object tickerTrendVo; // 趋势数据，可能为null

        // Getters and Setters
        public Long getT() {
            return t;
        }

        public void setT(Long t) {
            this.t = t;
        }

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

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public String getV() {
            return v;
        }

        public void setV(String v) {
            this.v = v;
        }

        public String getO() {
            return o;
        }

        public void setO(String o) {
            this.o = o;
        }

        public String getR() {
            return r;
        }

        public void setR(String r) {
            this.r = r;
        }

        public Object getTickerTrendVo() {
            return tickerTrendVo;
        }

        public void setTickerTrendVo(Object tickerTrendVo) {
            this.tickerTrendVo = tickerTrendVo;
        }
    }
}