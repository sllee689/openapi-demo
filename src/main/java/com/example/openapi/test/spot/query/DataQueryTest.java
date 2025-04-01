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

public class DataQueryTest {

    private static final Logger log = LoggerFactory.getLogger(DataQueryTest.class);
    private static ApiClient apiClient;

    /**
     * 获取K线数据
     *
     * @param symbol 交易对，例如："BTC_USDT"
     * @param interval 时间间隔，例如："1m", "5m", "15m", "30m", "1h", "4h", "1d", "1w", "1M"
     * @param limit 限制条数，默认500，最大1500
     * @param startTime 起始时间（毫秒时间戳）
     * @param endTime 结束时间（毫秒时间戳）
     * @return K线数据列表
     * @throws HashExApiException 如果API调用失败
     */
    public List<KlineVO> getKlineData(String symbol, String interval, Integer limit,
                                      Long startTime, Long endTime) throws HashExApiException {
        try {
            // 验证必填参数
            if (symbol == null || symbol.isEmpty()) {
                throw new HashExApiException("交易对不能为空");
            }
            if (interval == null || interval.isEmpty()) {
                throw new HashExApiException("时间间隔不能为空");
            }

            // 创建查询参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();
            queryParams.put("symbol", symbol);
            queryParams.put("interval", interval);

            // 添加可选参数
            if (limit != null) {
                // 验证limit范围
                if (limit < 1 || limit > 1500) {
                    throw new HashExApiException("limit必须在1到1500之间");
                }
                queryParams.put("limit", limit.toString());
            }

            if (startTime != null) {
                queryParams.put("startTime", startTime.toString());
            }

            if (endTime != null) {
                queryParams.put("endTime", endTime.toString());
            }

            // 调用API - 使用正确的API路径
            String responseJson = apiClient.sendGetRequest("/spot/v1/p/quotation/kline", queryParams,false);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            // 使用适合实际返回结构的映射
            ApiResponse<List<KlineVO>> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<List<KlineVO>>>() {}, false);

            // 使用msg而不是message来判断信息
            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("获取K线数据失败: " + apiResponse.getMessage());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("获取K线数据时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试获取最近的K线数据
     */
    private void testGetKlineData() throws HashExApiException {
        log.info("===== 获取K线数据测试 =====");

        // 获取BTC_USDT的1小时K线，最近10条
        List<KlineVO> klineList = getKlineData("BTC_USDT", "1h", 10, null, null);

        log.info("获取到{}条K线数据", klineList.size());
        for (KlineVO kline : klineList) {
            log.info("交易对: {}, 时间: {}, 开盘价: {}, 最高价: {}, 最低价: {}, 收盘价: {}, 成交量: {}, 成交额: {}",
                    kline.getS(), kline.getT(), kline.getO(), kline.getH(),
                    kline.getL(), kline.getC(), kline.getA(), kline.getV());
        }
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient("https://open.hashex.vip",
                "0a9970e8986247d6e6d5deadc886a4e558c0a1c4f2047c2a00bc96e2efd24499",
                "yZGQ4OWExMjVmMWViYWE1MmU0ZGQwY2ZmODQ4NDI0ZWI0OWU1MTUyNmUyZDU4NWJmZWRmYmM4ZDA1NWEyYjAxMmE=");
        DataQueryTest dataQueryTest = new DataQueryTest();

        dataQueryTest.testGetKlineData();
    }

    /**
     * K线数据模型类 - 根据实际API返回结构定义
     */
    public static class KlineVO {
        private String s;    // 交易对
        private Long t;      // 时间戳
        private String o;    // 开盘价
        private String h;    // 最高价
        private String l;    // 最低价
        private String c;    // 收盘价
        private String a;    // 成交量
        private String v;    // 成交额

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

        public String getC() {
            return c;
        }

        public void setC(String c) {
            this.c = c;
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
    }
}