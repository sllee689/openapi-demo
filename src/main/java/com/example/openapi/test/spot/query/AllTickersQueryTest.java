package com.example.openapi.test.spot.query;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TreeMap;

public class AllTickersQueryTest {

    private static final Logger log = LoggerFactory.getLogger(AllTickersQueryTest.class);
    private static ApiClient apiClient;

    /**
     * 获取全交易对的ticker信息
     *
     * @return 所有交易对的Ticker数据列表
     * @throws HashExApiException 如果API调用失败
     */
    public List<TickerVO> getAllTickersData() throws HashExApiException {
        try {
            // 创建空的查询参数Map，因为该API不需要参数
            TreeMap<String, String> queryParams = new TreeMap<>();

            // 调用API，注意添加/spot/v1前缀
            String responseJson = apiClient.sendGetRequest("/spot/v1/p/quotation/tickers", queryParams,false);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            // 使用适合实际返回结构的映射
            ApiResponse<List<TickerVO>> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<List<TickerVO>>>() {}, false);

            if (!apiResponse.isSuccess()) {
                throw new HashExApiException("获取全交易对Ticker数据失败: " + apiResponse.getMsg());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("获取全交易对Ticker数据时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试获取全交易对的ticker数据
     */
    private void testGetAllTickersData() throws HashExApiException {
        log.info("===== 获取全交易对Ticker数据测试 =====");

        // 获取所有交易对的ticker数据
        List<TickerVO> tickerList = getAllTickersData();

        log.info("共获取到 {} 个交易对的行情数据", tickerList.size());

        // 打印前5个交易对的数据作为示例
        int count = 0;
        for (TickerVO ticker : tickerList) {
            log.info("交易对: {}, 最新价格: {}, 24小时涨跌幅: {}%",
                    ticker.getS(), ticker.getC(), ticker.getR());

            count++;
            if (count >= 5) {
                break;
            }
        }

        // 如果想查找特定交易对的数据
        log.info("===== 查找特定交易对 =====");
        tickerList.stream()
                .filter(ticker -> "BTC_USDT".equals(ticker.getS()))
                .findFirst()
                .ifPresent(ticker -> {
                    log.info("BTC_USDT 详细数据:");
                    log.info("交易对: {}", ticker.getS());
                    log.info("当前时间戳: {}", ticker.getT());
                    log.info("最新价格: {}", ticker.getC());
                    log.info("开盘价: {}", ticker.getO());
                    log.info("24小时最高价: {}", ticker.getH());
                    log.info("24小时最低价: {}", ticker.getL());
                    log.info("24小时成交量: {}", ticker.getA());
                    log.info("24小时成交额: {}", ticker.getV());
                    log.info("24小时涨跌幅: {}%", ticker.getR());
                });
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient("https://open.mgbx.com");
        AllTickersQueryTest allTickersQueryTest = new AllTickersQueryTest();

        allTickersQueryTest.testGetAllTickersData();
    }

    /**
     * Ticker数据模型类 - 复用已有的TickerVO类结构
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