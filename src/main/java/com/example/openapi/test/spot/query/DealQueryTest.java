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

public class DealQueryTest {

    private static final Logger log = LoggerFactory.getLogger(DealQueryTest.class);
    private static ApiClient apiClient;

    /**
     * 获取交易对的最新成交信息
     *
     * @param symbol 交易对，例如："BTC_USDT"
     * @param num 获取数量
     * @return 成交数据列表
     * @throws HashExApiException 如果API调用失败
     */
    public List<DealVO> getDealData(String symbol, Integer num) throws HashExApiException {
        try {
            // 验证必填参数
            if (symbol == null || symbol.isEmpty()) {
                throw new HashExApiException("交易对不能为空");
            }

            if (num == null || num < 1) {
                throw new HashExApiException("数量必须大于等于1");
            }

            // 创建查询参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();
            queryParams.put("symbol", symbol);
            queryParams.put("num", num.toString());

            // 调用API，添加/spot/v1前缀
            String responseJson = apiClient.sendGetRequest("/spot/v1/p/quotation/deal", queryParams,false);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            // 使用适合实际返回结构的映射
            ApiResponse<List<DealVO>> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<List<DealVO>>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("获取成交数据失败: " + apiResponse.getMsg());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("获取成交数据时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试获取最新成交信息
     */
    private void testGetDealData() throws HashExApiException {
        log.info("===== 获取最新成交信息测试 =====");

        // 获取BTC_USDT的10条最新成交数据
        List<DealVO> deals = getDealData("BTC_USDT", 10);

        log.info("共获取到 {} 条成交记录", deals.size());

        // 打印成交记录
        for (DealVO deal : deals) {
            log.info("时间: {}, 交易对: {}, 价格: {}, 数量: {}, 方向: {}",
                    deal.getT(), deal.getS(), deal.getP(),
                    deal.getA(), "BID".equals(deal.getM()) ? "买入" : "卖出");
        }
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient("https://open.hashex.vip",
                "0a9970e8986247d6e6d5deadc886a4e558c0a1c4f2047c2a00bc96e2efd24499",
                "dd89a125f1ebaa52e4dd0cff848424eb49e51526e2d585bfedfbc8d055a2b01a");
        DealQueryTest dealQueryTest = new DealQueryTest();

        dealQueryTest.testGetDealData();
    }

    /**
     * 成交数据模型类 - 根据实际API返回结构定义
     */
    public static class DealVO {
        private Long t;        // 成交时间戳
        private String s;      // 交易对
        private String p;      // 成交价格
        private String a;      // 成交数量
        private String m;      // 成交方向：BID-买入，ASK-卖出

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

        public String getM() {
            return m;
        }

        public void setM(String m) {
            this.m = m;
        }
    }
}