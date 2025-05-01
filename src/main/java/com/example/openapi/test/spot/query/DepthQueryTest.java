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

public class DepthQueryTest {

    private static final Logger log = LoggerFactory.getLogger(DepthQueryTest.class);
    private static ApiClient apiClient;

    /**
     * 获取交易对的深度信息
     *
     * @param symbol 交易对，例如："BTC_USDT"
     * @param level 档位，范围：1-50
     * @return 深度数据
     * @throws HashExApiException 如果API调用失败
     */
    public DepthVO getDepthData(String symbol, Integer level) throws HashExApiException {
        try {
            // 验证必填参数
            if (symbol == null || symbol.isEmpty()) {
                throw new HashExApiException("交易对不能为空");
            }

            if (level == null) {
                throw new HashExApiException("档位不能为空");
            }

            // 验证level范围
            if (level < 1 || level > 50) {
                throw new HashExApiException("档位必须在1到50之间");
            }

            // 创建查询参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();
            queryParams.put("symbol", symbol);
            queryParams.put("level", level.toString());

            // 调用API，添加/spot/v1前缀
            String responseJson = apiClient.sendGetRequest("/spot/v1/p/quotation/depth", queryParams,false);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            // 使用适合实际返回结构的映射
            ApiResponse<DepthVO> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<DepthVO>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("获取深度数据失败: " + apiResponse.getMsg());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("获取深度数据时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试获取深度数据
     */
    private void testGetDepthData() throws HashExApiException {
        log.info("===== 获取深度数据测试 =====");

        // 获取BTC_USDT的10档深度数据
        DepthVO depth = getDepthData("BTC_USDT", 10);

        log.info("交易对: {}", depth.getS());
        log.info("时间戳: {}", depth.getT());
        log.info("更新ID: {}", depth.getU());

        // 打印买盘数据
        log.info("买盘数据 (前5档):");
        int count = 0;
        for (List<String> bid : depth.getB()) {
            log.info("价格: {}, 数量: {}", bid.get(0), bid.get(1));
            count++;
            if (count >= 5) {
                break;
            }
        }

        // 打印卖盘数据
        log.info("卖盘数据 (前5档):");
        count = 0;
        for (List<String> ask : depth.getA()) {
            log.info("价格: {}, 数量: {}", ask.get(0), ask.get(1));
            count++;
            if (count >= 5) {
                break;
            }
        }
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient("https://open.mgbx.com");
        DepthQueryTest depthQueryTest = new DepthQueryTest();

        depthQueryTest.testGetDepthData();
    }

    /**
     * 深度数据模型类 - 根据实际API返回结构定义
     */
    public static class DepthVO {
        private Long t;                // 时间戳
        private String s;              // 交易对
        private Long u;                // 更新ID
        private List<List<String>> b;  // 买盘 [价格, 数量]
        private List<List<String>> a;  // 卖盘 [价格, 数量]

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

        public Long getU() {
            return u;
        }

        public void setU(Long u) {
            this.u = u;
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