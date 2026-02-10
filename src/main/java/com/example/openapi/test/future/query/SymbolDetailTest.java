package com.example.openapi.test.future.query;

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

public class SymbolDetailTest {

    private static final Logger log = LoggerFactory.getLogger(SymbolDetailTest.class);
    private static ApiClient apiClient;

    /**
     * 获取交易对配置信息
     *
     * @param symbol 交易对，例如 "btc_usdt"
     * @return 交易对配置信息
     * @throws HashExApiException 如果API调用失败
     */
    public SymbolDetailVO getSymbolDetail(String symbol) throws HashExApiException {
        try {
            // 创建查询参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();

            // 如果提供了symbol，添加到查询参数中
            if (symbol != null && !symbol.isEmpty()) {
                queryParams.put("symbol", symbol);
            }

            // 调用API（公共接口不需要认证，第三个参数为false）
            String responseJson = apiClient.sendGetRequest("/fut/v1/public/symbol/detail", queryParams, false);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            // 使用适合实际返回结构的映射
            ApiResponse<SymbolDetailVO> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<SymbolDetailVO>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("获取交易对配置信息失败: " + apiResponse.getMsg());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("获取交易对配置信息时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试获取交易对配置信息
     */
    private void testGetSymbolDetail() throws HashExApiException {
        log.info("===== 获取交易对配置信息测试 =====");

        // 获取指定交易对的配置
        String symbol = "btc_usdt";
        SymbolDetailVO symbolDetail = getSymbolDetail(symbol);

        log.info("交易对: {} 配置信息如下:", symbol);
        log.info("交易对唯一ID: {}", symbolDetail.getId());
        log.info("交易对名称: {}", symbolDetail.getSymbol());
        log.info("合约类型: {} (1:永续;2:交割)", symbolDetail.getContractType());
        log.info("标的类型: {} (1:币本位合约;2:USDT本位合约)", symbolDetail.getUnderlyingType());
        log.info("标的资产: {}, 报价资产: {}", symbolDetail.getBaseCoin(), symbolDetail.getQuoteCoin());
        log.info("中文名称: {}, 英文名称: {}", symbolDetail.getCnName(), symbolDetail.getEnName());
        log.info("合约面值: {}", symbolDetail.getContractSize());

        // 精度相关
        log.info("价格精度: {}, 最小价格变动单位: {}", symbolDetail.getPricePrecision(), symbolDetail.getMinStepPrice());
        log.info("数量精度: {}", symbolDetail.getQuantityPrecision());
        log.info("标的币种精度: {}, 显示精度: {}", symbolDetail.getBaseCoinPrecision(), symbolDetail.getBaseCoinDisplayPrecision());
        log.info("报价币种精度: {}, 显示精度: {}", symbolDetail.getQuoteCoinPrecision(), symbolDetail.getQuoteCoinDisplayPrecision());

        // 交易限制
        log.info("最小交易单位: {}", symbolDetail.getMinQty());
        log.info("最小名义价值: {}, 最大名义价值: {}", symbolDetail.getMinNotional(), symbolDetail.getMaxNotional());
        log.info("初始杠杆倍数: {}x", symbolDetail.getInitLeverage());
        log.info("初始化持仓模式: {} (1全仓 2逐仓)", symbolDetail.getInitPositionType());

        // 费率信息
        log.info("maker手续费率: {}, taker手续费率: {}", symbolDetail.getMakerFee(), symbolDetail.getTakerFee());
        log.info("强平手续费率: {}", symbolDetail.getLiquidationFee());
        log.info("市价单最多价格偏离: {}", symbolDetail.getMarketTakeBound());

        // 支持的订单类型和时效
        log.info("支持订单类型: {}", symbolDetail.getSupportOrderType());
        log.info("支持有效方式: {}", symbolDetail.getSupportTimeInForce());
        log.info("支持计划委托类型: {}", symbolDetail.getSupportEntrustType());
        log.info("支持仓位类型: {}", symbolDetail.getSupportPositionType());

        // 价格限制
        log.info("撮合限价买单价格上限百分比: {}", symbolDetail.getMultiplierUp());
        log.info("撮合限价卖单下限百分比: {}", symbolDetail.getMultiplierDown());

        // 其他信息
        log.info("最多open订单数: {}, 最多open条件单数: {}", symbolDetail.getMaxOpenOrders(), symbolDetail.getMaxEntrusts());
        log.info("盘口精度合并: {}", symbolDetail.getDepthPrecisionMerge());
        log.info("超级合约杠杆倍数: {}", symbolDetail.getSuperLeverage());
        log.info("上线日期: {}", symbolDetail.getOnboardDate());
        log.info("合约交易开关: {}", symbolDetail.isTradeSwitch());
        log.info("状态: {}", symbolDetail.getState());
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient(
            FutureTestConfig.BASE_URL,
            FutureTestConfig.ACCESS_KEY,
            FutureTestConfig.SECRET_KEY);
        SymbolDetailTest symbolTest = new SymbolDetailTest();

        symbolTest.testGetSymbolDetail();
    }

    /**
     * 交易对配置数据模型类
     */
    public static class SymbolDetailVO {
        private Integer id;                      // 交易对唯一id
        private String symbol;                   // 交易对
        private String contractType;             // 合约类型，1:永续；2:交割
        private String underlyingType;           // 标的类型，1:币本位合约；2:USDT本位合约
        private String contractSize;             // 合约面值
        private boolean tradeSwitch;             // 合约交易开关
        private Integer state;                   // 状态
        private Integer initLeverage;            // 初始杠杆倍数，默认20x
        private String initPositionType;         // 初始化持仓模式，默认逐仓
        private String baseCoin;                 // 标的资产
        private String quoteCoin;                // 报价资产
        private Integer baseCoinPrecision;       // 标的币种精度
        private Integer baseCoinDisplayPrecision; // 标的币种显示精度
        private Integer quoteCoinPrecision;      // 报价币种精度
        private Integer quoteCoinDisplayPrecision; // 报价币种显示精度
        private Integer quantityPrecision;       // 数量精度
        private Integer pricePrecision;          // 价格精度
        private String supportOrderType;         // 支持订单类型，1,2表示支持全部方式
        private String supportTimeInForce;       // 支持有效方式，1,2,3,4表示支持全部方式
        private String supportEntrustType;       // 支持计划委托类型，1,2,3,4,5表示支持全部方式
        private String supportPositionType;      // 支持仓位类型 1全仓 2逐仓
        private String minPrice;                 // 最小价格
        private String minQty;                   // 最小交易单位
        private String minNotional;              // 最小名义价值
        private String maxNotional;              // 最大名义价值
        private String multiplierDown;           // 撮合限价卖单下限百分比，如0.95
        private String multiplierUp;             // 撮合限价买单价格上限百分比，如1.05
        private Integer maxOpenOrders;           // 最多open订单数
        private Integer maxEntrusts;             // 最多open条件单数
        private String makerFee;                 // maker 手续费率
        private String takerFee;                 // taker 手续费率
        private String liquidationFee;           // 强平手续费率
        private String marketTakeBound;          // 市价单最多价格偏离(相对于标记价格)
        private Integer depthPrecisionMerge;     // 盘口精度合并
        private List<String> labels;             // 标签
        private Long onboardDate;                // 上线日期
        private String superLeverage;            // 超级合约杠杆倍数，[500,1000]
        private String enName;                   // 合约英文名称
        private String cnName;                   // 合约中文名称
        private String minStepPrice;             // 最小价格变动单位

        // Getters and Setters
        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getContractType() {
            return contractType;
        }

        public void setContractType(String contractType) {
            this.contractType = contractType;
        }

        public String getUnderlyingType() {
            return underlyingType;
        }

        public void setUnderlyingType(String underlyingType) {
            this.underlyingType = underlyingType;
        }

        public String getContractSize() {
            return contractSize;
        }

        public void setContractSize(String contractSize) {
            this.contractSize = contractSize;
        }

        public boolean isTradeSwitch() {
            return tradeSwitch;
        }

        public void setTradeSwitch(boolean tradeSwitch) {
            this.tradeSwitch = tradeSwitch;
        }

        public Integer getState() {
            return state;
        }

        public void setState(Integer state) {
            this.state = state;
        }

        public Integer getInitLeverage() {
            return initLeverage;
        }

        public void setInitLeverage(Integer initLeverage) {
            this.initLeverage = initLeverage;
        }

        public String getInitPositionType() {
            return initPositionType;
        }

        public void setInitPositionType(String initPositionType) {
            this.initPositionType = initPositionType;
        }

        public String getBaseCoin() {
            return baseCoin;
        }

        public void setBaseCoin(String baseCoin) {
            this.baseCoin = baseCoin;
        }

        public String getQuoteCoin() {
            return quoteCoin;
        }

        public void setQuoteCoin(String quoteCoin) {
            this.quoteCoin = quoteCoin;
        }

        public Integer getBaseCoinPrecision() {
            return baseCoinPrecision;
        }

        public void setBaseCoinPrecision(Integer baseCoinPrecision) {
            this.baseCoinPrecision = baseCoinPrecision;
        }

        public Integer getBaseCoinDisplayPrecision() {
            return baseCoinDisplayPrecision;
        }

        public void setBaseCoinDisplayPrecision(Integer baseCoinDisplayPrecision) {
            this.baseCoinDisplayPrecision = baseCoinDisplayPrecision;
        }

        public Integer getQuoteCoinPrecision() {
            return quoteCoinPrecision;
        }

        public void setQuoteCoinPrecision(Integer quoteCoinPrecision) {
            this.quoteCoinPrecision = quoteCoinPrecision;
        }

        public Integer getQuoteCoinDisplayPrecision() {
            return quoteCoinDisplayPrecision;
        }

        public void setQuoteCoinDisplayPrecision(Integer quoteCoinDisplayPrecision) {
            this.quoteCoinDisplayPrecision = quoteCoinDisplayPrecision;
        }

        public Integer getQuantityPrecision() {
            return quantityPrecision;
        }

        public void setQuantityPrecision(Integer quantityPrecision) {
            this.quantityPrecision = quantityPrecision;
        }

        public Integer getPricePrecision() {
            return pricePrecision;
        }

        public void setPricePrecision(Integer pricePrecision) {
            this.pricePrecision = pricePrecision;
        }

        public String getSupportOrderType() {
            return supportOrderType;
        }

        public void setSupportOrderType(String supportOrderType) {
            this.supportOrderType = supportOrderType;
        }

        public String getSupportTimeInForce() {
            return supportTimeInForce;
        }

        public void setSupportTimeInForce(String supportTimeInForce) {
            this.supportTimeInForce = supportTimeInForce;
        }

        public String getSupportEntrustType() {
            return supportEntrustType;
        }

        public void setSupportEntrustType(String supportEntrustType) {
            this.supportEntrustType = supportEntrustType;
        }

        public String getSupportPositionType() {
            return supportPositionType;
        }

        public void setSupportPositionType(String supportPositionType) {
            this.supportPositionType = supportPositionType;
        }

        public String getMinPrice() {
            return minPrice;
        }

        public void setMinPrice(String minPrice) {
            this.minPrice = minPrice;
        }

        public String getMinQty() {
            return minQty;
        }

        public void setMinQty(String minQty) {
            this.minQty = minQty;
        }

        public String getMinNotional() {
            return minNotional;
        }

        public void setMinNotional(String minNotional) {
            this.minNotional = minNotional;
        }

        public String getMaxNotional() {
            return maxNotional;
        }

        public void setMaxNotional(String maxNotional) {
            this.maxNotional = maxNotional;
        }

        public String getMultiplierDown() {
            return multiplierDown;
        }

        public void setMultiplierDown(String multiplierDown) {
            this.multiplierDown = multiplierDown;
        }

        public String getMultiplierUp() {
            return multiplierUp;
        }

        public void setMultiplierUp(String multiplierUp) {
            this.multiplierUp = multiplierUp;
        }

        public Integer getMaxOpenOrders() {
            return maxOpenOrders;
        }

        public void setMaxOpenOrders(Integer maxOpenOrders) {
            this.maxOpenOrders = maxOpenOrders;
        }

        public Integer getMaxEntrusts() {
            return maxEntrusts;
        }

        public void setMaxEntrusts(Integer maxEntrusts) {
            this.maxEntrusts = maxEntrusts;
        }

        public String getMakerFee() {
            return makerFee;
        }

        public void setMakerFee(String makerFee) {
            this.makerFee = makerFee;
        }

        public String getTakerFee() {
            return takerFee;
        }

        public void setTakerFee(String takerFee) {
            this.takerFee = takerFee;
        }

        public String getLiquidationFee() {
            return liquidationFee;
        }

        public void setLiquidationFee(String liquidationFee) {
            this.liquidationFee = liquidationFee;
        }

        public String getMarketTakeBound() {
            return marketTakeBound;
        }

        public void setMarketTakeBound(String marketTakeBound) {
            this.marketTakeBound = marketTakeBound;
        }

        public Integer getDepthPrecisionMerge() {
            return depthPrecisionMerge;
        }

        public void setDepthPrecisionMerge(Integer depthPrecisionMerge) {
            this.depthPrecisionMerge = depthPrecisionMerge;
        }

        public List<String> getLabels() {
            return labels;
        }

        public void setLabels(List<String> labels) {
            this.labels = labels;
        }

        public Long getOnboardDate() {
            return onboardDate;
        }

        public void setOnboardDate(Long onboardDate) {
            this.onboardDate = onboardDate;
        }

        public String getSuperLeverage() {
            return superLeverage;
        }

        public void setSuperLeverage(String superLeverage) {
            this.superLeverage = superLeverage;
        }

        public String getEnName() {
            return enName;
        }

        public void setEnName(String enName) {
            this.enName = enName;
        }

        public String getCnName() {
            return cnName;
        }

        public void setCnName(String cnName) {
            this.cnName = cnName;
        }

        public String getMinStepPrice() {
            return minStepPrice;
        }

        public void setMinStepPrice(String minStepPrice) {
            this.minStepPrice = minStepPrice;
        }
    }
}