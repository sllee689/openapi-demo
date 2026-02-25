package com.example.openapi.test.future.order;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import com.example.openapi.test.future.FutureTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

/**
 * 合约止盈止损委托创建测试类
 */
public class EntrustCreateProfitTest {

    private static final Logger log = LoggerFactory.getLogger(EntrustCreateProfitTest.class);
    private static ApiClient apiClient;

    public EntrustCreateProfitTest() {
    }

    public EntrustCreateProfitTest(ApiClient apiClient) {
        EntrustCreateProfitTest.apiClient = apiClient;
    }

    /**
     * 创建止盈止损委托
     *
     * @param request 委托请求参数
     * @return 委托ID
     * @throws HashExApiException 如果API调用失败
     */
    public Object createProfitEntrust(ProfitEntrustRequest request) throws HashExApiException {
        try {
            // 验证必填参数
            if (request.getSymbol() == null || request.getSymbol().isEmpty()) {
                throw new HashExApiException("交易对不能为空");
            }
            if (request.getTriggerProfitPrice() == null && request.getTriggerStopPrice() == null) {
                throw new HashExApiException("止盈触发价和止损触发价至少填写一个");
            }
            if (request.getOrigQty() == null || request.getOrigQty().isEmpty()) {
                throw new HashExApiException("origQty 必填");
            }

            // 创建参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();
            queryParams.put("symbol", request.getSymbol());

            // 添加可选参数
            if (request.getTriggerProfitPrice() != null) {
                queryParams.put("triggerProfitPrice", request.getTriggerProfitPrice());
            }
            if (request.getTriggerStopPrice() != null) {
                queryParams.put("triggerStopPrice", request.getTriggerStopPrice());
            }
            if (request.getTriggerPriceType() != null) {
                queryParams.put("triggerPriceType", request.getTriggerPriceType());
            }
            if (request.getPositionId() != null) {
                queryParams.put("positionId", request.getPositionId());
            }
            if (request.getOrigQty() != null) {
                queryParams.put("origQty", request.getOrigQty());
            }
            if (request.getExpireTime() != null) {
                queryParams.put("expireTime", request.getExpireTime().toString());
            }

            // 调用API
            String responseJson = apiClient.sendPostRequest("/fut/v1/entrust/create-profit", queryParams);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            ApiResponse<Object> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<Object>>() {
                    }, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("创建止盈止损委托失败: " + apiResponse.getMsg());
            }
            if (apiResponse.getData() == null) {
                throw new HashExApiException("创建止盈止损委托成功但返回data为空");
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("创建止盈止损委托时出错: " + e.getMessage(), e);
        }
    }

    private Object runCreateTest(ProfitEntrustRequest request, String scenario) throws HashExApiException {
        Object result = createProfitEntrust(request);
        log.info("{}结果（委托ID）: {}", scenario, result);
        return result;
    }

    private void runCreateExpectFail(ProfitEntrustRequest request, String scenario) throws HashExApiException {
        try {
            createProfitEntrust(request);
            throw new HashExApiException(scenario + " 预期失败但实际成功");
        } catch (HashExApiException e) {
            log.info("{}按预期失败: {}", scenario, e.getMessage());
        }
    }

    /**
     * 测试创建止盈止损委托（指定数量）
     */
    private void testCreateTakeProfitAndStopLoss() throws HashExApiException {
        log.info("===== 创建止盈止损委托测试 (带origQty) =====");

        ProfitEntrustRequest request = new ProfitEntrustRequest();
        request.setPositionId("592447815234371840");
        request.setSymbol("btc_usdt");
        request.setOrigQty("3002");
        request.setTriggerProfitPrice("100000");
        request.setTriggerStopPrice("30000");
        request.setTriggerPriceType("LATEST_PRICE");

        runCreateTest(request, "止盈止损委托创建");
    }

    private void testCreateWithTriggerPriceTypeVariants() throws HashExApiException {
        log.info("===== 测试 triggerPriceType 参数枚举 =====");

        ProfitEntrustRequest latestPriceReq = new ProfitEntrustRequest();
        latestPriceReq.setPositionId("592447815234371840");
        latestPriceReq.setSymbol("btc_usdt");
        latestPriceReq.setOrigQty("3002");
        latestPriceReq.setTriggerProfitPrice("100000");
        latestPriceReq.setTriggerStopPrice("30000");
        latestPriceReq.setTriggerPriceType("LATEST_PRICE");
        runCreateTest(latestPriceReq, "LATEST_PRICE 创建");

        ProfitEntrustRequest markPriceReq = new ProfitEntrustRequest();
        markPriceReq.setPositionId("592447815234371840");
        markPriceReq.setSymbol("btc_usdt");
        markPriceReq.setOrigQty("3002");
        markPriceReq.setTriggerProfitPrice("100000");
        markPriceReq.setTriggerStopPrice("30000");
        markPriceReq.setTriggerPriceType("MARK_PRICE");
        runCreateTest(markPriceReq, "MARK_PRICE 创建");
    }

    private void testCreateWithInvalidEnumParams() throws HashExApiException {
        log.info("===== 测试非法枚举参数 =====");

        ProfitEntrustRequest invalidTriggerTypeReq = new ProfitEntrustRequest();
        invalidTriggerTypeReq.setSymbol("btc_usdt");
        invalidTriggerTypeReq.setOrigQty("3002");
        invalidTriggerTypeReq.setTriggerProfitPrice("100000");
        invalidTriggerTypeReq.setTriggerPriceType("BAD_TRIGGER_TYPE");
        runCreateExpectFail(invalidTriggerTypeReq, "非法triggerPriceType");
    }

    public static void main(String[] args) {
        apiClient = new ApiClient(
                FutureTestConfig.BASE_URL,
                FutureTestConfig.ACCESS_KEY,
                FutureTestConfig.SECRET_KEY);
        EntrustCreateProfitTest test = new EntrustCreateProfitTest();

        // ===== 组合1: LATEST_PRICE + positionId + origQty（止盈+止损）=====
        try {
            log.info("===== 组合1: LATEST_PRICE + positionId + origQty（止盈+止损）=====");
            ProfitEntrustRequest req1 = new ProfitEntrustRequest();
            req1.setPositionId("592447815234371840");
            req1.setSymbol("btc_usdt");
            req1.setOrigQty("3002");
            req1.setTriggerProfitPrice("100000");
            req1.setTriggerStopPrice("30000");
            req1.setTriggerPriceType("LATEST_PRICE");
            Object r1 = test.createProfitEntrust(req1);
            log.info("组合1 ✅ 成功, 委托ID: {}", r1);
        } catch (Exception e) {
            log.error("组合1 ❌ 失败: {}", e.getMessage());
        }

        // ===== 组合2: MARK_PRICE + positionId + origQty（止盈+止损）=====
        try {
            log.info("===== 组合2: MARK_PRICE + positionId + origQty（止盈+止损）=====");
            ProfitEntrustRequest req2 = new ProfitEntrustRequest();
            req2.setPositionId("592447815234371840");
            req2.setSymbol("btc_usdt");
            req2.setOrigQty("3002");
            req2.setTriggerProfitPrice("100000");
            req2.setTriggerStopPrice("30000");
            req2.setTriggerPriceType("MARK_PRICE");
            Object r2 = test.createProfitEntrust(req2);
            log.info("组合2 ✅ 成功, 委托ID: {}", r2);
        } catch (Exception e) {
            log.error("组合2 ❌ 失败: {}", e.getMessage());
        }

        // ===== 组合3: LATEST_PRICE + 只止盈 =====
        try {
            log.info("===== 组合3: LATEST_PRICE + 只止盈 =====");
            ProfitEntrustRequest req3 = new ProfitEntrustRequest();
            req3.setPositionId("592447815234371840");
            req3.setSymbol("btc_usdt");
            req3.setOrigQty("3002");
            req3.setTriggerProfitPrice("100000");
            req3.setTriggerPriceType("LATEST_PRICE");
            Object r3 = test.createProfitEntrust(req3);
            log.info("组合3 ✅ 成功, 委托ID: {}", r3);
        } catch (Exception e) {
            log.error("组合3 ❌ 失败: {}", e.getMessage());
        }

        // ===== 组合4: MARK_PRICE + 只止盈 =====
        try {
            log.info("===== 组合4: MARK_PRICE + 只止盈 =====");
            ProfitEntrustRequest req4 = new ProfitEntrustRequest();
            req4.setPositionId("592447815234371840");
            req4.setSymbol("btc_usdt");
            req4.setOrigQty("3002");
            req4.setTriggerProfitPrice("100000");
            req4.setTriggerPriceType("MARK_PRICE");
            Object r4 = test.createProfitEntrust(req4);
            log.info("组合4 ✅ 成功, 委托ID: {}", r4);
        } catch (Exception e) {
            log.error("组合4 ❌ 失败: {}", e.getMessage());
        }

        // ===== 组合5: LATEST_PRICE + 只止损 =====
        try {
            log.info("===== 组合5: LATEST_PRICE + 只止损 =====");
            ProfitEntrustRequest req5 = new ProfitEntrustRequest();
            req5.setPositionId("592447815234371840");
            req5.setSymbol("btc_usdt");
            req5.setOrigQty("3002");
            req5.setTriggerStopPrice("30000");
            req5.setTriggerPriceType("LATEST_PRICE");
            Object r5 = test.createProfitEntrust(req5);
            log.info("组合5 ✅ 成功, 委托ID: {}", r5);
        } catch (Exception e) {
            log.error("组合5 ❌ 失败: {}", e.getMessage());
        }

        // ===== 组合6: 不传 triggerPriceType（用默认值）=====
        try {
            log.info("===== 组合6: 不传triggerPriceType =====");
            ProfitEntrustRequest req6 = new ProfitEntrustRequest();
            req6.setPositionId("592447815234371840");
            req6.setSymbol("btc_usdt");
            req6.setOrigQty("3002");
            req6.setTriggerProfitPrice("100000");
            req6.setTriggerStopPrice("30000");
            Object r6 = test.createProfitEntrust(req6);
            log.info("组合6 ✅ 成功, 委托ID: {}", r6);
        } catch (Exception e) {
            log.error("组合6 ❌ 失败: {}", e.getMessage());
        }

        log.info("===== 所有组合测试完毕 =====");
    }

    /**
     * 止盈止损委托请求参数类
     */
    public static class ProfitEntrustRequest {
        private String symbol;                 // 交易对（必填）
        private String triggerProfitPrice;     // 止盈触发价
        private String triggerStopPrice;       // 止损触发价
        private String triggerPriceType;       // 触发价格类型：MARK_PRICE(标记价格)、LATEST_PRICE(最新价格)
        private String positionId;             // 仓位ID
        private String origQty;               // 委托数量（张），必填且须大于0
        private Long expireTime;              // 过期时间

        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getTriggerProfitPrice() { return triggerProfitPrice; }
        public void setTriggerProfitPrice(String triggerProfitPrice) { this.triggerProfitPrice = triggerProfitPrice; }
        public String getTriggerStopPrice() { return triggerStopPrice; }
        public void setTriggerStopPrice(String triggerStopPrice) { this.triggerStopPrice = triggerStopPrice; }
        public String getTriggerPriceType() { return triggerPriceType; }
        public void setTriggerPriceType(String triggerPriceType) { this.triggerPriceType = triggerPriceType; }
        public String getPositionId() { return positionId; }
        public void setPositionId(String positionId) { this.positionId = positionId; }
        public String getOrigQty() { return origQty; }
        public void setOrigQty(String origQty) { this.origQty = origQty; }
        public Long getExpireTime() { return expireTime; }
        public void setExpireTime(Long expireTime) { this.expireTime = expireTime; }
    }
}
