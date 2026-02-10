package com.example.openapi.test.future.order;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import com.example.openapi.test.future.FutureTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TreeMap;

public class UserBalanceTest {

    private static final Logger log = LoggerFactory.getLogger(UserBalanceTest.class);
    private static ApiClient apiClient;

    /**
     * 获取用户资金
     *
     * @param balanceType 资金类型（可选）
     * @return 资金信息列表
     * @throws HashExApiException 如果API调用失败
     */
    public List<BalanceVO> getUserBalance(String balanceType) throws HashExApiException {
        try {
            // 创建查询参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();

            // 如果提供了balanceType，添加到查询参数中
            if (balanceType != null && !balanceType.isEmpty()) {
                queryParams.put("balanceType", balanceType);
            }

            // 调用API（需要认证，所以第三个参数为true）
            String responseJson = apiClient.sendGetRequest("/fut/v1/balance/list", queryParams, true);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            // 使用适合实际返回结构的映射
            ApiResponse<List<BalanceVO>> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<List<BalanceVO>>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("获取用户资金失败: " + apiResponse.getMsg());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("获取用户资金时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试获取用户资金
     */
    private void testGetUserBalance() throws HashExApiException {
        log.info("===== 获取用户资金测试 =====");

        // 不传入资金类型，获取所有资金
        List<BalanceVO> allBalances = getUserBalance(null);
        log.info("共获取到 {} 条资金记录", allBalances.size());

        // 打印资金信息
        for (BalanceVO balance : allBalances) {
            log.info("币种: {}, 资金类型: {}, 钱包余额: {}, 可用余额: {}, 委托冻结: {}, 逐仓保证金: {}, 全仓保证金: {}, 赠送金额: {}",
                    balance.getCoin(), balance.getBalanceType(), balance.getWalletBalance(),
                    balance.getAvailableBalance(), balance.getOpenOrderMarginFrozen(),
                    balance.getIsolatedMargin(), balance.getCrossedMargin(), balance.getBonus());
        }

        // 可选：按特定资金类型筛选
        // List<BalanceVO> contractBalances = getUserBalance("CONTRACT");
        // log.info("共获取到 {} 条合约资金记录", contractBalances.size());
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient(
            FutureTestConfig.BASE_URL,
            FutureTestConfig.ACCESS_KEY,
            FutureTestConfig.SECRET_KEY);
        UserBalanceTest balanceTest = new UserBalanceTest();

        balanceTest.testGetUserBalance();
    }

    /**
     * 用户资金数据模型类 - 根据实际返回结构优化
     */
    public static class BalanceVO {
        private String coin;                  // 币种
        private String balanceType;           // 资金类型
        private String walletBalance;         // 钱包余额
        private String availableBalance;      // 可用余额
        private String openOrderMarginFrozen; // 委托冻结金额
        private String isolatedMargin;        // 逐仓保证金
        private String crossedMargin;         // 全仓保证金
        private String bonus;                 // 赠送金额

        // Getters and Setters
        public String getCoin() {
            return coin;
        }

        public void setCoin(String coin) {
            this.coin = coin;
        }

        public String getBalanceType() {
            return balanceType;
        }

        public void setBalanceType(String balanceType) {
            this.balanceType = balanceType;
        }

        public String getWalletBalance() {
            return walletBalance;
        }

        public void setWalletBalance(String walletBalance) {
            this.walletBalance = walletBalance;
        }

        public String getAvailableBalance() {
            return availableBalance;
        }

        public void setAvailableBalance(String availableBalance) {
            this.availableBalance = availableBalance;
        }

        public String getOpenOrderMarginFrozen() {
            return openOrderMarginFrozen;
        }

        public void setOpenOrderMarginFrozen(String openOrderMarginFrozen) {
            this.openOrderMarginFrozen = openOrderMarginFrozen;
        }

        public String getIsolatedMargin() {
            return isolatedMargin;
        }

        public void setIsolatedMargin(String isolatedMargin) {
            this.isolatedMargin = isolatedMargin;
        }

        public String getCrossedMargin() {
            return crossedMargin;
        }

        public void setCrossedMargin(String crossedMargin) {
            this.crossedMargin = crossedMargin;
        }

        public String getBonus() {
            return bonus;
        }

        public void setBonus(String bonus) {
            this.bonus = bonus;
        }
    }
}