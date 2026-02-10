package com.example.openapi.test.spot;

import com.example.openapi.utils.HashexApiUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Console;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 现货余额查询测试
 */
public class SpotBalanceTest {
    private static final Logger log = LoggerFactory.getLogger(SpotBalanceTest.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    // //替换自己的 accessKey 和 secretKey
    private static final String BASE_URL = "https://open.mgbx.com";
    private static final String ACCESS_KEY = "e7579248d6edd162cc573524f56ce2c5c6e93980aa1e7b2fed6405c9dd06d87a";
    private static final String SECRET_KEY = "5fefe4c332c14ae19f33223fefa65de0dad0ae8786d486a1ca5e9221673a2d22";

   //private static final String BASE_URL = "https://api.hashex.vip";
   // private static final String ACCESS_KEY = "42106772bfbc981b2439a0840eef5342d4f3ff86d1d75c8e77ea9ad1f4b2b725";
  //  private static final String SECRET_KEY = "e4cf1a747f2644698828cf96e68b57505dc50790317903017ec29b741471d461";

    // 接口端点
    private static final String BALANCE_ENDPOINT = "/spot/v1/u/balance/spot";

    public static void main(String[] args) {
        CloseableHttpClient httpClient = null;
        try {
            // 创建HTTP客户端
            httpClient = HttpClients.createDefault();

            // 查询所有币种余额
            log.info("==== 查询所有币种余额 ====");
            ApiResponse<List<BalanceInfo>> allBalancesResponse = queryBalance(httpClient, null);

            if (allBalancesResponse.getCode() == 0) {
                List<BalanceInfo> balances = allBalancesResponse.getData();
                log.info("查询到 {} 个币种的余额信息", balances.size());

                for (BalanceInfo balance : balances) {
                    log.info("币种: {}, 可用余额: {}, 总余额: {}, 估值(USDT): {}, 估值(CNY): {}",
                            balance.getCoin(),
                            balance.getAvailableBalance(),
                            balance.getBalance(),
                            balance.getEstimatedTotalAmount(),
                            balance.getEstimatedCynAmount());
                }
            } else {
                log.error("查询所有币种余额失败: code={}, msg={}",
                        allBalancesResponse.getCode(),
                        allBalancesResponse.getMsg());
            }

            // 查询指定币种余额
            log.info("==== 查询指定币种余额 ====");
            ApiResponse<List<BalanceInfo>> btcBalanceResponse = queryBalance(httpClient, "BTC");

            if (btcBalanceResponse.getCode() == 0 && !btcBalanceResponse.getData().isEmpty()) {
                BalanceInfo btcBalance = btcBalanceResponse.getData().get(0);
                log.info("BTC余额信息:");
                log.info("  总余额: {}", btcBalance.getBalance());
                log.info("  可用余额: {}", btcBalance.getAvailableBalance());
                log.info("  冻结金额: {}", btcBalance.getFreeze());
                log.info("  估值(USDT): {}", btcBalance.getEstimatedTotalAmount());
                log.info("  估值(CNY): {}", btcBalance.getEstimatedCynAmount());
            } else {
                log.error("查询BTC余额失败: code={}, msg={}",
                        btcBalanceResponse.getCode(),
                        btcBalanceResponse.getMsg());
            }

        } catch (Exception e) {
            log.error("查询余额失败: {}", e.getMessage());
            log.error("异常详情", e);
        } finally {
            // 释放资源
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    log.error("关闭HTTP客户端失败: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 查询现货余额
     * @param httpClient HTTP客户端
     * @param coin 币种，可为null查询所有币种
     * @return 余额信息对象
     * @throws Exception 请求异常
     */
    private static ApiResponse<List<BalanceInfo>> queryBalance(CloseableHttpClient httpClient, String coin) throws Exception {
        // 构建查询参数
        TreeMap<String, String> queryParams = new TreeMap<>();
        if (coin != null && !coin.isEmpty()) {
            queryParams.put("coin", coin);
        }

        // 构建完整URL
        String endPointUrl = BASE_URL + BALANCE_ENDPOINT;
        URIBuilder uriBuilder = new URIBuilder(endPointUrl);
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            uriBuilder.addParameter(entry.getKey(), entry.getValue());
        }

        URI uri = uriBuilder.build();

        // 创建GET请求
        HttpGet httpGet = new HttpGet(uri);

        // 添加认证头
        HashexApiUtils.addAuthHeaders(httpGet, ACCESS_KEY, SECRET_KEY, queryParams);

        // 执行请求
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            int statusCode = response.getCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            log.info("接口 {},返回结果{}", BALANCE_ENDPOINT, responseBody);
            if (statusCode >= 200 && statusCode < 300) {
                return objectMapper.readValue(
                        responseBody,
                        objectMapper.getTypeFactory().constructParametricType(
                                ApiResponse.class,
                                objectMapper.getTypeFactory().constructCollectionType(List.class, BalanceInfo.class)
                        )
                );
            } else {
                throw new RuntimeException("API请求失败，状态码: " + statusCode + ", 响应: " + responseBody);
            }
        }
    }

    /**
     * API响应基础类
     */
    public static class ApiResponse<T> {
        private Integer code;    // 状态码
        private String msg;      // 响应消息
        private T data;          // 响应数据

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }

    /**
     * 余额信息类
     */
    public static class BalanceInfo {
        private String coin;                   // 币种
        private String balance;                // 总余额
        private String freeze;                 // 冻结金额
        private String availableBalance;       // 可用余额
        private String estimatedTotalAmount;   // 估计总金额（USDT）
        private String estimatedCynAmount;     // 估计总金额（CNY）
        private String estimatedAvailableAmount; // 估计可用金额
        private String estimatedFreeze;        // 估计冻结金额
        private String estimatedCoinType;      // 估价使用的币种

        public String getCoin() {
            return coin;
        }

        public void setCoin(String coin) {
            this.coin = coin;
        }

        public String getBalance() {
            return balance;
        }

        public void setBalance(String balance) {
            this.balance = balance;
        }

        public String getFreeze() {
            return freeze;
        }

        public void setFreeze(String freeze) {
            this.freeze = freeze;
        }

        public String getAvailableBalance() {
            return availableBalance;
        }

        public void setAvailableBalance(String availableBalance) {
            this.availableBalance = availableBalance;
        }

        public String getEstimatedTotalAmount() {
            return estimatedTotalAmount;
        }

        public void setEstimatedTotalAmount(String estimatedTotalAmount) {
            this.estimatedTotalAmount = estimatedTotalAmount;
        }

        public String getEstimatedCynAmount() {
            return estimatedCynAmount;
        }

        public void setEstimatedCynAmount(String estimatedCynAmount) {
            this.estimatedCynAmount = estimatedCynAmount;
        }

        public String getEstimatedAvailableAmount() {
            return estimatedAvailableAmount;
        }

        public void setEstimatedAvailableAmount(String estimatedAvailableAmount) {
            this.estimatedAvailableAmount = estimatedAvailableAmount;
        }

        public String getEstimatedFreeze() {
            return estimatedFreeze;
        }

        public void setEstimatedFreeze(String estimatedFreeze) {
            this.estimatedFreeze = estimatedFreeze;
        }

        public String getEstimatedCoinType() {
            return estimatedCoinType;
        }

        public void setEstimatedCoinType(String estimatedCoinType) {
            this.estimatedCoinType = estimatedCoinType;
        }
    }
}