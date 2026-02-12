package com.example.openapi.test.future.position;

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

public class PositionListTest {

    private static final Logger log = LoggerFactory.getLogger(PositionListTest.class);
    private static ApiClient apiClient;
    private static final String DEFAULT_SYMBOL = System.getProperty("openapi.symbol", "btc_usdt");
    private static final String DEFAULT_CONTRACT_TYPE = System.getProperty("openapi.contractType", "PERPETUAL");
    private static final String DEFAULT_BALANCE_TYPE = System.getProperty("openapi.balanceType", "CONTRACT");

    /**
     * 获取持仓列表
     *
     * @param symbol       交易对（可选）
     * @param contractType 合约类型（可选）：PERPETUAL(永续)、SUPER(超级合约)
     * @param balanceType  余额类型（可选）：CONTRACT(合约)、COPY(跟单)
     * @return 持仓信息列表
     * @throws HashExApiException 如果API调用失败
     */
    public List<PositionVO> getPositionList(String symbol, String contractType, String balanceType) throws HashExApiException {
        try {
            TreeMap<String, String> queryParams = new TreeMap<>();

            if (symbol != null && !symbol.isEmpty()) {
                queryParams.put("symbol", symbol);
            }
            if (contractType != null && !contractType.isEmpty()) {
                queryParams.put("contractType", contractType);
            }
            if (balanceType != null && !balanceType.isEmpty()) {
                queryParams.put("balanceType", balanceType);
            }

            String responseJson = apiClient.sendGetRequest("/fut/v1/position/list", queryParams, true);

            JSONObject jsonObject = new JSONObject(responseJson);

            ApiResponse<List<PositionVO>> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<List<PositionVO>>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("获取持仓列表失败: " + apiResponse.getMsg());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("获取持仓列表时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试获取持仓列表
     */
    private void testGetPositionList() throws HashExApiException {
        log.info("===== 获取持仓列表测试 =====");

        // 不传入任何参数，获取所有持仓
        List<PositionVO> allPositions = getPositionList(null, null, null);
        log.info("共获取到 {} 条持仓记录", allPositions.size());
        logPositionDetails(allPositions);
    }

    /**
     * 测试带参数获取持仓列表
     */
    private void testGetPositionListWithFilters() throws HashExApiException {
        log.info("===== 按条件获取持仓列表测试 =====");
        List<PositionVO> filteredPositions = getPositionList(DEFAULT_SYMBOL, DEFAULT_CONTRACT_TYPE, DEFAULT_BALANCE_TYPE);
        log.info("使用 symbol={}, contractType={}, balanceType={} 获取到 {} 条持仓记录",
                DEFAULT_SYMBOL, DEFAULT_CONTRACT_TYPE, DEFAULT_BALANCE_TYPE, filteredPositions.size());
        if (filteredPositions.isEmpty()) {
            log.warn("过滤条件未返回持仓，请确认账户下是否存在对应持仓。");
        }
        logPositionDetails(filteredPositions);
    }

    private void logPositionDetails(List<PositionVO> positions) {
        for (PositionVO position : positions) {
            log.info("交易对: {}, 持仓ID: {}, 合约类型: {}, 持仓方向: {}, 持仓数量: {}, 开仓均价: {}, 杠杆: {}x, 逐仓保证金: {}, 已实现盈亏: {}",
                    position.getSymbol(), position.getPositionId(), position.getContractType(),
                    position.getPositionSide(), position.getPositionSize(), position.getEntryPrice(),
                    position.getLeverage(), position.getIsolatedMargin(), position.getRealizedProfit());

            if (position.getProfit() != null) {
                ProfitVO profit = position.getProfit();
                log.info("  止盈止损 - 止盈价: {}, 止损价: {}, 触发价格类型: {}",
                        profit.getProfitPrice(), profit.getStopPrice(), profit.getTriggerPriceType());
            }
        }
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient(
                FutureTestConfig.BASE_URL,
                FutureTestConfig.ACCESS_KEY,
                FutureTestConfig.SECRET_KEY);
        PositionListTest positionTest = new PositionListTest();

        positionTest.testGetPositionList();
        positionTest.testGetPositionListWithFilters();
    }

    /**
     * 持仓数据模型类
     */
    public static class PositionVO {
        private String symbol;                  // 交易对
        private String positionId;              // 持仓ID
        private String contractType;            // 合约类型
        private String balanceType;             // 余额类型
        private String positionType;            // 仓位类型
        private String positionSide;            // 持仓方向
        private String positionModel;           // 持仓模式
        private String underlyingType;          // 合约类型(U本位/币本位)
        private String positionSize;            // 持仓数量（张）
        private String closeOrderSize;          // 平仓挂单数量（张）
        private String availableCloseSize;      // 可平仓数量（张）
        private String entryPrice;              // 开仓均价
        private String isolatedMargin;          // 逐仓保证金
        private String bounsMargin;             // 体验金占用比例
        private String openOrderMarginFrozen;   // 开仓订单保证金占用
        private String realizedProfit;          // 已实现盈亏
        private Boolean autoMargin;             // 是否自动追加保证金
        private Integer leverage;               // 杠杆倍数
        private Boolean isLeaderPosition;       // 是否是带单仓位
        private Long updatedTime;               // 更新时间
        private String unsettledProfit;         // 未结算盈亏
        private ProfitVO profit;                // 止盈止损

        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getPositionId() { return positionId; }
        public void setPositionId(String positionId) { this.positionId = positionId; }
        public String getContractType() { return contractType; }
        public void setContractType(String contractType) { this.contractType = contractType; }
        public String getBalanceType() { return balanceType; }
        public void setBalanceType(String balanceType) { this.balanceType = balanceType; }
        public String getPositionType() { return positionType; }
        public void setPositionType(String positionType) { this.positionType = positionType; }
        public String getPositionSide() { return positionSide; }
        public void setPositionSide(String positionSide) { this.positionSide = positionSide; }
        public String getPositionModel() { return positionModel; }
        public void setPositionModel(String positionModel) { this.positionModel = positionModel; }
        public String getUnderlyingType() { return underlyingType; }
        public void setUnderlyingType(String underlyingType) { this.underlyingType = underlyingType; }
        public String getPositionSize() { return positionSize; }
        public void setPositionSize(String positionSize) { this.positionSize = positionSize; }
        public String getCloseOrderSize() { return closeOrderSize; }
        public void setCloseOrderSize(String closeOrderSize) { this.closeOrderSize = closeOrderSize; }
        public String getAvailableCloseSize() { return availableCloseSize; }
        public void setAvailableCloseSize(String availableCloseSize) { this.availableCloseSize = availableCloseSize; }
        public String getEntryPrice() { return entryPrice; }
        public void setEntryPrice(String entryPrice) { this.entryPrice = entryPrice; }
        public String getIsolatedMargin() { return isolatedMargin; }
        public void setIsolatedMargin(String isolatedMargin) { this.isolatedMargin = isolatedMargin; }
        public String getBounsMargin() { return bounsMargin; }
        public void setBounsMargin(String bounsMargin) { this.bounsMargin = bounsMargin; }
        public String getOpenOrderMarginFrozen() { return openOrderMarginFrozen; }
        public void setOpenOrderMarginFrozen(String openOrderMarginFrozen) { this.openOrderMarginFrozen = openOrderMarginFrozen; }
        public String getRealizedProfit() { return realizedProfit; }
        public void setRealizedProfit(String realizedProfit) { this.realizedProfit = realizedProfit; }
        public Boolean getAutoMargin() { return autoMargin; }
        public void setAutoMargin(Boolean autoMargin) { this.autoMargin = autoMargin; }
        public Integer getLeverage() { return leverage; }
        public void setLeverage(Integer leverage) { this.leverage = leverage; }
        public Boolean getIsLeaderPosition() { return isLeaderPosition; }
        public void setIsLeaderPosition(Boolean isLeaderPosition) { this.isLeaderPosition = isLeaderPosition; }
        public Long getUpdatedTime() { return updatedTime; }
        public void setUpdatedTime(Long updatedTime) { this.updatedTime = updatedTime; }
        public String getUnsettledProfit() { return unsettledProfit; }
        public void setUnsettledProfit(String unsettledProfit) { this.unsettledProfit = unsettledProfit; }
        public ProfitVO getProfit() { return profit; }
        public void setProfit(ProfitVO profit) { this.profit = profit; }
    }

    /**
     * 止盈止损数据模型类
     */
    public static class ProfitVO {
        private String profitId;            // 止盈止损ID
        private String profitPrice;         // 止盈价
        private String stopPrice;           // 止损价
        private String triggerPriceType;    // 触发价格类型

        public String getProfitId() { return profitId; }
        public void setProfitId(String profitId) { this.profitId = profitId; }
        public String getProfitPrice() { return profitPrice; }
        public void setProfitPrice(String profitPrice) { this.profitPrice = profitPrice; }
        public String getStopPrice() { return stopPrice; }
        public void setStopPrice(String stopPrice) { this.stopPrice = stopPrice; }
        public String getTriggerPriceType() { return triggerPriceType; }
        public void setTriggerPriceType(String triggerPriceType) { this.triggerPriceType = triggerPriceType; }
    }
}
