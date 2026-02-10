package com.example.openapi.test.spot.query;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.TreeMap;

public class SymbolConfigsQueryTest {

    private static final Logger log = LoggerFactory.getLogger(SymbolConfigsQueryTest.class);
    private static ApiClient apiClient;

    /**
     * 获取币种配置信息
     *
     * @return 币种配置列表
     * @throws HashExApiException 如果API调用失败
     */
    public List<SymbolConfigVO> getSymbolConfigs() throws HashExApiException {
        try {
            // 创建查询参数Map (此接口无需参数)
            TreeMap<String, String> queryParams = new TreeMap<>();

            // 调用API
            String responseJson = apiClient.sendGetRequest("/spot/v1/p/symbol/configs", queryParams, false);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            // 使用适合实际返回结构的映射
            ApiResponse<List<SymbolConfigVO>> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<List<SymbolConfigVO>>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("获取币种配置失败: " + apiResponse.getMsg());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("获取币种配置时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 根据交易对符号查询特定币种配置
     *
     * @param symbol 交易对符号，例如："BTC_USDT"
     * @return 币种配置信息，如果未找到返回null
     * @throws HashExApiException 如果API调用失败
     */
    public SymbolConfigVO getSymbolConfigBySymbol(String symbol) throws HashExApiException {
        if (symbol == null || symbol.isEmpty()) {
            throw new HashExApiException("交易对符号不能为空");
        }

        List<SymbolConfigVO> configs = getSymbolConfigs();
        return configs.stream()
                .filter(config -> symbol.equals(config.getSymbol()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 测试获取币种配置信息
     */
    private void testGetSymbolConfigs() throws HashExApiException {
        log.info("===== 获取币种配置信息测试 =====");

        List<SymbolConfigVO> configs = getSymbolConfigs();

        log.info("共获取到 {} 个币种配置", configs.size());

        // 打印前几个配置信息
        int displayCount = Math.min(5, configs.size());
        for (int i = 0; i < displayCount; i++) {
            SymbolConfigVO config = configs.get(i);
            log.info("交易对: {}, 基础资产: {}, 计价资产: {}, 价格精度: {}, 数量精度: {}, Maker费率: {}, Taker费率: {}",
                    config.getSymbol(), config.getBaseAsset(), config.getQuoteAsset(),
                    config.getPricePrecision(), config.getQuantityPrecision(),
                    config.getMakerFee(), config.getTakerFee());
        }

        if (configs.size() > displayCount) {
            log.info("... 还有 {} 个配置信息", configs.size() - displayCount);
        }
    }

    /**
     * 测试查询特定交易对配置
     */
    private void testGetSpecificSymbolConfig() throws HashExApiException {
        log.info("===== 查询特定交易对配置测试 =====");

        String testSymbol = "BTC_USDT";
        SymbolConfigVO config = getSymbolConfigBySymbol(testSymbol);

        if (config != null) {
            log.info("找到交易对 {} 的配置:", testSymbol);
            log.info("基础资产: {}, 基础资产精度: {}", config.getBaseAsset(), config.getBaseAssetPrecision());
            log.info("计价资产: {}, 计价资产精度: {}", config.getQuoteAsset(), config.getQuoteAssetPrecision());
            log.info("价格精度: {}, 数量精度: {}", config.getPricePrecision(), config.getQuantityPrecision());
            log.info("Maker费率: {}, Taker费率: {}", config.getMakerFee(), config.getTakerFee());
        } else {
            log.warn("未找到交易对 {} 的配置", testSymbol);
        }
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient("https://open.mgbx.com");
        SymbolConfigsQueryTest test = new SymbolConfigsQueryTest();

        test.testGetSymbolConfigs();
        test.testGetSpecificSymbolConfig();
    }

    /**
     * 币种配置数据模型类 - 根据实际API返回结构定义
     */
    public static class SymbolConfigVO {
        private String baseAsset;           // 基础资产
        private Integer baseAssetPrecision; // 基础资产精度
        private BigDecimal makerFee;        // Maker费率
        private Integer pricePrecision;     // 价格精度
        private Integer quantityPrecision;  // 数量精度
        private String quoteAsset;          // 计价资产
        private Integer quoteAssetPrecision; // 计价资产精度
        private String symbol;              // 交易对符号
        private BigDecimal takerFee;        // Taker费率

        // Getters and Setters
        public String getBaseAsset() {
            return baseAsset;
        }

        public void setBaseAsset(String baseAsset) {
            this.baseAsset = baseAsset;
        }

        public Integer getBaseAssetPrecision() {
            return baseAssetPrecision;
        }

        public void setBaseAssetPrecision(Integer baseAssetPrecision) {
            this.baseAssetPrecision = baseAssetPrecision;
        }

        public BigDecimal getMakerFee() {
            return makerFee;
        }

        public void setMakerFee(BigDecimal makerFee) {
            this.makerFee = makerFee;
        }

        public Integer getPricePrecision() {
            return pricePrecision;
        }

        public void setPricePrecision(Integer pricePrecision) {
            this.pricePrecision = pricePrecision;
        }

        public Integer getQuantityPrecision() {
            return quantityPrecision;
        }

        public void setQuantityPrecision(Integer quantityPrecision) {
            this.quantityPrecision = quantityPrecision;
        }

        public String getQuoteAsset() {
            return quoteAsset;
        }

        public void setQuoteAsset(String quoteAsset) {
            this.quoteAsset = quoteAsset;
        }

        public Integer getQuoteAssetPrecision() {
            return quoteAssetPrecision;
        }

        public void setQuoteAssetPrecision(Integer quoteAssetPrecision) {
            this.quoteAssetPrecision = quoteAssetPrecision;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public BigDecimal getTakerFee() {
            return takerFee;
        }

        public void setTakerFee(BigDecimal takerFee) {
            this.takerFee = takerFee;
        }
    }
}