package com.example.openapi.test.future.query;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import com.example.openapi.test.future.FutureTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
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
                throw new HashExApiException("获取全部合约行情数据失败: " + apiResponse.getMsg());
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
            log.info(ticker.toString());

            count++;
            if (count >= 5) break; // 只打印前5个
        }
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient(
                FutureTestConfig.BASE_URL,
                FutureTestConfig.ACCESS_KEY,
                FutureTestConfig.SECRET_KEY);
        FuturesTickersTest tickersTest = new FuturesTickersTest();

        tickersTest.testGetAllFuturesTickers();
    }

    /**
     * 合约行情数据模型类 - 已根据API实际返回结构调整
     */
    public static class FuturesTickerVO {
        private String s;      // 交易对
        private String c;      // 最新价格
        private String o;      // 24小时前价格
        private String h;      // 24小时最高价
        private String l;      // 24小时最低价
        private String a;      // 24小时成交量(文档中为v)
        private String v;      // 24小时成交额(文档中为q)
        private String r;      // 24小时涨跌幅(%)(文档中为p)
        private String t;      // 时间戳
        private TickerTrendVO tickerTrendVo; // 价格趋势数据

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

        public String getR() {
            return r;
        }

        public void setR(String r) {
            this.r = r;
        }

        public String getT() {
            return t;
        }

        public void setT(String t) {
            this.t = t;
        }

        public TickerTrendVO getTickerTrendVo() {
            return tickerTrendVo;
        }

        public void setTickerTrendVo(TickerTrendVO tickerTrendVo) {
            this.tickerTrendVo = tickerTrendVo;
        }
        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timeStr = t != null ? sdf.format(new Date(Long.parseLong(t))) : "未知";

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("交易对: %s, 最新价: %s, 最高价: %s, 最低价: %s\n", s, c, h, l));
            sb.append(String.format("成交量: %s, 成交额: %s, 涨跌幅: %s%%\n", a, v, r));
            sb.append(String.format("时间: %s\n", timeStr));

            if (tickerTrendVo != null) {
                sb.append(tickerTrendVo);
            }

            return sb.toString();
        }
    }

    public static class TickerTrendVO {
        private List<TickerTrendItem> list;

        public List<TickerTrendItem> getList() {
            return list;
        }

        public void setList(List<TickerTrendItem> list) {
            this.list = list;
        }

        @Override
        public String toString() {
            if (list == null || list.isEmpty()) {
                return "没有趋势数据";
            }
            StringBuilder sb = new StringBuilder("价格趋势数据：\n");
            for (TickerTrendItem item : list) {
                sb.append(item).append("\n");
            }
            return sb.toString();
        }
    }

    public static class TickerTrendItem {
        private Integer symbolId;
        private String symbol;
        private Double price;
        private Long time;

        public Integer getSymbolId() {
            return symbolId;
        }

        public void setSymbolId(Integer symbolId) {
            this.symbolId = symbolId;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public Long getTime() {
            return time;
        }

        public void setTime(Long time) {
            this.time = time;
        }

        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return String.format("  [%s] %s 价格: %s 时间: %s",
                    symbolId, symbol, price, sdf.format(new Date(time)));
        }
    }
}