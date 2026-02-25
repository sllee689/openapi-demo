package com.example.openapi.test.future.position;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.future.FutureTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * 合约持仓配置查询测试类
 */
public class PositionConfsTest {

    private static final Logger log = LoggerFactory.getLogger(PositionConfsTest.class);
    private static final String[] REQUIRED_ITEM_FIELDS = {
            "symbol", "leverage", "positionType", "positionModel", "positionSide", "autoMargin"
    };
    private static ApiClient apiClient;

    /**
     * 查询持仓配置
     *
     * @param symbol 交易对（必填）
     * @return 持仓配置列表
     * @throws HashExApiException 如果API调用失败
     */
    public List<PositionConfVO> getPositionConfs(String symbol) throws HashExApiException {
        try {
            TreeMap<String, String> queryParams = new TreeMap<>();

            if (symbol == null || symbol.isEmpty()) {
                throw new HashExApiException("交易对不能为空");
            }
            queryParams.put("symbol", symbol);

            String responseJson = apiClient.sendGetRequest("/fut/v1/position/confs", queryParams, true);

            JSONObject jsonObject = JSONUtil.parseObj(responseJson);
            if (jsonObject.getInt("code") != 0) {
                throw new HashExApiException("查询持仓配置失败: " + jsonObject.getStr("msg"));
            }

            JSONArray data = jsonObject.getJSONArray("data");
            if (data == null) {
                throw new HashExApiException("查询持仓配置失败: data为空");
            }

            List<PositionConfVO> result = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                JSONObject item = data.getJSONObject(i);
                validateItemFields(item);

                PositionConfVO conf = new PositionConfVO();
                conf.setSymbol(item.getStr("symbol"));
                conf.setLeverage(item.getInt("leverage"));
                conf.setPositionType(item.getStr("positionType"));
                conf.setPositionModel(item.getStr("positionModel"));
                conf.setPositionSide(item.getStr("positionSide"));
                conf.setAutoMargin(item.getBool("autoMargin"));
                result.add(conf);
            }

            return result;
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("查询持仓配置时出错: " + e.getMessage(), e);
        }
    }

    private void validateItemFields(JSONObject item) throws HashExApiException {
        for (String field : REQUIRED_ITEM_FIELDS) {
            if (!item.containsKey(field)) {
                throw new HashExApiException("持仓配置item缺少字段: " + field + ", item=" + item);
            }
        }
    }

    /**
     * 测试查询指定交易对的持仓配置
     */
    private void testGetPositionConfsBySymbol() throws HashExApiException {
        log.info("===== 查询指定交易对持仓配置 =====");

        String symbol = "btc_usdt";
        List<PositionConfVO> confs = getPositionConfs(symbol);
        log.info("{} 持仓配置 ({} 条):", symbol, confs.size());

        for (PositionConfVO conf : confs) {
            log.info("{}", conf);
        }
    }

    private void testGetPositionConfsBySymbolCaseVariants() throws HashExApiException {
        log.info("===== 测试 symbol 参数大小写 =====");
        String[] symbols = {"btc_usdt", "BTC_USDT"};
        for (String symbol : symbols) {
            try {
                List<PositionConfVO> confs = getPositionConfs(symbol);
                log.info("symbol={} 查询成功, count={}", symbol, confs.size());
            } catch (HashExApiException e) {
                log.warn("symbol={} 查询失败(疑似不支持): {}", symbol, e.getMessage());
            }
        }
    }

    private void testGetPositionConfsWithInvalidSymbol() throws HashExApiException {
        log.info("===== 测试非法 symbol 参数 =====");
        try {
            getPositionConfs("invalid_symbol_xxx");
            throw new HashExApiException("非法symbol预期失败但实际成功");
        } catch (HashExApiException e) {
            log.info("非法symbol按预期失败: {}", e.getMessage());
        }
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient(
                FutureTestConfig.BASE_URL,
                FutureTestConfig.ACCESS_KEY,
                FutureTestConfig.SECRET_KEY);
        PositionConfsTest test = new PositionConfsTest();

        // 执行测试
        test.testGetPositionConfsBySymbol();
        test.testGetPositionConfsBySymbolCaseVariants();
        test.testGetPositionConfsWithInvalidSymbol();
    }

    /**
     * 持仓配置VO
     */
    public static class PositionConfVO {
        private String symbol;              // 交易对
        private Integer leverage;           // 杠杆倍数
        private String positionType;        // 仓位类型：CROSSED(全仓)、ISOLATED(逐仓)
        private String positionModel;       // 仓位模式：AGGREGATION(合仓)、DISAGGREGATION(分仓)
        private String positionSide;        // 持仓方向
        private Boolean autoMargin;         // 是否自动追加保证金

        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public Integer getLeverage() { return leverage; }
        public void setLeverage(Integer leverage) { this.leverage = leverage; }
        public String getPositionType() { return positionType; }
        public void setPositionType(String positionType) { this.positionType = positionType; }
        public String getPositionModel() { return positionModel; }
        public void setPositionModel(String positionModel) { this.positionModel = positionModel; }
        public String getPositionSide() { return positionSide; }
        public void setPositionSide(String positionSide) { this.positionSide = positionSide; }
        public Boolean getAutoMargin() { return autoMargin; }
        public void setAutoMargin(Boolean autoMargin) { this.autoMargin = autoMargin; }

        @Override
        public String toString() {
            return String.format("持仓配置 { 交易对: %s, 杠杆: %dx, 仓位类型: %s, 仓位模式: %s, 持仓方向: %s, 自动追加保证金: %s }",
                    symbol, leverage,
                    positionType, positionModel, positionSide,
                    autoMargin != null && autoMargin ? "是" : "否");
        }
    }
}
