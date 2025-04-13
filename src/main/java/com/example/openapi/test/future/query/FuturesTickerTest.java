package com.example.openapi.test.future.query;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
                throw new HashExApiException("获取合约行情数据失败: " + apiResponse.getMsg());
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
                tickerData.getS(), tickerData.getC(), tickerData.getR(), tickerData.getH(),
                tickerData.getL(), tickerData.getA(), tickerData.getV());
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
        private String a;      // 24小时成交量 (注意：这是API实际返回的字��)
        private String v;      // 24小时成交额 (注意：这是API实际返回的字段)
        private String r;      // 24小时涨跌幅(%) (注意：这是API实际返回的字段)
        private Long t;        // 时间戳 (改为Long类型)
        private TickerTrendVO tickerTrendVo; // 价格趋势数据

        // 现有的getter/setter需要修改为对应新字段

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public String getR() {
            return r;
        }

        public void setR(String r) {
            this.r = r;
        }

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

        public TickerTrendVO getTickerTrendVo() {
            return tickerTrendVo;
        }

        public void setTickerTrendVo(TickerTrendVO tickerTrendVo) {
            this.tickerTrendVo = tickerTrendVo;
        }

        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            StringBuilder sb = new StringBuilder();
            sb.append("交易对: ").append(s)
                    .append(", 最新价格: ").append(c)
                    .append(", 24h涨跌幅: ").append(r).append("%")
                    .append(", 24h最高: ").append(h)
                    .append(", 24h最低: ").append(l)
                    .append(", 24h成交量: ").append(a)
                    .append(", 24h成交额: ").append(v)
                    .append(", 时间: ").append(t != null ? sdf.format(new Date(t)) : "未知");

            if (tickerTrendVo != null) {
                sb.append("\n").append(tickerTrendVo);
            }

            return sb.toString();
        }
        /**
         * 价格趋势数据VO类
         */
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

        /**
         * 价格趋势数据项类
         */
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
}